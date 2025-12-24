package anightdazingzoroark.example.item;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

//when the player holds this item and right clicks, they will spawn a bunch of bubbles that go forward
public class BubbleGunItem extends Item implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public BubbleGunItem() {
        this.maxStackSize = 1;
        this.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
    }
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        player.getCooldownTracker().setCooldown(this, 20);
        System.out.println("use item");
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "blow", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bubble_gun.blow_bubbles", LoopType.LOOP));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
