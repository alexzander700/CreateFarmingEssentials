package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A droplet that wears a small random patch of the fluid's own texture, like block-break debris does
 * for blocks. Works for any fluid because it never needs a registered particle type.
 */
public class FluidSprayParticle extends TextureSheetParticle {
    private final float uo;
    private final float vo;

    public FluidSprayParticle(ClientLevel level, double x, double y, double z,
                              double vx, double vy, double vz, TextureAtlasSprite sprite, int argbTint) {
        super(level, x, y, z);
        setSprite(sprite);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.gravity = 0.9F;
        this.hasPhysics = true;
        this.lifetime = 25 + random.nextInt(15);
        this.quadSize = 0.04F + random.nextFloat() * 0.03F;
        this.rCol = ((argbTint >> 16) & 0xFF) / 255.0F;
        this.gCol = ((argbTint >> 8) & 0xFF) / 255.0F;
        this.bCol = (argbTint & 0xFF) / 255.0F;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override protected float getU0() { return sprite.getU(uo / 4.0F); }
    @Override protected float getU1() { return sprite.getU((uo + 1.0F) / 4.0F); }
    @Override protected float getV0() { return sprite.getV(vo / 4.0F); }
    @Override protected float getV1() { return sprite.getV((vo + 1.0F) / 4.0F); }
}
