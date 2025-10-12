package com.example.bs.model;

/**
 * Модель услуги салона.
 */
public class Service {
    private long id; // ID из БД
    private String name; // Название услуги
    private long categoryId; // FK на category.id (замена type)
    private double price; // Цена
    private int duration; // Продолжительность в минутах

    // Конструкторы
    public Service() {}

    public Service(long id, String name, long categoryId, double price, int duration) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.duration = duration;
    }

    // Getter/Setter
    /**
     * Возвращает ID услуги.
     * @return ID
     */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /**
     * Возвращает название услуги.
     * @return Название
     */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Возвращает ID категории услуги.
     * @return Category ID
     */
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    /**
     * Возвращает цену услуги.
     * @return Цена
     */
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    /**
     * Возвращает продолжительность услуги.
     * @return Продолжительность в минутах
     */
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}