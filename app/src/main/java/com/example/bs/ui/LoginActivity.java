package com.example.bs.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bs.R;
import com.example.bs.db.DBHelper;
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

        // Инициализация DAO
        userDao = new UserDao(this);

        // Инициализируем SharedPreferences
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Привязка элементов UI
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);

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

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }
}