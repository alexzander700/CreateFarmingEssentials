package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * A crop that can be planted inside a trellis.
 *
 * @param id           unique id, saved in the world. Use your own namespace and don't change it later.
 * @param seed         the item that plants this crop (also dropped when the trellis is broken)
 * @param displayState the block state whose model is drawn inside the trellis (placeholder visuals)
 *
 * Both are suppliers so you can point at your own registry objects before they exist.
 */
public record TrellisCrop(ResourceLocation id,
                          Supplier<? extends ItemLike> seed,
                          Supplier<BlockState> displayState) {

    public boolean isSeed(ItemStack stack) {
        return stack.is(seed.get().asItem());
    }

    public ItemStack seedStack() {
        return new ItemStack(seed.get());
    }
}
