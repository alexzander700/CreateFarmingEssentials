package com.ingotcraft.create_farming_essentials.block;

import com.ingotcraft.create_farming_essentials.api.TrellisCrop;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import com.ingotcraft.create_farming_essentials.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Remembers which crop (if any) is planted in this trellis. Only the crop's id is stored. */
public class TrellisBlockEntity extends BlockEntity {
    @Nullable
    private ResourceLocation cropId;

    public TrellisBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRELLIS.get(), pos, state);
    }

    public boolean hasCrop() {
        return cropId != null;
    }

    /** Empty if nothing is planted, or if the mod that added the crop is no longer loaded. */
    public Optional<TrellisCrop> getCrop() {
        return cropId == null ? Optional.empty() : TrellisCrops.get(cropId);
    }

    public void setCrop(ResourceLocation id) {
        this.cropId = id;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (cropId != null) {
            tag.putString("crop", cropId.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        cropId = tag.contains("crop", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("crop")) : null;
    }

    // Sync the planted crop to clients so the renderer knows what to draw.
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
