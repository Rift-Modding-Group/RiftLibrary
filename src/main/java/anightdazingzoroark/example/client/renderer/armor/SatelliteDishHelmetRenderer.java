package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.SatelliteDishHelmetModel;
import anightdazingzoroark.example.item.SatelliteDishHelmet;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

public class SatelliteDishHelmetRenderer extends GeoArmorRenderer<SatelliteDishHelmet> {
    public SatelliteDishHelmetRenderer() {
        super(new SatelliteDishHelmetModel());
        this.setHeadBone("head");
    }
}
