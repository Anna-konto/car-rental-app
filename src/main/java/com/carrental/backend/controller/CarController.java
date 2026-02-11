package com.carrental.backend.controller;

import com.carrental.backend.dto.CarDto;
import com.carrental.backend.mapper.CarMapper;
import com.carrental.backend.model.Car;
import com.carrental.backend.model.CarStatus;
import com.carrental.backend.model.CarServiceEntry;
import com.carrental.backend.repository.CarRepository;
import com.carrental.backend.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {
    @Autowired
    private CarService carService;
    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    // ✅ CREATE
    @PostMapping
    public Car createCar(@RequestBody Car car) {
        return carRepository.save(car);
    }

    // ✅ READ ALL
    @GetMapping
    public List<CarDto> getAllCars() {
        return carRepository.findAll()
                .stream()
                .map(CarMapper::toDto)
                .toList();
    }

    // ✅ READ ONE
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));
    }

    @GetMapping("/insurance-expired")
    public List<CarDto> getCarsWithExpiredInsurance() {
        return carRepository
                .findByInsuranceValidUntilBefore(LocalDate.now())
                .stream()
                .map(CarMapper::toDto)
                .toList();
    }

    @GetMapping("/inspection-expired")
    public List<CarDto> getCarsWithExpiredInspection() {
        return carRepository
                .findByTechnicalInspectionValidUntilBefore(LocalDate.now())
                .stream()
                .map(CarMapper::toDto)
                .toList();
    }

    // ✅ UPDATE


    @PutMapping("/{id}/status")
    public CarDto changeCarStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        car.setStatus(CarStatus.valueOf(status));
        car.updateStatus();
        return CarMapper.toDto(carRepository.save(car));

    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> editCar(
            @PathVariable Long id,
            @RequestBody Car carDetails) {
        Car car = carService.findById(id);
        if (car == null) {
            return ResponseEntity.notFound().build();
        }

        car.setBrand(carDetails.getBrand());
        car.setModel(carDetails.getModel());
        car.setYear(carDetails.getYear());
        car.setPlateNumber(carDetails.getPlateNumber());
        car.setVin(carDetails.getVin());
        car.setTechnicalInspectionValidUntil(carDetails.getTechnicalInspectionValidUntil());
        car.setInsuranceValidUntil(carDetails.getInsuranceValidUntil());
        car.setStatus(carDetails.getStatus());

        Car updatedCar = carService.save(car);
        return ResponseEntity.ok(updatedCar);
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Car car = carService.findById(id);
        if (car == null) {
            return "redirect:/cars";
        }
        model.addAttribute("car", car);
        return "panel/edit-car";
    }
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("message", "Hello from Thymeleaf!");
        return "panel/test";
    }
}

