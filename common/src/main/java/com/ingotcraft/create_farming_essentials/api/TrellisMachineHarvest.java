package com.ingotcraft.create_farming_essentials.api;

import com.ingotcraft.create_farming_essentials.block.TrellisBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Lets automation (e.g. Create's Mechanical Harvester) treat a trellis like a crop.
 * <ul>
 *   <li>{@link #isReady}: is there a fully grown crop in the trellis at this position?</li>
 *   <li>{@link #markMachineBreak}: call right before the machine breaks the trellis using ordinary
 *       block-break loot. The trellis then drops the crop's seed and yield as usual but NOT its stick.</li>
 * </ul>
 * Server thread only. A mark applies to exactly one break, at that position, in the same game tick.
 */
public final class TrellisMachineHarvest {
    private record PendingBreak(ResourceKey<Level> dimension, BlockPos pos, long gameTime) {}

    private static PendingBreak pending;

    /** True if a crop is planted at this position and fully grown. */
    public static boolean isReady(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TrellisBlockEntity trellis
                && trellis.getCrop().map(trellis::isMature).orElse(false);
    }

    /** Call immediately before a machine breaks the trellis at {@code pos}. */
    public static void markMachineBreak(Level level, BlockPos pos) {
        pending = new PendingBreak(level.dimension(), pos.immutable(), level.getGameTime());
    }

    /**
     * Used by the trellis when it rolls its drops. Returns true if this break was marked as a machine
     * break, and clears the mark either way so it can never apply to a later break.
     */
    public static boolean consume(Level level, BlockPos pos) {
        PendingBreak mark = pending;
        pending = null;
        return mark != null
                && mark.dimension().equals(level.dimension())
                && mark.gameTime() == level.getGameTime()
                && mark.pos().equals(pos);
    }

    private TrellisMachineHarvest() {}
}
