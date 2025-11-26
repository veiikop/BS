package com.example.bs.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bs.R;
import com.example.bs.db.UserDao;
import com.example.bs.model.User;

/**
 * Активность для входа пользователя.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword;
    private UserDao userDao;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Проверяем, не авторизован ли уже пользователь
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (isUserLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        // Инициализация DAO
        userDao = new UserDao(this);

        // Привязка элементов UI
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textRegister = findViewById(R.id.textRegister); // Теперь этот ID существует

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
                // Сохраняем ID текущего пользователя
                sharedPreferences.edit()
                        .putLong("user_id", user.getId())
                        .apply();
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
    }

    /**
     * Проверяет, авторизован ли пользователь
     */
    private boolean isUserLoggedIn() {
        return sharedPreferences.getLong("user_id", -1) != -1;
    }

    /**
     * Переход к главной активности
     */
    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Переход к активности регистрации
     */
    private void navigateToRegister() {
        startActivity(new Intent(this, RegisterActivity.class));
        // Не закрываем LoginActivity, чтобы можно было вернуться назад
    }
}