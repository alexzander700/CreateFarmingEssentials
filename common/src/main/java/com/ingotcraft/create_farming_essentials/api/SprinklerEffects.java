package com.ingotcraft.create_farming_essentials.api;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Registry of sprinkler effects. Call {@link #register} from your mod's initializer. */
public final class SprinklerEffects {
    /** How often a running sprinkler applies its effects. */
    public static final int INTERVAL_TICKS = 10;

    private static final List<SprinklerEffect> EFFECTS = new CopyOnWriteArrayList<>();

    public static void register(SprinklerEffect effect) {
        EFFECTS.add(effect);
    }

    public static List<SprinklerEffect> all() {
        return Collections.unmodifiableList(EFFECTS);
    }

    private SprinklerEffects() {}
}
