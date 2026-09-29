package com.ingotcraft.create_farming_essentials.client;

import com.ingotcraft.create_farming_essentials.registry.ModBlockEntities;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;

/** Client-only setup. Only ever loaded through EnvExecutor, so servers never touch it. */
public final class TrellisClient {
    public static void init() {
        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft ->
                BlockEntityRendererRegistry.register(ModBlockEntities.TRELLIS.get(), TrellisBlockEntityRenderer::new));
    }

    private TrellisClient() {}
}
