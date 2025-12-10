package com.example.bs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bs.R;
import com.example.bs.db.UserDao;
import com.example.bs.model.User;
import com.example.bs.util.SessionManager;

/**
 * Активность для входа пользователя.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword;
    private CheckBox checkBoxRememberMe;
    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация менеджера сессий
        sessionManager = new SessionManager(this);

        // Проверяем существующую сессию
        if (sessionManager.isLoggedIn()) {
            // Автоматический вход с валидной сессией
            performAutoLogin();
            return;
        }

        // Инициализация DAO
        userDao = new UserDao(this);

        // Привязка элементов UI
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxRememberMe = findViewById(R.id.checkbox_remember_me);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textRegister = findViewById(R.id.textRegister);

        // Загружаем сохраненный логин, если есть
        String savedLogin = sessionManager.getSavedLogin();
        if (!savedLogin.isEmpty()) {
            editTextLogin.setText(savedLogin);
            checkBoxRememberMe.setChecked(true);
        }

        // Логика входа
        buttonLogin.setOnClickListener(v -> {
            String login = editTextLogin.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            // Используем безопасную аутентификацию
            User user = userDao.authenticateUser(login, password);
            if (user != null) {
                // Сохраняем сессию
                boolean rememberMe = checkBoxRememberMe.isChecked();
                sessionManager.createSession(user.getId(), user.getLogin(), rememberMe);

                Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        });

        // Кнопка перехода к регистрации
        textRegister.setOnClickListener(v -> {
            navigateToRegister();
        });
        // Настраиваем обработчик кнопки "Назад"
        setupBackPressHandler();
    }

    /**
     * Выполняет автоматический вход по сохраненной сессии
     */
    private void performAutoLogin() {
        long userId = sessionManager.getUserId();
        String login = sessionManager.getUserLogin();

        // Можно добавить дополнительную проверку существования пользователя
        UserDao userDao = new UserDao(this);
        User user = userDao.getUserById(userId);

        if (user != null) {
            // Продлеваем сессию
            sessionManager.updateLastLogin();

            Toast.makeText(this, "Автоматический вход: " + login, Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        } else {
            // Пользователь был удален, очищаем сессию
            sessionManager.clearSession();
            Toast.makeText(this, "Сессия устарела, войдите заново", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Переход к главной активности
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Переход к активности регистрации
     */
    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        // Не закрываем LoginActivity, чтобы можно было вернуться назад
    }

    @Override
    protected void onResume() {
        super.onResume();
        // При возвращении на экран входа проверяем сессию
        if (sessionManager.isLoggedIn()) {
            // Если уже авторизованы, уходим на главный экран
            navigateToMainActivity();
        }
    }

    /**
     * Настраивает обработчик кнопки "Назад"
     */
    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // При нажатии кнопки "Назад" закрываем приложение
                moveTaskToBack(true);
            }
        };

        // Добавляем callback к диспетчеру
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}