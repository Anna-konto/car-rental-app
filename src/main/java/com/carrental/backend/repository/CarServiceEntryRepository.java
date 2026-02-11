package com.carrental.backend.repository;

import com.carrental.backend.model.CarServiceEntry;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface CarServiceEntryRepository
        extends JpaRepository<CarServiceEntry, Long> {

    List<CarServiceEntry> findByCarIdOrderByServiceDateDesc(Long carId);
}
