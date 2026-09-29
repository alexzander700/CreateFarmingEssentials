package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Optional extra for stem-style crops (melon, pumpkin, or your own). Once the crop is mature it
 * occasionally places {@code fruitState} in a free spot next to the trellis, exactly like a vanilla
 * stem does, and won't make another until that fruit is gone.
 *
 * @param fruitState      the block placed beside the trellis
 * @param attachedDisplay what to draw inside the trellis while the fruit exists, given the direction
 *                        the fruit is in (vanilla draws the bent "attached stem"). May be null to keep
 *                        drawing the mature stage.
 */
public record TrellisFruit(Supplier<BlockState> fruitState,
                           @Nullable Function<Direction, BlockState> attachedDisplay) {

    public TrellisFruit(Supplier<BlockState> fruitState) {
        this(fruitState, null);
    }
}
