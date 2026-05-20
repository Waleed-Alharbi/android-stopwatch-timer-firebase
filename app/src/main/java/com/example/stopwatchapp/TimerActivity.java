package com.example.stopwatchapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class TimerActivity extends AppCompatActivity {

    private EditText hoursInput, minutesInput, secondsInput;
    private TextView timerDisplay;
    private Button startButton, stopButton, backButton;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Initialize views
        hoursInput = findViewById(R.id.hoursInput);
        minutesInput = findViewById(R.id.minutesInput);
        secondsInput = findViewById(R.id.secondsInput);
        timerDisplay = findViewById(R.id.timerDisplay);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        backButton = findViewById(R.id.backButton);

        // Set button listeners
        startButton.setOnClickListener(v -> startTimer());
        stopButton.setOnClickListener(v -> stopTimer());
        backButton.setOnClickListener(v -> {
            stopTimer();
            startActivity(new Intent(this, StopwatchActivity.class));
            finish();
        });
    }

    private void startTimer() {
        // Get input values
        int hours = parseInt(hoursInput.getText().toString(), 0);
        int minutes = parseInt(minutesInput.getText().toString(), 0);
        int seconds = parseInt(secondsInput.getText().toString(), 0);

        // Validate input
        if (hours == 0 && minutes == 0 && seconds == 0) {
            Toast.makeText(this, "Please set a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate total time in milliseconds
        timeLeftInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L;

        // Disable inputs during timer
        setInputsEnabled(false);
        stopButton.setEnabled(true);

        // Create countdown timer
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timerDisplay.setText("00:00:00");
                Toast.makeText(TimerActivity.this, "Timer finished!", Toast.LENGTH_SHORT).show();
                setInputsEnabled(true);
                stopButton.setEnabled(false);
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            setInputsEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    private void updateTimerDisplay() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format(Locale.getDefault(),
                "%02d:%02d:%02d", hours, minutes, seconds);
        timerDisplay.setText(timeFormatted);
    }

    private void setInputsEnabled(boolean enabled) {
        hoursInput.setEnabled(enabled);
        minutesInput.setEnabled(enabled);
        secondsInput.setEnabled(enabled);
        startButton.setEnabled(enabled);
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}