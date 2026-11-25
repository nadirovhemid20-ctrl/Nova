package com.nova.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView status;
    private TextToSpeech tts;

    private static final int REQUEST_MIC = 100;
    private static final int REQUEST_SPEECH = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("az", "AZ"));
            }
        });

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(35, 35, 35, 35);
        layout.setBackgroundColor(Color.rgb(5, 12, 30));

        TextView title = new TextView(this);
        title.setText("N O V A");
        title.setTextSize(42);
        title.setTextColor(Color.rgb(0, 200, 255));
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your Personal AI");
        subtitle.setTextSize(18);
        subtitle.setTextColor(Color.WHITE);
        subtitle.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("\nSalam! Mən NOVA.\nSəni dinləməyə hazıram.");
        status.setTextSize(17);
        status.setTextColor(Color.rgb(120, 180, 220));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 35, 0, 35);

        Button listenButton = new Button(this);
        listenButton.setText("🎙  NOVA, DİNLƏ");
        listenButton.setTextSize(18);

        listenButton.setOnClickListener(v -> checkMicrophone());

        layout.addView(title);
        layout.addView(subtitle);
        layout.addView(status);
        layout.addView(listenButton);

        setContentView(layout);
    }

    private void checkMicrophone() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MIC
            );

        } else {
            startListening();
        }
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "az-AZ");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "NOVA səni dinləyir...");

        try {
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (Exception e) {
            status.setText("Səs tanıma xidməti telefonda mövcud deyil.");
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MIC
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startListening();
        } else {
            status.setText("Mikrofon icazəsi verilmədi.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);

                status.setText("Səni eşitdim:\n\n" + spokenText);

                if (tts != null) {
                    tts.speak(
                            "Səni eşitdim. " + spokenText,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "NOVA_REPLY"
                    );
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
