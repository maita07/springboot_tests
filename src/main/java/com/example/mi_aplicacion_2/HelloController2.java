package com.example.mi_aplicacion_2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController2 {

    @GetMapping("/hello2")
    public String hola() {
        return "Hola Mundo";
    }
}