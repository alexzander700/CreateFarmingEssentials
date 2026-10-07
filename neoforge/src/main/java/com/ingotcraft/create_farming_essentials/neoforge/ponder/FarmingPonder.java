package com.ingotcraft.create_farming_essentials.neoforge.ponder;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Client-only wiring for this mod's Ponder scenes. Only ever called on the client dist. */
public final class FarmingPonder {
    public static void init(IEventBus modBus) {
        modBus.addListener(FarmingPonder::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new Plugin()));
    }

    private static final class Plugin implements PonderPlugin {
        @Override
        public String getModId() {
            return Create_farming_essentials.MOD_ID;
        }

        @Override
        public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
            // Attached to the vanilla Stick: hovering it in an inventory offers "Hold [W] to Ponder".
            helper.forComponents(ResourceLocation.withDefaultNamespace("stick"))
                    .addStoryBoard("trellis_planting", TrellisScenes::planting)
                    .addStoryBoard("trellis_harvest", TrellisScenes::harvest);

            helper.forComponents(ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, "sprinkler"))
                    .addStoryBoard("sprinkler_placement", SprinklerScenes::placement)
                    .addStoryBoard("sprinkler_water", SprinklerScenes::water)
                    .addStoryBoard("sprinkler_creatures", SprinklerScenes::creatures);
        }
    }

    private FarmingPonder() {}
}
