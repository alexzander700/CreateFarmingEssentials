package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.block.TrellisBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Create_farming_essentials.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<TrellisBlockEntity>> TRELLIS =
            BLOCK_ENTITIES.register("trellis", () ->
                    BlockEntityType.Builder.of(TrellisBlockEntity::new, ModBlocks.TRELLIS.get()).build(null));

    public static void register() {
        BLOCK_ENTITIES.register();
    }

    private ModBlockEntities() {}
}
