package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class MerryGoRoundTileEntity extends TileEntity implements IAnimatable<AnimationDataTileEntity> {
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);

    @Override
    public List<AnimationController<?, AnimationDataTileEntity>> createAnimationControllers() {
        return List.of(
                new AnimationController<MerryGoRoundTileEntity, AnimationDataTileEntity>(
                        this, "rotate", "default",
                        new AnimationControllerState<AnimationDataTileEntity>("default")
                                .addAnimation("animation.merry_go_round.rotate")
                )
        );
    }

    @Override
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }
}
