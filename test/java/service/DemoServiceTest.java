package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemoServiceTest {

    @Test
    void testGetHelloMessage() {
        DemoService demoService = Mockito.spy(DemoService.class);
        assertEquals("Hello, Spring Boot with Jenkins!", demoService.getHelloMessage());
    }
}