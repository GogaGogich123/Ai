/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.player;

import com.mistrx.buildpaste.firebase.UndoBuildDataStore;
import com.mistrx.buildpaste.player.UploadingDataStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

@Immutable
public class PlayerData {
    private Player player;
    private String mcname = "";
    private String uuid;
    private String selectedBuildId = "";
    private String uploadedBuildID = "";
    private UndoBuildDataStore undoBuildData;
    private UploadingDataStore uploadingDataStore;
    private Vector3d lastFirstPos;
    private Vector3d lastSecondPos;
    private ArrayList<String> blocksInBuild = new ArrayList();
    private HashMap<String, Integer> playerItems = new HashMap();
    private HashMap<String, Integer> buildBlocks = new HashMap();
    private String userDataResponseString = "";
    private int incompatibleBlocksAmount = 0;
    private List<String> incompatibleBlocksExampleBlocksReplacedArray = new ArrayList<String>();

    public PlayerData(Player player) {
        String uuid = player.getStringUUID();
        this.player = player;
        this.mcname = player.getName().getString();
        this.uuid = uuid;
        this.selectedBuildId = "";
        this.uploadedBuildID = "";
        this.undoBuildData = null;
        this.uploadingDataStore = new UploadingDataStore();
        this.lastFirstPos = null;
        this.lastSecondPos = null;
        this.blocksInBuild = new ArrayList();
        this.playerItems = new HashMap();
        this.buildBlocks = new HashMap();
        this.userDataResponseString = "";
        this.incompatibleBlocksAmount = 0;
        this.incompatibleBlocksExampleBlocksReplacedArray = new ArrayList<String>();
    }

    public UploadingDataStore getUploadingDataStore() {
        return this.uploadingDataStore;
    }

    public String getMcname() {
        return this.mcname;
    }

    public void setMcname(String mcname) {
        this.mcname = mcname;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSelectedBuildId() {
        return this.selectedBuildId;
    }

    public void setSelectedBuildId(String selectedBuildId) {
        this.selectedBuildId = selectedBuildId;
    }

    public String getUploadedBuildID() {
        return this.uploadedBuildID;
    }

    public void setUploadedBuildID(String uploadedBuildID) {
        this.uploadedBuildID = uploadedBuildID;
    }

    public UndoBuildDataStore getUndoBuildData() {
        return this.undoBuildData;
    }

    public void setUndoBuildData(UndoBuildDataStore undoBuildData) {
        this.undoBuildData = undoBuildData;
    }

    public Vector3d getLastFirstPos() {
        return this.lastFirstPos;
    }

    public void setLastFirstPos(Vector3d lastFirstPos) {
        this.lastFirstPos = lastFirstPos;
    }

    public Vector3d getLastSecondPos() {
        return this.lastSecondPos;
    }

    public void setLastSecondPos(Vector3d lastSecondPos) {
        this.lastSecondPos = lastSecondPos;
    }

    public ArrayList<String> getBlocksInBuild() {
        return this.blocksInBuild;
    }

    public void setBlocksInBuild(ArrayList<String> blocksInBuild) {
        this.blocksInBuild = blocksInBuild;
    }

    public HashMap<String, Integer> getPlayerItems() {
        return this.playerItems;
    }

    public void setPlayerItems(HashMap<String, Integer> playerItems) {
        this.playerItems = playerItems;
    }

    public HashMap<String, Integer> getBuildBlocks() {
        return this.buildBlocks;
    }

    public void setBuildBlocks(HashMap<String, Integer> buildBlocks) {
        this.buildBlocks = buildBlocks;
    }

    public String getUserDataResponseString() {
        return this.userDataResponseString;
    }

    public void setUserDataResponseString(String userDataResponseString) {
        this.userDataResponseString = userDataResponseString;
    }

    public int getIncompatibleBlocksAmount() {
        return this.incompatibleBlocksAmount;
    }

    public void setIncompatibleBlocksAmount(int incompatibleBlocksAmount) {
        this.incompatibleBlocksAmount = incompatibleBlocksAmount;
    }

    public List<String> getIncompatibleBlocksExampleBlocksReplacedArray() {
        return this.incompatibleBlocksExampleBlocksReplacedArray;
    }

    public void setIncompatibleBlocksExampleBlocksReplacedArray(List<String> incompatibleBlocksExampleBlocksReplacedArray) {
        this.incompatibleBlocksExampleBlocksReplacedArray = incompatibleBlocksExampleBlocksReplacedArray;
    }
}

