/*
 * Copyright (c) topratedapps 2017.
 */

package com.topratedapps.screenrecorder.adapter;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Date;

/**
 * Created by bullhead on 07-11-2017.
 */

public class Video implements Comparable<Video> {
    private String FileName;
    private File file;
    private Bitmap thumbnail;
    private Date lastModified;
    private boolean isSection = false;
    private boolean isSelected = false;

    public Video(boolean isSection, Date lastModified) {
        this.isSection = isSection;
        this.lastModified = lastModified;
    }

    public Video(String fileName, File file, Bitmap thumbnail, Date lastModified) {
        FileName = fileName;
        this.file = file;
        this.thumbnail = thumbnail;
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return FileName;
    }

    public File getFile() {
        return file;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public boolean isSection() {
        return isSection;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int compareTo(Video video) {
        return getLastModified().compareTo(video.getLastModified());
    }
}
