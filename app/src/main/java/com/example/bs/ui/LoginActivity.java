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
 * Активность для входа пользователя.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Инициализация DAO
        userDao = new UserDao(this);

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

            User user = userDao.getUserByLogin(login);
            if (user != null && user.getPassword().equals(password)) {
                Toast.makeText(this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class)); // Переход на главную страницу
                finish();
            } else {
                Toast.makeText(this, "Неправильный пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }
}