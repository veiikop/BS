package com.example.bs.model;

/**
 * Модель записи на услугу.
 */
public class Appointment {
    private long id; // ID из БД
    private long userId; // FK на user.id
    private long serviceId; // FK на service.id
    private long masterId; // FK на master.id (новое)
    private String dateTime; // Дата и время (формат YYYY-MM-DD HH:MM)
    private double price; // Цена на момент записи
    private String status; // Статус (future, past, cancelled)

    // Конструкторы
    public Appointment() {}

    public Appointment(long id, long userId, long serviceId, long masterId, String dateTime, double price, String status) {
        this.id = id;
        this.userId = userId;
        this.serviceId = serviceId;
        this.masterId = masterId;
        this.dateTime = dateTime;
        this.price = price;
        this.status = status;
    }

    // Getter/Setter
    /**
     * Возвращает ID записи.
     * @return ID
     */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /**
     * Возвращает ID пользователя, связанного с записью.
     * @return User ID
     */
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    /**
     * Возвращает ID услуги, связанной с записью.
     * @return Service ID
     */
    public long getServiceId() { return serviceId; }
    public void setServiceId(long serviceId) { this.serviceId = serviceId; }

    /**
     * Возвращает ID мастера, связанного с записью.
     * @return Master ID
     */
    public long getMasterId() { return masterId; }
    public void setMasterId(long masterId) { this.masterId = masterId; }

    /**
     * Возвращает дату и время записи.
     * @return Дата и время
     */
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    /**
     * Возвращает цену записи.
     * @return Цена
     */
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    /**
     * Возвращает статус записи.
     * @return Статус
     */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}