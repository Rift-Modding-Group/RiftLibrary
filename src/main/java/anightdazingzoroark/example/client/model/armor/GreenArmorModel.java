package anightdazingzoroark.example.client.model.armor;

import anightdazingzoroark.example.armor.GreenArmor;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GreenArmorModel extends AnimatedGeoModel<GreenArmor> {
	@Override
	@NotNull
	public String getModId() {
		return RiftLib.ModID;
	}

	@Override
	public String getModelIdentifier(GreenArmor object) {
		return "geometry.green_armor";
	}

	@Override
	public ResourceLocation getTextureLocation(GreenArmor object) {
		return new ResourceLocation(RiftLib.ModID, "textures/item/green_armor.png");
	}

	@Override
	@NotNull
	public List<String> getAnimationIdentifiers(GreenArmor animatable) {
        return List.of();
	}
}
