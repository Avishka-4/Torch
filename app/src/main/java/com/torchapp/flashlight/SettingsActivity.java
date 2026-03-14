package com.torchapp.flashlight;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {
    
    private Switch autoTurnOffSwitch;
    private Switch vibrateSwitch;
    private Switch soundEffectSwitch;
    
    private SharedPreferences preferences;
    private Vibrator vibrator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        initializeViews();
        loadSettings();
        setupListeners();
    }
    
    private void initializeViews() {
        autoTurnOffSwitch = findViewById(R.id.autoTurnOffSwitch);
        vibrateSwitch = findViewById(R.id.vibrateSwitch);
        soundEffectSwitch = findViewById(R.id.soundEffectSwitch);
    }
    
    private void loadSettings() {
        autoTurnOffSwitch.setChecked(preferences.getBoolean("auto_turn_off", true));
        vibrateSwitch.setChecked(preferences.getBoolean("vibrate", true));
        soundEffectSwitch.setChecked(preferences.getBoolean("sound_effects", false));
    }
    
    private void setupListeners() {
        autoTurnOffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("auto_turn_off", isChecked).apply();
            Toast.makeText(this, "Auto turn-off " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
        
        vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("vibrate", isChecked).apply();
            if (isChecked) {
                // Give feedback vibration to confirm it works
                try {
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(100);
                        }
                    }
                } catch (Throwable ignored) {}
                Toast.makeText(this, "Vibration enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vibration disabled", Toast.LENGTH_SHORT).show();
            }
        });
        
        soundEffectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("sound_effects", isChecked).apply();
            if (isChecked) {
                // Play a demo tone so user hears it works
                try {
                    ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    // Release after a delay
                    buttonView.postDelayed(tg::release, 300);
                } catch (Throwable ignored) {}
                Toast.makeText(this, "Sound effects enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sound effects disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}