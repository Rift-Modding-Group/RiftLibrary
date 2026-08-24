package anightdazingzoroark.example.client.model.entity;

import net.minecraft.util.ResourceLocation;
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
	public String getModelIdentifier(Object object) {
		return "geometry.creeper";
	}

	@Override
	public ResourceLocation getTextureLocation(Object object) {
		return new ResourceLocation(RiftLib.ModID, "textures/model/entity/creeper.png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(Object animatable) {
		return new ResourceLocation(RiftLib.ModID, "animations/creeper.animation.json");
	}
}
