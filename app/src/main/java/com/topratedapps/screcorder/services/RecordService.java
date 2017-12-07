package com.topratedapps.screcorder.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by bullhead on 12/7/17.
 */

public class RecordService extends Service {
    private IBinder binder = new RecordServiceBinder();

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
}
