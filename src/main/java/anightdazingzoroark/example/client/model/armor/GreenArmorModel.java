package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.AnimatedGreenArmorHolder;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreenArmorModel extends AnimatedGeoModel<AnimatedGreenArmorHolder> {
	@Override
	@NotNull
	public String getModId() {
		return RiftLib.ModID;
	}

	@Override
	public String getModelIdentifier(AnimatedGreenArmorHolder object) {
		return "geometry.green_armor";
	}

	@Override
	public String getTextureLocation(AnimatedGreenArmorHolder object) {
		return "item/green_armor.png";
	}

	@Override
	@NotNull
	public List<String> getAnimationIdentifiers(AnimatedGreenArmorHolder animatable) {
        return List.of();
	}
}
