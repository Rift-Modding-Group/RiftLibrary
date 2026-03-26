package anightdazingzoroark.riftlibrary.example.client.model;

import anightdazingzoroark.riftlibrary.example.entity.RedDragonEntity;
import anightdazingzoroark.riftlibrary.main.RiftLibrary;
import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import anightdazingzoroark.riftlibrary.main.geo.animated.AnimatedRiftLibModel;
import net.minecraft.util.ResourceLocation;

public class RedDragonModel extends AnimatedRiftLibModel<RedDragonEntity> {
    @Override
    public ResourceLocation getModelLocation(RedDragonEntity object) {
        return new ResourceLocation(RiftLibraryMod.MODID, "geo/dragon.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(RedDragonEntity object) {
        return new ResourceLocation(RiftLibraryMod.MODID, "textures/dragon.png");
    }
}
