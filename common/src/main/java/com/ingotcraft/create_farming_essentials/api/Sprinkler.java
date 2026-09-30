package com.ingotcraft.create_farming_essentials.api;

import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * What other mods can see of a sprinkler: {@code level.getBlockEntity(pos) instanceof Sprinkler s}.
 * Server thread for {@link #getArea()}.
 */
public interface Sprinkler {
    /** True while fluid is arriving (or arrived within the last few ticks). */
    boolean isRunning();

    /** The fluid it most recently received, or null if none yet. Only meaningful while {@link #isRunning()}. */
    @Nullable
    Fluid getFluid();

    /** The 9x9x9 region this sprinkler currently reaches, with walls taken into account. Briefly cached. */
    SprinklerArea getArea();
}
