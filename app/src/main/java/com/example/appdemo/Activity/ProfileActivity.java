package com.example.appdemo.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdemo.R;

public class ProfileActivity extends AppCompatActivity {
    private EditText fullNameEdt, emailEdt, phoneEdt, addressEdt;
    private ImageView profileImageView;
    private Button saveProfileBtn, logoutBtn, changePhotoBtn;
    private LinearLayout changePasswordBtn, orderHistoryBtn, paymentMethodsBtn;

    private ActivityResultLauncher<String> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
        bottom_navigation();
        setupImagePicker();

        // Load profile data from preferences or database
        loadProfileData();
    }

    private void initViews() {
        // EditText fields
        fullNameEdt = findViewById(R.id.fullNameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        phoneEdt = findViewById(R.id.phoneEdt);
        addressEdt = findViewById(R.id.addressEdt);

        // Profile image
        profileImageView = findViewById(R.id.profileImageView);

        // Buttons
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        changePhotoBtn = findViewById(R.id.changePhotoBtn);

        // Settings sections
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        orderHistoryBtn = findViewById(R.id.orderHistoryBtn);
        paymentMethodsBtn = findViewById(R.id.paymentMethodsBtn);
    }

    private void setupListeners() {
        saveProfileBtn.setOnClickListener(view -> saveProfile());

        logoutBtn.setOnClickListener(view -> {
            // Clear session/preferences
            // Redirect to login screen
            Toast.makeText(ProfileActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        changePhotoBtn.setOnClickListener(view -> {
            // Launch image picker
            mGetContent.launch("image/*");
        });

        changePasswordBtn.setOnClickListener(view -> {
            // Navigate to change password screen
            Toast.makeText(ProfileActivity.this, "Change Password clicked", Toast.LENGTH_SHORT).show();
            // Implement navigation to ChangePasswordActivity
        });

        orderHistoryBtn.setOnClickListener(view -> {
            // Navigate to order history screen
            Toast.makeText(ProfileActivity.this, "Order History clicked", Toast.LENGTH_SHORT).show();
            // Implement navigation to OrderHistoryActivity
        });

        paymentMethodsBtn.setOnClickListener(view -> {
            // Navigate to payment methods screen
            Toast.makeText(ProfileActivity.this, "Payment Methods clicked", Toast.LENGTH_SHORT).show();
            // Implement navigation to PaymentMethodsActivity
        });
    }

    private void setupImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImageView.setImageURI(uri);
                        // Save image URI to preferences or database
                        saveImageUri(uri);
                    }
                });
    }

    private void saveImageUri(Uri uri) {
        // Save image URI to preferences or database
        // This is a placeholder - implement based on your data storage approach
        String imageUriString = uri.toString();
        // Example using SharedPreferences:
        // SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        // SharedPreferences.Editor editor = prefs.edit();
        // editor.putString("profile_image_uri", imageUriString);
        // editor.apply();
    }

    private void loadProfileData() {
        // Load profile data from preferences or database
        // This is a placeholder - implement based on your data storage approach

        // Example using dummy data
        fullNameEdt.setText("John Doe");
        emailEdt.setText("john.doe@example.com");
        phoneEdt.setText("+1 (555) 123-4567");
        addressEdt.setText("123 Main Street\nAnytown, CA 12345");

        // Example of loading image URI from SharedPreferences:
        // SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        // String imageUriString = prefs.getString("profile_image_uri", null);
        // if (imageUriString != null) {
        //     Uri imageUri = Uri.parse(imageUriString);
        //     profileImageView.setImageURI(imageUri);
        // }
    }

    private void saveProfile() {
        // Validate inputs
        String fullName = fullNameEdt.getText().toString().trim();
        String email = emailEdt.getText().toString().trim();
        String phone = phoneEdt.getText().toString().trim();
        String address = addressEdt.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Full Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to preferences or database
        // This is a placeholder - implement based on your data storage approach

        // Show success message
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void bottom_navigation() {
        LinearLayout homeBtn = findViewById(R.id.homeBtn);
        LinearLayout cartBtn = findViewById(R.id.cartBtn);
        LinearLayout profileBtn = findViewById(R.id.profileBtn);

        homeBtn.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, MainActivity.class)));
        cartBtn.setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, CartActivity.class)));
        profileBtn.setOnClickListener(view -> {
            // Already on profile page, do nothing or refresh
        });
    }
}