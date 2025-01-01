package com.example.goodreads2;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookDetailsFragment extends Fragment {

    private Book book;
    private String bookId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button wantToReadButton, readButton, currentlyReadingButton, addReviewButton, submitRatingButton, clearButton;
    private RatingBar ratingBar;
    private List<Review> reviewList = new ArrayList<>();
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;


    public BookDetailsFragment() {
        // Required empty public constructor
    }

    public BookDetailsFragment(Book book) {
        this.book = book;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);

        // Find views by their IDs
        Log.d("BookDetailsFragment", "Book ID: " + book.getBookID());
        ImageView coverImageView = view.findViewById(R.id.image_view_cover);
        TextView titleTextView = view.findViewById(R.id.text_view_title);
        TextView authorsTextView = view.findViewById(R.id.text_view_authors);
        TextView descriptionTextView = view.findViewById(R.id.text_view_description);
        TextView isbnTextView = view.findViewById(R.id.text_view_isbn);
        wantToReadButton = view.findViewById(R.id.btn_set_as_want_to_read);
        readButton = view.findViewById(R.id.btn_set_as_read);
        currentlyReadingButton = view.findViewById(R.id.btn_set_as_currently_reading);
        addReviewButton = view.findViewById(R.id.btn_add_review);
        ratingBar = view.findViewById(R.id.rating_bar);
        submitRatingButton = view.findViewById(R.id.btn_submit_rating);
        clearButton = view.findViewById(R.id.btn_clear);

        ratingBar.setVisibility(View.GONE);
        submitRatingButton.setVisibility(View.GONE);
        addReviewButton.setVisibility(View.GONE);

        recyclerViewReviews = view.findViewById(R.id.recycler_view_reviews);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter(reviewList, getContext());
        recyclerViewReviews.setAdapter(reviewAdapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set values to views
        if (book != null) {
            // Load cover image
            if (book.getCoverURL() != null) {
                Log.e("Error", book.getCoverURL());
                Glide.with(this)
                        .load(book.getCoverURL())
                        .override(150, 200)
                        .into(coverImageView);
            }

            titleTextView.setText(book.getTitle());
            authorsTextView.setText(book.getAuthors());
            descriptionTextView.setText(book.getDescription());
            isbnTextView.setText(book.getIsbn10());

            if (book.getBookID() == null){
                addBookToBooks(book);
            }

//            checkExistingBook(book, exists -> {
//                if (!exists) {
//
//                }
//            });
//            addBookToBooks(book);


            checkBookInList("want_to_read", book.getBookID(), existsInList -> {
                if (existsInList) {
                    Log.d("Want to Read", "TRUE for Want to Read "+book.getBookID());
                    wantToReadButton.setEnabled(false);
                    wantToReadButton.setText("Want to Read ✔");
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                }
                else {
                    Log.d("Want to Read", "FALSE for Want to Read "+book.getBookID());
                }
            });

            checkBookInList("currently_reading", book.getBookID(), existsInList -> {
                if (existsInList) {
                    Log.d("Currently Reading", "TRUE for Currently Reading "+book.getBookID());
                    currentlyReadingButton.setEnabled(false);
                    wantToReadButton.setEnabled(false);
                    currentlyReadingButton.setText("Currently Reading ✔");
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                    currentlyReadingButton.setBackgroundColor(getResources().getColor(R.color.background));
                }
                else {
                    Log.d("Currently Reading", "FALSE for Currently Reading "+book.getBookID());
                }
            });

            checkBookInList("already_read", book.getBookID(), existsInList -> {
                if (existsInList) {
                    Log.d("Read", "TRUE for Read "+book.getBookID());
                    readButton.setEnabled(false);
                    wantToReadButton.setEnabled(false);
                    currentlyReadingButton.setEnabled(false);
                    readButton.setText("Read ✔");
                    readButton.setBackgroundColor(getResources().getColor(R.color.background));
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                    currentlyReadingButton.setBackgroundColor(getResources().getColor(R.color.background));

                    ratingBar.setVisibility(View.VISIBLE);
                    addReviewButton.setVisibility(View.VISIBLE);
                    submitRatingButton.setVisibility(View.VISIBLE);
//                    //SETTING THE RATING BAR IF ALREADY RATED BY USER
                    checkAndSetUserRating(book.getBookID()); // Call the new method here
                }
                else {
                    Log.d("Read", "FALSE for Read "+book.getBookID());
                }
            });

            wantToReadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBookToBooks(book);
                    addBookToWantToReadList(book.getBookID(),book.getTitle());
                    removeFromRecommendations();
                    updateWantToReadButton(true, false);
                    wantToReadButton.setEnabled(false);
                    wantToReadButton.setText("Want to Read ✔");
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                }
            });

            readButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRatingDialog(book);
                }
            });

            submitRatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //SETTING THE RATING BAR IF ALREADY RATED BY USER
                    saveRating(book.getBookID(), mAuth.getUid(), ratingBar.getRating());
                    checkAndSetUserRating(bookId);

                    submitRatingButton.setEnabled(false);
                    refreshFragment();
                }
            });

            currentlyReadingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBookToBooks(book);
                    addbookToCurrentlyReadingList(book.getBookID(), book.getTitle());
                    currentlyReadingButton.setEnabled(false);
                    wantToReadButton.setEnabled(false);
                    currentlyReadingButton.setText("Currently Reading ✔");
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                    currentlyReadingButton.setBackgroundColor(getResources().getColor(R.color.background));
                    removeBookFromList("want_to_read", book.getBookID());
                    removeFromRecommendations();
                }
            });

            addReviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open a dialog or activity for adding a review
                    openReviewDialog();
                }
            });

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeBookFromList("already_read", book.getBookID());
                    removeBookFromList("want_to_read", book.getBookID());
                    removeBookFromList("currently_reading", book.getBookID());

                    removeFromRecommendations();

                    ratingBar.setVisibility(View.GONE);
                    addReviewButton.setVisibility(View.GONE);
                    submitRatingButton.setVisibility(View.GONE);

                    wantToReadButton.setEnabled(true);
                    readButton.setEnabled(true);
                    currentlyReadingButton.setEnabled(true);

                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.primary));
                    currentlyReadingButton.setBackgroundColor(getResources().getColor(R.color.primary));
                    readButton.setBackgroundColor(getResources().getColor(R.color.primary));

                    wantToReadButton.setText("Want to Read");
                    readButton.setText("Read");
                    currentlyReadingButton.setText("Currently Reading");
                }
            });
            fetchReviews();
        }
        return view;
    }

    private void showRatingDialog(Book book) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.rating_dialog, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // Use requireContext() or getActivity()
        builder.setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Handle "Add" button click
                    float rating = ratingBar.getRating();
                    addToAlreadyReadBooks(book,rating);
                    saveRating(book.getBookID(), mAuth.getUid(), rating);
                    addBookToBooks(book);
                    readButton.setEnabled(false);
                    wantToReadButton.setEnabled(false);
                    currentlyReadingButton.setEnabled(false);
                    readButton.setText("Read ✔");
                    readButton.setBackgroundColor(getResources().getColor(R.color.background));
                    wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
                    currentlyReadingButton.setBackgroundColor(getResources().getColor(R.color.background));
                    removeBookFromList("want_to_read", book.getBookID());
                    removeBookFromList("currently_reading", book.getBookID());
                    removeFromRecommendations();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public interface OnCheckBookInListListener {
        void onCheckBookInList(boolean exists);
    }

    public interface OnCheckExistingBookListener {
        void onCheckExistingBook(boolean exists);
    }


    private void checkExistingBook(Book book, OnCheckExistingBookListener listener) {
        db.collection("books")
                .whereEqualTo("title", book.getTitle())
                .whereEqualTo("author", book.getAuthors())
                .whereEqualTo("coverImageUrl", book.getCoverURL())
                .whereEqualTo("description", book.getDescription())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Check", "The book "+document.getId()+" already in books");
                                bookId = document.getId();
                                book.setBookID(bookId);
//                                book.setCoverURL();
                            }
                            listener.onCheckExistingBook(true);
                        } else {
                            listener.onCheckExistingBook(false);
                        }
                    } else {
                        listener.onCheckExistingBook(false);
                        Log.d("BookDetailsFragment", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addBookToBooks(Book book) {
        checkExistingBook(book, exists -> {
            if (!exists) {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("title", book.getTitle());
                bookMap.put("author", book.getAuthors());
                bookMap.put("coverImageUrl", book.getCoverURL());
                bookMap.put("description", book.getDescription());
                bookMap.put("genre", book.getCategories());
                bookMap.put("isbn10", book.getIsbn10());
                bookMap.put("isbn13", book.getIsbn13());

                db.collection("books")
                        .add(bookMap)
                        .addOnSuccessListener(documentReference -> {
                            bookId = documentReference.getId();
                            book.setBookID(bookId);
                        })
                        .addOnFailureListener(e -> Log.w("BookDetailsFragment", "Error adding document", e));
            }
        });
    }

    private void addbookToCurrentlyReadingList(String bookId,String bookName){
        String userId=mAuth.getUid();
        if (userId==null){
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e("BookDetailsFragment", "User ID is null. User not authenticated.");
            return;
        }
        CollectionReference currentlyReadingCollection=db.collection("lists").document(userId).collection("currently_reading");
        Map<String, Object> book=new HashMap<>();
        book.put("bookId",bookId);
        book.put("bookName",bookName);
        currentlyReadingCollection.add(book)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BookDetailsFragment", "Book added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Book added to 'Currently Reading' list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetailsFragment", "Error adding book: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to add book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            Log.d("BookDetailsFragment", "Book added to 'Currently Reading' list");
        });
    }

    private void addBookToWantToReadList(String bookId, String bookName) {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e("BookDetailsFragment", "User ID is null. User not authenticated.");
            return;
        }

        // Reference to the user's "want_to_read" collection
        CollectionReference wantToReadCollection = db.collection("lists").document(userId).collection("want_to_read");
        Log.d("BookDetailsFragment", "Collection Reference: " + wantToReadCollection.getPath());

        // Create a book object
        Map<String, Object> book = new HashMap<>();
        book.put("bookId", bookId);
        book.put("bookName", bookName);

        Log.d("BookDetailsFragment", "Book ID: " + bookId);

        wantToReadCollection.add(book)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BookDetailsFragment", "Book added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Book added to 'Want to Read' list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetailsFragment", "Error adding book: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to add book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                        Log.d("BookDetailsFragment", "Book added to 'Want to Read' list");

                });
    }

        private void addToAlreadyReadBooks(Book book,float rating) {
        String userId = mAuth.getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e("BookDetailsFragment", "User ID is null. User not authenticated.");
            return;
        }

        // Reference to the user's "already_read" collection
        CollectionReference alreadyReadCollection = db.collection("lists").document(userId).collection("already_read");
        Log.d("BookDetailsFragment", "Collection Reference: " + alreadyReadCollection.getPath());

        // Create a book object
        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("bookId", book.getBookID());
        bookMap.put("bookName", book.getTitle());
        bookMap.put("rating", rating);
        bookMap.put("timestamp", FieldValue.serverTimestamp());

        Log.d("BookDetailsFragment", "Book ID: " + book.getBookID());
        alreadyReadCollection.add(bookMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BookDetailsFragment", "Book added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Book added to 'Already Read' list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("BookDetailsFragment", "Error adding book: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Failed to add book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            Log.d("BookDetailsFragment", "Book added to 'Already Read' list");

        });
    }





    private void updateWantToReadButton(boolean inWantToReadList, boolean inReadList) {
        if (inReadList) {
            wantToReadButton.setEnabled(false);
            wantToReadButton.setText("Already Read");
            wantToReadButton.setBackgroundColor(getResources().getColor(R.color.purple_200));
            removeBookFromList("want_to_read", bookId);
        } else if (inWantToReadList) {
            wantToReadButton.setEnabled(false);
            wantToReadButton.setText("Want to Read ✔");
            wantToReadButton.setBackgroundColor(getResources().getColor(R.color.background));
        } else {
            wantToReadButton.setEnabled(true);
            wantToReadButton.setText("Want to Read");
            wantToReadButton.setBackgroundColor(getResources().getColor(R.color.purple_500));
        }
    }

    private void removeBookFromList(String listName, String bookId) {
        db.collection("lists")
                .document(mAuth.getUid())
                .collection(listName)
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("BookDetailsFragment", "Book removed from " + listName))
                                    .addOnFailureListener(e -> Log.w("BookDetailsFragment", "Error deleting document", e));
                        }
                    } else {
                        Log.d("BookDetailsFragment", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void checkBookInList(String listName, String bookId, OnCheckBookInListListener listener) {
        Log.d("BookDetailsFragment", "Checking if the book already exists in book list:" + listName);

        db.collection("lists")
                .document(mAuth.getUid())
                .collection(listName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String docBookId = document.getString("bookId");
                            if (docBookId != null && docBookId.equals(bookId)) {
                                listener.onCheckBookInList(true);
                                Log.d("BookDetailsFragment", "True for " + bookId + " in " + listName);
                                return;
                            }
                        }
                        listener.onCheckBookInList(false);
                        Log.d("BookDetailsFragment", "False for " + bookId);
                    } else {
                        listener.onCheckBookInList(false);
                        Log.d("BookDetailsFragment", "False for " + bookId);
                    }
                });
    }
    private void saveRating(String bookId, String userId, float ratingValue) {
        // Check if a rating with the same userID and bookID already exists
        db.collection("ratings")
                .whereEqualTo("userID", userId)
                .whereEqualTo("bookID", bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // If a rating already exists, update its value
                            String ratingId = document.getId();
                            db.collection("ratings")
                                    .document(ratingId)
                                    .update("rating", ratingValue)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("BookDetailsFragment", "Rating updated successfully");
                                        Toast.makeText(getContext(), "Rating updated successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("BookDetailsFragment", "Error updating rating", e);
                                        Toast.makeText(getContext(), "Failed to update rating", Toast.LENGTH_SHORT).show();
                                    });
                            return; // Exit the loop after updating the first matching rating
                        }
                        // If no matching rating found, create a new one
                        createNewRating(bookId, userId, ratingValue);
                    } else {
                        Log.w("BookDetailsFragment", "Error getting ratings", task.getException());
                        Toast.makeText(getContext(), "Failed to get ratings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewRating(String bookId, String userId, float ratingValue) {
        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("bookID", bookId);
        ratingMap.put("userID", userId);
        ratingMap.put("timestamp", FieldValue.serverTimestamp());
        ratingMap.put("rating", ratingValue);

        db.collection("ratings")
                .add(ratingMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BookDetailsFragment", "New rating added successfully");
                    Toast.makeText(getContext(), "New rating added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("BookDetailsFragment", "Error adding new rating", e);
                    Toast.makeText(getContext(), "Failed to add new rating", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkAndSetUserRating(String bookId) {
        db.collection("ratings")
                .whereEqualTo("userID", mAuth.getUid())
                .whereEqualTo("bookID", bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot ratingDoc : task.getResult()) {
                            float rating = ratingDoc.getDouble("rating").floatValue();
                            ratingBar.setRating(rating);
                            ratingBar.setVisibility(View.VISIBLE);
                            Log.d("Rating", "Rating set: " + rating);
//                            refreshFragment();

                        }
                    } else {
                        Log.w("Rating", "Error getting rating: ", task.getException());
                    }
                });
    }


    // Method to open a dialog for adding a review
    private void openReviewDialog() {
        ReviewDialogFragment dialogFragment = new ReviewDialogFragment();
        dialogFragment.setOnReviewSubmittedListener(new ReviewDialogFragment.OnReviewSubmittedListener() {
            @Override
            public void onReviewSubmitted(String reviewText) {
                // Save the review to the database
                saveReview(reviewText);
            }
        });
        dialogFragment.show(getFragmentManager(), "ReviewDialogFragment");
    }

    // Method to save the review to the database
    private void saveReview(String reviewText) {
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("bookID", book.getBookID());
        reviewMap.put("userID", mAuth.getUid());
        reviewMap.put("timestamp", FieldValue.serverTimestamp());
        reviewMap.put("review", reviewText);

        db.collection("reviews")
                .add(reviewMap)
                .addOnSuccessListener(documentReference -> {
                    Log.d("BookDetailsFragment", "Review added successfully");
                    Toast.makeText(getContext(), "Review added successfully", Toast.LENGTH_SHORT).show();
                    refreshFragment();

                })
                .addOnFailureListener(e -> {
                    Log.w("BookDetailsFragment", "Error adding review", e);
                    Toast.makeText(getContext(), "Failed to add review", Toast.LENGTH_SHORT).show();
                });
    }
    private void removeFromRecommendations(){
        db.collection("recommendations")
                .whereEqualTo("userID", mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference listRef = document.getReference();
                                listRef.update("bookIDs", FieldValue.arrayRemove(book.getBookID()))
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("RemoveFromRecommendations", "Book removed from recommendations");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w("RemoveFromRecommendations", "Error updating document", e);
                                        });
                            }
                        }
                    } else {
                        Log.d("RemoveFromRecommendations", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void fetchReviews() {
        db.collection("reviews")
                .whereEqualTo("bookID", book.getBookID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = new Review();
                            String userID = document.getString("userID");
                            review.setBookId(book.getBookID());
                            review.setUserId(userID);
                            review.setReviewText(document.getString("review"));
                            review.setTimestamp(document.getTimestamp("timestamp"));

                            // Fetch user information
                            db.collection("user").document(userID).get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {
                                            DocumentSnapshot userDocument = userTask.getResult();
                                            if (userDocument.exists()) {
                                                review.setUserName(userDocument.getString("username"));
                                                review.setUserProfilePicture(userDocument.getString("profilePic"));
                                            }

                                            // Fetch rating information
                                            db.collection("ratings")
                                                    .whereEqualTo("bookID", book.getBookID())
                                                    .whereEqualTo("userID", userID)
                                                    .get()
                                                    .addOnCompleteListener(ratingTask -> {
                                                        if (ratingTask.isSuccessful()) {
                                                            for (QueryDocumentSnapshot ratingDoc : ratingTask.getResult()) {
                                                                review.setRating(ratingDoc.getDouble("rating").floatValue());
                                                            }
                                                            reviewList.add(review);
                                                            reviewAdapter.notifyDataSetChanged();
                                                            Log.d("ReviewDetails", "User: " + review.getUserName() + ", Rating: " + review.getRating());
                                                        } else {
                                                            Log.w("BookDetailsFragment", "Error getting ratings.", ratingTask.getException());
                                                        }
                                                    });
                                        } else {
                                            Log.w("BookDetailsFragment", "Error getting user details.", userTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w("BookDetailsFragment", "Error getting reviews.", task.getException());
                    }
                });
    }

    private void refreshFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new BookDetailsFragment(book));
        transaction.addToBackStack(null);
        transaction.commit();
    }


}
