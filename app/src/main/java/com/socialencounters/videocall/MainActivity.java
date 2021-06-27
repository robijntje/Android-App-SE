package com.socialencounters.videocall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.BroadcastIntentHelper;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import java.net.MalformedURLException;
import java.net.URL;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onBroadcastReceived(intent);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Config voor serververbinding
        URL serverURL;
        try {
            // Verbinding maken met de Jitsi server van Social Encounters
            serverURL = new URL("http://jitsi.socialencounters.nl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Server URL werkt niet. Foutcode 1.");
        }
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        registerForBroadcastMessages();
    }
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        super.onDestroy();
    }

    public void onButtonClick(View v) {
        EditText editText = findViewById(R.id.conferenceName);
        String text = editText.getText().toString();

        if (text.length() > 0) {
            JitsiMeetConferenceOptions options
                    = new JitsiMeetConferenceOptions.Builder()
                    .setRoom(text)
                    .build();
            JitsiMeetActivity.launch(this, options);
        }
    }

    private void registerForBroadcastMessages() {
        IntentFilter intentFilter = new IntentFilter();
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }


    private void onBroadcastReceived(Intent intent) {
        if (intent != null) {
            BroadcastEvent event = new BroadcastEvent(intent);
            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Timber.i("Conference Joined with url%s", event.getData().get("url"));
                    break;
                case PARTICIPANT_JOINED:
                    Timber.i("Participant joined%s", event.getData().get("name"));
                    break;
            }
        }
    }
    private void hangUp() {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);
    }
}
