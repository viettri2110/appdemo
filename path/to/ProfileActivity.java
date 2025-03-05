public class ProfileActivity extends AppCompatActivity {
    private EditText edtProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        edtProductName = findViewById(R.id.edtProductName);
        // ... existing code ...
    }
    // ... existing code ...
} 