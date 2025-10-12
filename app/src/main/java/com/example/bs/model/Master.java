package com.example.bs.model;

/**
 * Модель мастера салона.
 */
public class Master {
    private long id; // ID из БД
    private String name; // Имя
    private String surname; // Фамилия
    private String specialty; // Специализация (e.g. "макияж" или categoryId как String/long)

    // Конструкторы
    public Master() {}

    public Master(long id, String name, String surname, String specialty) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.specialty = specialty;
    }

    // Getter/Setter
    /**
     * Возвращает ID мастера.
     * @return ID
     */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /**
     * Возвращает имя мастера.
     * @return Имя
     */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Возвращает фамилию мастера.
     * @return Фамилия
     */
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    /**
     * Возвращает специализацию мастера.
     * @return Специализация
     */
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}