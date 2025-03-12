package com.example.appdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.example.appdemo.Model.Order;
import com.example.appdemo.Model.OrderItem;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppDB";
    private static final int DATABASE_VERSION = 4;

    // Bảng Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IS_ADMIN = "is_admin";

    // Thêm bảng orders
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_ORDER_DATE = "order_date";
    private static final String COLUMN_STATUS = "status";

    // Thêm bảng order_items
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_PRODUCT_NAME = "product_name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_PRICE = "price";

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

        // Tạo bảng orders trước order_items vì có foreign key
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " TEXT,"
                + COLUMN_TOTAL_AMOUNT + " REAL,"
                + COLUMN_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_STATUS + " TEXT DEFAULT 'Pending',"
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ")"
                + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Tạo bảng order_items
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ORDER_ID + " INTEGER,"
                + COLUMN_PRODUCT_ID + " INTEGER,"
                + COLUMN_PRODUCT_NAME + " TEXT,"
                + COLUMN_QUANTITY + " INTEGER,"
                + COLUMN_PRICE + " REAL,"
                + "FOREIGN KEY(" + COLUMN_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COLUMN_ORDER_ID + ")"
                + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        // Thêm tài khoản admin mặc định
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_EMAIL, "admin@gmail.com");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_NAME, "Administrator");
        adminValues.put(COLUMN_IS_ADMIN, 1);
        db.insert(TABLE_USERS, null, adminValues);

        // Thêm tài khoản user thông thường
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_EMAIL, "user@gmail.com");
        userValues.put(COLUMN_PASSWORD, "user123");
        userValues.put(COLUMN_NAME, "Normal User");
        userValues.put(COLUMN_IS_ADMIN, 0);
        db.insert(TABLE_USERS, null, userValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop các bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        
        // Tạo lại các bảng
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

    // Thêm đơn hàng mới
    public long addOrder(String userId, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TOTAL_AMOUNT, totalAmount);
        values.put(COLUMN_STATUS, "Đã thanh toán");
        return db.insert(TABLE_ORDERS, null, values);
    }

    // Lấy danh sách đơn hàng của user
    public List<Order> getOrdersByUser(String userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_ORDERS 
                + " WHERE " + COLUMN_USER_ID + " = ?"
                + " ORDER BY " + COLUMN_ORDER_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order();
                order.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_ID)));
                order.setUserId(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID)));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndex(COLUMN_TOTAL_AMOUNT)));
                order.setStatus(cursor.getString(cursor.getColumnIndex(COLUMN_STATUS)));
                // Chuyển đổi timestamp thành Date
                String dateStr = cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE));
                try {
                    order.setOrderDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                orders.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return orders;
    }

    // Thêm phương thức để lưu chi tiết đơn hàng
    public long addOrderItem(int orderId, int productId, String productName, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_ID, orderId);
        values.put(COLUMN_PRODUCT_ID, productId);
        values.put(COLUMN_PRODUCT_NAME, productName);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_PRICE, price);
        return db.insert(TABLE_ORDER_ITEMS, null, values);
    }

    // Sửa lại phương thức getOrderItems để sử dụng COLUMN_ORDER_ID thay vì id
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_ORDER_ITEMS 
                + " WHERE " + COLUMN_ORDER_ID + " = ?";

        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(orderId)});

        if (cursor.moveToFirst()) {
            do {
                OrderItem item = new OrderItem();
                item.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ITEM_ID)));
                item.setOrderId(cursor.getInt(cursor.getColumnIndex(COLUMN_ORDER_ID)));
                item.setProductId(cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID)));
                item.setProductName(cursor.getString(cursor.getColumnIndex(COLUMN_PRODUCT_NAME)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY)));
                item.setPrice(cursor.getDouble(cursor.getColumnIndex(COLUMN_PRICE)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }
} 