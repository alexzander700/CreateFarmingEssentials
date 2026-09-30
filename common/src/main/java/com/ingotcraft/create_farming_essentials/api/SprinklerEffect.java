package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.world.level.material.Fluid;

/**
 * Something a sprinkler does with a particular fluid. Register with {@link SprinklerEffects#register}.
 * {@link #apply} runs on the server roughly every {@value SprinklerEffects#INTERVAL_TICKS} ticks per running
 * sprinkler, so keep it cheap and make it safe to repeat (set a timer, don't stack).
 */
public interface SprinklerEffect {
    /** Does this effect react to this fluid? Tags and registry ids both work; sources and flowing are normalised. */
    boolean appliesTo(Fluid fluid);

    void apply(SprinklerContext context);
}
