package anightdazingzoroark.example.item;

import anightdazingzoroark.riftlib.RiftLibMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//when the player holds this item and right clicks, they will spawn a bunch of bubbles that go forward
public class BubbleGunItem extends Item {
    public BubbleGunItem() {
        this.maxStackSize = 1;
        this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }
}
