package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.GreenArmor;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class GreenArmorModel extends AnimatedGeoModel<GreenArmor> {
	@Override
	public ResourceLocation getModelLocation(GreenArmor object) {
		return new ResourceLocation(RiftLib.ModID, "geo/green_armor.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(GreenArmor object) {
		return new ResourceLocation(RiftLib.ModID, "textures/item/green_armor.png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(GreenArmor animatable) {
        return null;
	}
}
