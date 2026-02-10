package com.carrental.backend.controller;

import com.carrental.backend.model.Car;
import com.carrental.backend.model.Customer;
import com.carrental.backend.model.Rental;
import com.carrental.backend.model.RentalType;
import com.carrental.backend.model.CarStatus;
import com.carrental.backend.repository.CarRepository;
import com.carrental.backend.repository.CustomerRepository;
import com.carrental.backend.repository.RentalRepository;
import com.carrental.backend.service.ContractService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalTime;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/panel/rentals")
public class PanelRentalController {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalRepository rentalRepository;
    private final ContractService contractService;

    public PanelRentalController(
            RentalRepository rentalRepository,
            CarRepository carRepository,
            CustomerRepository customerRepository,
            ContractService contractService
    ) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.contractService = contractService;
    }
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(RentalType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text != null && !text.trim().isEmpty()) {
                    setValue(RentalType.valueOf(text.trim().toUpperCase()));
                }
            }
        });
    }

    // =====================================================
    // 📄 LISTA AKTYWNYCH WYPOŻYCZEŃ
    // URL: /panel/rentals
    // =====================================================
    @GetMapping("/{id}")
    public String carDetails(@PathVariable Long id, Model model) {
        Car car = carRepository.findById(id).orElseThrow();

        // Załaduj wypożyczenia dla tego samochodu
        List<Rental> rentals = rentalRepository.findByCarIdOrderByRentalDateDesc(id);

        model.addAttribute("car", car);
        model.addAttribute("rentals", rentals);

        return "panel/car-details";

    }

    // =====================================================
    // 📄 FORMULARZ WYPOŻYCZENIA
    // URL: /panel/rentals/add
    // =====================================================
    @GetMapping("/add")
    public String rentalForm(
            @RequestParam Long carId,
            Model model
    ) {
        Car car = carRepository.findById(carId).orElseThrow();

        model.addAttribute("car", car);
        model.addAttribute("customers", customerRepository.findAll());

        return "panel/rental-form";
    }
    // =====================================================
// 📄 LISTA AKTYWNYCH WYPOŻYCZEŃ (GŁÓWNA STRONA)
// URL: /panel/rentals
// =====================================================
    // Lista aktywnych wypożyczeń z sortowaniem i wyszukiwaniem
    @GetMapping
    public String rentals(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "customer.lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            Model model
    ) {
        List<Rental> rentals;

        // Wyszukiwanie po nazwisku
        if (search != null && !search.trim().isEmpty()) {
            rentals = rentalRepository.findByReturnedFalseAndCustomerLastNameContainingIgnoreCase(
                    search.trim()
            );
        } else {
            rentals = rentalRepository.findByReturnedFalse();
        }

        // Sortowanie
        rentals = rentals.stream()
                .filter(r -> r != null)
                .sorted((r1, r2) -> {
                    if ("customer.lastName".equals(sortBy)) {
                        String lastName1 = r1.getCustomer().getLastName();
                        String lastName2 = r2.getCustomer().getLastName();
                        return order.equalsIgnoreCase("asc")
                                ? lastName1.compareToIgnoreCase(lastName2)
                                : lastName2.compareToIgnoreCase(lastName1);
                    } else if ("car.brand".equals(sortBy)) {
                        String brand1 = r1.getCar().getBrand();
                        String brand2 = r2.getCar().getBrand();
                        return order.equalsIgnoreCase("asc")
                                ? brand1.compareToIgnoreCase(brand2)
                                : brand2.compareToIgnoreCase(brand1);
                    } else if ("startDate".equals(sortBy)) {
                        return order.equalsIgnoreCase("asc")
                                ? r1.getStartDate().compareTo(r2.getStartDate())
                                : r2.getStartDate().compareTo(r1.getStartDate());
                    }
                    return 0;
                })
                .toList();

        model.addAttribute("rentals", rentals);
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "panel/rentals";

    }
    // 📋 aktywne wypożyczenia
    @GetMapping("/active")
    public String activeRentals(Model model) {
        model.addAttribute(
                "rentals",
                rentalRepository.findByReturnedFalseOrderByRentalDateDesc()
        );
        return "panel/rentals-active";
    }

    // =====================================================
    // ✅ ZAPIS WYPOŻYCZENIA
    // =====================================================
    @PostMapping("/add")
    public String saveRental(

            @RequestParam Long carId,
            @RequestParam Long customerId,
            @RequestParam RentalType rentalType,

            @RequestParam(required = false) BigDecimal pricePerDay,
            @RequestParam(required = false) BigDecimal pricePerMonth,

            @RequestParam BigDecimal deposit,
            @RequestParam Integer startMileage,

            @RequestParam (required = false)LocalDate startDate,
            @RequestParam (required = false)LocalDate endDate,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,

            @RequestParam(required = false) Integer billingDayOfMonth

                ) {
        if (rentalType == RentalType.SHORT_TERM && pricePerDay == null) {
            throw new IllegalArgumentException("Brak ceny za dobę");
        }

        Car car = carRepository.findById(carId).orElseThrow();
        Customer customer = customerRepository.findById(customerId).orElseThrow();


        // 🔒 blokada – nie wypożyczamy niedostępnego auta
        if (!car.canBeRented()) {
            return "redirect:/panel/cars";
        }
        if (!car.canBeRented() || car.getStatus() != CarStatus.AVAILABLE) {
            return "redirect:/panel/cars?error=car_unavailable";
        }

            // ✅ WALIDACJA DAT
            if (startDate == null || endDate == null) {
                return "redirect:/panel/cars?error=missing_dates";
            }

            if (endDate.isBefore(startDate)) {
                return "redirect:/panel/cars?error=invalid_dates";
            }

            // ✅ WALIDACJA CENY
            if (rentalType == RentalType.SHORT_TERM && pricePerDay == null) {
                return "redirect:/panel/cars?error=missing_price";
            }

            if (rentalType == RentalType.LONG_TERM && pricePerMonth == null) {
                return "redirect:/panel/cars?error=missing_price";
            }
        Rental rental = new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setRentalType(rentalType);
        rental.setRentalDate(LocalDateTime.now());
        rental.setReturned(false);
        rental.setStartTime(startTime);
        rental.setEndTime(endTime);

        // 💰 dane umowy
        rental.setPricePerDay(pricePerDay);
        rental.setPricePerMonth(pricePerMonth);
        rental.setDeposit(deposit);

        // 🚗 stan auta
        rental.setStartMileage(startMileage);

        // 📅 okres
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setBillingDayOfMonth(billingDayOfMonth);

        // 🔄 aktualizacja auta
        car.setAvailable(false);
        car.updateStatus();

        rentalRepository.save(rental);
        carRepository.save(car);

        return "redirect:/panel/cars";
    }

    // =====================================================
    // 🔄 ZWROT SAMOCHODU
    // URL: /panel/rentals/{id}/return
    // =====================================================
    @PostMapping("/{id}/return")
    public String returnRental(@PathVariable Long id) {

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (rental.isReturned()) {
            return "redirect:/panel/rentals";
        }

        rental.setReturned(true);
        rental.setReturnDate(LocalDateTime.now());

        Car car = rental.getCar();
        car.setAvailable(true);
        car.updateStatus();

        rentalRepository.save(rental);
        carRepository.save(car);

        return "redirect:/panel/rentals";
    }

    @GetMapping("/{id}/contract/pdf")
    public ResponseEntity<byte[]> generateContract(@PathVariable Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        byte[] pdf = contractService.generateContractPdf(rental);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=umowa-krótkoterminowa-" + rental.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


}