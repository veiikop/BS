package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.User;

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
     * Регистрирует нового пользователя.
     * @param user Объект User
     * @return ID вставленной записи
     */
    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("login", user.getLogin());
        values.put("password", user.getPassword());
        values.put("name", user.getName());
        values.put("surname", user.getSurname());
        values.put("birthdate", user.getBirthdate());
        values.put("phone", user.getPhone());
        values.put("gender", user.getGender());
        return db.insert("users", null, values);
    }

    /**
     * Получает пользователя по логину.
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
     * Получает пользователя по ID.
     * @param id ID пользователя
     * @return User или null
     */
    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("login")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
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
     * Обновляет данные пользователя.
     * @param user Объект User
     * @return Количество обновленных строк
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", user.getPassword());
        values.put("name", user.getName());
        values.put("surname", user.getSurname());
        values.put("birthdate", user.getBirthdate());
        values.put("phone", user.getPhone());
        values.put("gender", user.getGender());
        return db.update("users", values, "id=?", new String[]{String.valueOf(user.getId())});
    }

    /**
     * Удаляет пользователя.
     * @param id ID пользователя
     * @return Количество удаленных строк
     */
    public int deleteUser(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("users", "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Получает всех пользователей.
     * @return Список пользователей
     */
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