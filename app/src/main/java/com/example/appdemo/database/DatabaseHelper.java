package com.example.appdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppDB";
    private static final int DATABASE_VERSION = 2;

    // Bảng Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IS_ADMIN = "is_admin";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_EMAIL + " TEXT PRIMARY KEY,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_IS_ADMIN + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Thêm tài khoản admin mặc định
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_EMAIL, "admin@gmail.com");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_NAME, "Administrator");
        adminValues.put(COLUMN_IS_ADMIN, 1);
        db.insert(TABLE_USERS, null, adminValues);

        // Thêm một tài khoản user thông thường
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_EMAIL, "user@gmail.com");
        userValues.put(COLUMN_PASSWORD, "user123");
        userValues.put(COLUMN_NAME, "Normal User");
        userValues.put(COLUMN_IS_ADMIN, 0);
        db.insert(TABLE_USERS, null, userValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addUser(String email, String password, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NAME, name);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
            new String[]{COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_IS_ADMIN},
            COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
            new String[]{email, password}, 
            null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            boolean isAdmin = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ADMIN)) == 1;
            
            // Lưu thông tin đăng nhập và quyền admin vào SharedPreferences
            SharedPreferences.Editor editor = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
            editor.putString("email", email);
            editor.putBoolean("isAdmin", isAdmin);
            editor.apply();
            
            cursor.close();
            return true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String name = null;

        try {
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_NAME}, 
                COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return name;
    }

    public boolean updateUserName(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);

        try {
            // Cập nhật tên người dùng dựa trên email
            int result = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", 
                new String[]{email});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public boolean updateUser(String email, String newName, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        values.put(COLUMN_PASSWORD, newPassword);

        try {
            // Cập nhật cả tên và mật khẩu dựa trên email
            int result = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", 
                new String[]{email});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = {email};
        int result = db.delete(TABLE_USERS, whereClause, whereArgs);
        db.close();
        return result > 0;
    }

    public boolean updateUserAdminStatus(String email, boolean isAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ADMIN, isAdmin ? 1 : 0);

        try {
            int result = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", 
                new String[]{email});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    // Thêm phương thức để lấy danh sách tất cả users
    public List<UserInfo> getAllUsers() {
        List<UserInfo> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_USERS,
            new String[]{COLUMN_EMAIL, COLUMN_NAME, COLUMN_IS_ADMIN},
            null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                boolean isAdmin = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ADMIN)) == 1;
                
                users.add(new UserInfo(email, name, isAdmin));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return users;
    }

    // Thêm class UserInfo để lưu thông tin user
    public static class UserInfo {
        private String email;
        private String name;
        private boolean isAdmin;

        public UserInfo(String email, String name, boolean isAdmin) {
            this.email = email;
            this.name = name;
            this.isAdmin = isAdmin;
        }

        public String getEmail() { return email; }
        public String getName() { return name; }
        public boolean isAdmin() { return isAdmin; }
    }
} 