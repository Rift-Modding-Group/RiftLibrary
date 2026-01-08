package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.SatelliteDishHelmetModel;
import anightdazingzoroark.example.item.SatelliteDishHelmet;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

public class SatelliteDishHelmetRenderer extends GeoArmorRenderer<SatelliteDishHelmet> {
    public SatelliteDishHelmetRenderer() {
        super(new SatelliteDishHelmetModel());
        this.headBone = "head";
        this.bodyBone = "body";
        this.rightArmBone = "rightArm";
        this.leftArmBone = "leftArm";
        this.rightLegBone = "rightLeg";
        this.leftLegBone = "leftLeg";
        this.rightBootBone = "rightFoot";
        this.leftBootBone = "leftFoot";
    }
}
