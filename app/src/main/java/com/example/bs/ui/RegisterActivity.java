package com.example.bs.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bs.R;
import com.example.bs.db.UserDao;
import com.example.bs.model.User;

/**
 * Активность для регистрации нового пользователя.
 * Обеспечивает безопасную регистрацию с хэшированием паролей.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword, editTextConfirmPassword;
    private UserDao userDao;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация DAO
        userDao = new UserDao(this);

        // Инициализируем SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Привязка элементов UI
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Логика регистрации
        buttonRegister.setOnClickListener(v -> {
            String login = editTextLogin.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (validateInput(login, password, confirmPassword)) {
                registerUser(login, password);
            }
        });

        buttonLogin.setOnClickListener(v -> {
            // Очищаем данные авторизации перед переходом к логину
            sharedPreferences.edit()
                    .remove("user_id")
                    .remove("user_login")
                    .apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Валидирует ввод пользователя при регистрации.
     * @param login Логин пользователя
     * @param password Пароль
     * @param confirmPassword Подтверждение пароля
     * @return true если валидация успешна, false в противном случае
     */
    private boolean validateInput(String login, String password, String confirmPassword) {
        // Проверка на пустые поля
        if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Заполните все поля");
            return false;
        }

        // Проверка минимальной длины логина
        if (login.length() < 3) {
            showToast("Логин должен содержать минимум 3 символа");
            return false;
        }

        // Проверка минимальной длины пароля
        if (password.length() < 6) {
            showToast("Пароль должен содержать минимум 6 символов");
            return false;
        }

        // Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            showToast("Пароли не совпадают");
            return false;
        }

        // Проверка на допустимые символы в логине
        if (!login.matches("^[a-zA-Z0-9_]+$")) {
            showToast("Логин может содержать только буквы, цифры и символ подчеркивания");
            return false;
        }

        return true;
    }

    /**
     * Регистрирует нового пользователя в системе.
     * @param login Логин пользователя
     * @param password Пароль пользователя (будет автоматически хэширован)
     */
    private void registerUser(String login, String password) {
        // Проверка уникальности логина
        if (userDao.getUserByLogin(login) != null) {
            showToast("Логин уже занят");
            return;
        }

        // Создание пользователя (пароль будет хэширован в UserDao.insertUser)
        User user = new User(0, login, password, "", "", "", "", "", "");
        long id = userDao.insertUser(user);

        if (id != -1) {
            // Успешная регистрация
            handleSuccessfulRegistration(id, login);
        } else {
            showToast("Ошибка при регистрации");
        }
    }

    /**
     * Обрабатывает успешную регистрацию пользователя.
     * @param userId ID зарегистрированного пользователя
     * @param login Логин пользователя
     */
    private void handleSuccessfulRegistration(long userId, String login) {
        // Сохраняем ID пользователя в SharedPreferences
        sharedPreferences.edit()
                .putLong("user_id", userId)
                .putString("user_login", login)
                .apply();

        showToast("Регистрация успешна! Добро пожаловать, " + login + "!");

        // Переход на главную активность
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Показывает всплывающее уведомление.
     * @param message Текст сообщения
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Очищает поля ввода при возобновлении активности.
     */
    @Override
    protected void onResume() {
        super.onResume();
        clearPasswordFields();
    }

    /**
     * Очищает поля паролей для безопасности.
     */
    private void clearPasswordFields() {
        editTextPassword.setText("");
        editTextConfirmPassword.setText("");
    }

    /**
     * Предотвращает утечку данных при сворачивании приложения.
     */
    @Override
    protected void onPause() {
        super.onPause();
        clearPasswordFields();
    }
}