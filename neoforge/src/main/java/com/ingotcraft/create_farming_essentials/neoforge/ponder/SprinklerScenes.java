package com.ingotcraft.create_farming_essentials.neoforge.ponder;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerBlockEntity;
import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerRegistry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.Vec3;

/**
 * Sprinkler scenes. Ponder's world is a client-side copy: fluid never arrives through the pipe and the
 * server-side effects never run, so these scenes drive everything by hand:
 *  - "running" and "fluid" are pushed into the block entity through its saved-data keys, so the head spins;
 *  - the spray is NOT faked: once "running" is set, the block entity's own client tick spawns the real
 *    FluidSprayOptions particles into the Ponder level;
 *  - hydration and the Enderman's reaction are scripted.
 * Text keys (text_1, text_2, ...) are numbered in the order showText runs, and must match en_us.json.
 */
public final class SprinklerScenes {

    // ------------------------------------------------------------------ placement

    public static void placement(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sprinkler_placement", "Placing a Sprinkler");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos floorPipe = util.grid().at(1, 0, 2);
        BlockPos floorSprinkler = util.grid().at(1, 1, 2);
        BlockPos ceilingPipe = util.grid().at(3, 4, 2);
        BlockPos ceilingSprinkler = util.grid().at(3, 3, 2);
        ItemStack sprinklerItem = new ItemStack(SprinklerRegistry.SPRINKLER_ITEM.get());

        // 1. Floor
        scene.overlay().showText(80)
                .text("Placed on a floor, a Sprinkler stands upright and takes fluid from the block below it")
                .pointAt(util.vector().topOf(floorPipe))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(floorPipe), Pointing.DOWN, 40)
                .rightClick()
                .withItem(sprinklerItem);
        scene.idle(10);
        scene.world().showSection(util.select().position(floorSprinkler), Direction.DOWN);
        scene.idle(70);

        // 2. Ceiling
        // The ceiling is every block of the 3x3 patch except the pipe the Sprinkler hangs from.
        scene.world().showSection(util.select().position(ceilingPipe), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(80)
                .text("Placed on the underside of a ceiling, it hangs upside down and takes fluid from the block above it")
                .pointAt(util.vector().blockSurface(ceilingPipe, Direction.DOWN))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().blockSurface(ceilingPipe, Direction.DOWN), Pointing.UP, 40)
                .rightClick()
                .withItem(sprinklerItem);
        scene.idle(10);
        scene.world().showSection(util.select().position(ceilingSprinkler), Direction.UP);
        scene.idle(60);

        // 3. Walls
        scene.overlay().showText(80)
                .text("Placed against a wall, the upper half of the face hangs it from the ceiling and the lower half stands it on the floor")
                .pointAt(util.vector().centerOf(floorSprinkler))
                .placeNearTarget();
        scene.idle(90);

