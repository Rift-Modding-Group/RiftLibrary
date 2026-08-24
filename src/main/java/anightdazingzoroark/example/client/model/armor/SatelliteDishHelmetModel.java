package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.SatelliteDishHelmet;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public String getTextureLocation(SatelliteDishHelmet object) {
        return "armor/satellite_dish_helmet.png";
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(SatelliteDishHelmet animatable) {
        return List.of("animation.satellite_dish_helmet.spin", "animation.satellite_dish_helmet.signal");
    }
}
