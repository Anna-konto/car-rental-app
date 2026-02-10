package com.carrental.backend.mapper;

import com.carrental.backend.dto.RentalDto;
import com.carrental.backend.model.Rental;

public class RentalMapper {

    public static RentalDto toDto(Rental rental) {
        RentalDto dto = new RentalDto();

        dto.setId(rental.getId());
        dto.setRentalDate(rental.getRentalDate());
        dto.setReturnDate(rental.getReturnDate());
        dto.setReturned(rental.isReturned());

        // ✅ CAR – BEZPIECZNIE
        if (rental.getCar() != null) {
            dto.setCarId(rental.getCar().getId());
            dto.setCarBrand(rental.getCar().getBrand());
            dto.setCarModel(rental.getCar().getModel());
            dto.setPlateNumber(rental.getCar().getPlateNumber());
        }

        // ✅ CUSTOMER – BEZPIECZNIE
        if (rental.getCustomer() != null) {
            dto.setCustomerId(rental.getCustomer().getId());
            dto.setCustomerName(
                    rental.getCustomer().getFirstName() + " " +
                            rental.getCustomer().getLastName()
            );
        }

        return dto;
    }
}