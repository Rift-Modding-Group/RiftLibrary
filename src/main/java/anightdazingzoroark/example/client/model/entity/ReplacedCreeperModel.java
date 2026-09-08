package anightdazingzoroark.example.client.model.entity;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("rawtypes")
public class ReplacedCreeperModel extends AnimatedGeoModel {
	@Override
	@NotNull
	public String getModId() {
		return RiftLib.ModID;
	}

	@Override
	@NotNull
	public String getModelIdentifier(Object object) {
		return "geometry.creeper";
	}

	@Override
	@NotNull
	public String getTextureLocation(Object object) {
		return "model/entity/creeper.png";
	}
}
