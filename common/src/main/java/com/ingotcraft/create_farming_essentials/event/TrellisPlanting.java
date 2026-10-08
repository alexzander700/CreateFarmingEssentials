package com.ingotcraft.create_farming_essentials.event;

import com.ingotcraft.create_farming_essentials.advancement.ModAdvancements;
import com.ingotcraft.create_farming_essentials.registry.ModBlocks;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Right-clicking the top of farmland with a stick plants a trellis. */
public final class TrellisPlanting {
    public static void register() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(TrellisPlanting::onRightClickBlock);
    }

    private static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.STICK) || face != Direction.UP) {
            return EventResult.pass();
        }

        Level level = player.level();
        if (!level.getBlockState(pos).is(Blocks.FARMLAND)) {
            return EventResult.pass();
        }

        BlockPos target = pos.above();
        BlockState existing = level.getBlockState(target);
        BlockState trellis = ModBlocks.TRELLIS.get().defaultBlockState();

        // Same checks on both sides so the client and server agree on whether this click is ours.
        if (!existing.canBeReplaced() || !existing.getFluidState().isEmpty()
                || !trellis.canSurvive(level, target)
                || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(target, Direction.UP, stack)) {
            return EventResult.pass();
        }

        // The server does the real work; the client just needs to report "handled" so the arm swings.
        if (level.isClientSide()) {
            return EventResult.interruptTrue();
        }

        level.setBlock(target, trellis, Block.UPDATE_ALL);
        SoundType sound = trellis.getSoundType();
        level.playSound(null, target, sound.getPlaceSound(), SoundSource.BLOCKS,
                (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, target, GameEvent.Context.of(player, trellis));

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.award(serverPlayer, ModAdvancements.STICK_Y_SITUATION);
        }
        return EventResult.interruptTrue();
    }

    private TrellisPlanting() {}
}
