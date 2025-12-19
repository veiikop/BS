package com.example.bs.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void testUserConstructorAndGetters() {
        // Arrange
        long id = 1L;
        String login = "testuser";
        String password = "hashedPassword123";
        String salt = "randomSalt";
        String name = "Иван";
        String surname = "Иванов";
        String birthdate = "1990-05-15";
        String phone = "+7 (912) 345-67-89";
        String gender = "мужской";

        // Act
        User user = new User(id, login, password, salt, name, surname, birthdate, phone, gender);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(login, user.getLogin());
        assertEquals(password, user.getPassword());
        assertEquals(salt, user.getSalt());
        assertEquals(name, user.getName());
        assertEquals(surname, user.getSurname());
        assertEquals(birthdate, user.getBirthdate());
        assertEquals(phone, user.getPhone());
        assertEquals(gender, user.getGender());
    }

    @Test
    public void testUserSetters() {
        // Arrange
        User user = new User();

        // Act
        user.setId(1L);
        user.setLogin("testuser");
        user.setPassword("hashedPassword123");
        user.setSalt("randomSalt");
        user.setName("Мария");
        user.setSurname("Петрова");
        user.setBirthdate("1995-08-20");
        user.setPhone("+7 (987) 654-32-10");
        user.setGender("женский");

        // Assert
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getLogin());
        assertEquals("hashedPassword123", user.getPassword());
        assertEquals("randomSalt", user.getSalt());
        assertEquals("Мария", user.getName());
        assertEquals("Петрова", user.getSurname());
        assertEquals("1995-08-20", user.getBirthdate());
        assertEquals("+7 (987) 654-32-10", user.getPhone());
        assertEquals("женский", user.getGender());
    }

    @Test
    public void testUserWithEmptyConstructor() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertEquals(0L, user.getId());
        assertNull(user.getLogin());
        assertNull(user.getPassword());
        assertNull(user.getSalt());
        assertNull(user.getName());
        assertNull(user.getSurname());
        assertNull(user.getBirthdate());
        assertNull(user.getPhone());
        assertNull(user.getGender());
    }
}