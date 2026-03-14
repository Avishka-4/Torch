package com.torchapp.flashlight;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MorseCodeDialog {
    
    // Timing constants
    private static final int DOT_TIME = 150;
    private static final int DASH_TIME = 400;
    private static final int GAP_TIME = 150;
    private static final int LETTER_GAP = 400;
    
    private final Context context;
    private final MorseCodeListener listener;
    private String selectedPattern = "SOS";
    
    public interface MorseCodeListener {
        void onMorsePatternSelected(String patternName, int[] pattern);
    }
    
    public MorseCodeDialog(Context context, MorseCodeListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("⚡ Morse Code Configuration");
        
        String[] presets = {"SOS", "HELLO", "YES", "HELP", "THANKS"};
        
        builder.setSingleChoiceItems(presets, 0, (dialog, which) -> {
            selectedPattern = presets[which];
        });
        
        builder.setNeutralButton("🧪 Test", (dialog, which) -> {
            testPattern(selectedPattern);
        });
        
        builder.setPositiveButton("✓ Apply", (dialog, which) -> {
            applyPattern(selectedPattern);
            dialog.dismiss();
        });
        
        builder.setNegativeButton("✏️ Custom", (dialog, which) -> {
            showCustomMorseDialog();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void testPattern(String patternName) {
        int[] pattern = generateMorsePattern(patternName);
        if (pattern == null) return;
        
        String morseVisual = textToMorseDisplay(patternName);
        
        new AlertDialog.Builder(context)
            .setTitle("🧪 " + patternName)
            .setMessage(morseVisual + "\n\n⏱ Duration: " + (calculateDuration(pattern) / 1000) + "s")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showCustomMorseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("✏️ Create Custom Morse Code");
        
        final EditText input = new EditText(context);
        input.setHint("Enter text (A-Z, 0-9)");
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);
        
        builder.setPositiveButton("✓ Create", (dialog, which) -> {
            String text = input.getText().toString().toUpperCase().trim();
            if (text.isEmpty()) {
                Toast.makeText(context, "Please enter text", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int[] pattern = generateMorsePattern(text);
            if (pattern == null) {
                Toast.makeText(context, "Invalid characters (A-Z, 0-9 only)", Toast.LENGTH_SHORT).show();
                return;
            }
            
            showCustomPatternConfirm(text, pattern);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCustomPatternConfirm(String text, int[] pattern) {
        String morseVisual = textToMorseDisplay(text);
        
        new AlertDialog.Builder(context)
            .setTitle("✓ " + text)
            .setMessage(morseVisual + "\n\n⏱ Duration: " + (calculateDuration(pattern) / 1000) + "s")
            .setPositiveButton("✓ Apply", (d, w) -> {
                listener.onMorsePatternSelected(text, pattern);
                Toast.makeText(context, "Morse pattern applied: " + text, Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("🧪 Test", (d, w) -> {
                testPattern(text);
                showCustomPatternConfirm(text, pattern);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void applyPattern(String patternName) {
        int[] pattern = generateMorsePattern(patternName);
        if (pattern != null) {
            listener.onMorsePatternSelected(patternName, pattern);
            Toast.makeText(context, "✓ Morse applied: " + patternName, Toast.LENGTH_SHORT).show();
        }
    }
    
    private int[] generateMorsePattern(String text) {
        List<Integer> pattern = new ArrayList<>();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String morse = charToMorse(c);
            
            if (morse == null) return null; // Invalid character
            
            // Convert morse string to timing pattern
            for (char m : morse.toCharArray()) {
                if (m == '.') {
                    pattern.add(DOT_TIME);    // on
                    pattern.add(GAP_TIME);    // off
                } else if (m == '-') {
                    pattern.add(DASH_TIME);   // on
                    pattern.add(GAP_TIME);    // off
                }
            }
            
            // Add extra gap between letters (remove last gap and add letter gap)
            if (pattern.size() > 0) {
                pattern.remove(pattern.size() - 1); // Remove last gap
                if (i < text.length() - 1) {
                    pattern.add(LETTER_GAP); // Add letter gap
                }
            }
        }
        
        // Add final pause between repetitions
        pattern.add(600); // Final pause before repeating
        
        int[] result = new int[pattern.size()];
        for (int i = 0; i < pattern.size(); i++) {
            result[i] = pattern.get(i);
        }
        return result;
    }
    
    private String charToMorse(char c) {
        switch (Character.toUpperCase(c)) {
            case 'A': return ".-";
            case 'B': return "-...";
            case 'C': return "-.-.";
            case 'D': return "-..";
            case 'E': return ".";
            case 'F': return "..-.";
            case 'G': return "--.";
            case 'H': return "....";
            case 'I': return "..";
            case 'J': return ".---";
            case 'K': return "-.-";
            case 'L': return ".-..";
            case 'M': return "--";
            case 'N': return "-.";
            case 'O': return "---";
            case 'P': return ".--.";
            case 'Q': return "--.-";
            case 'R': return ".-.";
            case 'S': return "...";
            case 'T': return "-";
            case 'U': return "..-";
            case 'V': return "...-";
            case 'W': return ".--";
            case 'X': return "-..-";
            case 'Y': return "-.--";
            case 'Z': return "--..";
            case '0': return "-----";
            case '1': return ".----";
            case '2': return "..---";
            case '3': return "...--";
            case '4': return "....-";
            case '5': return ".....";
            case '6': return "-....";
            case '7': return "--...";
            case '8': return "---..";
            case '9': return "----.";
            default: return null;
        }
    }
    
    private String textToMorseDisplay(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toUpperCase().toCharArray()) {
            String morse = charToMorse(c);
            if (morse != null) {
                String visual = morse.replace('.', '·').replace('-', '—');
                sb.append(visual).append("  ");
            }
        }
        return sb.toString().trim();
    }
    
    private int calculateDuration(int[] pattern) {
        int total = 0;
        for (int delay : pattern) {
            total += delay;
        }
        return total;
    }
}
