package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The positions a sprinkler reaches: a 9x9x9 box, centred horizontally on the sprinkler, that spreads out
 * from the sprinkler through any block that is not a full solid block. Full blocks stop the spread, so
 * nothing behind a wall is reached. Unloaded chunks also stop it.
 *
 * Vertically: a ceiling-mounted sprinkler is level with the TOP of the box (the box extends 8 layers below
 * it); a floor-mounted sprinkler has the BOTTOM of the box one layer below it (so farmland at the height of
 * the pipe underneath is covered), extending 7 layers above it.
 */
public final class SprinklerArea {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 9;
    /** Floor-mounted: how many layers of the box lie below the sprinkler's own layer. */
    public static final int FLOOR_LAYERS_BELOW = 1;
    /** Ceiling-mounted: how many layers of the box lie above the sprinkler's own layer. */
    public static final int CEILING_LAYERS_ABOVE = 0;

    private final AABB bounds;
    private final Set<BlockPos> positions;

    private SprinklerArea(AABB bounds, Set<BlockPos> positions) {
        this.bounds = bounds;
        this.positions = Collections.unmodifiableSet(positions);
    }

    public static SprinklerArea compute(Level level, BlockPos origin, boolean hangingFromCeiling) {
        int radius = WIDTH / 2;
        int minX = origin.getX() - radius, maxX = origin.getX() + radius;
        int minZ = origin.getZ() - radius, maxZ = origin.getZ() + radius;
        int minY, maxY;
        if (hangingFromCeiling) {
            maxY = origin.getY() + CEILING_LAYERS_ABOVE;
            minY = maxY - (HEIGHT - 1);
        } else {
            minY = origin.getY() - FLOOR_LAYERS_BELOW;
            maxY = minY + (HEIGHT - 1);
        }

        Set<BlockPos> reached = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockPos start = origin.immutable();
        reached.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (next.getX() < minX || next.getX() > maxX
                        || next.getY() < minY || next.getY() > maxY
                        || next.getZ() < minZ || next.getZ() > maxZ
                        || level.isOutsideBuildHeight(next)
                        || !level.isLoaded(next)
                        || reached.contains(next)) {
                    continue;
                }
                BlockState state = level.getBlockState(next);
                // Farmland, crops, slabs, pipes... let the spray through. Full solid blocks stop it.
                if (state.isCollisionShapeFullBlock(level, next)) {
                    continue;
                }
                reached.add(next);
                queue.add(next);
            }
        }

        AABB box = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        return new SprinklerArea(box, reached);
    }

    /** The full 9x9x9 box, ignoring walls. Use {@link #contains} to respect them. */
    public AABB bounds() {
        return bounds;
    }

    /** Every reached position (the sprinkler's own included). */
    public Set<BlockPos> positions() {
        return positions;
    }

    public boolean contains(BlockPos pos) {
        return positions.contains(pos);
    }

    /** Living things (mobs and players) standing in a reached position. */
    public List<LivingEntity> livingEntities(Level level) {
        return level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> contains(entity.blockPosition()));
    }
}
