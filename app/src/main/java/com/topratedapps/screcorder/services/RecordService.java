package com.topratedapps.screcorder.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.Surface;
import android.view.SurfaceView;

import com.topratedapps.screcorder.R;
import com.topratedapps.screcorder.ui.activities.MainActivity;

/**
 * Created by bullhead on 12/7/17.
 *
 */

public class RecordService extends Service {
    private IBinder binder = new RecordServiceBinder();

    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private SurfaceView mSurfaceView;
    private boolean isRecording = false;

    private final static String CHANNEL_ID = "Main";
    private static final String EXIT_INTENT_FILTER = "exit_app";
    private static final String CAPTURE_INTENT_FILTER = "capture_app";
    private static final String STOP_CAPTURE_FILTER = "stop_capture";
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    public static final String OUTPUT_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/o-recorder/videos/";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class RecordServiceBinder extends Binder {
        public RecordService getServiceInstance() {
            return RecordService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showRunningNotification();
        init(intent);
        return START_NOT_STICKY;
    }

    private void init(Intent intent) {
        registerReceiver(exitReceiver, new IntentFilter(EXIT_INTENT_FILTER));
    }

    private void showRunningNotification() {
        PendingIntent secondAction = isRecording ? stopCaptureIntent() : captureIntent();
        String secondString = isRecording ? "Stop" : "Record";

        Intent launchAppIntent = new Intent(this, MainActivity.class);
        launchAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launchPendingIntent =
                PendingIntent.getActivity(this, 2, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_small)
                        .setContentTitle("Screen recorder is running")
                        .addAction(R.drawable.ic_notification_close, "Exit", exitPendingIntent())
                        .addAction(R.drawable.ic_notification_small, secondString, secondAction)
                        .setContentIntent(launchPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "App",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, notificationBuilder.build());

    }

    private PendingIntent stopCaptureIntent() {
        return exitPendingIntent();
    }

    private PendingIntent exitPendingIntent() {
        Intent exitIntent = new Intent(EXIT_INTENT_FILTER);
        return PendingIntent.getBroadcast(this, 1,
                exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent captureIntent() {
        Intent intent = new Intent(CAPTURE_INTENT_FILTER);
        return PendingIntent.getBroadcast(this, 10, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private BroadcastReceiver exitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopForeground(true);
        }
    };

}
