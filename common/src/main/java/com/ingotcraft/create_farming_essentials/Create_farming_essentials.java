package com.ingotcraft.create_farming_essentials;

import com.ingotcraft.create_farming_essentials.client.TrellisClient;
import com.ingotcraft.create_farming_essentials.event.TrellisPlanting;
import com.ingotcraft.create_farming_essentials.registry.ModBlockEntities;
import com.ingotcraft.create_farming_essentials.registry.ModBlocks;
import com.ingotcraft.create_farming_essentials.registry.VanillaTrellisCrops;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;

public final class Create_farming_essentials {
    public static final String MOD_ID = "create_farming_essentials";

    public static void init() {
        ModBlocks.register();
        ModBlockEntities.register();
        VanillaTrellisCrops.register();
        TrellisPlanting.register();

        EnvExecutor.runInEnv(Env.CLIENT, () -> TrellisClient::init);
    }
}
