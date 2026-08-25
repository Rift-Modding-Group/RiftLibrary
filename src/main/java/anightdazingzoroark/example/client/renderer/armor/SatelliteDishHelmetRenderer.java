package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.SatelliteDishHelmetModel;
import anightdazingzoroark.example.armor.AnimatedSatelliteDishHelmetHolder;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

public class SatelliteDishHelmetRenderer extends GeoArmorRenderer<AnimatedSatelliteDishHelmetHolder> {
    public SatelliteDishHelmetRenderer() {
        super(new SatelliteDishHelmetModel(), AnimatedSatelliteDishHelmetHolder::new);
        this.setHeadBone("head");
    }
}
