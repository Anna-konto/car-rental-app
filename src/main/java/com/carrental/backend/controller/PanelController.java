package com.carrental.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

    @GetMapping("/")
    public String root() {
        return "redirect:/panel/cars";
    }
}
