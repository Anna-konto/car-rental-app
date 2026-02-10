package com.carrental.backend.model;
import java.time.LocalDate;
import com.carrental.backend.model.CarStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;

    @Column(name = "production_year")
    private Integer year;

    private String plateNumber;

    @Enumerated(EnumType.STRING)
    private CarStatus status = CarStatus.AVAILABLE;
    @Column(nullable = false)
    private boolean archived = false;
    @Column(length = 17, unique = true, nullable=false)
    private String vin;

    // 📄 dane formalne
    private LocalDate insuranceValidUntil;
    private LocalDate technicalInspectionValidUntil;

    private boolean available = true;

    public Car() {}

    // ===== STATUS LOGIC =====
    public void updateStatus() {

        // 🗄 archiwalne – nie zmieniamy statusu
        if (this.archived) {
            return;
        }

        // ❌ brak ważnego badania technicznego
        if (technicalInspectionValidUntil == null ||
                technicalInspectionValidUntil.isBefore(LocalDate.now())) {
            this.status = CarStatus.OUT_OF_SERVICE;
            return;
        }

        // ❌ brak ważnego ubezpieczenia
        if (insuranceValidUntil == null ||
                insuranceValidUntil.isBefore(LocalDate.now())) {
            this.status = CarStatus.OUT_OF_SERVICE;
            return;
        }

        // ❌ wypożyczony
        if (!this.available) {
            this.status = CarStatus.RENTED;
            return;
        }

        // ✅ wszystko OK
        this.status = CarStatus.AVAILABLE;
    }

    // ===== GETTERS & SETTERS =====

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

    public LocalDate getInspectionValidUntil() {
        return technicalInspectionValidUntil;
    }

    public void setInspectionValidUntil(LocalDate inspectionValidUntil) {
        this.technicalInspectionValidUntil = inspectionValidUntil;
    }

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }
    public boolean canBeRented() {
        return !archived
                && status == CarStatus.AVAILABLE
                && technicalInspectionValidUntil != null
                && !technicalInspectionValidUntil.isBefore(LocalDate.now())
                && insuranceValidUntil != null
                && !insuranceValidUntil.isBefore(LocalDate.now());
    }


    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }


    @Transient
    public boolean isInsuranceExpired() {
        return insuranceValidUntil != null && insuranceValidUntil.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isInsuranceExpiringSoon() {
        return insuranceValidUntil != null
                && !insuranceValidUntil.isBefore(LocalDate.now())
                && insuranceValidUntil.isBefore(LocalDate.now().plusDays(30));
    }

    @Transient
    public boolean isInspectionExpired() {
        return technicalInspectionValidUntil != null
                && technicalInspectionValidUntil.isBefore(LocalDate.now());
    }

    @Transient
    public boolean isInspectionExpiringSoon() {
        return technicalInspectionValidUntil != null
                && !technicalInspectionValidUntil.isBefore(LocalDate.now())
                && technicalInspectionValidUntil.isBefore(LocalDate.now().plusDays(30));
    }

}
