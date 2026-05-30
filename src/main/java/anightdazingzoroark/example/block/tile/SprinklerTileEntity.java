package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class SprinklerTileEntity extends TileEntity implements IAnimatable<SprinklerTileEntity, AnimationDataTileEntity> {
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);

    @Override
    public void initializeAnimationData(AnimationDataTileEntity animationData) {
        animationData.addAnimationController(new AnimationController<SprinklerTileEntity, AnimationDataTileEntity>(
                this, "sprinkler", "default",
                new AnimationControllerState<AnimationDataTileEntity>("default")
                        .addAnimation("animation.sprinkler.spinning")
                        .addAnimation("animation.sprinkler.hose_water_zero")
                        .addAnimation("animation.sprinkler.hose_water_one")
                        .addAnimation("animation.sprinkler.hose_water_two")
                        .addAnimation("animation.sprinkler.hose_water_three")
        ));
    }

    @Override
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }
}
