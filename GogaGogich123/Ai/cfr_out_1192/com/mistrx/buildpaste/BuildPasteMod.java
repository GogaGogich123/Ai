/*
 * Decompiled with CFR 0.152.
 */
package com.mistrx.buildpaste;

import com.mistrx.buildpaste.util.RegistryHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value="buildpaste")
public class BuildPasteMod {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "buildpaste";

    public BuildPasteMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        RegistryHandler.init();
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    private void setup(FMLCommonSetupEvent event) {
    }

    private void doClientStuff(FMLClientSetupEvent event) {
    }
}

