package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import net.minecraft.tileentity.TileEntity;

public class MerryGoRoundTileEntity extends TileEntity implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rotate", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.merry_go_round.rotate", true));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
