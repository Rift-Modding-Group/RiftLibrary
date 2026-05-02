package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.armor.RiftLibArmor;

import java.util.List;

public class GreenArmor extends RiftLibArmor {
	public GreenArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot slot) {
		super(materialIn, renderIndexIn, slot);
		this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
	}

	@Override
	public List<AnimationControllerNew<?, AnimationDataArmor>> createAnimationControllers() {
		return List.of();
	}
}
