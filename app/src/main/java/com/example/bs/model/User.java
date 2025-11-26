package com.example.bs.model;

/**
 * Модель пользователя для хранения профиля клиента
 */
public class User {
    private long id; // ID из БД
    private String login; // Логин (уникальный)
    private String password; // Хэшированный пароль
    private String salt; // Соль для хэширования пароля (новое поле)
    private String name; // Имя
    private String surname; // Фамилия
    private String birthdate; // Дата рождения (формат YYYY-MM-DD)
    private String phone; // Телефон
    private String gender; // Пол

    // Конструктор (пустой и полный)
    public User() {}

    public User(long id, String login, String password, String salt, String name, String surname, String birthdate, String phone, String gender) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.salt = salt;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.phone = phone;
        this.gender = gender;
    }

    // Getter и Setter для каждого поля
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}