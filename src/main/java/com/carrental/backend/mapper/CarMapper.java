package com.carrental.backend.mapper;

import com.carrental.backend.dto.CarDto;
import com.carrental.backend.model.Car;
import com.carrental.backend.model.CarStatus;

public class CarMapper {

    public static CarDto toDto(Car car) {
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setYear(car.getYear());
        dto.setPlateNumber(car.getPlateNumber());
        dto.setAvailable(car.isAvailable());
        dto.setInsuranceValidUntil(car.getInsuranceValidUntil());
        dto.setTechnicalInspectionValidUntil(car.getTechnicalInspectionValidUntil());
        dto.setStatus(car.getStatus().name());

        return dto;
    }
}
