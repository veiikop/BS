package com.example.bs.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bs.util.SessionManager;

/**
 * Стартовая активность для проверки сессии пользователя
 * Определяет, куда перенаправить пользователя: на главный экран или экран входа
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Не устанавливаем разметку - используем тему splash screen
        SessionManager sessionManager = new SessionManager(this);

        // Задержка для отображения splash screen (2 секунды)
        new Handler().postDelayed(() -> {
            Intent intent;
            if (sessionManager.isLoggedIn()) {
                // Пользователь уже вошел - идем на главный экран
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Пользователь не вошел - идем на экран входа
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Закрываем SplashActivity, чтобы нельзя было вернуться назад
        }, 20); // Задержка
    }

    @Override
    protected void onPause() {
        super.onPause();
        // плавная анимация перехода
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}