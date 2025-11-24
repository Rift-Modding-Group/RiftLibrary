package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.item.GreenArmorItem;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;

public class GreenArmorModel extends AnimatedGeoModel<GreenArmorItem> {
	@Override
	public ResourceLocation getModelLocation(GreenArmorItem object) {
		return new ResourceLocation(RiftLib.ModID, "geo/green_armor.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(GreenArmorItem object) {
		return new ResourceLocation(RiftLib.ModID, "textures/item/green_armor.png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(GreenArmorItem animatable) {
        return null;
	}
}
