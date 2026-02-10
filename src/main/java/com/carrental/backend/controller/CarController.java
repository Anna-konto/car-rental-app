package com.carrental.backend.controller;

import com.carrental.backend.model.Car;
import com.carrental.backend.repository.CarRepository;
import org.springframework.web.bind.annotation.*;
import com.carrental.backend.dto.CarDto;
import com.carrental.backend.mapper.CarMapper;
import java.time.LocalDate;
import java.util.List;
import com.carrental.backend.model.CarStatus;


import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {

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
    @PutMapping("/{id}")
    public Car updateCar(
            @PathVariable Long id,
            @RequestBody Car updatedCar) {

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        car.setBrand(updatedCar.getBrand());
        car.setModel(updatedCar.getModel());
        car.setYear(updatedCar.getYear());
        car.setPlateNumber(updatedCar.getPlateNumber());
        car.setAvailable(updatedCar.isAvailable());

        return carRepository.save(car);
    }
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
}
