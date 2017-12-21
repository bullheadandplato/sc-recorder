/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder;

/**
 * Created by bullhead on 11-10-2017.
 *
 */
//Interface for permission result callback
interface PermissionResultListener {
    void onPermissionResult(int requestCode,
                            String permissions[], int[] grantResults);
}
