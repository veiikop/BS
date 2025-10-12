package com.example.bs.model;

/**
 * Модель категории услуг салона.
 */
public class Category {
    private long id; // ID из БД
    private String name; // Название категории (e.g. "макияж")

    // Конструкторы
    public Category() {}

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getter/Setter
    /**
     * Возвращает ID категории.
     * @return ID
     */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /**
     * Возвращает название категории.
     * @return Название
     */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}