package com.carrental.backend.repository;

import com.carrental.backend.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByCarIdOrderByRentalDateDesc(Long carId);

    List<Rental> findByCustomerIdOrderByRentalDateDesc(Long customerId);
    List<Rental> findByCustomerIdAndReturnedFalse(Long customerId);
    // aktywne wypożyczenia (do panelu)
    List<Rental> findByReturnedFalse();
    List<Rental> findByReturnedFalseOrderByRentalDateDesc();
    // sprawdza czy dany samochód jest aktualnie wypożyczony
    List<Rental> findByCarIdAndReturnedFalse(Long carId);
    List<Rental> findByCarIsNotNull();

        // ✅ NOWA METODA - wyszukiwanie aktywnych wypożyczeń po nazwisku
        List<Rental> findByReturnedFalseAndCustomerLastNameContainingIgnoreCase(String lastName);

        // ✅ NOWA METODA - sortowanie
        List<Rental> findByReturnedFalseOrderByCustomerLastNameAsc();
        List<Rental> findByReturnedFalseOrderByCustomerLastNameDesc();
    }

