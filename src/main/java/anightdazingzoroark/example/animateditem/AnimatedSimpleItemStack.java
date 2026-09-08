package anightdazingzoroark.example.animateditem;

import anightdazingzoroark.riftlib.core.manager.AnimationDataItemStack;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class AnimatedSimpleItemStack extends AnimatedItemStackHolder {
    public AnimatedSimpleItemStack(ItemStack stack) {
        super(stack);
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataItemStack animationData) {}
}
