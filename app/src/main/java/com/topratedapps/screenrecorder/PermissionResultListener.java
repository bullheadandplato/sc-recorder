/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

/**
 * Created by vijai on 11-10-2016.
 */
//Interface for permission result callback
interface PermissionResultListener {
    void onPermissionResult(int requestCode,
                            String permissions[], int[] grantResults);
}
