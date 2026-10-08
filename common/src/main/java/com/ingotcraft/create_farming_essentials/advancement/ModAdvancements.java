package com.ingotcraft.create_farming_essentials.advancement;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Awards this mod's advancements from code. Each advancement (data/create_farming_essentials/advancement)
 * uses the vanilla "impossible" trigger, which nothing can fire by itself, with one criterion named
 * {@link #CRITERION}; this class grants that criterion at the moment the thing actually happens.
 * Doing it this way needs no custom trigger registration, so it works the same on NeoForge and Fabric.
 */
public final class ModAdvancements {
    public static final ResourceLocation HOT_HOT_HOT = id("hot_hot_hot");
    public static final ResourceLocation GRIZZLYS_DREAM = id("grizzlys_dream");
    public static final ResourceLocation STICK_Y_SITUATION = id("stick_y_situation");

    private static final String CRITERION = "triggered";

    /** Server thread only. Does nothing if the player already has it, or the advancement isn't loaded. */
    public static void award(ServerPlayer player, ResourceLocation advancement) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AdvancementHolder holder = server.getAdvancements().get(advancement);
        if (holder != null) {
            player.getAdvancements().award(holder, CRITERION);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, path);
    }

    private ModAdvancements() {}
}
