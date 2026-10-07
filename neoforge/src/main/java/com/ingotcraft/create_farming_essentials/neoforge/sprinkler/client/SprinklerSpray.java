package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.FluidSprayOptions;
import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * Emits droplets from the two nozzle tips of the spinning head. Client only.
 * Droplets are requested with Level#addParticle (a FluidSprayOptions) rather than built here, so the same
 * code works in the real client level and in Ponder's level.
 */
public final class SprinklerSpray {
    /**
     * Nozzle tips from the Blockbench model, in pixels: x, y, z of the tip, then the direction it points
     * in (x, z) in the head's own frame. The left nozzle points +x, the right one -x.
     */
    private static final double[][] NOZZLES = {
            {11.0, 8.0, 4.5, 1.0, 0.0},
            {5.0, 8.0, 11.5, -1.0, 0.0},
    };
    private static final double OUTWARD_SPEED = 0.25;
    private static final double UPWARD_SPEED = 0.12;
    private static final float MIN_SPEED = 6.0F;

    public static void tick(SprinklerBlockEntity be) {
        Level level = be.getLevel();
        Fluid fluid = be.getFluid();
        if (level == null || fluid == null || !be.isRunning() || be.getSpeed() < MIN_SPEED) {
            return;
        }

        FluidSprayOptions options = new FluidSprayOptions(BuiltInRegistries.FLUID.getKey(fluid));

        RandomSource random = level.random;
        double radians = Math.toRadians(be.getAngle(1.0F));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        boolean top = be.isTop();
        BlockPos pos = be.getBlockPos();
        float fraction = be.getSpeed() / SprinklerBlockEntity.MAX_SPEED;

        for (double[] nozzle : NOZZLES) {
            int count = 1 + (random.nextFloat() < fraction ? 1 : 0);
            for (int i = 0; i < count; i++) {
                // Position relative to the block centre, in blocks, in the head's frame.
                double px = (nozzle[0] - 8.0) / 16.0;
                double py = (nozzle[1] - 8.0) / 16.0;
                double pz = (nozzle[2] - 8.0) / 16.0;
                // Velocity in the head's frame: outward along the nozzle, a bit upward, a little scatter.
                double vx = nozzle[3] * OUTWARD_SPEED + (random.nextDouble() - 0.5) * 0.05;
                double vy = UPWARD_SPEED + (random.nextDouble() - 0.5) * 0.04;
                double vz = nozzle[4] * OUTWARD_SPEED + (random.nextDouble() - 0.5) * 0.05;

                // Same order the visual uses: spin about Y, then flip about X for ceiling mounting.
                double sx = px * cos + pz * sin;
                double sz = -px * sin + pz * cos;
                double dx = vx * cos + vz * sin;
                double dz = -vx * sin + vz * cos;
                double sy = py;
                double dy = vy;
                if (top) {
                    sy = -sy; sz = -sz;
                    dy = -dy; dz = -dz;
                }

                level.addParticle(options,
                        pos.getX() + 0.5 + sx, pos.getY() + 0.5 + sy, pos.getZ() + 0.5 + sz,
                        dx, dy, dz);
            }
        }
    }

    private SprinklerSpray() {}
}
