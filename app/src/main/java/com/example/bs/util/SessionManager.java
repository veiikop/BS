package com.example.bs.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Менеджер сессий для управления пользовательскими данными и автоматическим входом
 */
public class SessionManager {

    // Имя файла SharedPreferences
    private static final String PREF_NAME = "user_session";

    // Ключи для хранения данных
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_LOGIN = "user_login";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_LAST_LOGIN = "last_login";

    // Режимы SharedPreferences
    private static final int PRIVATE_MODE = 0;

    // Объекты для работы с SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // Конструктор
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    /**
     * Создание новой сессии пользователя
     * @param userId ID пользователя
     * @param login Логин пользователя
     * @param rememberMe Флаг "запомнить меня"
     */
    public void createSession(long userId, String login, boolean rememberMe) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_LOGIN, login);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.putString(KEY_SESSION_TOKEN, generateSessionToken(userId, login));
        editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis());

        editor.apply();
        Log.d("SessionManager", "Сессия создана для пользователя: " + login);
    }

    /**
     * Проверяет, существует ли активная сессия
     * @return true если пользователь авторизован и сессия валидна
     */
    public boolean isLoggedIn() {
        long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        // Проверяем валидность токена сессии (опционально)
        if (userId != -1) {
            String savedToken = sharedPreferences.getString(KEY_SESSION_TOKEN, "");
            String login = sharedPreferences.getString(KEY_USER_LOGIN, "");
            String expectedToken = generateSessionToken(userId, login);

            // Если токены не совпадают, считаем сессию невалидной
            if (!savedToken.equals(expectedToken)) {
                Log.w("SessionManager", "Невалидный токен сессии");
                clearSession();
                return false;
            }

            // Проверяем срок действия сессии (30 дней для rememberMe, 1 день без него)
            long lastLogin = sharedPreferences.getLong(KEY_LAST_LOGIN, 0);
            long sessionDuration = rememberMe ? 30L * 24 * 60 * 60 * 1000 : 24L * 60 * 60 * 1000;

            if (System.currentTimeMillis() - lastLogin > sessionDuration) {
                Log.w("SessionManager", "Сессия истекла");
                clearSession();
                return false;
            }

            return true;
        }
        return false;
    }

    /**
     * Проверяет, включен ли режим "запомнить меня"
     * @return true если включен
     */
    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Получает ID текущего пользователя
     * @return ID пользователя или -1 если нет сессии
     */
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * Получает логин текущего пользователя
     * @return Логин пользователя или пустую строку
     */
    public String getUserLogin() {
        return sharedPreferences.getString(KEY_USER_LOGIN, "");
    }

    /**
     * Получает сохраненный логин для авто-заполнения
     * @return Логин или пустую строку
     */
    public String getSavedLogin() {
        // Возвращаем сохраненный логин, если есть активная сессия
        if (isRememberMeEnabled() && getUserId() != -1) {
            return getUserLogin();
        }
        return "";
    }

    /**
     * Очищает всю сессию (полный выход)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
        Log.d("SessionManager", "Сессия очищена");
    }

    /**
     * Обновляет время последнего входа (продлевает сессию)
     */
    public void updateLastLogin() {
        editor.putLong(KEY_LAST_LOGIN, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Генерирует токен сессии для проверки валидности
     * @param userId ID пользователя
     * @param login Логин пользователя
     * @return Сгенерированный токен
     */
    private String generateSessionToken(long userId, String login) {
        try {
            // Используем простой хэш для демонстрации
            String data = userId + ":" + login + ":" + "salon_app_secret";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());

            // Конвертируем в hex строку
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("SessionManager", "Ошибка генерации токена", e);
            // Возвращаем простую строку в случае ошибки
            return userId + ":" + login + ":" + "fallback_token";
        }
    }

    /**
     * Проверяет валидность текущей сессии
     * @return true если сессия валидна
     */
    public boolean validateSession() {
        if (!isLoggedIn()) {
            return false;
        }

        // Обновляем время последней активности
        updateLastLogin();
        return true;
    }

    /**
     * Получает информацию о текущей сессии (для отладки)
     * @return Строка с информацией о сессии
     */
    public String getSessionInfo() {
        return String.format(
                "User ID: %d\nLogin: %s\nRemember Me: %s\nLast Login: %s",
                getUserId(),
                getUserLogin(),
                isRememberMeEnabled(),
                new java.util.Date(sharedPreferences.getLong(KEY_LAST_LOGIN, 0))
        );
    }
}