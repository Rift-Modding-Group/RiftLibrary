package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class MerryGoRoundTileEntity extends TileEntity implements IAnimatable<AnimationDataTileEntity> {
    @NotNull
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);

    @Override
    public void initializeAnimationData(@NonNull AnimationDataTileEntity animationData) {
        animationData.addAnimationController(new AnimationController<MerryGoRoundTileEntity, AnimationDataTileEntity>(
                this, "rotate", "default",
                new AnimationControllerState<AnimationDataTileEntity>("default")
                        .addAnimation("animation.merry_go_round.rotate")
        ));
    }

    @Override
    @NotNull
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }
}
