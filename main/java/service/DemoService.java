package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class DemoService {
    public String getHelloMessage() {
        return "Hello, Spring Boot with Jenkins!";
    }
}