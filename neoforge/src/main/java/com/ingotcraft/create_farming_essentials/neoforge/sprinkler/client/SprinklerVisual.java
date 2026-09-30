package com.ingotcraft.create_farming_essentials.neoforge.sprinkler.client;

import com.ingotcraft.create_farming_essentials.neoforge.sprinkler.SprinklerBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

import java.util.function.Consumer;

/** Flywheel-instanced sprinkler head, rotated every frame from the block entity's smoothed angle. */
public class SprinklerVisual extends AbstractBlockEntityVisual<SprinklerBlockEntity>
        implements SimpleDynamicVisual, LightUpdatedVisual {

    private final TransformedInstance head;

    public SprinklerVisual(VisualizationContext context, SprinklerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        head = instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, Models.partial(SprinklerPartialModels.HEAD))
                .createInstance();
        animate(partialTick);
        updateLight(partialTick);
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        animate(context.partialTick());
    }

    private void animate(float partialTick) {
        head.setIdentityTransform()
                .translate(getVisualPosition())
                .center();
        if (blockEntity.isTop()) {
            head.rotateXDegrees(180.0F);      // hang it upside down for ceiling mounting
        }
        head.rotateYDegrees(blockEntity.getAngle(partialTick))
                .uncenter()
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(head);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(head);
    }

    @Override
    protected void _delete() {
        head.delete();
    }
}
