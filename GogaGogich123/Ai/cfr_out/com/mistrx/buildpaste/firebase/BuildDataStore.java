/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.firebase;

import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import org.joml.Vector3d;

@Immutable
public class BuildDataStore {
    private final List<Object> blockIDs;
    private final List<String> blockData;
    private final JsonObject blockNBT;
    private final Vector3d size;
    private final String uploadDirection;

    public BuildDataStore(List<Object> blockIDs, List<String> blockData, JsonObject blockNBT, Vector3d size, String uploadDirection) {
        this.blockIDs = blockIDs;
        this.blockData = blockData;
        this.blockNBT = blockNBT;
        this.size = size;
        this.uploadDirection = uploadDirection;
    }

    public List<Object> getBlockIds() {
        return this.blockIDs;
    }

    public List<String> getBlockData() {
        return this.blockData;
    }

    public JsonObject getBlockNBT() {
        return this.blockNBT;
    }

    public Vector3d getSize() {
        return this.size;
    }

    public String getUploadDirection() {
        return this.uploadDirection;
    }
}

