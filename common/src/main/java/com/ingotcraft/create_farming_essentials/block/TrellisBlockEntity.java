package com.ingotcraft.create_farming_essentials.block;

import com.ingotcraft.create_farming_essentials.api.TrellisCrop;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import com.ingotcraft.create_farming_essentials.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

/** Remembers which crop (if any) is planted in this trellis, how far along it is, and where its fruit is. */
public class TrellisBlockEntity extends BlockEntity {
    @Nullable
    private ResourceLocation cropId;
    private int age;
    /** Side of the trellis a grown fruit block sits on, or null if there is none. */
    @Nullable
    private Direction fruitDirection;

    public TrellisBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRELLIS.get(), pos, state);
    }

    public boolean hasCrop() {
        return cropId != null;
    }

    @Nullable
    public ResourceLocation getCropId() {
        return cropId;
    }

    /** Empty if nothing is planted, or if the mod that added the crop is no longer loaded. */
    public Optional<TrellisCrop> getCrop() {
        return cropId == null ? Optional.empty() : TrellisCrops.get(cropId);
    }

    public int getAge() {
        return age;
    }

    public boolean isMature(TrellisCrop crop) {
        return age >= crop.maxAge();
    }

    @Nullable
    public Direction getFruitDirection() {
        return fruitDirection;
    }

    /** Plants a fresh crop at growth stage 0. */
    public void setCrop(ResourceLocation id) {
        this.cropId = id;
        this.age = 0;
        this.fruitDirection = null;
        markChangedAndSync();
    }

    public void setAge(int age) {
        this.age = Math.max(0, age);
        markChangedAndSync();
    }

    public void setFruitDirection(@Nullable Direction direction) {
        if (this.fruitDirection != direction) {
            this.fruitDirection = direction;
            markChangedAndSync();
        }
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (cropId != null) {
            tag.putString("crop", cropId.toString());
            tag.putInt("age", age);
            if (fruitDirection != null) {
                tag.putString("fruit_dir", fruitDirection.getName());
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        cropId = tag.contains("crop", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("crop")) : null;
        age = cropId != null ? Math.max(0, tag.getInt("age")) : 0;
        fruitDirection = cropId != null && tag.contains("fruit_dir", Tag.TAG_STRING)
                ? Direction.byName(tag.getString("fruit_dir")) : null;
    }

    // Sync the planted crop, its age and its fruit to clients so the renderer knows what to draw.
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
