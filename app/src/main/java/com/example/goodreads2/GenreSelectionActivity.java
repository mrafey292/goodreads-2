package com.example.goodreads2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenreSelectionActivity extends AppCompatActivity {

    private LinearLayout genreContainer;
    private ArrayList<String> selectedGenres;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_selection);

        genreContainer = findViewById(R.id.genre_container);
        selectedGenres = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // List of genres
        String[] genres = {"Art", "Biography", "Business", "Chick Lit", "Children's", "Christian", "Classics",
                "Comics", "Contemporary", "Cookbooks", "Crime", "Ebooks", "Fantasy", "Fiction",
                "Graphic Novels", "Historical Fiction", "History", "Horror",
                "Humor and Comedy", "Manga", "Memoir", "Music", "Mystery", "Nonfiction", "Paranormal",
                "Philosophy", "Poetry", "Psychology", "Religion", "Romance", "Science", "Science Fiction",
                "Self Help", "Suspense", "Spirituality", "Sports", "Thriller", "Travel", "Young Adult"};

        // Dynamically add checkboxes for each genre
        for (String genre : genres) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(genre);
            checkBox.setOnClickListener(v -> {
                if (checkBox.isChecked()) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
            });
            genreContainer.addView(checkBox);
        }

        // Submit button
        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (selectedGenres.isEmpty()) {
                Toast.makeText(GenreSelectionActivity.this, "Please select at least one genre.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("preferredGenres", selectedGenres);

            db.collection("user").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(GenreSelectionActivity.this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                        // Redirect to MainActivity
                        startActivity(new Intent(GenreSelectionActivity.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(GenreSelectionActivity.this, "Error saving preferences: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
