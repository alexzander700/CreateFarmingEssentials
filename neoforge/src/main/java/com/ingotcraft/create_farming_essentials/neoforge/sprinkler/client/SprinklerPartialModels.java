package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

/** Models that are drawn separately from the block so they can be animated. */
public final class SprinklerPartialModels {
    public static final PartialModel HEAD = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, "block/sprinkler_head"));

    /** Call early (mod constructor) so the model is registered before models are baked. */
    public static void init() {}

    private SprinklerPartialModels() {}
}
