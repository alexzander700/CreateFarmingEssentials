package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/** Emits droplets from the two nozzle tips of the spinning head. Client only. */
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
        if (!(level instanceof ClientLevel clientLevel) || fluid == null
                || !be.isRunning() || be.getSpeed() < MIN_SPEED) {
            return;
        }

        FluidStack stack = new FluidStack(fluid, 1000);
        IClientFluidTypeExtensions fluidInfo = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation texture = fluidInfo.getStillTexture(stack);
        if (texture == null) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        int tint = fluidInfo.getTintColor(stack);

        RandomSource random = clientLevel.random;
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

                Minecraft.getInstance().particleEngine.add(new FluidSprayParticle(clientLevel,
                        pos.getX() + 0.5 + sx, pos.getY() + 0.5 + sy, pos.getZ() + 0.5 + sz,
                        dx, dy, dz, sprite, tint));
            }
        }
    }

    private SprinklerSpray() {}
}
