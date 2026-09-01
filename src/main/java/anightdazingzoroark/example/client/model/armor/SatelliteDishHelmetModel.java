package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.AnimatedSatelliteDishHelmetHolder;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SatelliteDishHelmetModel extends AnimatedGeoModel<AnimatedSatelliteDishHelmetHolder> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(AnimatedSatelliteDishHelmetHolder object) {
        return "geometry.satellite_dish_helmet";
    }

    @Override
    public String getTextureLocation(AnimatedSatelliteDishHelmetHolder object) {
        return "armor/satellite_dish_helmet.png";
    }
}
