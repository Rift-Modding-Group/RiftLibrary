package anightdazingzoroark.example.block.tile;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import net.minecraft.tileentity.TileEntity;

public class SprinklerTileEntity extends TileEntity implements IAnimatable<AnimationDataTileEntity> {
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);

    @Override
    public void registerAnimationControllers(AnimationDataTileEntity data) {
        data.addAnimationController(new AnimationController<>(
                this, "sprinker_spin", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sprinkler.spinning", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
        data.addAnimationController(new AnimationController<>(
                this, "sprinker_water_zero", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sprinkler.hose_water_zero", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
        data.addAnimationController(new AnimationController<>(
                this, "sprinker_water_one", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sprinkler.hose_water_one", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
        data.addAnimationController(new AnimationController<>(
                this, "sprinker_water_two", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sprinkler.hose_water_two", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
        data.addAnimationController(new AnimationController<>(
                this, "sprinker_water_three", 0,
                event -> {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sprinkler.hose_water_three", LoopType.LOOP));
                    return PlayState.CONTINUE;
                }
        ));
    }

    @Override
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }
}
