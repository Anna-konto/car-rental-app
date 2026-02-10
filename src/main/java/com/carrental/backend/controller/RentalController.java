package com.carrental.backend.controller;

import com.carrental.backend.dto.RentalDto;
import com.carrental.backend.mapper.RentalMapper;
import com.carrental.backend.model.Car;
import com.carrental.backend.model.Customer;
import com.carrental.backend.model.Rental;
import com.carrental.backend.repository.CarRepository;
import com.carrental.backend.repository.CustomerRepository;
import com.carrental.backend.repository.RentalRepository;
import com.carrental.backend.model.CarStatus;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    public RentalController(
            RentalRepository rentalRepository,
            CarRepository carRepository,
            CustomerRepository customerRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    // 🚗 WYPOŻYCZENIE
    @PostMapping
    public RentalDto createRental(
            @RequestParam Long carId,
            @RequestParam Long customerId
    ) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        boolean alreadyRented =
                !rentalRepository.findByCarIdAndReturnedFalse(carId).isEmpty();

        if (alreadyRented) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Car is already rented"
            );
        }

        car.setStatus(CarStatus.RENTED);
        carRepository.save(car);

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setRentalDate(LocalDateTime.now());
        rental.setReturned(false);
        car.setAvailable(false);
        car.updateStatus();
        carRepository.save(car);

        return RentalMapper.toDto(rentalRepository.save(rental));
    }

    // 📋 LISTA WYPOŻYCZEŃ
    @GetMapping
    public List<RentalDto> getAllRentals() {
        return rentalRepository.findAll()
                .stream()
                .map(RentalMapper::toDto)
                .toList();
    }

    // 🔁 ZWROT
    @PutMapping("/{id}/return")
    public RentalDto returnCar(@PathVariable Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        rental.setReturned(true);
        rental.setReturnDate(LocalDateTime.now());

        Car car = rental.getCar();
        car.setStatus(CarStatus.AVAILABLE);
        carRepository.save(car);
        car.setAvailable(true);
        car.updateStatus();
        carRepository.save(car);

        return RentalMapper.toDto(rentalRepository.save(rental));
    }
}
