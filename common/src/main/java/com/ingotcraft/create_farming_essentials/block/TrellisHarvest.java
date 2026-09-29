package com.ingotcraft.create_farming_essentials.block;

import com.ingotcraft.create_farming_essentials.api.TrellisCrop;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/** Rolls a mature crop's yield as if the breaking tool had one more level of Fortune. */
final class TrellisHarvest {
    static List<ItemStack> roll(TrellisCrop crop, LootParams.Builder params) {
        ServerLevel level = params.getLevel();

        // Copy the real tool so silk touch etc. still apply. An empty hand can't hold
        // enchantments, so a throwaway stick stands in for it.
        ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
        ItemStack boosted = (tool == null || tool.isEmpty()) ? new ItemStack(Items.STICK) : tool.copy();

        Holder<Enchantment> fortune = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);
        boosted.enchant(fortune, EnchantmentHelper.getItemEnchantmentLevel(fortune, boosted) + 1);

        LootParams.Builder harvestParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, params.getParameter(LootContextParams.ORIGIN))
                .withParameter(LootContextParams.TOOL, boosted)
                .withOptionalParameter(LootContextParams.THIS_ENTITY,
                        params.getOptionalParameter(LootContextParams.THIS_ENTITY))
                .withOptionalParameter(LootContextParams.EXPLOSION_RADIUS,
                        params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS));

        // BlockState#getDrops adds BLOCK_STATE itself and rolls that block's loot table.
        return crop.harvestState().get().getDrops(harvestParams);
    }

    private TrellisHarvest() {}
}
