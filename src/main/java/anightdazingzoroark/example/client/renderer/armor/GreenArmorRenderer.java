package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.client.model.armor.GreenArmorModel;
import anightdazingzoroark.example.item.GreenArmorItem;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;

public class GreenArmorRenderer extends GeoArmorRenderer<GreenArmorItem> {
	public GreenArmorRenderer() {
		super(new GreenArmorModel());
		this.setHeadBone("head");
		this.setBodyBone("body");
		this.setRightArmBone("rightArm");
		this.setLeftArmBone("leftArm");
        this.setHipsBone("hips");
		this.setRightLegBone("rightLeg");
		this.setLeftLegBone("leftLeg");
		this.setRightBootBone("rightFoot");
		this.setLeftBootBone("leftFoot");
	}
}
