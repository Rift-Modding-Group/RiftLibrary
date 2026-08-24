package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.SatelliteDishHelmet;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SatelliteDishHelmetModel extends AnimatedGeoModel<SatelliteDishHelmet> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(SatelliteDishHelmet object) {
        return "geometry.satellite_dish_helmet";
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
