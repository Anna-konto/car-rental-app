package com.carrental.backend.service;

import com.carrental.backend.model.Car;
import com.carrental.backend.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    public Car findById(Long id) {
        Optional<Car> carOptional = carRepository.findById(id);
        return carOptional.orElse(null);
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }
}