package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import net.minecraft.tileentity.TileEntity;

public class MerryGoRoundTileEntity extends TileEntity implements IAnimatable<AnimationDataTileEntity> {
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);

    @Override
    public void registerAnimationControllers(AnimationDataTileEntity data) {
        data.addAnimationController(new AnimationController<>(
                this, "rotate", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.merry_go_round.rotate", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
    }

    @Override
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }
}
