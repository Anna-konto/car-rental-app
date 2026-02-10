package com.carrental.backend.repository;

import com.carrental.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
}


