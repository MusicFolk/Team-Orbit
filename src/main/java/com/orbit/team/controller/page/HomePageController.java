package com.orbit.team.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {
    @GetMapping("/home")
    public String home() {
        return "redirect:/events";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/events";
    }
}
