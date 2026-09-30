package com.ingotcraft.create_farming_essentials.registry;

import com.ingotcraft.create_farming_essentials.api.SprinklerContext;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffect;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffects;
import net.minecraft.core.BlockPos;
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

    /**
     * Chance, per farmland block per sprinkler pass (every 10 ticks), of gaining one level of moisture.
     * 0.1 means about one level per 5 seconds, so dry farmland takes roughly 35 seconds to become fully wet.
     */
    private static final float HYDRATION_CHANCE = 0.1F;

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
