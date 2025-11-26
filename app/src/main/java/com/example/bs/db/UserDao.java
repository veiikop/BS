package com.example.bs.db;

// Добавляем импорт
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.User;
import com.example.bs.util.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO для операций с пользователями (паттерн Facade).
 */
public class UserDao {
    private DBHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /**
     * Регистрирует нового пользователя с хэшированным паролем.
     * @param user Объект User (пароль должен быть в чистом виде)
     * @return ID вставленной записи
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Хэшируем пароль и генерируем соль
        String[] hashResult = PasswordHasher.hashNewPassword(user.getPassword());
        String salt = hashResult[0];
        String hashedPassword = hashResult[1];

        values.put("login", user.getLogin());
        values.put("password", hashedPassword);
        values.put("salt", salt);
        values.put("name", user.getName());
        values.put("surname", user.getSurname());
        values.put("birthdate", user.getBirthdate());
        values.put("phone", user.getPhone());
        values.put("gender", user.getGender());

        return db.insert("users", null, values);
    }

    /**
     * Проверяет логин и пароль пользователя.
     * @param login Логин
     * @param password Пароль в чистом виде
     * @return User если аутентификация успешна, null в противном случае
     */
    public User authenticateUser(String login, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "login=?", new String[]{login}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            String salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));

            // Проверяем пароль с использованием соли
            if (PasswordHasher.verifyPassword(password, salt, storedPassword)) {
                user = new User(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("login")),
                        storedPassword,
                        salt,
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("birthdate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
            }
        }
        cursor.close();
        return user;
    }

    /**
     * Получает пользователя по логину (без проверки пароля).
     * @param login Логин
     * @return User или null
     */
    public User getUserByLogin(String login) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "login=?", new String[]{login}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("login")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                    cursor.getString(cursor.getColumnIndexOrThrow("birthdate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gender"))
            );
        }
        cursor.close();
        return user;
    }

    /**
     * Обновляет пароль пользователя с хэшированием.
     * @param userId ID пользователя
     * @param newPassword Новый пароль в чистом виде
     * @return Количество обновленных строк
     */
    public int updateUserPassword(long userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Хэшируем новый пароль
        String[] hashResult = PasswordHasher.hashNewPassword(newPassword);
        values.put("password", hashResult[1]);
        values.put("salt", hashResult[0]);

        return db.update("users", values, "id=?", new String[]{String.valueOf(userId)});
    }

    /**
     * Обновляет данные пользователя (кроме пароля).
     * @param user Объект User
     * @return Количество обновленных строк
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("surname", user.getSurname());
        values.put("birthdate", user.getBirthdate());
        values.put("phone", user.getPhone());
        values.put("gender", user.getGender());
        return db.update("users", values, "id=?", new String[]{String.valueOf(user.getId())});
    }

    // Остальные методы остаются без изменений...
    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("login")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                    cursor.getString(cursor.getColumnIndexOrThrow("birthdate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gender"))
            );
        }
        cursor.close();
        return user;
    }

    public int deleteUser(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("users", "id=?", new String[]{String.valueOf(id)});
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                User user = new User(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("login")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("salt")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("surname")),
                        cursor.getString(cursor.getColumnIndexOrThrow("birthdate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                );
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }
}