package com.carrental.backend.dto;
import java.time.LocalDate;

public class CarDto {

    private Long id;
    private String brand;
    private String model;
    private Integer year;
    private String plateNumber;
    private boolean available;
    private String status;

    public CarDto() {
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    private LocalDate insuranceValidUntil;
    private LocalDate technicalInspectionValidUntil;

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getInsuranceValidUntil() {
        return insuranceValidUntil;
    }

    public void setInsuranceValidUntil(LocalDate insuranceValidUntil) {
        this.insuranceValidUntil = insuranceValidUntil;
    }

    public LocalDate getTechnicalInspectionValidUntil() {
        return technicalInspectionValidUntil;
    }

    public void setTechnicalInspectionValidUntil(LocalDate technicalInspectionValidUntil) {
        this.technicalInspectionValidUntil = technicalInspectionValidUntil;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

