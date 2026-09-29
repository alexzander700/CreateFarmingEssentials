package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * A crop that can be planted inside a trellis.
 *
 * @param id           unique id, saved in the world. Use your own namespace and don't change it later.
 * @param seed         the item that plants this crop (also returned when the trellis is broken)
 * @param maxAge       the last growth stage. Stages run from 0 to maxAge; the crop is mature at maxAge.
 * @param stageState   the block state whose model is drawn inside the trellis at a given stage (0..maxAge)
 * @param harvestState the block state whose loot table is rolled when a MATURE trellis is broken.
 *                     The roll is made as if the tool had one extra level of Fortune.
 * @param fruit        optional: a block the mature crop grows beside the trellis (null for none)
 *
 * Everything that touches game objects is a supplier or function so you can point at your own
 * registry objects before they exist.
 */
public record TrellisCrop(ResourceLocation id,
                          Supplier<? extends ItemLike> seed,
                          int maxAge,
                          IntFunction<BlockState> stageState,
                          Supplier<BlockState> harvestState,
                          @Nullable TrellisFruit fruit) {

    public TrellisCrop {
        if (maxAge < 0) {
            throw new IllegalArgumentException("maxAge must be >= 0 for trellis crop " + id);
        }
    }

    /** A crop with no fruit block. */
    public TrellisCrop(ResourceLocation id, Supplier<? extends ItemLike> seed, int maxAge,
                       IntFunction<BlockState> stageState, Supplier<BlockState> harvestState) {
        this(id, seed, maxAge, stageState, harvestState, null);
    }

    public boolean isSeed(ItemStack stack) {
        return stack.is(seed.get().asItem());
    }

    public ItemStack seedStack() {
        return new ItemStack(seed.get());
    }

    /** The state to render at this age; out-of-range ages are clamped. */
    public BlockState stateForAge(int age) {
        return stageState.apply(Mth.clamp(age, 0, maxAge));
    }

    /** What to draw inside the trellis right now, taking an attached fruit into account. */
    public BlockState displayState(int age, @Nullable Direction fruitDirection) {
        if (fruitDirection != null && fruit != null && fruit.attachedDisplay() != null) {
            return fruit.attachedDisplay().apply(fruitDirection);
        }
        return stateForAge(age);
    }
}
