package com.ingotcraft.create_farming_essentials.neoforge.sprinkler;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A sprinkler that can be mounted on a floor (HALF=bottom) or a ceiling (HALF=top).
 * Its pipe connection is on the mounting side: DOWN when floor-mounted, UP when ceiling-mounted.
 */
public class SprinklerBlock extends Block implements EntityBlock {
    public static final MapCodec<SprinklerBlock> CODEC = simpleCodec(SprinklerBlock::new);
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    // Bounds of the Blockbench model: x/z 3..13, y 0..9 (mirrored for ceiling mounting).
    private static final VoxelShape SHAPE_FLOOR = Block.box(3, 0, 3, 13, 9, 13);
    private static final VoxelShape SHAPE_CEILING = Block.box(3, 7, 3, 13, 16, 13);

    public SprinklerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** The side of the block where a pipe can attach. */
    public static Direction connectionSide(BlockState state) {
        return state.getValue(HALF) == Half.TOP ? Direction.UP : Direction.DOWN;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Half half;
        if (face == Direction.DOWN) {
            half = Half.TOP;            // clicked the underside of a ceiling
        } else if (face == Direction.UP) {
            half = Half.BOTTOM;         // clicked the top of a floor
        } else {
            // Clicked a wall: upper half of the wall hangs from the ceiling, lower half stands on the floor.
            double y = context.getClickLocation().y - context.getClickedPos().getY();
            half = y > 0.5 ? Half.TOP : Half.BOTTOM;
        }
        return defaultBlockState().setValue(HALF, half);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == Half.TOP ? SHAPE_CEILING : SHAPE_FLOOR;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != SprinklerRegistry.SPRINKLER_BE.get()) {
            return null;
        }
        BlockEntityTicker<SprinklerBlockEntity> ticker = level.isClientSide()
                ? (l, p, s, be) -> be.clientTick()
                : (l, p, s, be) -> be.serverTick();
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> cast = (BlockEntityTicker<T>) ticker;
        return cast;
    }
}
