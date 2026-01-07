/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Raycast {
    public static BlockHitResult getHitBlock(Level pLevel, Player pPlayer, ClipContext.Fluid pFluidMode, double reach) {
        float f = pPlayer.getXRot();
        float f1 = pPlayer.getYRot();
        Vec3 vec3 = pPlayer.getEyePosition();
        float f2 = Mth.cos((float)(-f1 * ((float)Math.PI / 180) - (float)Math.PI));
        float f3 = Mth.sin((float)(-f1 * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = -Mth.cos((float)(-f * ((float)Math.PI / 180)));
        float f5 = Mth.sin((float)(-f * ((float)Math.PI / 180)));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double)f6 * reach, (double)f5 * reach, (double)f7 * reach);
        BlockHitResult hitResult = pLevel.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, pFluidMode, (Entity)pPlayer));
        return hitResult;
    }
}

