/*
 * Decompiled with CFR.
 */
package com.mistrx.buildpaste;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid="buildpaste")
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue SHOW_ENTRY_MESSAGE = BUILDER.comment("Show a message showcasing how to paste a build when entering a world").define("showEntryMessage", true);
    public static final ModConfigSpec.IntValue FIRST_TIME_USING_BUILDPASTE = BUILDER.comment("Stores the times the player joined using Buildpaste. This data is NOT sent to a server but instead only to show a Dismiss message for the default text message when joining").defineInRange("joinCounter", 0, 0, 1000000);
    static final ModConfigSpec SPEC = BUILDER.build();
    public static boolean showEntryMessage;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        showEntryMessage = (Boolean)SHOW_ENTRY_MESSAGE.get();
    }
}

