package com.example.appdemo.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appdemo.Model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ProductDB";
    private static final int DATABASE_VERSION = 2;

    // Bảng Products
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_CATEGORY = "category";

    public ProductDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT,"
                + COLUMN_CATEGORY + " TEXT"
                + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Thêm cột category nếu chưa có
            db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Cho phép hạ cấp database mà không gây lỗi
        onUpgrade(db, oldVersion, newVersion);
    }

    // Thêm sản phẩm mới
    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_IMAGE_URL, product.getImageUrl());
        values.put(COLUMN_CATEGORY, product.getCategory());

        long id = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return id;
    }

    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY))
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }

    // Cập nhật sản phẩm
    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_IMAGE_URL, product.getImageUrl());
        values.put(COLUMN_CATEGORY, product.getCategory());

        int result = db.update(TABLE_PRODUCTS, values, 
            COLUMN_ID + " = ?",
            new String[]{String.valueOf(product.getId())});
        db.close();
        return result;
    }

    // Xóa sản phẩm
    public void deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, COLUMN_ID + " = ?", 
            new String[]{String.valueOf(id)});
        db.close();
    }

    // Lấy sản phẩm theo ID
    public Product getProduct(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS,
            new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_PRICE, COLUMN_DESCRIPTION, COLUMN_IMAGE_URL, COLUMN_CATEGORY},
            COLUMN_ID + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Product product = new Product(
                cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)),
                cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY))
            );
            cursor.close();
            return product;
        }
        return null;
    }

    // Tìm kiếm sản phẩm theo tên
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS + 
            " WHERE " + COLUMN_NAME + " LIKE '%" + keyword + "%'";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY))
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }
} 