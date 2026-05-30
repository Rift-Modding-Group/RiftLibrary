package anightdazingzoroark.example.armor;

import anightdazingzoroark.riftlib.core.manager.AnimationDataArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.armor.RiftLibArmor;

import java.util.List;

public class GreenArmor extends RiftLibArmor<GreenArmor> {
	public GreenArmor(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot slot) {
		super(materialIn, renderIndexIn, slot);
		this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
	}

	@Override
	public void initializeAnimationData(AnimationDataArmor animationData) {}
}
