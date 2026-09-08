package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.AnimatedSatelliteDishHelmetHolder;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class SatelliteDishHelmetModel extends AnimatedGeoModel<AnimatedSatelliteDishHelmetHolder> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    @NotNull
    public String getModelIdentifier(AnimatedSatelliteDishHelmetHolder object) {
        return "geometry.satellite_dish_helmet";
    }

    @Override
    @NotNull
    public String getTextureLocation(AnimatedSatelliteDishHelmetHolder object) {
        return "armor/satellite_dish_helmet.png";
    }
}
