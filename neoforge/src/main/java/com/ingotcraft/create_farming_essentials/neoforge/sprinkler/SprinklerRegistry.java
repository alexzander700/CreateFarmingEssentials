package com.ingotcraft.create_farming_essentials.neoforge.sprinkler;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Sprinkler registrations. Lives in the neoforge module because the fluid handler capability
 * (and later the Flywheel visual) are NeoForge/Create specific. Port this when doing Fabric.
 */
public final class SprinklerRegistry {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.BLOCK);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.ITEM);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<Block> SPRINKLER = BLOCKS.register("sprinkler", () ->
            new SprinklerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(1.5F)
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final RegistrySupplier<Item> SPRINKLER_ITEM = ITEMS.register("sprinkler", () ->
            new BlockItem(SPRINKLER.get(), new Item.Properties()));

    public static final RegistrySupplier<BlockEntityType<SprinklerBlockEntity>> SPRINKLER_BE =
            BLOCK_ENTITIES.register("sprinkler", () ->
                    BlockEntityType.Builder.of(SprinklerBlockEntity::new, SPRINKLER.get()).build(null));

    /** The Sprinkler's droplet. See {@link FluidSprayOptions}. */
    public static final RegistrySupplier<ParticleType<FluidSprayOptions>> FLUID_SPRAY =
            PARTICLE_TYPES.register("fluid_spray", FluidSprayOptions.Type::new);

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITIES.register();
        PARTICLE_TYPES.register();
        CreativeTabRegistry.append(CreativeModeTabs.FUNCTIONAL_BLOCKS, SPRINKLER_ITEM);
    }

    /** Lets Create's pipes (and any other fluid mod) push fluid into the sprinkler. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, SPRINKLER_BE.get(),
                (be, side) -> be.getFluidHandler(side));
    }

    private SprinklerRegistry() {}
}
