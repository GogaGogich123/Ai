/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.player;

import javax.annotation.concurrent.Immutable;
import org.joml.Vector3d;

@Immutable
public class UploadingDataStore {
    private Vector3d pos1 = null;
    private Vector3d pos2 = null;
    private boolean isUploading = false;
    private int uploadSize = 0;

    public Vector3d getPos1() {
        return this.pos1;
    }

    public void setPos1(Vector3d pos1) {
        this.pos1 = pos1;
    }

    public Vector3d getPos2() {
        return this.pos2;
    }

    public void setPos2(Vector3d pos2) {
        this.pos2 = pos2;
    }

    public boolean isUploading() {
        return this.isUploading;
    }

    public void setUploading(boolean uploading) {
        this.isUploading = uploading;
    }

    public int getUploadSize() {
        return this.uploadSize;
    }

    public void setUploadSize(int uploadSize) {
        this.uploadSize = uploadSize;
    }
}

