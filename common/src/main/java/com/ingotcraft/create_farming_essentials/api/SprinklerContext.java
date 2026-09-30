package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;

/** Everything an effect needs for one pass: which sprinkler, which fluid, and the area it reaches. */
public record SprinklerContext(ServerLevel level, BlockPos sprinklerPos, Fluid fluid, SprinklerArea area) {}
