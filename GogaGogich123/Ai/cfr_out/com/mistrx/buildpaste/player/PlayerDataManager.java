/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.player;

import com.mistrx.buildpaste.player.PlayerData;
import java.util.HashMap;
import net.minecraft.world.entity.player.Player;

public class PlayerDataManager {
    private static HashMap<String, PlayerData> playerDataHashMap = new HashMap();

    public static PlayerData getOrCreatePlayerData(Player player) {
        String playerName = player.getName().getString();
        if (!playerDataHashMap.containsKey(playerName)) {
            PlayerData playerData = new PlayerData(player);
            playerDataHashMap.put(playerName, playerData);
            return playerData;
        }
        return playerDataHashMap.get(playerName);
    }
}

