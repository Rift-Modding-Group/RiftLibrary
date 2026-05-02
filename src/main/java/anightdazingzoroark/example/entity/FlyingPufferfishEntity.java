package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

import java.util.List;

public class FlyingPufferfishEntity extends EntityFlying implements IAnimatable<AnimationDataEntity>, IMultiHitboxUser {
    private static final DataParameter<Boolean> RESET = EntityDataManager.createKey(FlyingPufferfishEntity.class, DataSerializers.BOOLEAN);
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);
    private Entity[] hitboxes = {};
    private int resetTick;

    public FlyingPufferfishEntity(World worldIn) {
        super(worldIn);
        this.setSize(1f, 1f);
        this.initializeHitboxes(this);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RESET, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote) {
            if (this.canReset()) {
                this.resetTick++;
                if (this.resetTick >= 5) {
                    this.setReset(false);
                    this.resetTick = 0;
                }
            }
            else {
                this.resetTick++;
                if (this.resetTick >= 40) {
                    this.setReset(true);
                    this.resetTick = 0;
                }
            }
        }
    }

    public boolean canReset() {
        return this.dataManager.get(RESET);
    }

    public void setReset(boolean value) {
        this.dataManager.set(RESET, value);
    }

    //hitbox stuff starts here
    @Override
    public Entity getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return 2f;
    }

    @Override
    public Entity[] getParts() {
        return this.hitboxes;
    }

    @Override
    public void setParts(Entity[] hitboxes) {
        this.hitboxes = hitboxes;
    }

    @Override
    public World getWorld() {
        return this.world;
    }
    //hitbox stuff ends here

    @Override
    public List<AnimationController<?, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                new AnimationController<FlyingPufferfishEntity, AnimationDataEntity>(
                        this, "goUp", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.flying_pufferfish.go_up")
                                .addStateTransition("ascend", data -> this.canReset()),
                        new AnimationControllerState<AnimationDataEntity>("ascend")
                                .addStateTransition("default", data -> !this.canReset())
                ),
                new AnimationController<FlyingPufferfishEntity, AnimationDataEntity>(
                        this, "puff", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.flying_pufferfish.inflate_loop")
                )
        );
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
