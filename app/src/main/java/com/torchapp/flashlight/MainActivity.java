package com.torchapp.flashlight;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    // UI Components
    private ImageButton torchButton;
    private Button sosButton;
    private Button morseConfigButton;
    private SeekBar rateSeekBar;
    private TextView statusText;
    private TextView rateText;
    
    // Torch Management
    private boolean isTorchOn = false;
    private boolean isBlinking = false;
    private boolean isSosActive = false;
    private CameraManager cameraManager;
    private String cameraId;
    private Handler handler;
    private SharedPreferences preferences;
    private Vibrator vibrator;
    private Runnable blinkRunnable;
    private Runnable sosRunnable;
    private int blinkRate = 500;
    
    // Morse Code Pattern
    private int[] morsePattern = null;
    private String currentMorsePatternName = "SOS";
    private Runnable morseRunnable;
    private boolean isMorseActive = false;
    
    // Sound
    private ToneGenerator toneGenerator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Initialize camera manager and find camera ID first
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Initialize sound
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
        } catch (Throwable ignored) {}
        
        initCamera();
        
        initializeViews();
        setupListeners();
    }
    
    private void initCamera() {
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (hasFlash != null && hasFlash) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeViews() {
        torchButton = findViewById(R.id.torchButton);
        sosButton = findViewById(R.id.sosButton);
        morseConfigButton = findViewById(R.id.morseConfigButton);
        rateSeekBar = findViewById(R.id.rateSeekBar);
        statusText = findViewById(R.id.statusText);
        rateText = findViewById(R.id.rateText);

        rateText.setText("Blink: OFF");
        updateMorseButtonLabel();
        
        // Always enable controls
        rateSeekBar.setEnabled(true);
        morseConfigButton.setEnabled(true);
        
        updateUI();
    }
    
    private void updateMorseButtonLabel() {
        if (morsePattern != null) {
            morseConfigButton.setText("⚡ " + currentMorsePatternName);
        } else {
            morseConfigButton.setText("⚡ Configure Morse");
        }
    }
    
    private void setupListeners() {
        torchButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                toggleFlashlight();
            }
        });
        
        sosButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                toggleSOS();
            }
        });
        
        morseConfigButton.setOnClickListener(v -> {
            openMorseCodeConfiguration();
        });
        
        rateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    rateText.setText("Blink: OFF");
                    if (fromUser && isBlinking) {
                        stopBlinking();
                        // Restore steady torch if on
                        if (isTorchOn && cameraId != null) {
                            try {
                                cameraManager.setTorchMode(cameraId, true);
                            } catch (Throwable ignored) {}
                        }
                    }
                } else {
                    blinkRate = 1100 - progress;
                    if (blinkRate < 50) blinkRate = 50;
                    rateText.setText("Blink: " + blinkRate + "ms");
                    if (fromUser && isTorchOn) {
                        // Auto-start or restart blinking
                        if (isBlinking) {
                            stopBlinking();
                        }
                        startBlinking();
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void toggleFlashlight() {
        if (isTorchOn) {
            turnTorchOff();
        } else {
            turnTorchOn();
        }
    }
    
    private void turnTorchOn() {
        if (cameraId == null) initCamera();
        if (cameraId == null) {
            Toast.makeText(this, "No flashlight available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            isTorchOn = true;
            
            // Check if morse pattern is selected — start morse if so
            if (morsePattern != null) {
                cameraManager.setTorchMode(cameraId, false); // Turn off first
                startMorseCode();
            } 
            // Check if blink rate slider is already set — start blinking if so
            else if (rateSeekBar.getProgress() > 0) {
                cameraManager.setTorchMode(cameraId, true);
                blinkRate = 1100 - rateSeekBar.getProgress();
                if (blinkRate < 50) blinkRate = 50;
                startBlinking();
            }
            // Default: steady torch on
            else {
                cameraManager.setTorchMode(cameraId, true);
            }
            
            playSound(true);
            vibrate(50);
            updateUI();
        } catch (Throwable e) {
            Toast.makeText(this, "Failed to turn on flashlight", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void turnTorchOff() {
        if (cameraId == null) return;
        try {
            stopBlinking();
            stopSOS();
            stopMorse();
            cameraManager.setTorchMode(cameraId, false);
            isTorchOn = false;
            playSound(false);
            vibrate(30);
            updateUI();
        } catch (Throwable ignored) {
            isTorchOn = false;
            updateUI();
        }
    }
    
    private void startBlinking() {
        if (isBlinking || cameraId == null) return;
        isBlinking = true;
        blinkRunnable = new Runnable() {
            private boolean blinkState = true;
            @Override
            public void run() {
                if (!isBlinking) return;
                try {
                    cameraManager.setTorchMode(cameraId, blinkState);
                    blinkState = !blinkState;
                    handler.postDelayed(this, blinkRate);
                } catch (Throwable e) {
                    isBlinking = false;
                }
            }
        };
        handler.post(blinkRunnable);
    }
    
    private void stopBlinking() {
        isBlinking = false;
        if (blinkRunnable != null) {
            handler.removeCallbacks(blinkRunnable);
        }
    }
    
    // ---- SOS Mode ----
    private void toggleSOS() {
        if (isSosActive) {
            stopSOS();
        } else {
            startSOS();
        }
    }
    
    private void startSOS() {
        if (isSosActive || cameraId == null) return;
        
        // Stop blinking if active
        if (isBlinking) stopBlinking();
        
        isSosActive = true;
        isTorchOn = true;
        sosButton.setText("STOP SOS");
        sosButton.setBackground(ContextCompat.getDrawable(this, R.drawable.sos_button_active_bg));
        statusText.setText("SOS ACTIVE");
        statusText.setTextColor(ContextCompat.getColor(this, R.color.sos_active));
        updateUI();
        
        // SOS Morse code: ··· --- ··· 
        sosRunnable = new Runnable() {
            private int step = 0;
            private final int[] pattern = {
                // S: · · ·
                150, 150, 150, 150, 150, 400,
                // O: — — —
                400, 150, 400, 150, 400, 400,
                // S: · · ·
                150, 150, 150, 150, 150, 1000
            };
            
            @Override
            public void run() {
                if (!isSosActive) return;
                try {
                    boolean isOn = (step % 2 == 0);
                    cameraManager.setTorchMode(cameraId, isOn);
                    int delay = pattern[step % pattern.length];
                    step++;
                    handler.postDelayed(this, delay);
                } catch (Throwable e) {
                    isSosActive = false;
                }
            }
        };
        handler.post(sosRunnable);
    }
    
    private void stopSOS() {
        if (!isSosActive) return;
        isSosActive = false;
        sosButton.setText("SOS");
        sosButton.setBackground(ContextCompat.getDrawable(this, R.drawable.sos_button_bg));
        if (sosRunnable != null) handler.removeCallbacks(sosRunnable);
        
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (Throwable ignored) {}
        isTorchOn = false;
        updateUI();
    }
    
    private void playSound(boolean on) {
        if (!preferences.getBoolean("sound_effects", false)) return;
        if (toneGenerator == null) return;
        try {
            if (on) {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80);
            } else {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 60);
            }
        } catch (Throwable ignored) {}
    }
    
    private void vibrate(int duration) {
        if (!preferences.getBoolean("vibrate", true)) return;
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(duration);
            }
        } catch (Throwable ignored) {}
    }
    
    private void updateUI() {
        if (isTorchOn) {
            torchButton.setImageResource(R.drawable.ic_torch_on);
            torchButton.setBackground(ContextCompat.getDrawable(this, R.drawable.torch_button_on_bg));
            if (!isSosActive && !isMorseActive) {
                statusText.setText(isBlinking ? "Blinking" : "Flashlight ON");
                statusText.setTextColor(ContextCompat.getColor(this, R.color.torch_on));
            }
        } else {
            torchButton.setImageResource(R.drawable.ic_torch_off);
            torchButton.setBackground(ContextCompat.getDrawable(this, R.drawable.torch_button_off_bg));
            statusText.setText("Flashlight OFF");
            statusText.setTextColor(ContextCompat.getColor(this, R.color.torch_off));
        }
        
        // Control button states
        rateSeekBar.setEnabled(!isSosActive && !isMorseActive);
        morseConfigButton.setEnabled(!isSosActive);
        sosButton.setEnabled(!isMorseActive);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (preferences.getBoolean("auto_turn_off", false)) {
            turnTorchOff();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        turnTorchOff();
        stopMorse();
        if (toneGenerator != null) {
            try { toneGenerator.release(); } catch (Throwable ignored) {}
        }
    }
    
    private void openMorseCodeConfiguration() {
        MorseCodeDialog morseDialog = new MorseCodeDialog(this, (patternName, pattern) -> {
            currentMorsePatternName = patternName;
            morsePattern = pattern;
            updateMorseButtonLabel();
            
            // Auto-start morse if torch is already on
            if (isTorchOn) {
                startMorseCode();
            } else {
                // Show confirmation
                Toast.makeText(MainActivity.this, 
                    "✓ Morse pattern set: " + patternName + "\nTurn on torch to play", 
                    Toast.LENGTH_SHORT).show();
            }
        });
        morseDialog.show();
    }
    
    private void startMorseCode() {
        if (morsePattern == null || cameraId == null) return;
        stopBlinking();
        stopMorse();
        
        isMorseActive = true;
        rateText.setText("Morse: " + currentMorsePatternName);
        statusText.setText("🔦 Morse: " + currentMorsePatternName);
        statusText.setTextColor(ContextCompat.getColor(this, R.color.torch_on));
        
        morseRunnable = new Runnable() {
            private int step = 0;
            
            @Override
            public void run() {
                if (!isMorseActive || !isTorchOn) {
                    return;
                }
                
                try {
                    // Even indices = turn on/off, odd indices = wait time
                    boolean isOn = (step % 2 == 0);
                    
                    if (isOn) {
                        cameraManager.setTorchMode(cameraId, true);
                    } else {
                        cameraManager.setTorchMode(cameraId, false);
                    }
                    
                    int delay = morsePattern[step % morsePattern.length];
                    step++;
                    handler.postDelayed(this, delay);
                } catch (Throwable e) {
                    isMorseActive = false;
                }
            }
        };
        handler.post(morseRunnable);
    }
    
    private void stopMorse() {
        isMorseActive = false;
        if (morseRunnable != null) {
            handler.removeCallbacks(morseRunnable);
        }
    }
}