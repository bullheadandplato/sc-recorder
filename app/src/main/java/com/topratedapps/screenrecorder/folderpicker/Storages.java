/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder.folderpicker;

/**
 * Created by bullhead on 24-05-2017.
 */

public class Storages {
    private String path;
    private StorageType type;

    public enum StorageType {Internal, External}

    public Storages(String path, StorageType type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public StorageType getType() {
        return type;
    }
}
