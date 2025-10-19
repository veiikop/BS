package com.example.bs.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper для управления БД SQLite (паттерн Singleton).
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "bsmobile.db";
    private static final int DB_VERSION = 6;
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
        // Создание таблицы categories
        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE);");

        // Создание таблицы masters
        db.execSQL("CREATE TABLE masters (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "surname TEXT NOT NULL, " +
                "specialty TEXT);");

        // Создание таблицы users
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "login TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "name TEXT, " +
                "surname TEXT, " +
                "birthdate TEXT, " +
                "phone TEXT, " +
                "gender TEXT);");

        // Создание таблицы services
        db.execSQL("CREATE TABLE services (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "price REAL NOT NULL, " +
                "duration INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE);");

        // Создание таблицы appointments
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
        db.execSQL("INSERT INTO categories (name) VALUES ('ламинирование ресниц');");
        db.execSQL("INSERT INTO categories (name) VALUES ('массаж');");
        db.execSQL("INSERT INTO categories (name) VALUES ('брови');");


        // Мастера
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Юлия', 'Куликова', 'макияж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Валерия', 'Золотова', 'макияж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Антон', 'Зинков', 'стрижка');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Вероника', 'Рамос', 'маникюр');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Екатерина', 'Фролова', 'маникюр');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Ольга', 'Петрова', 'массаж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Елена', 'Сулейманова', 'массаж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Анна', 'Кандыбина', 'массаж');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Наталья', 'Громова', 'педикюр');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Юлия', 'Салихова', 'педикюр');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Сергей', 'Мерзликин', 'окрашивание волос');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Ермолова', 'Анастасия', 'окрашивание волос');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Персиянова', 'Эвелина', 'наращивание ресниц');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Иманкулова', 'Оксана', 'ламинирование ресниц');");
        db.execSQL("INSERT INTO masters (name, surname, specialty) VALUES ('Гусева', 'Светлана', 'брови');");

        // Услуги
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Классический макияж', 1, 1500, 45);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Маникюр гель-лак', 2, 1000, 60);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Наращивание гель-лак', 2, 2000, 120);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Гигиенический маникюр', 2, 500, 30);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Педикюр гель-лак', 3, 1200, 75);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Гигиенический педикюр', 3, 600, 40);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Стрижка мужская', 4, 800, 30);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Стрижка женская', 4, 1000, 60);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Окрашивание корней', 5, 2000, 90);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Окрашивание всей длины', 5, 3000, 120);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Окрашивание техникой airtouch', 5, 5000, 160);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Наращивание ресниц 2D', 6, 2500, 120);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Наращивание ресниц 3D', 6, 2700, 120);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Ламинирование ресниц', 7, 2000, 100);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Массаж спины', 8, 1800, 60);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Массаж шеи', 8, 1200, 30);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Окрашивание бровей', 9, 800, 45);");
        db.execSQL("INSERT INTO services (name, category_id, price, duration) VALUES ('Ламинирование бровей', 9, 1000, 45);");
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