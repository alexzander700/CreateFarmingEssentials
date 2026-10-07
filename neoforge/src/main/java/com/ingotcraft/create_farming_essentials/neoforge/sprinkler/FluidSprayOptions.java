package com.ingotcraft.create_farming_essentials.neoforge.sprinkler;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * "A droplet of this fluid." Carrying the fluid's id lets any code that can call Level#addParticle (the
 * real client, and Ponder's level) ask for the Sprinkler's spray, and the client-side provider turns it
 * into a FluidSprayParticle wearing that fluid's texture. Safe to load on a server: no client classes here.
 */
public record FluidSprayOptions(ResourceLocation fluid) implements ParticleOptions {
    public static final MapCodec<FluidSprayOptions> CODEC =
            ResourceLocation.CODEC.fieldOf("fluid").xmap(FluidSprayOptions::new, FluidSprayOptions::fluid);
    public static final StreamCodec<ByteBuf, FluidSprayOptions> STREAM_CODEC =
            ResourceLocation.STREAM_CODEC.map(FluidSprayOptions::new, FluidSprayOptions::fluid);

    @Override
    public ParticleType<?> getType() {
        return SprinklerRegistry.FLUID_SPRAY.get();
    }

    /** The registered particle type. Never needs a sprite json: the texture comes from the fluid. */
    public static final class Type extends ParticleType<FluidSprayOptions> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<FluidSprayOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FluidSprayOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
