package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.api.SprinklerContext;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffect;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

/** The effects this mod ships with. They use the same public API other mods do. */
public final class VanillaSprinklerEffects {
    private static final TagKey<Fluid> HONEY_TAG =
            TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", "honey"));
    private static final ResourceLocation CREATE_HONEY = ResourceLocation.fromNamespaceAndPath("create", "honey");

    /** Fluids that act as fertilizer. Other mods add theirs to data/create_farming_essentials/tags/fluid/fertilizer.json. */
    private static final TagKey<Fluid> FERTILIZER_TAG = TagKey.create(Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, "fertilizer"));
    /** Blocks fertilizer can speed up: all crops, plus the trellis (which holds its crop in a block entity). */
    private static final TagKey<Block> GROWTH_BOOSTABLE_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, "growth_boostable"));

    /**
     * Chance, per farmland block per sprinkler pass (every 10 ticks), of gaining one level of moisture.
     * 0.1 means about one level per 5 seconds, so dry farmland takes roughly 35 seconds to become fully wet.
     */
    private static final float HYDRATION_CHANCE = 0.1F;

    /**
     * Chance, per boostable block per sprinkler pass (every 10 ticks), of an extra random tick.
     * Vanilla gives a block about 0.015 random ticks per second (randomTickSpeed 3), and this gives
     * 0.04 * 2 passes per second = 0.08 per second, so roughly 5x faster growth.
     */
    private static final float FERTILIZE_CHANCE = 0.04F;

    public static void register() {
        SprinklerEffects.register(new WaterEffect());

        // Lava: sets anything living in reach on fire (refreshed every pass, so it stays lit while inside).
        SprinklerEffects.register(new SprinklerEffect() {
            @Override
            public boolean appliesTo(Fluid fluid) {
                return fluid.is(FluidTags.LAVA);
            }

            @Override
            public void apply(SprinklerContext ctx) {
                for (LivingEntity entity : ctx.area().livingEntities(ctx.level())) {
                    entity.igniteForSeconds(5.0F);
                }
            }
        });

        // Honey: slows anything living in reach while it is inside.
        SprinklerEffects.register(new SprinklerEffect() {
            @Override
            public boolean appliesTo(Fluid fluid) {
                return fluid.is(HONEY_TAG) || CREATE_HONEY.equals(BuiltInRegistries.FLUID.getKey(fluid));
            }

            @Override
            public void apply(SprinklerContext ctx) {
                for (LivingEntity entity : ctx.area().livingEntities(ctx.level())) {
                    // Short duration, refreshed each pass: slowness ends soon after leaving the area.
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false, true));
                }
            }
        });

        // Fertilizer: gives every crop (and trellis) in reach extra random ticks, so it grows faster.
        SprinklerEffects.register(new FertilizerEffect());
    }

    /**
     * Fertilizer: occasionally runs a block's own random tick, which is exactly what makes it grow normally.
     * That means each block keeps its own rules (light level, growth roll, farmland bonus, a trellis's
     * double growth attempts and its fruit), we just give it more chances to use them.
     */
    private static final class FertilizerEffect implements SprinklerEffect {
        @Override
        public boolean appliesTo(Fluid fluid) {
            return fluid.is(FERTILIZER_TAG);
        }

        @Override
        public void apply(SprinklerContext ctx) {
            ServerLevel level = ctx.level();
            RandomSource random = level.random;

            for (BlockPos pos : ctx.area().positions()) {
                if (random.nextFloat() >= FERTILIZE_CHANCE) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.is(GROWTH_BOOSTABLE_TAG)) {
                    continue;
                }
                state.randomTick(level, pos, random);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.25, 0.25, 0.25, 0.0);
            }
        }
    }

    /** Water: waters farmland gradually, puts out fires (blocks and burning creatures), and hurts water-sensitive mobs. */
    private static final class WaterEffect implements SprinklerEffect {
        @Override
        public boolean appliesTo(Fluid fluid) {
            return fluid.is(FluidTags.WATER);
        }

        @Override
        public void apply(SprinklerContext ctx) {
            ServerLevel level = ctx.level();
            RandomSource random = level.random;

            for (BlockPos pos : ctx.area().positions()) {
                BlockState state = level.getBlockState(pos);
                if (state.is(BlockTags.FIRE)) {
                    level.removeBlock(pos, false);
                    level.levelEvent(1009, pos, 0);          // vanilla "fire extinguished" hiss and smoke
                } else if (state.is(BlockTags.CAMPFIRES) && state.getValue(CampfireBlock.LIT)) {
                    level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), Block.UPDATE_ALL);
                    level.levelEvent(1009, pos, 0);
                } else if (state.is(Blocks.FARMLAND)) {
                    int moisture = state.getValue(FarmBlock.MOISTURE);
                    if (moisture < FarmBlock.MAX_MOISTURE && random.nextFloat() < HYDRATION_CHANCE) {
                        level.setBlock(pos, state.setValue(FarmBlock.MOISTURE, moisture + 1), Block.UPDATE_CLIENTS);
                    }
                }
            }

            for (LivingEntity entity : ctx.area().livingEntities(level)) {
                if (entity.isOnFire()) {
                    entity.clearFire();
                    entity.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F,
                            1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
                }
                // Blazes, endermen, snow golems and any modded mob that vanilla rain or water already hurts.
                // Same damage source as rain, so endermen also teleport away exactly as they do in rain.
                if (entity.isSensitiveToWater()) {
                    entity.hurt(level.damageSources().drown(), 1.0F);
                }
            }
        }
    }

    private VanillaSprinklerEffects() {}
}
