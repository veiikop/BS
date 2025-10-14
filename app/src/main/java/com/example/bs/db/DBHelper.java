package com.example.bs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper для управления БД SQLite (паттерн Singleton).
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "bsmobile.db";
    private static final int DB_VERSION = 3;
    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Получает единственный экземпляр DBHelper (паттерн Singleton).
     * @param context Контекст приложения
     * @return Экземпляр DBHelper
     */
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы categories (категории услуг)
        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE);");

        // Создание таблицы masters (мастера)
        db.execSQL("CREATE TABLE masters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "surname TEXT NOT NULL, " +
                "specialty TEXT);");

        // Создание таблицы users (без изменений)
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT, " +
                "surname TEXT, " +
                "birthdate TEXT, " +
                "phone TEXT, " +
                "gender TEXT);");

        // Создание таблицы services (с category_id вместо type)
        db.execSQL("CREATE TABLE services (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "duration INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE);");

        // Создание таблицы appointments (с master_id)
        db.execSQL("CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "service_id INTEGER NOT NULL, " +
                "master_id INTEGER NOT NULL, " +
                "date_time TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "status TEXT DEFAULT 'future', " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (master_id) REFERENCES masters(id) ON DELETE CASCADE);");

        // Инициализация sample-данных
        // Категории
        db.execSQL("INSERT INTO categories (name) VALUES ('макияж');");
        db.execSQL("INSERT INTO categories (name) VALUES ('маникюр');");
        db.execSQL("INSERT INTO categories (name) VALUES ('педикюр');");
        db.execSQL("INSERT INTO categories (name) VALUES ('стрижка');");
        db.execSQL("INSERT INTO categories (name) VALUES ('окрашивание волос');");
        db.execSQL("INSERT INTO categories (name) VALUES ('наращивание ресниц');");
        db.execSQL("INSERT INTO categories (name) VALUES ('массаж');");

        // Мастера
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Юлия', 'Куликова', 'макияж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Валерия', 'Золотова', 'макияж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Антон', 'Зинков', 'стрижка');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Вероника', 'Рамос', 'маникюр');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Ольга', 'Петрова', 'массаж');");

        // Услуги
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Классический макияж', 1, 1500, 45);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Маникюр гель-лак', 2, 1000, 60);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Педикюр', 3, 1200, 75);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Стрижка мужская', 4, 800, 30);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Окрашивание корней', 5, 2000, 90);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Наращивание ресниц 2D', 6, 2500, 120);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Массаж спины', 7, 1800, 60);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Массаж шеи', 7, 1200, 30);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаление всех таблиц и пересоздание
        db.execSQL("DROP TABLE IF EXISTS appointments;");
        db.execSQL("DROP TABLE IF EXISTS services;");
        db.execSQL("DROP TABLE IF EXISTS users;");
        db.execSQL("DROP TABLE IF EXISTS masters;");
        db.execSQL("DROP TABLE IF EXISTS categories;");
        onCreate(db);
    }
}