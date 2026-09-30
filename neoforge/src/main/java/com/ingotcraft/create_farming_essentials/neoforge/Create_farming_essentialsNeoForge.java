package com.ingotcraft.create_farming_essentials.neoforge;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerRegistry;
import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client.SprinklerClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Create_farming_essentials.MOD_ID)
public final class Create_farming_essentialsNeoForge {
    public Create_farming_essentialsNeoForge(IEventBus modBus) {
        // Run our common setup.
        Create_farming_essentials.init();

        // NeoForge-only: the sprinkler (fluid capability + Flywheel visual).
        SprinklerRegistry.register();
        modBus.addListener(SprinklerRegistry::registerCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SprinklerClient.init(modBus);
        }
    }
}
