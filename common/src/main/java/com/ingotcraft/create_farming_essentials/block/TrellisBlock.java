package com.ingotcraft.create_farming_essentials.block;

import com.ingotcraft.create_farming_essentials.api.TrellisCrop;
import com.ingotcraft.create_farming_essentials.api.TrellisCrops;
import com.ingotcraft.create_farming_essentials.api.TrellisFruit;
import com.ingotcraft.create_farming_essentials.api.TrellisMachineHarvest;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
 * A seed can be planted inside it; the planted crop and its growth live in the block entity.
 */
public class TrellisBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<TrellisBlock> CODEC = simpleCodec(TrellisBlock::new);

    /** Growth attempts per random tick. Vanilla crops make one, so 2 = twice as fast. */
    private static final int GROWTH_ATTEMPTS = 2;

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

    // ------------------------------------------------------------------ growth

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof TrellisBlockEntity trellis)) {
            return;
        }
        Optional<TrellisCrop> planted = trellis.getCrop();
        if (planted.isEmpty()) {
            return;
        }
        TrellisCrop crop = planted.get();
        TrellisFruit fruit = crop.fruit();

        // Safety net: a fruit that vanished while nothing was watching (unloaded chunk, commands)
        // frees the vine again. Normal removal is caught immediately in updateShape below.
        Direction attached = trellis.getFruitDirection();
        if (attached != null && !isFruit(level.getBlockState(pos.relative(attached)), fruit)) {
            trellis.setFruitDirection(null);
        }

        boolean growing = !trellis.isMature(crop);
        boolean canFruit = !growing && fruit != null && trellis.getFruitDirection() == null;
        if ((!growing && !canFruit) || level.getRawBrightness(pos, 0) < 9) {
            return;
        }

        // Same roll a vanilla crop makes, repeated GROWTH_ATTEMPTS times per random tick.
        // A successful roll ages the crop, or, once mature, tries to grow a fruit (like a vanilla stem).
        int oneInN = (int) (25.0F / getGrowthSpeed(level, pos, crop)) + 1;
        int age = trellis.getAge();
        for (int attempt = 0; attempt < GROWTH_ATTEMPTS; attempt++) {
            if (random.nextInt(oneInN) != 0) {
                continue;
            }
            if (age < crop.maxAge()) {
                age++;
            } else if (fruit != null && trellis.getFruitDirection() == null) {
                trySpawnFruit(level, pos, trellis, fruit, random);
            }
        }
        if (age != trellis.getAge()) {
            trellis.setAge(age);
        }
    }

    /**
     * Vanilla's StemBlock rule: pick a random horizontal side; if that spot is empty and sits on
     * farmland or dirt, grow the fruit there.
     */
    private static void trySpawnFruit(ServerLevel level, BlockPos pos, TrellisBlockEntity trellis,
                                      TrellisFruit fruit, RandomSource random) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos fruitPos = pos.relative(direction);
        BlockState ground = level.getBlockState(fruitPos.below());
        if (level.getBlockState(fruitPos).isAir() && (ground.is(Blocks.FARMLAND) || ground.is(BlockTags.DIRT))) {
            // Record the side first so the neighbour update caused by placing the fruit finds it in place.
            trellis.setFruitDirection(direction);
            level.setBlockAndUpdate(fruitPos, fruit.fruitState().get());
        }
    }

    private static boolean isFruit(BlockState state, @Nullable TrellisFruit fruit) {
        return fruit != null && state.is(fruit.fruitState().get().getBlock());
    }

    /** When the block on the fruit's side stops being the fruit (picked, broken), the vine is free again. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState result = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (result.is(this) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof TrellisBlockEntity trellis
                && direction == trellis.getFruitDirection()) {
            TrellisFruit fruit = trellis.getCrop().map(TrellisCrop::fruit).orElse(null);
            if (!isFruit(neighborState, fruit)) {
                trellis.setFruitDirection(null);
            }
        }
        return result;
    }

    /**
     * Vanilla's CropBlock.getGrowthSpeed, copied because it's protected and this block doesn't extend
     * CropBlock. Farmland in the 3x3 below adds speed (moist farmland more), and neighbouring plants of
     * the same kind halve it. "Same kind" here means a trellis with the same crop planted.
     */
    private static float getGrowthSpeed(ServerLevel level, BlockPos pos, TrellisCrop crop) {
        float speed = 1.0F;
        BlockPos below = pos.below();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                float bonus = 0.0F;
                BlockState ground = level.getBlockState(below.offset(dx, 0, dz));
                if (ground.is(Blocks.FARMLAND)) {
                    bonus = 1.0F;
                    if (ground.getValue(FarmBlock.MOISTURE) > 0) {
                        bonus = 3.0F;
                    }
                }
                if (dx != 0 || dz != 0) {
                    bonus /= 4.0F;
                }
                speed += bonus;
            }
        }

        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        boolean sameEastWest = isSameCrop(level, west, crop) || isSameCrop(level, east, crop);
        boolean sameNorthSouth = isSameCrop(level, north, crop) || isSameCrop(level, south, crop);
        if (sameEastWest && sameNorthSouth) {
            speed /= 2.0F;
        } else {
            boolean sameDiagonal = isSameCrop(level, west.north(), crop) || isSameCrop(level, east.north(), crop)
                    || isSameCrop(level, east.south(), crop) || isSameCrop(level, west.south(), crop);
            if (sameDiagonal) {
                speed /= 2.0F;
            }
        }
        return speed;
    }

    private static boolean isSameCrop(ServerLevel level, BlockPos pos, TrellisCrop crop) {
        return level.getBlockEntity(pos) instanceof TrellisBlockEntity other && crop.id().equals(other.getCropId());
    }

    // ------------------------------------------------------------------ drops

    /**
     * Breaking the trellis drops its stick (loot table) and the seed of whatever was planted.
     * If the crop is fully grown it also drops the crop's yield, rolled with one extra Fortune level.
     * When a machine marked this break (see {@link TrellisMachineHarvest}) the stick is left out.
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        boolean machineBreak = origin != null
                && TrellisMachineHarvest.consume(params.getLevel(), BlockPos.containing(origin));

        List<ItemStack> drops = machineBreak ? new ArrayList<>() : new ArrayList<>(super.getDrops(state, params));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TrellisBlockEntity trellis) {
            trellis.getCrop().ifPresent(crop -> {
                drops.add(crop.seedStack());
                if (trellis.isMature(crop)) {
                    drops.addAll(TrellisHarvest.roll(crop, params));
                }
            });
        }
        return drops;
    }

    /** The block has no item of its own, so middle-click gives the stick that planted it. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.STICK);
    }
}
