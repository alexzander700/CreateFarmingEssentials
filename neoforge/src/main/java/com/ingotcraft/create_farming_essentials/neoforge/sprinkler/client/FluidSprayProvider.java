package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.FluidSprayOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Builds the Sprinkler's droplet for a fluid: its still texture and tint, exactly as the spray always used. */
public final class FluidSprayProvider implements ParticleProvider<FluidSprayOptions> {
    @Nullable
    @Override
    public Particle createParticle(FluidSprayOptions options, ClientLevel level,
                                   double x, double y, double z, double vx, double vy, double vz) {
        Fluid fluid = BuiltInRegistries.FLUID.get(options.fluid());
        if (fluid == Fluids.EMPTY) {
            return null;
        }
        FluidStack stack = new FluidStack(fluid, 1000);
        IClientFluidTypeExtensions info = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation texture = info.getStillTexture(stack);
        if (texture == null) {
            return null;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        return new FluidSprayParticle(level, x, y, z, vx, vy, vz, sprite, info.getTintColor(stack));
    }
}
