package com.ingotcraft.create_farming_essentials.fabric;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import net.fabricmc.api.ModInitializer;

public final class Create_farming_essentialsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Create_farming_essentials.init();
    }
}
