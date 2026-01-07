/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.firebase;

import com.google.gson.JsonObject;
import com.mistrx.buildpaste.firebase.BuildDataStore;
import java.util.List;
import org.joml.Vector3d;

public class UndoBuildDataStore {
    private List<Object> lastBlockIDs;
    private List<String> lastBlockData;
    private JsonObject lastNBT;
    private Vector3d lastBuildSize;
    private String lastUploadDirection;
    private String lastDirection;
    private Vector3d lastPastePlayerPos;

    public UndoBuildDataStore(BuildDataStore buildDataStore, String lastPasteDirection, Vector3d lastPastePos) {
        this.lastBlockIDs = buildDataStore.getBlockIds();
        this.lastBlockData = buildDataStore.getBlockData();
        this.lastNBT = buildDataStore.getBlockNBT();
        this.lastBuildSize = buildDataStore.getSize();
        this.lastUploadDirection = buildDataStore.getUploadDirection();
        this.lastDirection = lastPasteDirection;
        this.lastPastePlayerPos = lastPastePos;
    }

    public BuildDataStore toBuildDataStore() {
        return new BuildDataStore(this.lastBlockIDs, this.lastBlockData, this.lastNBT, this.lastBuildSize, this.lastUploadDirection);
    }

    public List<Object> getLastBlockIDs() {
        return this.lastBlockIDs;
    }

    public void setLastBlockIDs(List<Object> lastBlockIDs) {
        this.lastBlockIDs = lastBlockIDs;
    }

    public List<String> getLastBlockData() {
        return this.lastBlockData;
    }

    public void setLastBlockData(List<String> lastBlockData) {
        this.lastBlockData = lastBlockData;
    }

    public JsonObject getLastNBT() {
        return this.lastNBT;
    }

    public void setLastNBT(JsonObject lastNBT) {
        this.lastNBT = lastNBT;
    }

    public Vector3d getLastBuildSize() {
        return this.lastBuildSize;
    }

    public void setLastBuildSize(Vector3d lastBuildSize) {
        this.lastBuildSize = lastBuildSize;
    }

    public String getLastUploadDirection() {
        return this.lastUploadDirection;
    }

    public void setLastUploadDirection(String lastUploadDirection) {
        this.lastUploadDirection = lastUploadDirection;
    }

    public String getLastDirection() {
        return this.lastDirection;
    }

    public void setLastDirection(String lastDirection) {
        this.lastDirection = lastDirection;
    }

    public Vector3d getLastPastePlayerPos() {
        return this.lastPastePlayerPos;
    }

    public void setLastPastePlayerPos(Vector3d lastPastePlayerPos) {
        this.lastPastePlayerPos = lastPastePlayerPos;
    }
}

