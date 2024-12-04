package com.example.mi_aplicacion_2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController3 {

    @GetMapping("/hello3")
    public String hola() {
        return "Hola Mundos";
    }
}