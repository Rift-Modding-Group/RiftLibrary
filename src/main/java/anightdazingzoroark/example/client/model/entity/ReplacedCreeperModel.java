package anightdazingzoroark.example.client.model.entity;

import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("rawtypes")
public class ReplacedCreeperModel extends AnimatedGeoModel {
	@Override
	@NotNull
	public String getModId() {
		return RiftLib.ModID;
	}

	@Override
	public String getModelIdentifier(Object object) {
		return "geometry.creeper";
	}

	@Override
	public String getTextureLocation(Object object) {
		return "model/entity/creeper.png";
	}
}
