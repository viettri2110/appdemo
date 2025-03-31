package com.example.appdemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.example.appdemo.Model.Order;
import com.example.appdemo.Model.OrderItem;
import com.example.appdemo.Model.Message;
import com.example.appdemo.Model.ChatPreview;
import com.example.appdemo.Model.Banner;
import com.example.appdemo.Model.Product;
import com.example.appdemo.database.ProductDatabaseHelper;
import com.example.appdemo.Model.Review;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppDB";
    private static final int DATABASE_VERSION = 7;

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

    // Thêm các hằng số cho bảng messages
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_SENDER_EMAIL = "sender_email";
    private static final String COLUMN_MESSAGE_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IS_FROM_ADMIN = "is_from_admin";

    // Thêm bảng banners
    private static final String TABLE_BANNERS = "banners";
    private static final String COLUMN_BANNER_ID = "id";
    private static final String COLUMN_BANNER_IMAGE = "image_url";
    private static final String COLUMN_BANNER_TEXT = "text";

    // Thêm bảng favorites
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_USER_EMAIL = "user_email";

    // Thêm bảng reviews
    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_REVIEW_ID = "review_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_DATE = "date";

    // Thêm tham chiếu đến bảng products
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "id";

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

        // Tạo bảng messages
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SENDER_EMAIL + " TEXT,"
                + COLUMN_MESSAGE_CONTENT + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_IS_FROM_ADMIN + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_SENDER_EMAIL + ") REFERENCES " 
                + TABLE_USERS + "(" + COLUMN_EMAIL + ")"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        // Tạo bảng banners
        String CREATE_BANNERS_TABLE = "CREATE TABLE " + TABLE_BANNERS + "("
                + COLUMN_BANNER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BANNER_IMAGE + " TEXT,"
                + COLUMN_BANNER_TEXT + " TEXT"
                + ")";
        db.execSQL(CREATE_BANNERS_TABLE);

        // Tạo bảng favorites
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_PRODUCT_ID + " INTEGER,"
                + "PRIMARY KEY (" + COLUMN_USER_EMAIL + ", " + COLUMN_PRODUCT_ID + ")"
                + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        ProductDatabaseHelper productDb = new ProductDatabaseHelper(context);
        
        // Tạo bảng reviews
        String CREATE_REVIEWS_TABLE = "CREATE TABLE " + TABLE_REVIEWS + "("
                + COLUMN_REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PRODUCT_ID + " INTEGER,"
                + COLUMN_USER_EMAIL + " TEXT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_RATING + " FLOAT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES " + productDb.getTableName() + "(" + ProductDatabaseHelper.getColumnId() + "),"
                + "FOREIGN KEY(" + COLUMN_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ")"
                + ")";
        db.execSQL(CREATE_REVIEWS_TABLE);

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
        // Backup dữ liệu users hiện tại trước khi drop table
        List<ContentValues> userBackup = new ArrayList<>();
        if (oldVersion < 5) {
            Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_EMAIL, cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    values.put(COLUMN_PASSWORD, cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                    values.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    values.put(COLUMN_IS_ADMIN, cursor.getInt(cursor.getColumnIndex(COLUMN_IS_ADMIN)));
                    userBackup.add(values);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Drop các bảng cũ
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BANNERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);

        // Tạo lại các bảng
        onCreate(db);

        // Khôi phục lại dữ liệu users
        if (oldVersion < 5) {
            for (ContentValues values : userBackup) {
                db.insert(TABLE_USERS, null, values);
            }
        }
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

    // Thêm phương thức để lưu tin nhắn
    public long saveMessage(String senderEmail, String content, boolean isFromAdmin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER_EMAIL, senderEmail);
        values.put(COLUMN_MESSAGE_CONTENT, content);
        values.put(COLUMN_IS_FROM_ADMIN, isFromAdmin ? 1 : 0);
        return db.insert(TABLE_MESSAGES, null, values);
    }

    // Lấy tin nhắn của một user
    public List<Message> getMessagesForUser(String userEmail) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES 
                + " WHERE " + COLUMN_SENDER_EMAIL + " = ? OR " 
                + COLUMN_IS_FROM_ADMIN + " = 1"
                + " ORDER BY " + COLUMN_TIMESTAMP + " ASC";
                
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_CONTENT));
                boolean isFromAdmin = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FROM_ADMIN)) == 1;
                messages.add(new Message(content, isFromAdmin));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    // Lấy tất cả tin nhắn cho admin
    public List<ChatPreview> getAllChatsForAdmin() {
        List<ChatPreview> chats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT DISTINCT " + COLUMN_SENDER_EMAIL + ", "
                + "(SELECT " + COLUMN_MESSAGE_CONTENT 
                + " FROM " + TABLE_MESSAGES + " m2"
                + " WHERE m2." + COLUMN_SENDER_EMAIL + " = m1." + COLUMN_SENDER_EMAIL
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1) as last_message,"
                + "(SELECT " + COLUMN_TIMESTAMP
                + " FROM " + TABLE_MESSAGES + " m2"
                + " WHERE m2." + COLUMN_SENDER_EMAIL + " = m1." + COLUMN_SENDER_EMAIL
                + " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1) as last_time"
                + " FROM " + TABLE_MESSAGES + " m1"
                + " WHERE " + COLUMN_SENDER_EMAIL + " != 'admin'"
                + " ORDER BY last_time DESC";
                
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(0);
                String lastMessage = cursor.getString(1);
                String timestamp = cursor.getString(2);
                chats.add(new ChatPreview(email, lastMessage, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chats;
    }

    // Thêm phương thức quản lý banner
    public long addBanner(String imageUrl, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BANNER_IMAGE, imageUrl);
        values.put(COLUMN_BANNER_TEXT, text);
        return db.insert(TABLE_BANNERS, null, values);
    }

    public List<Banner> getAllBanners() {
        List<Banner> banners = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BANNERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Banner banner = new Banner(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_BANNER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_BANNER_IMAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_BANNER_TEXT))
                );
                banners.add(banner);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return banners;
    }

    public void deleteBanner(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BANNERS, COLUMN_BANNER_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // Thêm các phương thức quản lý favorites
    public void addToFavorites(String userEmail, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Kiểm tra xem sản phẩm có tồn tại trong bảng products không
        ProductDatabaseHelper productDb = new ProductDatabaseHelper(context);
        Product product = productDb.getProduct(productId);
        
        if (product != null) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_EMAIL, userEmail);
            values.put(COLUMN_PRODUCT_ID, productId);
            
            long result = db.insert(TABLE_FAVORITES, null, values);
            Log.d("DatabaseHelper", "Added to favorites - Email: " + userEmail + 
                  ", ProductID: " + productId + ", Result: " + result);
        } else {
            Log.e("DatabaseHelper", "Cannot add to favorites - Product not found: " + productId);
        }
    }

    public void removeFromFavorites(String userEmail, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FAVORITES, 
            COLUMN_USER_EMAIL + "=? AND " + COLUMN_PRODUCT_ID + "=?",
            new String[]{userEmail, String.valueOf(productId)});
        Log.d("DatabaseHelper", "Removing from favorites - Email: " + userEmail + ", ProductID: " + productId + ", Result: " + result);
    }

    public boolean isFavorite(String userEmail, int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES,
            null,
            COLUMN_USER_EMAIL + "=? AND " + COLUMN_PRODUCT_ID + "=?",
            new String[]{userEmail, String.valueOf(productId)},
            null, null, null);
        boolean isFavorite = cursor.getCount() > 0;
        cursor.close();
        return isFavorite;
    }

    public List<Product> getFavoriteProducts(String userEmail) {
        List<Product> favorites = new ArrayList<>();
        
        // Lấy danh sách product_id từ bảng favorites
        SQLiteDatabase db = this.getReadableDatabase();
        String favQuery = "SELECT " + COLUMN_PRODUCT_ID + " FROM " + TABLE_FAVORITES + 
                         " WHERE " + COLUMN_USER_EMAIL + " = ?";
        
        Cursor favCursor = db.rawQuery(favQuery, new String[]{userEmail});
        Log.d("DatabaseHelper", "Found " + favCursor.getCount() + " favorites for user: " + userEmail);
        
        // Nếu có sản phẩm yêu thích
        if (favCursor.moveToFirst()) {
            // Tạo ProductDatabaseHelper để truy cập bảng products
            ProductDatabaseHelper productDb = new ProductDatabaseHelper(context);
            
            do {
                int productId = favCursor.getInt(favCursor.getColumnIndex(COLUMN_PRODUCT_ID));
                Log.d("DatabaseHelper", "Getting product with ID: " + productId);
                
                // Lấy thông tin sản phẩm từ ProductDatabaseHelper
                Product product = productDb.getProduct(productId);
                if (product != null) {
                    favorites.add(product);
                    Log.d("DatabaseHelper", "Added product to favorites: " + product.getName());
                } else {
                    Log.e("DatabaseHelper", "Product not found with ID: " + productId);
                }
            } while (favCursor.moveToNext());
        }
        
        favCursor.close();
        Log.d("DatabaseHelper", "Returning " + favorites.size() + " favorite products");
        return favorites;
    }

    // Thêm phương thức để kiểm tra bảng favorites
    public void checkFavoritesTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES;
        Cursor cursor = db.rawQuery(query, null);
        Log.d("DatabaseHelper", "Total favorites in table: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL));
                int productId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID));
                Log.d("DatabaseHelper", "Favorite found - Email: " + email + ", ProductID: " + productId);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        
        db.update(TABLE_USERS, values, 
            COLUMN_EMAIL + " = ?", 
            new String[]{email});
    }

    public boolean checkUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
            new String[]{COLUMN_EMAIL},
            COLUMN_EMAIL + "=?",
            new String[]{email},
            null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Thêm các phương thức để thao tác với reviews
    public long addReview(Review review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_PRODUCT_ID, review.getProductId());
        values.put(COLUMN_USER_EMAIL, review.getUserEmail());
        values.put(COLUMN_USER_NAME, review.getUserName());
        values.put(COLUMN_RATING, review.getRating());
        values.put(COLUMN_CONTENT, review.getContent());
        values.put(COLUMN_DATE, review.getDate());

        // Thêm log để debug
        Log.d("DatabaseHelper", "Adding review: " + 
              "ProductID=" + review.getProductId() + 
              ", UserEmail=" + review.getUserEmail() + 
              ", Rating=" + review.getRating());

        long result = db.insert(TABLE_REVIEWS, null, values);
        Log.d("DatabaseHelper", "Add review result: " + result);
        return result;
    }

    public List<Review> getProductReviews(int productId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Thêm log để debug
        Log.d("DatabaseHelper", "Getting reviews for product: " + productId);

        String query = "SELECT * FROM " + TABLE_REVIEWS + 
                      " WHERE " + COLUMN_PRODUCT_ID + " = ? " +
                      " ORDER BY " + COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
        Log.d("DatabaseHelper", "Found " + cursor.getCount() + " reviews");

        if (cursor.moveToFirst()) {
            do {
                Review review = new Review(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_REVIEW_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                    cursor.getFloat(cursor.getColumnIndex(COLUMN_RATING)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                );
                reviews.add(review);
                Log.d("DatabaseHelper", "Loaded review: " + review.getContent());
            } while (cursor.moveToNext());
        }
        cursor.close();

        return reviews;
    }

    public List<Review> getUserReviews(String userEmail) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Log.d("DatabaseHelper", "Getting reviews for user: " + userEmail);

        String query = "SELECT r.*, p.name as product_name FROM " + TABLE_REVIEWS + " r " +
                      "INNER JOIN " + TABLE_PRODUCTS + " p ON r." + COLUMN_PRODUCT_ID + " = p." + COLUMN_ID +
                      " WHERE r." + COLUMN_USER_EMAIL + " = ? " +
                      "ORDER BY r." + COLUMN_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{userEmail});
        Log.d("DatabaseHelper", "Found " + cursor.getCount() + " reviews");

        if (cursor.moveToFirst()) {
            do {
                Review review = new Review(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_REVIEW_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_PRODUCT_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)),
                    cursor.getFloat(cursor.getColumnIndex(COLUMN_RATING)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_DATE))
                );
                // Thêm tên sản phẩm vào review
                review.setProductName(cursor.getString(cursor.getColumnIndex("product_name")));
                reviews.add(review);
                Log.d("DatabaseHelper", "Loaded review for product: " + review.getProductName());
            } while (cursor.moveToNext());
        }
        cursor.close();

        return reviews;
    }
} 