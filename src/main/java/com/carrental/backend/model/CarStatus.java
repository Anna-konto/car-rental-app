package com.carrental.backend.model;

public enum CarStatus {
    AVAILABLE,        // auto dostępne
    RENTED,           // wypożyczone
    OUT_OF_SERVICE,    // niedostępne (przegląd / ubezpieczenie)
    ARCHIVED
}
