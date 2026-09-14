package com.pocketjarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout chat;
    EditText input;
    TextToSpeech tts;
    SharedPreferences memory;

    final int MIC_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        memory = getSharedPreferences("jarvis_memory", MODE_PRIVATE);

        buildUI();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION
            );
        }

        addMessage(
                "JARVIS",
                "Online. Local systems initialized. How may I assist?"
        );
    }

    void buildUI() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25, 20, 25, 20);
        root.setBackgroundColor(android.graphics.Color.rgb(5, 7, 10));

        TextView header = new TextView(this);
        header.setText("◉  J A R V I S");
        header.setTextSize(25);
        header.setTextColor(android.graphics.Color.CYAN);
        header.setGravity(Gravity.CENTER);
        header.setPadding(10, 10, 10, 5);

        TextView status = new TextView(this);
        status.setText("ONLINE  •  LOCAL MODE");
        status.setTextSize(12);
        status.setTextColor(android.graphics.Color.GRAY);
        status.setGravity(Gravity.CENTER);

        root.addView(header);
        root.addView(status);

        ScrollView scroll = new ScrollView(this);

        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(5, 20, 5, 20);

        scroll.addView(chat);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);

        input = new EditText(this);
        input.setHint("Command JARVIS...");
        input.setTextColor(android.graphics.Color.WHITE);
        input.setHintTextColor(android.graphics.Color.GRAY);
        input.setSingleLine(true);

        controls.addView(
                input,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button mic = new Button(this);
        mic.setText("🎙");

        mic.setOnClickListener(v -> listen());

        controls.addView(mic);

        Button send = new Button(this);
        send.setText("SEND");

        send.setOnClickListener(v -> {

            String command = input.getText().toString().trim();

            if (!command.isEmpty()) {
                input.setText("");
                processCommand(command);
            }

        });

        controls.addView(send);

        root.addView(controls);

        setContentView(root);
    }

    void addMessage(String sender, String message) {

        TextView text = new TextView(this);

        text.setText(sender + "\n" + message);
        text.setTextSize(16);
        text.setTextColor(
                sender.equals("JARVIS")
                        ? android.graphics.Color.CYAN
                        : android.graphics.Color.WHITE
        );

        text.setPadding(15, 15, 15, 15);

        chat.addView(text);
    }

    void reply(String message) {

        addMessage("JARVIS", message);

        if (tts != null) {
            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "jarvis_reply"
            );
        }
    }

    void processCommand(String command) {

        addMessage("YOU", command);

        String c = command.toLowerCase(Locale.ROOT);

        if (c.equals("hello") ||
                c.contains("hello jarvis") ||
                c.equals("hi")) {

            reply("Hello. Systems are ready.");

        }

        else if (c.contains("time")) {

            String time = new SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
            ).format(new Date());

            reply("The current time is " + time);

        }

        else if (c.contains("date")) {

            String date = new SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
            ).format(new Date());

            reply("Today is " + date);

        }

        else if (c.startsWith("remember ")) {

            String fact = command.substring(9).trim();

            memory.edit()
                    .putString("memory_" + System.currentTimeMillis(), fact)
                    .apply();

            reply("I will remember that.");

        }

        else if (c.contains("what do you remember")) {

            Map<String, ?> all = memory.getAll();

            if (all.isEmpty()) {

                reply("My local memory is currently empty.");

            } else {

                StringBuilder result = new StringBuilder();

                for (Object value : all.values()) {
                    result.append(value).append(". ");
                }

                reply(result.toString());
            }

        }

        else if (c.contains("youtube")) {

            openUrl("https://www.youtube.com");
            reply("Opening YouTube.");

        }

        else if (c.contains("google")) {

            openUrl("https://www.google.com");
            reply("Opening Google.");

        }

        else if (c.contains("maps") ||
                c.contains("map")) {

            openUrl("https://maps.google.com");
            reply("Opening Maps.");

        }

        else if (c.startsWith("search ")) {

            String query = command.substring(7).trim();

            openUrl(
                    "https://www.google.com/search?q="
                            + Uri.encode(query)
            );

            reply("Searching for " + query);

        }

        else if (c.contains("settings")) {

            startActivity(
                    new Intent(
                            android.provider.Settings.ACTION_SETTINGS
                    )
            );

            reply("Opening system settings.");

        }

        else if (c.contains("who are you")) {

            reply(
                    "I am Pocket JARVIS, your personal Android assistant."
            );

        }

        else if (c.contains("status")) {

            reply(
                    "All local JARVIS systems are operational."
            );

        }

        else {

            reply(
                    "I heard you. I currently operate in local assistant mode. " +
                    "Try asking for the time, date, opening Google, Maps, YouTube, " +
                    "or say remember followed by something."
            );
        }
    }

    void openUrl(String url) {

        try {

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    void listen() {

        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Command JARVIS..."
        );

        try {

            startActivityForResult(intent, 200);

        } catch (Exception e) {

            reply("Voice recognition is unavailable on this device.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 200 &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (results != null &&
                    !results.isEmpty()) {

                processCommand(results.get(0));
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
