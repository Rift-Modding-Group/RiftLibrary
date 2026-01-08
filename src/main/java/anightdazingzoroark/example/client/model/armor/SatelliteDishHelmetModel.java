package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.item.SatelliteDishHelmet;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;

public class SatelliteDishHelmetModel extends AnimatedGeoModel<SatelliteDishHelmet> {
    @Override
    public ResourceLocation getModelLocation(SatelliteDishHelmet object) {
        return new ResourceLocation(RiftLib.ModID, "geo/satellite_dish_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(SatelliteDishHelmet object) {
        return new ResourceLocation(RiftLib.ModID, "textures/armor/satellite_dish_helmet.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(SatelliteDishHelmet animatable) {
        return new ResourceLocation(RiftLib.ModID, "animations/satellite_dish_helmet.animation.json");
    }
}
