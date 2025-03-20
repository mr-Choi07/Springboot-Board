package com.study.board.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class UserController {
    @GetMapping("/stipulation")
    public String stipulation() {
        return "stipulation";
    }
}
