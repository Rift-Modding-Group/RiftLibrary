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
	public ResourceLocation getTextureLocation(Object object) {
		return new ResourceLocation(RiftLib.ModID, "textures/model/entity/creeper.png");
	}

	@Override
	@NotNull
	public List<String> getAnimationIdentifiers(Object animatable) {
		return List.of("creeper_idle", "creeper_walk", "creeper_explode");
	}
}
