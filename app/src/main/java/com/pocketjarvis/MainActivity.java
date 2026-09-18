package com.pocketjarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private LinearLayout chat;
    private EditText input;
    private TextToSpeech tts;
    private SharedPreferences memory;

    private static final int MIC_PERMISSION = 100;
    private static final int VOICE_REQUEST = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        memory = getSharedPreferences(
                "jarvis_memory",
                MODE_PRIVATE
        );

        buildInterface();

        tts = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        tts.setLanguage(Locale.US);

                    }

                }
        );

        addJarvis(
                "JARVIS online. How can I assist you?"
        );
    }

    private void buildInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                24,
                28,
                24,
                20
        );

        root.setBackgroundColor(
                0xFF05070A
        );

        // TITLE

        TextView title =
                new TextView(this);

        title.setText(
                "◉ J A R V I S"
        );

        title.setTextColor(
                0xFFFFFFFF
        );

        title.setTextSize(24);

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // STATUS

        TextView status =
                new TextView(this);

        status.setText(
                "ONLINE • UNIVERSAL APP MODE"
        );

        status.setTextColor(
                0xFF8FA3B8
        );

        status.setTextSize(12);

        status.setGravity(
                Gravity.CENTER
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        // CHAT AREA

        ScrollView scroll =
                new ScrollView(this);

        chat =
                new LinearLayout(this);

        chat.setOrientation(
                LinearLayout.VERTICAL
        );

        chat.setPadding(
                0,
                20,
                0,
                20
        );

        scroll.addView(chat);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // COMMAND BAR

        LinearLayout controls =
                new LinearLayout(this);

        input =
                new EditText(this);

        input.setHint(
                "Command JARVIS..."
        );

        input.setSingleLine(true);

        input.setTextColor(
                0xFFFFFFFF
        );

        input.setHintTextColor(
                0xFF7D8794
        );

        controls.addView(
                input,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        // MICROPHONE

        Button mic =
                new Button(this);

        mic.setText("MIC");

        mic.setOnClickListener(
                v -> startVoice()
        );

        controls.addView(
                mic,
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                )
        );

        // SEND

        Button send =
                new Button(this);

        send.setText("SEND");

        send.setOnClickListener(
                v -> sendCommand()
        );

        controls.addView(
                send,
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                )
        );

        input.setOnEditorActionListener(
                (v, actionId, event) -> {

                    sendCommand();

                    return true;

                }
        );

        root.addView(
                controls,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        setContentView(root);
    }

    private void sendCommand() {

        String command =
                input
                        .getText()
                        .toString()
                        .trim();

        if (command.isEmpty()) {
            return;
        }

        addUser(command);

        input.setText("");

        handleCommand(command);
    }

    private void handleCommand(String raw) {

        String command =
                raw.toLowerCase(
                        Locale.US
                ).trim();

        String reply;

        // GREETING

        if (
                command.equals("hello")
                        ||
                command.equals("hi")
                        ||
                command.contains("hello jarvis")
        ) {

            reply =
                    "Hello. I am ready.";

        }

        // TIME

        else if (
                command.equals("time")
                        ||
                command.contains("what time")
        ) {

            reply =
                    new SimpleDateFormat(
                            "hh:mm a",
                            Locale.US
                    ).format(
                            new Date()
                    );
        }

        // DATE

        else if (
                command.equals("date")
                        ||
                command.contains("what date")
                        ||
                command.contains("today's date")
        ) {

            reply =
                    new SimpleDateFormat(
                            "EEEE, dd MMMM yyyy",
                            Locale.US
                    ).format(
                            new Date()
                    );
        }

        // MEMORY

        else if (
                command.startsWith(
                        "remember "
                )
        ) {

            String item =
                    raw.substring(9).trim();

            memory.edit()
                    .putString(
                            "last_memory",
                            item
                    )
                    .apply();

            reply =
                    "I will remember: "
                            + item;
        }

        else if (
                command.contains(
                        "what do you remember"
                )
        ) {

            reply =
                    "I remember: "
                            +
                    memory.getString(
                            "last_memory",
                            "nothing yet"
                    );
        }

        // SETTINGS

        else if (
                command.equals("settings")
                        ||
                command.contains(
                        "open settings"
                )
        ) {

            try {

                startActivity(
                        new Intent(
                                Settings.ACTION_SETTINGS
                        )
                );

                reply =
                        "Opening settings.";

            } catch (Exception e) {

                reply =
                        "I could not open settings.";

            }
        }

        // IDENTITY

        else if (
                command.equals(
                        "who are you"
                )
                        ||
                command.contains(
                        "what are you"
                )
        ) {

            reply =
                    "I am Pocket JARVIS, your Android assistant.";

        }

        // STATUS

        else if (
                command.equals("status")
        ) {

            reply =
                    "All systems online. Universal app mode is active.";

        }

        // GOOGLE SEARCH

        else if (
                command.startsWith(
                        "search "
                )
        ) {

            String query =
                    raw.substring(7).trim();

            try {

                String url =
                        "https://www.google.com/search?q="
                                +
                        Uri.encode(query);

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                        )
                );

                reply =
                        "Searching for "
                                + query;

            } catch (Exception e) {

                reply =
                        "I could not start the browser.";

            }
        }

        // LIST APPS

        else if (
                command.equals("list apps")
                        ||
                command.equals("show apps")
                        ||
                command.contains(
                        "installed apps"
                )
        ) {

            reply =
                    listApps();
        }

        // APP LAUNCHER

        else {

            reply =
                    launchMatchingApp(raw);

        }

        addJarvis(reply);

        speak(reply);
    }

    private String launchMatchingApp(
            String query
    ) {

        String cleanQuery =
                query
                        .toLowerCase(
                                Locale.US
                        )
                        .trim();

        // Remove common command words

        if (
                cleanQuery.startsWith(
                        "open "
                )
        ) {

            cleanQuery =
                    cleanQuery.substring(5)
                            .trim();
        }

        if (
                cleanQuery.startsWith(
                        "launch "
                )
        ) {

            cleanQuery =
                    cleanQuery.substring(7)
                            .trim();
        }

        Intent main =
                new Intent(
                        Intent.ACTION_MAIN,
                        null
                );

        main.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                getPackageManager()
                        .queryIntentActivities(
                                main,
                                0
                        );

        for (
                ResolveInfo info :
                apps
        ) {

            String label =
                    info
                            .loadLabel(
                                    getPackageManager()
                            )
                            .toString();

            String packageName =
                    info.activityInfo
                            .packageName;

            if (
                    label
                            .toLowerCase(
                                    Locale.US
                            )
                            .contains(
                                    cleanQuery
                            )
                            ||
                    packageName
                            .toLowerCase(
                                    Locale.US
                            )
                            .contains(
                                    cleanQuery
                            )
            ) {

                Intent launch =
                        new Intent(
                                Intent.ACTION_MAIN
                        );

                launch.addCategory(
                        Intent.CATEGORY_LAUNCHER
                );

                launch.setClassName(
                        packageName,
                        info.activityInfo.name
                );

                launch.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                try {

                    startActivity(launch);

                    return
                            "Opening "
                                    + label
                                    + ".";

                } catch (Exception ignored) {

                    return
                            "I found "
                                    + label
                                    + ", but Android would not let me open it.";
                }
            }
        }

        return
                "I do not know that command yet. "
                        +
                "please try again next time" 
                        +
                "time, date, settings, or list apps.";
    }

    private String listApps() {

        Intent main =
                new Intent(
                        Intent.ACTION_MAIN,
                        null
                );

        main.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                getPackageManager()
                        .queryIntentActivities(
                                main,
                                0
                        );

        StringBuilder result =
                new StringBuilder(
                        "Installed launchable apps: "
                );

        int count = 0;

        for (
                ResolveInfo info :
                apps
        ) {

            if (count >= 20) {
                break;
            }

            if (count > 0) {
                result.append(", ");
            }

            result.append(
                    info.loadLabel(
                            getPackageManager()
                    )
            );

            count++;
        }

        return result.toString();
    }

    private void startVoice() {

        if (
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                )
                        !=
                PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MIC_PERMISSION
            );

            return;
        }

        try {

            Intent intent =
                    new Intent(
                            RecognizerIntent
                                    .ACTION_RECOGNIZE_SPEECH
                    );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent
                            .LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            startActivityForResult(
                    intent,
                    VOICE_REQUEST
            );

        } catch (Exception e) {

            addJarvis(
                    "Voice input is not available on this device."
            );
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == VOICE_REQUEST
                        &&
                resultCode == RESULT_OK
                        &&
                data != null
        ) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (
                    results != null
                            &&
                    !results.isEmpty()
            ) {

                input.setText(
                        results.get(0)
                );

                sendCommand();
            }
        }
    }

    private void addUser(
            String text
    ) {

        addMessage(
                "YOU",
                text
        );
    }

    private void addJarvis(
            String text
    ) {

        addMessage(
                "JARVIS",
                text
        );
    }

    private void addMessage(
            String who,
            String text
    ) {

        TextView message =
                new TextView(this);

        message.setText(
                who
                        +
                "\n"
                        +
                text
        );

        message.setTextColor(
                0xFFFFFFFF
        );

        message.setTextSize(16);

        message.setPadding(
                16,
                12,
                16,
                12
        );

        chat.addView(
                message,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        message.post(
                () -> {

                    if (
                            chat.getParent()
                                    instanceof ScrollView
                    ) {

                        (
                                (ScrollView)
                                        chat.getParent()
                        ).fullScroll(
                                ScrollView.FOCUS_DOWN
                        );
                    }

                }
        );
    }

    private void speak(
            String text
    ) {

        try {

            if (tts != null) {

                tts.speak(
                        text,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "jarvis_reply"
                );
            }

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {

            try {

                tts.stop();

                tts.shutdown();

            } catch (Exception ignored) {
            }
        }

        super.onDestroy();
    }
          }
