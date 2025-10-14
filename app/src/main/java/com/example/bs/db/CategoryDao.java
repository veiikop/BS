package com.example.bs.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bs.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO для операций с категориями.
 */
public class CategoryDao {
    private DBHelper dbHelper;

    public CategoryDao(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /**
     * Добавляет новую категорию.
     * @param category Объект Category
     * @return ID вставленной записи
     */
    public long insertCategory(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", category.getName());
        return db.insert("categories", null, values);
    }

    /**
     * Получает все категории.
     * @return Список категорий
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("categories", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name"))
                );
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    /**
     * Получает категорию по ID.
     * @param id ID категории
     * @return Category или null
     */
    public Category getCategoryById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("categories", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Category category = null;
        if (cursor.moveToFirst()) {
            category = new Category(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name"))
            );
        }
        cursor.close();
        return category;
    }
}