package anightdazingzoroark.example.client.renderer.armor;

import anightdazingzoroark.example.armor.AnimatedGreenArmorHolder;
import anightdazingzoroark.example.client.model.armor.GreenArmorModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class GreenArmorRenderer extends GeoArmorRenderer<AnimatedGreenArmorHolder> {
	public GreenArmorRenderer() {
		super(new GreenArmorModel(), AnimatedGreenArmorHolder::new);
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.HEAD, "head");
		this.bindBoneToBodyPart(ArmorBodyPart.HEAD, "head");

		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.CHEST, "body");
		this.bindBoneToBodyPart(ArmorBodyPart.BODY, "body");
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.CHEST, "rightArm");
		this.bindBoneToBodyPart(ArmorBodyPart.RIGHT_ARM, "rightArm");
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.CHEST, "leftArm");
		this.bindBoneToBodyPart(ArmorBodyPart.LEFT_ARM, "leftArm");

		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.LEGS, "hips");
		this.bindBoneToBodyPart(ArmorBodyPart.HIPS, "hips");
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.LEGS, "rightLeg");
		this.bindBoneToBodyPart(ArmorBodyPart.RIGHT_LEG, "rightLeg");
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.LEGS, "leftLeg");
		this.bindBoneToBodyPart(ArmorBodyPart.LEFT_LEG, "leftLeg");

		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.FEET, "rightFoot");
		this.bindBoneToBodyPart(ArmorBodyPart.RIGHT_FOOT, "rightFoot");
		this.bindBoneToEquipmentSlot(EntityEquipmentSlot.FEET, "leftFoot");
		this.bindBoneToBodyPart(ArmorBodyPart.LEFT_FOOT, "leftFoot");
	}
}
