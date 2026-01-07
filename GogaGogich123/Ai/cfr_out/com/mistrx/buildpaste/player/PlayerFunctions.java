/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.player;

import net.minecraft.world.entity.player.Player;

public class PlayerFunctions {
    public static Boolean uuidMessageSent = false;

    public static Boolean hasInvalidUUID(Player player) {
        if (player.getStringUUID().equals("00000000-0000-0000-0000-000000000000") && !uuidMessageSent.booleanValue()) {
            uuidMessageSent = true;
            return true;
        }
        return false;
    }
}

