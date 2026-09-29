package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Registry of crops that can be planted in a trellis.
 * Call one of the register methods from your mod's initializer, on both client and server,
 * so the client knows how to render each growth stage.
 */
public final class TrellisCrops {
    private static final Map<ResourceLocation, TrellisCrop> CROPS = new ConcurrentHashMap<>();

    /** Full control: you supply the model for every growth stage. See {@link TrellisCrop}. */
    public static TrellisCrop register(ResourceLocation id,
                                       Supplier<? extends ItemLike> seed,
                                       int maxAge,
                                       IntFunction<BlockState> stageState,
                                       Supplier<BlockState> harvestState) {
        return register(id, seed, maxAge, stageState, harvestState, null);
    }

    /** Same as above, plus an optional fruit block the mature crop grows beside the trellis. */
    public static TrellisCrop register(ResourceLocation id,
                                       Supplier<? extends ItemLike> seed,
                                       int maxAge,
                                       IntFunction<BlockState> stageState,
                                       Supplier<BlockState> harvestState,
                                       @Nullable TrellisFruit fruit) {
        TrellisCrop crop = new TrellisCrop(id, seed, maxAge, stageState, harvestState, fruit);
        if (CROPS.putIfAbsent(id, crop) != null) {
            throw new IllegalStateException("Trellis crop already registered: " + id);
        }
        return crop;
    }

    /**
     * Shortcut for crops whose growth is a single integer block-state property (vanilla crops and stems,
     * and most modded ones). Stage 0 is the property's lowest value, mature is its highest, and each
     * stage is drawn with that crop block's own model.
     */
    public static TrellisCrop registerAgeProperty(ResourceLocation id,
                                                  Supplier<? extends ItemLike> seed,
                                                  Supplier<? extends Block> cropBlock,
                                                  IntegerProperty ageProperty,
                                                  Supplier<BlockState> harvestState) {
        return registerAgeProperty(id, seed, cropBlock, ageProperty, harvestState, null);
    }

    /** Same as above, plus an optional fruit block. */
    public static TrellisCrop registerAgeProperty(ResourceLocation id,
                                                  Supplier<? extends ItemLike> seed,
                                                  Supplier<? extends Block> cropBlock,
                                                  IntegerProperty ageProperty,
                                                  Supplier<BlockState> harvestState,
                                                  @Nullable TrellisFruit fruit) {
        IntSummaryStatistics range = ageProperty.getPossibleValues().stream()
                .mapToInt(Integer::intValue).summaryStatistics();
        int min = range.getMin();
        return register(id, seed, range.getMax() - min,
                age -> cropBlock.get().defaultBlockState().setValue(ageProperty, min + age),
                harvestState, fruit);
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
