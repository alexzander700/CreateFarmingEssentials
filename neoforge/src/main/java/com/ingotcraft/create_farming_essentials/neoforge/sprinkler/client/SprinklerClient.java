package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only wiring. Only ever called when running on the client dist. */
public final class SprinklerClient {
    public static void init(IEventBus modBus) {
        SprinklerPartialModels.init();
        modBus.addListener(SprinklerClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(SprinklerRegistry.SPRINKLER_BE.get(), SprinklerRenderer::new);
            SimpleBlockEntityVisualizer.builder(SprinklerRegistry.SPRINKLER_BE.get())
                    .factory(SprinklerVisual::new)
                    .skipVanillaRender(be -> true)
                    .apply();
        });
    }

    private SprinklerClient() {}
}
