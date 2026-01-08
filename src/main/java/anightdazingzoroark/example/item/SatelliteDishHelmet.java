package anightdazingzoroark.example.item;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import anightdazingzoroark.riftlib.item.GeoArmorItem;
import net.minecraft.inventory.EntityEquipmentSlot;

public class SatelliteDishHelmet extends GeoArmorItem implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public SatelliteDishHelmet(ArmorMaterial materialIn, int renderIndexIn) {
        super(materialIn, renderIndexIn, EntityEquipmentSlot.HEAD);
        this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