        // 4. Reach
        scene.overlay().showText(90)
                .text("A running Sprinkler reaches a 9x9x9 area")
                .pointAt(util.vector().centerOf(ceilingSprinkler))
                .placeNearTarget();
        scene.idle(100);
    }

    // ------------------------------------------------------------------ water + farmland

    public static void water(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sprinkler_water", "Spraying Water");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos pipe = util.grid().at(2, 0, 2);
        BlockPos sprinkler = util.grid().at(2, 1, 2);
        // Everything on the floor except the pipe under the Sprinkler.
        Selection farmland = util.select().fromTo(0, 0, 3, 4, 0, 4)
                .add(util.select().position(0, 0, 2))
                .add(util.select().position(1, 0, 2))
                .add(util.select().position(3, 0, 2))
                .add(util.select().position(4, 0, 2));
        Selection farmlandCutaway = util.select().fromTo(0, 0, 0, 4, 0, 1);
        scene.world().showSection(util.select().position(sprinkler), Direction.DOWN);
        scene.idle(15);

        // 1. Piping. The farmland is cut away so the pipe under the Sprinkler can be seen.
        scene.world().hideSection(farmlandCutaway, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(90)
                .text("The Sprinkler accepts fluid through its mounting face. Connect Create's pipes to it and it sprays whatever flows in")
                .pointAt(util.vector().centerOf(pipe))
                .placeNearTarget();
        scene.idle(100);
        scene.world().showSection(farmlandCutaway, Direction.UP);
        scene.idle(20);

        // 2. Running
        setRunning(scene, util, sprinkler, true);
        scene.overlay().showText(80)
                .text("While fluid is flowing the head spins up and sprays. The fluid itself is used up")
                .pointAt(util.vector().topOf(sprinkler))
                .placeNearTarget();
        scene.idle(90);

        // 3. Hydration
        scene.overlay().showText(90)
                .text("Water slowly hydrates farmland in reach, even with no water source nearby")
                .pointAt(util.vector().topOf(util.grid().at(0, 0, 0)))
                .placeNearTarget();
        scene.idle(30);
        scene.world().replaceBlocks(farmland, farmland(3), false);
        scene.world().replaceBlocks(farmlandCutaway, farmland(3), false);
        scene.idle(40);
        scene.world().replaceBlocks(farmland, farmland(7), false);
        scene.world().replaceBlocks(farmlandCutaway, farmland(7), false);
        scene.idle(70);
    }

    // ------------------------------------------------------------------ creatures + other fluids

    public static void creatures(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("sprinkler_creatures", "Sprinklers and Creatures");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos pipe = util.grid().at(2, 0, 2);
        BlockPos sprinkler = util.grid().at(2, 1, 2);
        BlockPos standing = util.grid().at(3, 0, 2);
        scene.world().showSection(util.select().position(sprinkler), Direction.DOWN);
        scene.idle(10);
        setRunning(scene, util, sprinkler, true);

        ElementLink<EntityElement> enderman = scene.world().createEntity(level -> {
            EnderMan e = EntityType.ENDERMAN.create(level);
            Vec3 p = util.vector().topOf(standing);
            e.setPos(p.x, p.y, p.z);
            e.xo = p.x;
            e.yo = p.y;
            e.zo = p.z;
            e.setYRot(90.0F);       // face the Sprinkler
            e.yRotO = 90.0F;
            e.yHeadRot = 90.0F;
            e.yHeadRotO = 90.0F;
            e.yBodyRot = 90.0F;
            e.yBodyRotO = 90.0F;
            return e;
        });
        scene.idle(20);

        // 1. Water hurts water-sensitive mobs, same as rain
        scene.overlay().showText(90)
                .text("Sprayed water hurts creatures that are sensitive to it, such as Endermen, just like rain does")
                .pointAt(util.vector().topOf(standing).add(0, 2.0, 0))
                .placeNearTarget();
        scene.idle(30);
        for (int i = 0; i < 3; i++) {
            scene.world().modifyEntity(enderman, e -> {
                if (e instanceof LivingEntity living) {
                    living.animateHurt(0.0F);
                }
            });
            scene.idle(12);
        }
        // ...and, like in rain, the Enderman teleports away.
        scene.effects().emitParticles(util.vector().topOf(standing).add(0, 1.0, 0),
                scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, new Vec3(0, 0.1, 0)), 10, 6);
        scene.world().modifyEntity(enderman, Entity::discard);
        scene.idle(60);

        // 2. Other fluids
        scene.overlay().showText(110)
                .text("Other fluids do other things: lava sets creatures alight, and honey slows them down")
                .pointAt(util.vector().centerOf(pipe))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(pipe), Pointing.RIGHT, 40)
                .withItem(new ItemStack(Items.LAVA_BUCKET));
        scene.idle(45);
        scene.overlay().showControls(util.vector().centerOf(pipe), Pointing.RIGHT, 40)
                .withItem(new ItemStack(Items.HONEY_BOTTLE));
        scene.idle(60);

        // 3. Extensibility
        scene.overlay().showText(80)
                .text("Other mods can register their own effects for their own fluids")
                .pointAt(util.vector().topOf(sprinkler))
                .placeNearTarget();
        scene.idle(90);
    }

    // ------------------------------------------------------------------ helpers

    private static net.minecraft.world.level.block.state.BlockState farmland(int moisture) {
        return Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, moisture);
    }

    /**
     * The Sprinkler's running flag and fluid are server state that it sends to clients as saved data;
     * writing the same keys reaches the same code path (loadAdditional) the real client uses.
     */
    private static void setRunning(SceneBuilder scene, SceneBuildingUtil util, BlockPos pos, boolean running) {
        scene.world().modifyBlockEntityNBT(util.select().position(pos), SprinklerBlockEntity.class, tag -> {
            tag.putBoolean("running", running);
            if (running) {
                tag.putString("fluid", "minecraft:water");
            }
        });
    }

    private SprinklerScenes() {}
}
