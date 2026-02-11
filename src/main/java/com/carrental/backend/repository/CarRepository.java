package com.carrental.backend.repository;

import com.carrental.backend.model.Car;
import com.carrental.backend.model.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByVin(String vin);

    // 🔹 do panelu wypożyczeń
    List<Car> findByArchivedFalse();

    List<Car> findByArchivedFalseAndStatus(CarStatus status);

    List<Car> findByArchivedFalseAndPlateNumberContainingIgnoreCase(String plate);

    List<Car> findByArchivedTrue();
    // 🔹 do kontroli ważności dokumentów
    List<Car> findByInsuranceValidUntilBefore(LocalDate date);

    List<Car> findByTechnicalInspectionValidUntilBefore(LocalDate date);
}
