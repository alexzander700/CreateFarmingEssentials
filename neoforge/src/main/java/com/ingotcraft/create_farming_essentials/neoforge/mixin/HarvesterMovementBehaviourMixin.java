package com.ingotcraft.create_farming_essentials.neoforge.mixin;

import com.ingotcraft.create_farming_essentials.api.TrellisMachineHarvest;
import com.ingotcraft.create_farming_essentials.block.TrellisBlock;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Teaches Create's Mechanical Harvester how to treat a trellis.
 *
 * The harvester asks isValidCrop, then isValidOther, about each block it passes over, and breaks the
 * block if either says yes. Left alone it says yes to the trellis via isValidOther (any collision-free
 * plant counts as "cuttable"), so it broke every trellis, grown or not. Here:
 *  - a trellis is a crop, and only when its planted crop is fully grown;
 *  - it is never a mere "cuttable plant";
 *  - when we say yes, we mark the imminent break as a machine break so the stick isn't dropped.
 */
@Mixin(value = HarvesterMovementBehaviour.class, remap = false)
public abstract class HarvesterMovementBehaviourMixin {

    @Inject(method = "isValidCrop", at = @At("HEAD"), cancellable = true)
    private void create_farming_essentials$trellisIsValidCrop(Level world, BlockPos pos, BlockState state,
                                                              CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof TrellisBlock) {
            boolean ready = TrellisMachineHarvest.isReady(world, pos);
            if (ready) {
                TrellisMachineHarvest.markMachineBreak(world, pos);
            }
            cir.setReturnValue(ready);
        }
    }

    @Inject(method = "isValidOther", at = @At("HEAD"), cancellable = true)
    private void create_farming_essentials$trellisIsNotOther(Level world, BlockPos pos, BlockState state,
                                                             CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof TrellisBlock) {
            cir.setReturnValue(false);
        }
    }
}
