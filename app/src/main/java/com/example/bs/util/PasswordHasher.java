package com.example.bs.util;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Утилита для безопасного хэширования паролей с использованием SHA-256 и соли
 */
public class PasswordHasher {

    /**
     * Генерирует случайную соль для хэширования пароля
     * @return Соль в формате Base64
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Хэширует пароль с использованием соли
     * @param password Пароль в чистом виде
     * @param salt Соль для хэширования
     * @return Хэшированный пароль в формате Base64
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();

            // Добавляем соль к паролю
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes());

            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Проверяет соответствие пароля хэшу
     * @param password Пароль для проверки
     * @param salt Соль, использованная при хэшировании
     * @param hashedPassword Хэшированный пароль для сравнения
     * @return true если пароль верный, false в противном случае
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String newHash = hashPassword(password, salt);
        return newHash.equals(hashedPassword);
    }

    /**
     * Генерирует соль и хэширует пароль (удобный метод для регистрации)
     * @param password Пароль в чистом виде
     * @return массив [соль, хэшированный_пароль]
     */
    public static String[] hashNewPassword(String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return new String[]{salt, hashedPassword};
    }
}