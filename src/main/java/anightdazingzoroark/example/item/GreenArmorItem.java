package anightdazingzoroark.example.item;

import net.minecraft.inventory.EntityEquipmentSlot;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import anightdazingzoroark.riftlib.item.GeoArmorItem;

public class GreenArmorItem extends GeoArmorItem implements IAnimatable {
	private final AnimationFactory factory = new AnimationFactory(this);

	public GreenArmorItem(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot slot) {
		super(materialIn, renderIndexIn, slot);
		this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
	}

	@Override
	public void registerControllers(AnimationData data) {}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}
}
