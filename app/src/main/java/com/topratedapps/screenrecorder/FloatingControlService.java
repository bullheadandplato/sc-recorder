/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.topratedapps.screenrecorder.Const.RecordingState;
import com.topratedapps.screenrecorder.widgets.OverlayView;

/**
 * Created by bullhead on 05-11-2017.
 *
 */

public class FloatingControlService extends Service {

    private WindowManager windowManager;
    private OverlayView overlayView;
    private IBinder binder = new ServiceBinder();
    private OverlayView.Listener listener;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        showOverlay();
        return START_STICKY;
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
    private void pauseScreenRecording() {
        Intent pauseIntent = new Intent(this, RecorderService.class);
        pauseIntent.setAction(Const.SCREEN_RECORDING_PAUSE);
        startService(pauseIntent);
    }

    // Set stop intent and start the recording service
    private void stopScreenSharing() {
        Intent stopIntent = new Intent(this, RecorderService.class);
        stopIntent.setAction(Const.SCREEN_RECORDING_STOP);
        startService(stopIntent);
    }

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
        Log.d(Const.TAG, "Binding successful!");
        return binder;
    }

    //Stop the service once the service is unbinded from recording service
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(Const.TAG, "Unbinding and stopping service");
        stopSelf();
        return super.onUnbind(intent);
    }

    //Method to convert dp to px
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    void showOverlay() {

        listener = new OverlayView.Listener() {
            @Override
            public void onCancel() {
                cancelOverlay();
            }

            @Override
            public void onPrepare() {
                listener.onPrepare();
            }

            @Override
            public void onStart() {
                resumeScreenRecording();
            }

            @Override
            public void onStop() {
                stopScreenSharing();
            }

            @Override
            public void onResize() {
                windowManager.updateViewLayout(overlayView, overlayView.getLayoutParams());
            }
        };
        overlayView = OverlayView.create(this, listener, true);
        windowManager.addView(overlayView, OverlayView.createLayoutParams(this));

    }

    private void hideOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            overlayView = null;

        }
    }

    private void cancelOverlay() {
        hideOverlay();

    }
}
