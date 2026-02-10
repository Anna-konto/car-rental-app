package com.carrental.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalTime;
import com.carrental.backend.model.RentalType;

@Entity
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Car car;

    @ManyToOne(optional = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type")
    private RentalType rentalType;

    // 🕒 DATY
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private boolean returned;

    // 💰 FINANSE
    private BigDecimal pricePerDay;     // short-term
    private BigDecimal pricePerMonth;   // long-term
    private BigDecimal deposit;         // kaucja

    // 🚗 STAN AUTA
    private Integer startMileage;

    // 📆 OKRES WYNAJMU
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // 📅 ROZLICZENIA (LONG TERM)
    private Integer billingDayOfMonth;

    // ===== GETTERY / SETTERY =====

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public RentalType getRentalType() {
        return rentalType;
    }

    public void setRentalType(RentalType rentalType) {
        this.rentalType = rentalType;
    }

    public LocalDateTime getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDateTime rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(BigDecimal pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public Integer getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(Integer startMileage) {
        this.startMileage = startMileage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getBillingDayOfMonth() {
        return billingDayOfMonth;
    }

    public void setBillingDayOfMonth(Integer billingDayOfMonth) {
        this.billingDayOfMonth = billingDayOfMonth;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public long getDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }

    // Oblicza liczbę miesięcy wypożyczenia
    public long getMonths() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate);
        }
        return 0;
    }
    public BigDecimal getTotalPrice() {
        if (rentalType == RentalType.SHORT_TERM && pricePerDay != null) {
            long days = getDays();
            if (days == 0) days = 1;
            return pricePerDay.multiply(BigDecimal.valueOf(days));
        } else if (rentalType == RentalType.LONG_TERM && pricePerMonth != null) {
            long months = getMonths();
            if (months == 0) months = 1;
            return pricePerMonth.multiply(BigDecimal.valueOf(months));
        }
        return BigDecimal.ZERO;
    }

    // ✅ NOWA METODA - zwraca typ wypożyczenia jako tekst
    public String getRentalTypeLabel() {
        return rentalType == RentalType.SHORT_TERM ? "Dobowy" : "Miesięczny";
    }
}