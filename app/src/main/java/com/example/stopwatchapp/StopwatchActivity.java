package com.example.stopwatchapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StopwatchActivity extends AppCompatActivity {

    // UI Components
    private TextView timerTextView;
    private Button startButton, pauseButton, resetButton, lapButton, logoutButton, timerButton, clearLapsButton;
    private ListView lapListView;

    // Firebase Components
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private String userId;

    // Timer Components
    private long startTime = 0;
    private long pauseOffset = 0;
    private boolean isRunning = false;
    private final Handler handler = new Handler();
    private final List<Lap> lapList = new ArrayList<>();
    private ArrayAdapter<Lap> lapAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        initializeViews();
        setupButtonListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            userId = currentUser.getUid();
            initializeUserSession();
        }
    }

    private void initializeViews() {
        timerTextView = findViewById(R.id.timerTextView);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);
        lapButton = findViewById(R.id.lapButton);
        logoutButton = findViewById(R.id.logoutButton);
        timerButton = findViewById(R.id.timerButton);
        clearLapsButton = findViewById(R.id.clearLapsButton);
        lapListView = findViewById(R.id.lapListView);

        lapAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lapList);
        lapListView.setAdapter(lapAdapter);
    }

    private void setupButtonListeners() {
        startButton.setOnClickListener(v -> {
            Log.d("Timer", "Start button clicked");
            startTimer();
        });

        pauseButton.setOnClickListener(v -> {
            Log.d("Timer", "Pause button clicked");
            pauseTimer();
        });

        resetButton.setOnClickListener(v -> {
            Log.d("Timer", "Reset button clicked");
            resetTimer();
        });

        lapButton.setOnClickListener(v -> {
            Log.d("Timer", "Lap button clicked");
            recordLap();
        });

        logoutButton.setOnClickListener(v -> logoutUser());

        timerButton.setOnClickListener(v -> {
            startActivity(new Intent(StopwatchActivity.this, TimerActivity.class));
        });

        clearLapsButton.setOnClickListener(v -> clearLaps());
    }

    private void initializeUserSession() {
        databaseRef.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        enableStopwatchFeatures();
                        loadLaps();
                    } else {
                        createUserRecord();
                    }
                });
    }

    private void startTimer() {
        if (!isRunning) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            } else {
                startTime = System.currentTimeMillis() - pauseOffset;
            }
            handler.postDelayed(updateTimer, 10);
            isRunning = true;
            updateButtonStates();
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            pauseOffset = System.currentTimeMillis() - startTime;
            handler.removeCallbacks(updateTimer);
            isRunning = false;
            updateButtonStates();
        }
    }

    private void resetTimer() {
        handler.removeCallbacks(updateTimer);
        timerTextView.setText("00:00:00.00");
        startTime = 0;
        pauseOffset = 0;
        isRunning = false;
        updateButtonStates();
    }

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000) % 60;
            int minutes = (int) ((millis / (1000 * 60)) % 60);
            int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
            int milliseconds = (int) (millis % 1000) / 10 ;

            timerTextView.setText(String.format(Locale.getDefault(),
                    "%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds));

            handler.postDelayed(this, 10);
        }
    };

    private void recordLap() {
        if (isRunning) {
            String currentTime = timerTextView.getText().toString();
            String lapId = databaseRef.child("users").child(userId).child("laps").push().getKey();

            if (lapId != null) {
                Lap lap = new Lap(currentTime, System.currentTimeMillis());
                lap.setId(lapId);

                databaseRef.child("users").child(userId).child("laps").child(lapId)
                        .setValue(lap)
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Failed to save lap", e);
                            Toast.makeText(this, "Failed to save lap", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void loadLaps() {
        databaseRef.child("users").child(userId).child("laps")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        lapList.clear();
                        for (DataSnapshot lapSnapshot : snapshot.getChildren()) {
                            Lap lap = lapSnapshot.getValue(Lap.class);
                            if (lap != null) {
                                lap.setId(lapSnapshot.getKey());
                                lapList.add(lap);
                            }
                        }
                        lapList.sort(Comparator.comparingLong(Lap::getTimestamp).reversed());
                        lapAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to load laps", error.toException());
                        Toast.makeText(StopwatchActivity.this,
                                "Failed to load laps", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearLaps() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Laps")
                .setMessage("Are you sure you want to delete all lap records?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear from Firebase
                    databaseRef.child("users").child(userId).child("laps").removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Clear local list
                                lapList.clear();
                                lapAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "All laps cleared", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Failed to clear laps", e);
                                Toast.makeText(this, "Failed to clear laps", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateButtonStates() {
        startButton.setEnabled(!isRunning);
        pauseButton.setEnabled(isRunning);
        resetButton.setEnabled(startTime != 0 || isRunning);
        lapButton.setEnabled(isRunning);
        clearLapsButton.setEnabled(!lapList.isEmpty());
    }

    private void createUserRecord() {
        databaseRef.child("users").child(userId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        enableStopwatchFeatures();
                    } else {
                        Toast.makeText(this, "Failed to initialize user", Toast.LENGTH_SHORT).show();
                        logoutUser();
                    }
                });
    }

    private void enableStopwatchFeatures() {
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
        lapButton.setEnabled(false);
        clearLapsButton.setEnabled(false);
    }

    private void logoutUser() {
        pauseTimer();
        mAuth.signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}