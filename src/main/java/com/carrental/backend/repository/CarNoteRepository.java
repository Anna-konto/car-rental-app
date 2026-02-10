package com.carrental.backend.repository;

import com.carrental.backend.model.CarNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarNoteRepository extends JpaRepository<CarNote, Long> {

    List<CarNote> findByCarIdOrderByCreatedAtDesc(Long carId);
}
