package com.example.bs.ui;

import android.content.Intent;
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
 * Активность для регистрации нового пользователя.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword, editTextConfirmPassword;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Инициализация DAO
        userDao = new UserDao(this);

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

            if (login.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверка уникальности логина (упрощенная)
            if (userDao.getUserByLogin(login) != null) {
                Toast.makeText(this, "Логин уже занят", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создание пользователя
            User user = new User(0, login, password, "", "", "", "", "");
            long id = userDao.insertUser(user);
            if (id != -1) {
                Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
            }
        });

        // Переход на страницу входа
        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}