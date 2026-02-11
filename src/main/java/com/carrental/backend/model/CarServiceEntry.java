package com.carrental.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "car_service_entries")
public class CarServiceEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id")
    private Car car;

    private LocalDate serviceDate;
    private String type; // np. OLEJ, NAPRAWA, SERWIS, INNE

    @Column(length = 2000)
    private String description;

    // ===== getters / setters =====

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}