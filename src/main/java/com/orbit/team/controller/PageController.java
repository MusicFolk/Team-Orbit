package com.orbit.team.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    @GetMapping("/")
    public String welcomePage() {
        return "welcome";
    }

    @GetMapping("/home")
    public String homePage() {
        return "welcome";
    }

}