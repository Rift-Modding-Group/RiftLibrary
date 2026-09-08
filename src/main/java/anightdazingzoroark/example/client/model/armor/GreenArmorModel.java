package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.AnimatedGreenArmorHolder;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

public class GreenArmorModel extends AnimatedGeoModel<AnimatedGreenArmorHolder> {
	@Override
	@NotNull
	public String getModId() {
		return RiftLib.ModID;
	}

	@Override
	@NotNull
	public String getModelIdentifier(AnimatedGreenArmorHolder object) {
		return "geometry.green_armor";
	}

	@Override
	@NotNull
	public String getTextureLocation(AnimatedGreenArmorHolder object) {
		return "item/green_armor.png";
	}
}
