/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste.events;

import com.mistrx.buildpaste.BuildPasteMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid="buildpaste")
public class ModEvents {
    @SubscribeEvent
    public static void onRegisterCustomItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept((ItemLike)BuildPasteMod.POSITION_SELECTOR.asItem());
            event.accept((ItemLike)BuildPasteMod.BUILD_PLACER.asItem());
        }
    }
}

