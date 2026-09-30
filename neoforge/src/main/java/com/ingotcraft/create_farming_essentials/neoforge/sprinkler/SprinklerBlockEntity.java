package com.ingotcraft.create_farming_essentials.neoforge.sprinkler;

import com.ingotcraft.create_farming_essentials.api.Sprinkler;
import com.ingotcraft.create_farming_essentials.api.SprinklerArea;
import com.ingotcraft.create_farming_essentials.api.SprinklerContext;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffect;
import com.ingotcraft.create_farming_essentials.api.SprinklerEffects;
import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client.SprinklerSpray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Accepts (and voids) fluid through its mounting face, at most {@link #MB_PER_TICK} per tick.
 * Server: tracks whether fluid arrived recently, which fluid, and applies fluid effects to its area.
 * Client: spins the head with a little inertia and sprays particles of the fluid.
 */
public class SprinklerBlockEntity extends BlockEntity implements Sprinkler {
    /** 100 mB per second at 20 ticks per second. */
    public static final int MB_PER_TICK = 2;
    /** Keeps spinning this many ticks after the last fluid arrived, so pipe flow that skips a tick doesn't flicker. */
    private static final int GRACE_TICKS = 10;

    // Client animation tuning (degrees per tick, and degrees per tick, per tick).
    public static final float MAX_SPEED = 24.0F;
    private static final float ACCELERATION = 0.8F;
    private static final float FRICTION = 0.5F;

    /** The bubbling sound repeats this often (10 ticks = half a second) while fluid is being dispensed. */
    private static final int SOUND_INTERVAL_TICKS = 10;

    // --- server state (not saved: a sprinkler always starts a world idle) ---
    private boolean running;
    @Nullable
    private Fluid fluid;
    private long lastFillTick;
    private long budgetTick = Long.MIN_VALUE;
    private int usedThisTick;
    @Nullable
    private SprinklerArea area;
    private long areaTick;

    // --- client animation state ---
    private float angle;
    private float prevAngle;
    private float speed;
    private int soundCooldown;

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return MB_PER_TICK; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) { return true; }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (level == null || level.isClientSide() || resource.isEmpty()) {
                return 0;
            }
            long now = level.getGameTime();
            if (now != budgetTick) {          // new tick, fresh budget
                budgetTick = now;
                usedThisTick = 0;
            }
            int accepted = Math.min(resource.getAmount(), MB_PER_TICK - usedThisTick);
            if (accepted <= 0) {
                return 0;
            }
            if (action.execute()) {
                usedThisTick += accepted;     // the fluid itself is simply voided
                lastFillTick = now;
                Fluid incoming = resource.getFluid();
                if (incoming instanceof FlowingFluid flowing) {
                    incoming = flowing.getSource();   // treat source and flowing forms as the same fluid
                }
                update(true, incoming);
            }
            return accepted;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(SprinklerRegistry.SPRINKLER_BE.get(), pos, state);
    }

    /** Only the mounting face (where the pipe attaches) exposes the fluid handler. */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side == null || side == SprinklerBlock.connectionSide(getBlockState()) ? fluidHandler : null;
    }

    // ---- Sprinkler API ----

    @Override
    public boolean isRunning() {
        return running;
    }

    @Nullable
    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public SprinklerArea getArea() {
        long now = level.getGameTime();
        if (area == null || now - areaTick >= SprinklerEffects.INTERVAL_TICKS) {
            area = SprinklerArea.compute(level, worldPosition, isTop());
            areaTick = now;
        }
        return area;
    }

    // ---- state ----

    public boolean isTop() {
        return getBlockState().getValue(SprinklerBlock.HALF) == Half.TOP;
    }

    /** Head rotation in degrees, smoothed between ticks. */
    public float getAngle(float partialTick) {
        return Mth.lerp(partialTick, prevAngle, angle);
    }

    /** Current head speed in degrees per tick (client). */
    public float getSpeed() {
        return speed;
    }

    /** Updates the running flag and/or fluid, and tells clients if anything they care about changed. */
    private void update(boolean newRunning, @Nullable Fluid newFluid) {
        boolean changed = running != newRunning || (newFluid != null && newFluid != fluid);
        running = newRunning;
        if (newFluid != null) {
            fluid = newFluid;
        }
        if (changed) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    public void serverTick() {
        long now = level.getGameTime();
        if (running && now - lastFillTick > GRACE_TICKS) {
            update(false, null);
        }
        // Stagger sprinklers by position so they don't all do their work on the same tick.
        if (running && fluid != null && level instanceof ServerLevel server
                && Math.floorMod(now + worldPosition.asLong(), SprinklerEffects.INTERVAL_TICKS) == 0) {
            SprinklerContext context = new SprinklerContext(server, worldPosition, fluid, getArea());
            for (SprinklerEffect effect : SprinklerEffects.all()) {
                if (effect.appliesTo(fluid)) {
                    effect.apply(context);
                }
            }
        }
    }

    public void clientTick() {
        prevAngle = angle;
        float target = running ? MAX_SPEED : 0.0F;
        if (speed < target) {
            speed = Math.min(target, speed + ACCELERATION);
        } else {
            speed = Math.max(target, speed - FRICTION);
        }
        angle += speed;
        if (angle >= 3600.0F) {               // keep the numbers small; 3600 is a whole number of turns
            angle -= 3600.0F;
            prevAngle -= 3600.0F;
        }
        playSound();
        SprinklerSpray.tick(this);
    }

    /** Bubble-column bubbling, pitched up, every half second while running. Played locally on each client. */
    private void playSound() {
        if (!running) {
            soundCooldown = 0;                // so the first bubble plays as soon as fluid starts flowing
            return;
        }
        if (--soundCooldown > 0) {
            return;
        }
        soundCooldown = SOUND_INTERVAL_TICKS;
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                1.5F, 1.6F + level.random.nextFloat() * 0.2F, false);
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                1.5F, 1.6F + level.random.nextFloat() * 0.2F, false);
    }

    // ---- networking: the client needs to know whether it is running, and with what ----

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        running = tag.getBoolean("running");
        fluid = null;
        if (tag.contains("fluid", Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("fluid"));
            if (id != null) {
                fluid = BuiltInRegistries.FLUID.getOptional(id).orElse(null);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("running", running);
        if (fluid != null) {
            tag.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
