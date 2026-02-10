package com.carrental.backend.controller;

import com.carrental.backend.dto.CustomerDto;
import com.carrental.backend.mapper.CustomerMapper;
import com.carrental.backend.model.Customer;
import com.carrental.backend.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ✅ CREATE
    @PostMapping
    public CustomerDto createCustomer(@Valid @RequestBody Customer customer) {
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    // ✅ READ ALL
    @GetMapping
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerMapper::toDto)
                .toList();
    }

    // ✅ READ ONE
    @GetMapping("/{id}")
    public CustomerDto getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return CustomerMapper.toDto(customer);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public CustomerDto updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDto dto
    ) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerRepository.deleteById(id);
    }
}
