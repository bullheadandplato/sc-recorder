/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.topratedapps.screenrecorder.Const.RecordingState;

/**
 * Created by bullhead on 05-11-2017.
 *
 */

public class FloatingControlService extends Service {
    private static final String EXIT_INTENT_FILTER = "exit_app";
    private static final String STOP_CAPTURE_FILTER = "stop_capture";
    private static final String START_CAPTURE_INTENT = "start_capture";

    private final static String CHANNEL_ID = "Main";

    private IBinder binder = new ServiceBinder();
    private NotificationManager notificationManager;
    private boolean isRecording;
    private Intent data;
    private int result;
    private boolean stopped = false;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null && intent.getAction().equals("stop")) {
            stopForeground(true);
            return START_NOT_STICKY;
        }
        data = intent.getParcelableExtra(Const.RECORDER_INTENT_DATA);
        result = intent.getIntExtra(Const.RECORDER_INTENT_RESULT, Activity.RESULT_OK);


        registerReceiver(exitReceiver, new IntentFilter(EXIT_INTENT_FILTER));
        registerReceiver(startCapture, new IntentFilter(START_CAPTURE_INTENT));
        registerReceiver(stopCapture, new IntentFilter(STOP_CAPTURE_FILTER));
        if (startRecordingImmediate) {
            isRecording = true;
            startScreenRecording();
            startRecordingImmediate = false;
        }
        showRunningNotification();
        return START_NOT_STICKY;
    }


    /* Set resume intent and start the recording service
     * NOTE: A service can be started only once. Any subsequent startService only passes the intent
     * if any by calling onStartCommand */
    private void resumeScreenRecording() {
        Intent resumeIntent = new Intent(this, RecorderService.class);
        resumeIntent.setAction(Const.SCREEN_RECORDING_RESUME);
        startService(resumeIntent);
    }

    // Set pause intent and start the recording service
    private void startScreenRecording() {
        Intent recorderService = new Intent(this, RecorderService.class);
        recorderService.setAction(Const.SCREEN_RECORDING_START);
        recorderService.putExtra(Const.RECORDER_INTENT_DATA, data);
        recorderService.putExtra(Const.RECORDER_INTENT_RESULT, result);
        startService(recorderService);
    }

    // Set stop intent and start the recording service
    private void stopScreenSharing() {
        isRecording = false;
        Intent stopIntent = new Intent(this, RecorderService.class);
        stopIntent.setAction(Const.SCREEN_RECORDING_STOP);
        startService(stopIntent);
    }

    private void showRunningNotification() {
        String secondAction = isRecording ? "Stop" : "Record";
        PendingIntent secondIntent = isRecording ? stopCaptureIntent() : startCaptureInten();
        Intent launchAppIntent = new Intent(this, MainActivity.class);
        launchAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launchPendingIntent =
                PendingIntent.getActivity(this, 2, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle("Screen recorder is running")
                        .addAction(R.drawable.ic_notification_close, "Exit", exitPendingIntent())
                        .addAction(R.drawable.ic_notification_small, secondAction, secondIntent)
                        .setContentIntent(launchPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "App",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, notificationBuilder.build());

    }

    private PendingIntent exitPendingIntent() {
        Intent exitIntent = new Intent(EXIT_INTENT_FILTER);
        return PendingIntent.getBroadcast(this, 1,
                exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent startCaptureInten() {
        Intent startIntent = new Intent(START_CAPTURE_INTENT);
        return PendingIntent.getBroadcast(this, 2,
                startIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private PendingIntent stopCaptureIntent() {
        Intent stopCapture = new Intent(STOP_CAPTURE_FILTER);
        return PendingIntent.getBroadcast(this, 3,
                stopCapture, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private BroadcastReceiver exitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stop();

        }
    };
    private BroadcastReceiver stopCapture = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopScreenSharing();
            showRunningNotification();
            stopped = true;

        }
    };
    private boolean startRecordingImmediate = false;
    private BroadcastReceiver startCapture = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (stopped) {
                stopped = false;
                Intent intent1 = new Intent(FloatingControlService.this, OverlyPermissionActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startRecordingImmediate = true;
                startActivity(intent1);
                return;
            }
            startScreenRecording();
            isRecording = true;
            showRunningNotification();
        }
    };

    public void setRecordingState(RecordingState recordingState) {
        //this.recordingState = recordingState;
    }


    //Binder class for binding to recording service
    public class ServiceBinder extends Binder {
        FloatingControlService getService() {
            return FloatingControlService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Return ServiceBinder instance on successful binding
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private void stop() {
        try {
            if (isRecording) {
                stopScreenSharing();
            }
            unregisterReceiver(exitReceiver);
            unregisterReceiver(stopCapture);
            unregisterReceiver(startCapture);
            stopForeground(true);
        } catch (Exception ex) {
            //ignore
            ex.printStackTrace();
        }
    }
}
