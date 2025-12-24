package anightdazingzoroark.example.item;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import net.minecraft.item.Item;

//when the player holds this item and right clicks, they will spawn a bunch of bubbles that do nothing really
public class BubbleGunItem extends Item implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public BubbleGunItem() {
        super();
        this.maxStackSize = 1;
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
