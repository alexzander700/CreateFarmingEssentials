package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry of crops that can be planted in a trellis.
 * Call {@link #register} from your mod's initializer, on both client and server,
 * so the client knows how to render the crop.
 */
public final class TrellisCrops {
    private static final Map<ResourceLocation, TrellisCrop> CROPS = new ConcurrentHashMap<>();

    public static TrellisCrop register(ResourceLocation id,
                                       Supplier<? extends ItemLike> seed,
                                       Supplier<BlockState> displayState) {
        TrellisCrop crop = new TrellisCrop(id, seed, displayState);
        if (CROPS.putIfAbsent(id, crop) != null) {
            throw new IllegalStateException("Trellis crop already registered: " + id);
        }
        return crop;
    }

    public static Optional<TrellisCrop> get(ResourceLocation id) {
        return Optional.ofNullable(CROPS.get(id));
    }

    /** Finds the crop planted by this item, if any. */
    public static Optional<TrellisCrop> findBySeed(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return CROPS.values().stream().filter(crop -> crop.isSeed(stack)).findFirst();
    }

    public static Collection<TrellisCrop> all() {
        return Collections.unmodifiableCollection(CROPS.values());
    }

    private TrellisCrops() {}
}
