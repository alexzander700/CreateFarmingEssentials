package com.ingotcraft.create_farming_essentials.neoforge.ponder;

import com.ingotcraft.create_farming_essentials.Create_farming_essentials;
import com.ingotcraft.create_farming_essentials.block.TrellisBlockEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Both scenes use a 5x5 farmland floor. The schematic already contains the trellis at (2,1,2) and a
 * melon at (3,1,2); they are simply not shown until the scene reaches them.
 * Text keys (text_1, text_2, ...) are numbered in the order the showText calls run, and must match en_us.json.
 */
public final class TrellisScenes {
    private static final ResourceLocation MELON =
            ResourceLocation.fromNamespaceAndPath(Create_farming_essentials.MOD_ID, "melon");
    private static final int MAX_AGE = 7;

    public static void planting(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("trellis_planting", "Planting a Trellis");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos farmland = util.grid().at(2, 0, 2);
        BlockPos trellis = util.grid().at(2, 1, 2);
        BlockPos fruit = util.grid().at(3, 1, 2);

        // Make sure the hidden trellis starts empty.
        scene.world().modifyBlockEntity(trellis, TrellisBlockEntity.class,
                be -> be.setCrop(MELON));
        scene.world().modifyBlockEntity(trellis, TrellisBlockEntity.class,
                be -> be.setAge(0));

        // 1. Stick on farmland -> trellis
        scene.overlay().showText(70)
                .text("Right-click the top of farmland with a Stick to plant a Trellis")
                .pointAt(util.vector().topOf(farmland))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(trellis), Pointing.DOWN, 40)
                .rightClick()
                .withItem(new ItemStack(Items.STICK));
        scene.idle(10);
        scene.world().showSection(util.select().position(trellis), Direction.DOWN);
        scene.idle(60);

        // 2. Seeds -> crop (the trellis already holds a stage-0 melon; the seeds are what "plants" it)
        scene.overlay().showText(70)
                .text("Right-click the Trellis with melon or pumpkin seeds to plant a vine inside it")
                .pointAt(util.vector().topOf(trellis))
                .placeNearTarget();
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(trellis), Pointing.DOWN, 40)
                .rightClick()
                .withItem(new ItemStack(Items.MELON_SEEDS));
        scene.idle(60);

        // 3. Growth
        scene.overlay().showText(80)
                .text("The vine grows about twice as fast as it would in normal farmland")
                .pointAt(util.vector().centerOf(trellis))
                .placeNearTarget();
        for (int age = 1; age <= MAX_AGE; age++) {
            final int stage = age;
            scene.idle(10);
            scene.world().modifyBlockEntity(trellis, TrellisBlockEntity.class,
                    be -> be.setAge(stage));
        }
        scene.idle(40);

        // 4. Fruit beside the trellis, like a vanilla stem
        scene.overlay().showText(80)
                .text("If the crop normally does so, the crop can produce it's fruit next to the Trellis as normal.")
                .pointAt(util.vector().centerOf(fruit))
                .placeNearTarget();
        scene.world().modifyBlockEntity(trellis, TrellisBlockEntity.class,
                be -> be.setFruitDirection(Direction.EAST));
        scene.world().showSection(util.select().position(fruit), Direction.DOWN);
        scene.idle(90);
    }

    public static void harvest(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("trellis_harvest", "Harvesting a Trellis");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos trellis = util.grid().at(2, 1, 2);
        BlockPos fruit = util.grid().at(3, 1, 2);

        scene.world().modifyBlockEntity(trellis, TrellisBlockEntity.class, be -> {
            be.setCrop(MELON);
            be.setAge(MAX_AGE);
            be.setFruitDirection(Direction.EAST);
        });
        scene.world().showSection(util.select().fromTo(trellis, fruit), Direction.DOWN);
        scene.idle(20);

        // 1. Breaking a grown trellis
        scene.overlay().showText(90)
                .text("Breaking a crop in a Trellis drops its Stick, its seeds and the crop's yield. It is more likely to produce extra fruit.")
                .pointAt(util.vector().centerOf(trellis))
                .placeNearTarget();
        scene.idle(30);
        scene.world().destroyBlock(trellis);
        Vec3 spawn = util.vector().centerOf(trellis);
        scene.world().createItemEntity(spawn, util.vector().of(-0.05, 0.2, 0.05), new ItemStack(Items.STICK));
        scene.world().createItemEntity(spawn, util.vector().of(0.0, 0.2, -0.05), new ItemStack(Items.MELON_SEEDS));
        scene.idle(80);

        // 2. Machines
        scene.overlay().showText(90)
                .text("They are very fragile however, so if you use a Mechanical Harvester the Trellis will Shatter, dropping only the crop.")
                .pointAt(util.vector().centerOf(trellis))
                .placeNearTarget();
        scene.idle(100);
    }

    private TrellisScenes() {}
}
