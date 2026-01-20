package anightdazingzoroark.example.item;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class AnimatedItemBlock extends ItemBlock implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public AnimatedItemBlock(Block block) {
        super(block);
    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
