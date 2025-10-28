package com.av.avmessenger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    Uri imageURI;
    final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().hide();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI elements
        loginbut = findViewById(R.id.loginbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);

        // Open login screen
        loginbut.setOnClickListener(v -> {
            startActivity(new Intent(registration.this, login.class));
            finish();
        });

        // Profile image selection
        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // Handle sign up button
        rg_signup.setOnClickListener(v -> {
            String username = rg_username.getText().toString().trim();
            String email = rg_email.getText().toString().trim();
            String password = rg_password.getText().toString().trim();
            String confirmPassword = rg_repassword.getText().toString().trim();
            String status = "Hey I'm using this application";

            // Validation
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.matches(emailPattern)) {
                rg_email.setError("Enter a valid email");
                return;
            }
            if (password.length() < 6) {
                rg_password.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(confirmPassword)) {
                rg_repassword.setError("Passwords do not match");
                return;
            }

            progressDialog.show();

            // Check if user already exists
            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    boolean userExists = !task.getResult().getSignInMethods().isEmpty();
                    if (userExists) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show();
                    } else {
                        createUser(email, password, username, status);
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error checking email", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void createUser(String email, String password, String username, String status) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser == null) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "User creation failed", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = firebaseUser.getUid();
                DatabaseReference reference = database.getReference().child("user").child(userId);
                StorageReference storageReference = storage.getReference().child("Upload").child(userId);

                // If image is selected
                if (imageURI != null) {
                    storageReference.putFile(imageURI).addOnSuccessListener(task1 -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            Users user = new Users(userId, username, email, password, profileImageUrl, status);
                            saveUserToDatabase(reference, user);
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to retrieve image URL", Toast.LENGTH_SHORT).show();
                        });
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Use default profile image
                    String defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/av-messenger-dc8f3.appspot.com/o/man.png?alt=media&token=880f431d-9344-45e7-afe4-c2cafe8a5257";
                    Users user = new Users(userId, username, email, password, defaultImageUrl, status);
                    saveUserToDatabase(reference, user);
                }

            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToDatabase(DatabaseReference reference, Users user) {
        reference.setValue(user).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(registration.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Handle image chooser result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
