package com.ingotcraft.create_farming_essentials.block;

import com.ingotcraft.create_farming_essentials.api.TrellisCrop;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A trellis planted on farmland. Extends BushBlock so it gets the same
 * "break when the block below is removed" behaviour that crops have.
 * A seed can be planted inside it; the planted crop lives in the block entity.
 */
public class TrellisBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<TrellisBlock> CODEC = simpleCodec(TrellisBlock::new);

    // Full-block outline so it's easy to aim at (the model itself is mostly gaps).
    // Collision is still disabled in the block properties, so you can walk through it.
    private static final VoxelShape SHAPE = Shapes.block();

    public TrellisBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    /** Only farmland can hold a trellis. BushBlock#canSurvive calls this. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrellisBlockEntity(pos, state);
    }

    /** Right-click with a registered seed to plant it in an empty trellis. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TrellisBlockEntity trellis) || trellis.hasCrop()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Optional<TrellisCrop> crop = TrellisCrops.findBySeed(stack);
        if (crop.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            trellis.setCrop(crop.get().id());
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Breaking the trellis drops its stick (loot table) plus the seed of whatever was planted. */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TrellisBlockEntity trellis) {
            trellis.getCrop().ifPresent(crop -> drops.add(crop.seedStack()));
        }
        return drops;
    }

    /** The block has no item of its own, so middle-click gives the stick that planted it. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.STICK);
    }
}
