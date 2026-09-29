package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;

/**
 * The crops this mod ships with. They go through the same public API other mods use.
 * Only vine-type crops that genuinely benefit from a trellis are included.
 */
public final class VanillaTrellisCrops {
    public static void register() {
        // Placeholder visuals: the fully grown stem model.
        TrellisCrops.register(id("melon"), () -> Items.MELON_SEEDS,
                () -> Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE));
        TrellisCrops.register(id("pumpkin"), () -> Items.PUMPKIN_SEEDS,
                () -> Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, path);
    }

    private VanillaTrellisCrops() {}
}
