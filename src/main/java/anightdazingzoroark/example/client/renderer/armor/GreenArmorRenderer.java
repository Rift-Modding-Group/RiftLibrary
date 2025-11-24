package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.GreenArmorModel;
import anightdazingzoroark.example.item.GreenArmorItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

public class GreenArmorRenderer extends GeoArmorRenderer<GreenArmorItem> {
	public GreenArmorRenderer() {
		super(new GreenArmorModel());
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
