package com.example.bs.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.bs.R;
import com.example.bs.ui.fragments.AppointmentsFragment;
import com.example.bs.ui.fragments.CatalogFragment;
import com.example.bs.ui.fragments.HomeFragment;
import com.example.bs.ui.fragments.ProfileFragment;
import com.example.bs.util.SessionManager;
import com.example.bs.util.NotificationHelper;
import com.example.bs.util.NotificationScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Главная активность с Bottom Navigation.
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Создаем канал уведомлений
        NotificationHelper.createNotificationChannel(this);
        // Запускаем планировщик уведомлений
        NotificationScheduler.scheduleReminderWork(this);
        // Инициализация менеджера сессий
        sessionManager = new SessionManager(this);

        // Проверяем авторизацию
        if (!sessionManager.isLoggedIn()) {
            // Если нет активной сессии, возвращаем на экран входа
            navigateToLogin();
            return;
        }

        navView = findViewById(R.id.bottom_navigation);

        // Установка начального фрагмента (Главная)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navView.setSelectedItemId(R.id.nav_home);
        }

        // Обработчик переключения
        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                tag = "home";
            } else if (itemId == R.id.nav_catalog) {
                selectedFragment = new CatalogFragment();
                tag = "catalog";
            } else if (itemId == R.id.nav_appointments) {
                // Проверяем авторизацию перед переходом к записям
                if (!sessionManager.isLoggedIn()) {
                    navigateToLogin();
                    return false;
                }
                selectedFragment = new AppointmentsFragment();
                tag = "appointments";
            } else if (itemId == R.id.nav_profile) {
                // Проверяем авторизацию перед переходом к профилю
                if (!sessionManager.isLoggedIn()) {
                    navigateToLogin();
                    return false;
                }
                selectedFragment = new ProfileFragment();
                tag = "profile";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, tag)
                        .addToBackStack(tag)
                        .commit();
                return true;
            }
            return false;
        });

        // Настраиваем обработчик кнопки "Назад" через OnBackPressedDispatcher
        setupBackPressHandler();
    }

    /**
     * Настраивает обработчик кнопки "Назад"
     */
    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Проверяем, есть ли фрагменты в стеке
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();

                    // Обновляем выделение в BottomNavigationView
                    updateBottomNavigationSelection();
                } else {
                    // Если это корневой фрагмент, выходим из приложения
                    moveTaskToBack(true);
                }
            }
        };

        // Добавляем callback к диспетчеру
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * Обновляет выделение в BottomNavigationView после нажатия "Назад"
     */
    private void updateBottomNavigationSelection() {
        // Получаем текущий фрагмент
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            String tag = currentFragment.getTag();
            if (tag != null) {
                switch (tag) {
                    case "home":
                        navView.setSelectedItemId(R.id.nav_home);
                        break;
                    case "catalog":
                        navView.setSelectedItemId(R.id.nav_catalog);
                        break;
                    case "appointments":
                        navView.setSelectedItemId(R.id.nav_appointments);
                        break;
                    case "profile":
                        navView.setSelectedItemId(R.id.nav_profile);
                        break;
                }
            }
        } else {
            // Если фрагмент не найден, показываем Home
            navView.setSelectedItemId(R.id.nav_home);
        }
    }

    /**
     * Перенаправляет на экран входа
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Проверяем валидность сессии при каждом возобновлении активности
        if (!sessionManager.validateSession()) {
            // Сессия невалидна, возвращаем на экран входа
            navigateToLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Можно добавить логику очистки при завершении приложения
    }
}