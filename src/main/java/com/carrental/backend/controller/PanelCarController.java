package com.carrental.backend.controller;

import com.carrental.backend.model.Car;
import com.carrental.backend.repository.CarRepository;
import com.carrental.backend.repository.RentalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.carrental.backend.model.CarStatus;
import java.util.List;
import com.carrental.backend.model.CarNote;
import com.carrental.backend.repository.CarNoteRepository;
import com.carrental.backend.model.CarServiceEntry;
import com.carrental.backend.repository.CarServiceEntryRepository;
import java.time.LocalDate;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

@RequestMapping("/panel/cars")
@Controller
public class PanelCarController {

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final CarNoteRepository carNoteRepository;
    private final CarServiceEntryRepository carServiceEntryRepository;

    public PanelCarController(
            CarRepository carRepository,
            RentalRepository rentalRepository,
            CarNoteRepository carNoteRepository,
            CarServiceEntryRepository carServiceEntryRepository
    ) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
        this.carNoteRepository = carNoteRepository;
        this.carServiceEntryRepository = carServiceEntryRepository;

    }

    // 📄 lista samochodów
    @GetMapping
    public String cars(
            @RequestParam(required = false) String plate,
            @RequestParam(required = false) CarStatus status,
            Model model
    ) {
        List<Car> cars;
        if (plate != null && !plate.isBlank() && status != null) {
            cars = carRepository.findByArchivedFalse().stream()
                    .filter(c -> c.getPlateNumber().toLowerCase().contains(plate.toLowerCase()))
                    .filter(c -> c.getStatus() == status)
                    .toList();

        } else if (plate != null && !plate.isBlank()) {
            cars = carRepository.findByArchivedFalseAndPlateNumberContainingIgnoreCase(plate);

        } else if (status != null) {
            cars = carRepository.findByArchivedFalseAndStatus(status);

        } else {
            cars = carRepository.findByArchivedFalse();
        }

        model.addAttribute("cars", cars);
        model.addAttribute("plate", plate);
        model.addAttribute("status", status);

        return "panel/cars";
    }

    // ➕ dodanie samochodu
    @PostMapping("/add")
    public String addCar(
            @ModelAttribute("car") Car car,
            Model model
    ) {
        try {
            car.updateStatus();
            carRepository.save(car);
            return "redirect:/panel/cars";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "❌ Samochód o podanym numerze VIN już istnieje");
            model.addAttribute("car", car); // ⬅ MUSI wrócić do widoku
            return "panel/car-form";
        }
    }


    // 🔍 szczegóły samochodu + historia wypożyczeń
    @GetMapping("/{id}")
    public String carDetails(@PathVariable Long id, Model model) {

        Car car = carRepository.findById(id)
                .orElse(null);

        if (car == null) {
            return "redirect:/panel/cars";
        }

        model.addAttribute("car", car);
        model.addAttribute(
                "rentals",
                rentalRepository.findByCarIdOrderByRentalDateDesc(id)
        );
        model.addAttribute(
                "notes",
                carNoteRepository.findByCarIdOrderByCreatedAtDesc(id)
        );
        model.addAttribute("newNote", new CarNote());

        return "panel/car-details";

    }
    @PostMapping("/{id}/notes")
    public String addCarNote(
            @PathVariable Long id,
            @RequestParam String content
    ) {
        Car car = carRepository.findById(id).orElseThrow();
CarNote note = new CarNote();
        note.setCar(car);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());

        carNoteRepository.save(note);

        return "redirect:/panel/cars/" + id;
    }
    @PostMapping("/{carId}/notes/{noteId}/delete")
    public String deleteCarNote(
            @PathVariable Long carId,
            @PathVariable Long noteId
    ) {
        carNoteRepository.deleteById(noteId);
        return "redirect:/panel/cars/" + carId;
    }
    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id, Model model) {

        // ❌ blokada – auto aktualnie wypożyczone
        if (!rentalRepository.findByCarIdAndReturnedFalse(id).isEmpty()) {
            model.addAttribute("error",
                    "Nie można usunąć samochodu, który jest aktualnie wypożyczony");
            model.addAttribute("cars", carRepository.findAll());
            model.addAttribute("newCar", new Car());
            return "panel/cars";
        }

        // ✅ można usunąć
        carRepository.deleteById(id);
        return "redirect:/panel/cars";
    }
    @PostMapping("/{id}/archive")
    public String archiveCar(@PathVariable Long id) {

        Car car = carRepository.findById(id).orElseThrow();

        // ❌ NIE WOLNO archiwizować wypożyczonego auta
        if (car.getStatus() == CarStatus.RENTED) {
            return "redirect:/panel/cars?error=car_rented";
        }

        car.setArchived(true);
        carRepository.save(car);

        return "redirect:/panel/cars";
    }
    @GetMapping("/archived")
    public String archivedCars(Model model) {

        model.addAttribute(
                "cars",
                carRepository.findByArchivedTrue()
        );

        return "panel/cars-archived";
    }
    @GetMapping("/{id}/service")
    public String carServiceBook(@PathVariable Long id, Model model) {

        Car car = carRepository.findById(id).orElseThrow(()
                  -> new RuntimeException("Car not found"));
        model.addAttribute("car", car);
        model.addAttribute("entries",
                carServiceEntryRepository.findByCarIdOrderByServiceDateDesc(id));

        return "panel/car-service";
    }
    @PostMapping("/{id}/service/add")
    public String addServiceEntry(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam(required = false) LocalDate serviceDate
    ) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        CarServiceEntry entry = new CarServiceEntry();
        entry.setCar(car);
        entry.setType(type);
        entry.setDescription(description);
        entry.setServiceDate(
                serviceDate != null ? serviceDate : LocalDate.now()
        );

        carServiceEntryRepository.save(entry);

        return "redirect:/panel/cars/" + id + "/service";
    }
    @PostMapping("/{id}/restore")
    public String restoreCar(@PathVariable Long id) {

        Car car = carRepository.findById(id).orElseThrow();

        car.setArchived(false);
        car.updateStatus();

        carRepository.save(car);

        return "redirect:/panel/cars/archived";
    }
    @GetMapping("/add")
    public String showAddCarForm(Model model) {
        model.addAttribute("car", new Car()); // 🔴 KLUCZOWE
        return "panel/car-form";
    }


}
