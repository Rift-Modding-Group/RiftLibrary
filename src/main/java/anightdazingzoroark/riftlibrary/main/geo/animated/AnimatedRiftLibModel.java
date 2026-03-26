package anightdazingzoroark.riftlibrary.main.geo.animated;

import anightdazingzoroark.riftlibrary.main.animator.IAnimated;
import anightdazingzoroark.riftlibrary.main.exception.GeoModelException;
import anightdazingzoroark.riftlibrary.main.geo.basic.RiftLibBone;
import anightdazingzoroark.riftlibrary.main.geo.basic.RiftLibModel;
import anightdazingzoroark.riftlibrary.main.resource.RiftLibraryCache;
import net.minecraft.util.ResourceLocation;

public abstract class AnimatedRiftLibModel<T extends IAnimated> {
    protected RiftLibModel currentModel;

    public abstract ResourceLocation getModelLocation(T object);

    public abstract ResourceLocation getTextureLocation(T object);

    public RiftLibModel getModel(ResourceLocation location) {
        RiftLibModel model = RiftLibraryCache.getInstance().getGeoModels().get(location);
        if (model == null) {
            throw new GeoModelException(location, "Could not find model.");
        }
        if (model != this.currentModel) {
            //change current model
            //this.animationProcessor.clearModelRendererList();
            for (RiftLibBone bone : model.topLevelBones) {
                this.registerBone(bone);
            }
            this.currentModel = model;
        }
        return model;
    }

    public void registerBone(RiftLibBone bone) {
        this.registerModelRenderer(bone);

        for (RiftLibBone childBone : bone.childBones) {
            this.registerBone(childBone);
        }
    }

    public void registerModelRenderer(RiftLibBone modelRenderer) {
        //this.animationProcessor.registerModelRenderer(modelRenderer);
    }
}
