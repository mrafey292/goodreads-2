package com.example.goodreads2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etName;
    private TextView textView;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Account created.", Toast.LENGTH_SHORT).show();

                                    String username = etUsername.getText().toString().trim();
                                    String name = etName.getText().toString().trim();

                                    // Prepare user data
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username);
                                    user.put("name", name);
                                    user.put("email", email);
                                    user.put("createdAt", FieldValue.serverTimestamp());

                                    // Prepare default lists
                                    String userId = mAuth.getCurrentUser().getUid();
                                    createDefaultLists(userId);

                                    // Save user data to Firestore
                                    db.collection("user").document(userId)
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                    // Redirect to GenreSelectionActivity
                                                    Intent intent = new Intent(getApplicationContext(), GenreSelectionActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(RegisterActivity.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void createDefaultLists(String userId) {
        // Define default lists
        String[] listNames = {"Read", "Want to Read", "Currently Reading"};
        for (String listName : listNames) {
            Map<String, Object> list = new HashMap<>();
            list.put("name", listName);
            list.put("publicStatus", false);
            list.put("createdAt", FieldValue.serverTimestamp());
            list.put("userID", userId);

            // Add list to Firestore
            db.collection("lists")
                    .add(list)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(RegisterActivity.this, "List \"" + listName + "\" created.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterActivity.this, "Failed to create list \"" + listName + "\".", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
