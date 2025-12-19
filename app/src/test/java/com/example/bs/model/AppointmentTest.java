package com.example.bs.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppointmentTest {

    @Test
    public void testAppointmentConstructorAndGetters() {
        // Arrange
        long id = 1L;
        long userId = 10L;
        long serviceId = 20L;
        long masterId = 30L;
        String dateTime = "2024-03-20 14:30";
        double price = 1500.0;
        String status = "future";

        // Act
        Appointment appointment = new Appointment(id, userId, serviceId, masterId, dateTime, price, status);

        // Assert
        assertEquals(id, appointment.getId());
        assertEquals(userId, appointment.getUserId());
        assertEquals(serviceId, appointment.getServiceId());
        assertEquals(masterId, appointment.getMasterId());
        assertEquals(dateTime, appointment.getDateTime());
        assertEquals(price, appointment.getPrice(), 0.001);
        assertEquals(status, appointment.getStatus());
    }

    @Test
    public void testAppointmentSetters() {
        // Arrange
        Appointment appointment = new Appointment();

        // Act
        appointment.setId(1L);
        appointment.setUserId(10L);
        appointment.setServiceId(20L);
        appointment.setMasterId(30L);
        appointment.setDateTime("2024-03-20 14:30");
        appointment.setPrice(1500.0);
        appointment.setStatus("future");

        // Assert
        assertEquals(1L, appointment.getId());
        assertEquals(10L, appointment.getUserId());
        assertEquals(20L, appointment.getServiceId());
        assertEquals(30L, appointment.getMasterId());
        assertEquals("2024-03-20 14:30", appointment.getDateTime());
        assertEquals(1500.0, appointment.getPrice(), 0.001);
        assertEquals("future", appointment.getStatus());
    }

    @Test
    public void testAppointmentDefaultConstructor() {
        // Arrange & Act
        Appointment appointment = new Appointment();

        // Assert
        assertEquals(0L, appointment.getId());
        assertEquals(0L, appointment.getUserId());
        assertEquals(0L, appointment.getServiceId());
        assertEquals(0L, appointment.getMasterId());
        assertNull(appointment.getDateTime());
        assertEquals(0.0, appointment.getPrice(), 0.001);
        assertNull(appointment.getStatus());
    }

    @Test
    public void testAppointmentStatusValues() {
        // Test different status values
        Appointment appointment = new Appointment();

        appointment.setStatus("future");
        assertEquals("future", appointment.getStatus());

        appointment.setStatus("past");
        assertEquals("past", appointment.getStatus());

        appointment.setStatus("cancelled");
        assertEquals("cancelled", appointment.getStatus());
    }
}