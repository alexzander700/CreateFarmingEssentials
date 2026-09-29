package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import com.ingotcraft.create_farming_essentials.api.TrellisFruit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;

/**
 * The crops this mod ships with. They go through the same public API other mods use.
 * Only vine-type crops that genuinely benefit from a trellis are included.
 */
public final class VanillaTrellisCrops {
    public static void register() {
        // All 8 vanilla stem stages (0-7). Breaking a mature trellis rolls the mature STEM's loot
        // table (seeds); the fruit itself is grown as a block beside the trellis, like a vanilla stem.
        TrellisCrops.registerAgeProperty(id("melon"), () -> Items.MELON_SEEDS,
                () -> Blocks.MELON_STEM, StemBlock.AGE,
                () -> Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE),
                new TrellisFruit(
                        () -> Blocks.MELON.defaultBlockState(),
                        dir -> Blocks.ATTACHED_MELON_STEM.defaultBlockState()
                                .setValue(HorizontalDirectionalBlock.FACING, dir)));
        TrellisCrops.registerAgeProperty(id("pumpkin"), () -> Items.PUMPKIN_SEEDS,
                () -> Blocks.PUMPKIN_STEM, StemBlock.AGE,
                () -> Blocks.PUMPKIN_STEM.defaultBlockState().setValue(StemBlock.AGE, StemBlock.MAX_AGE),
                new TrellisFruit(
                        () -> Blocks.PUMPKIN.defaultBlockState(),
                        dir -> Blocks.ATTACHED_PUMPKIN_STEM.defaultBlockState()
                                .setValue(HorizontalDirectionalBlock.FACING, dir)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, path);
    }

    private VanillaTrellisCrops() {}
}
