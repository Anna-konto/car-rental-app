package com.carrental.backend.controller;

import com.carrental.backend.model.Customer;
import com.carrental.backend.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.carrental.backend.repository.RentalRepository;

@Controller
@RequestMapping("/panel/customers")
public class PanelCustomerController {

    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;

    public PanelCustomerController(
            CustomerRepository customerRepository,
            RentalRepository rentalRepository
    ) {
        this.customerRepository = customerRepository;
        this.rentalRepository = rentalRepository;
    }

    // 📋 lista klientów
    @GetMapping
    public String customers(
            @RequestParam(required = false) String lastName,
            Model model) {

        if (lastName != null && !lastName.isBlank()) {
            model.addAttribute(
                    "customers",
                    customerRepository.findByLastNameContainingIgnoreCase(lastName)
            );
        } else {
            model.addAttribute("customers", customerRepository.findAll());
        }

        model.addAttribute("lastName", lastName); // żeby formularz pamiętał wpis
        return "panel/customers";
    }

    // ➕ formularz dodawania
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "panel/customer-form";
    }

    // ➕ zapis nowego klienta
    @PostMapping("/add")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "panel/customer-form";
        }
        customerRepository.save(customer);
        return "redirect:/panel/customers";
    }

    // ✏️ formularz edycji klienta
    @GetMapping("/{id}/edit")
    public String editCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        model.addAttribute("customer", customer);
        return "panel/customer-form";
    }
    @GetMapping("/{id}")
    public String customerDetails(@PathVariable Long id, Model model) {

        Customer customer = customerRepository.findById(id).orElseThrow();

        model.addAttribute("customer", customer);
        model.addAttribute(
                "rentals",
                rentalRepository.findByCustomerIdOrderByRentalDateDesc(id)
        );

        return "panel/customer-details";
    }

    // 💾 zapis edycji klienta
    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute Customer customer) {
        customerRepository.save(customer);
        return "redirect:/panel/customers";
    }
    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, Model model) {

        // ❌ blokada usuwania jeśli są aktywne wypożyczenia
        if (!rentalRepository.findByCustomerIdAndReturnedFalse(id).isEmpty()) {
            model.addAttribute("error",
                    "Nie można usunąć klienta z aktywnymi wypożyczeniami");
            model.addAttribute("customers", customerRepository.findAll());
            return "panel/customers";
        }

        // ✅ można usunąć
        customerRepository.deleteById(id);
        return "redirect:/panel/customers";
    }

}