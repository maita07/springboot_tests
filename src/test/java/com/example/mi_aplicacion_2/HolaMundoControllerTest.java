package com.example.mi_aplicacion_2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class HolaMundoControllerTest {

    @Test
    public void testHola() {
        HelloController controller = new HelloController();
        String respuesta = controller.hola();
        assertEquals("Hola Mundo", respuesta);
    }

    @Test
    public void testHola1() {
        HelloController controller = new HelloController1();
        String respuesta = controller.hola();
        assertEquals("Hola Mundo", respuesta);
    }

    @Test
    public void testHola2() {
        HelloController controller = new HelloController2();
        String respuesta = controller.hola();
        assertEquals("Hola Mundo", respuesta);
    }

    @Test
    public void testHola3() {
        HelloController controller = new HelloController3();
        String respuesta = controller.hola();
        assertEquals("Hola Mundo", respuesta);
    }
}