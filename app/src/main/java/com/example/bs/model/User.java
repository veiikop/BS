package com.example.bs.model;
/**
 * Модель пользователя для хранения профиля клиента
 */
public class User {
    private long id; // ID из БД
    private String login; // Логин (уникальный)
    private String password; // Хэшированный пароль
    private String name; // Имя
    private String surname; // Фамилия
    private String birthdate; // Дата рождения (формат YYYY-MM-DD)
    private String phone; // Телефон
    private String gender; // Пол

    // Конструктор (пустой и полный)
    public User() {}

    public User(long id, String login, String password, String name, String surname, String birthdate, String phone, String gender) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.phone = phone;
        this.gender = gender;
    }

    // Getter и Setter для каждого поля (с summary)
    /**
     * Возвращает ID пользователя.
     * @return ID
     */
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    /**
     * Возвращает логин пользователя.
     * @return Логин
     */
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    /**
     * Возвращает хэшированный пароль пользователя.
     * @return Пароль
     */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Возвращает имя пользователя.
     * @return Имя
     */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /**
     * Возвращает фамилию пользователя.
     * @return Фамилия
     */
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    /**
     * Возвращает дату рождения пользователя.
     * @return Дата рождения
     */
    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    /**
     * Возвращает номер телефона пользователя.
     * @return Телефон
     */
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Возвращает пол пользователя.
     * @return Пол
     */
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

}
