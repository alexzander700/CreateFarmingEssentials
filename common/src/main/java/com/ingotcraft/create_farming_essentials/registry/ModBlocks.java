package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.block.TrellisBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> TRELLIS = BLOCKS.register("trellis", () ->
            new TrellisBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instabreak()     // breaks instantly by hand, same as crops
                    .sound(SoundType.WOOD)
                    .noCollission()   // walk-through, like crops
                    .noOcclusion()    // neighbouring blocks still render their faces
                    .pushReaction(PushReaction.DESTROY)));

    public static void register() {
        BLOCKS.register();
    }

    private ModBlocks() {}
}
