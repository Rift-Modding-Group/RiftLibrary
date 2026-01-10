package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import anightdazingzoroark.riftlib.hitboxLogic.IMultiHitboxUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class FlyingPufferfishEntity extends EntityFlying implements IAnimatable, IMultiHitboxUser {
    private static final DataParameter<Boolean> RESET = EntityDataManager.createKey(FlyingPufferfishEntity.class, DataSerializers.BOOLEAN);
    private final AnimationFactory factory = new AnimationFactory(this);
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
        this.updateParts();

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
    public float scale() {
        return 2f;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "goUp", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                if (canReset()) {
                    event.getController().clearAnimationCache();
                    return PlayState.STOP;
                }
                else {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.flying_pufferfish.go_up", LoopType.HOLD_ON_LAST_FRAME));
                    return PlayState.CONTINUE;
                }
            }
        }));

        data.addAnimationController(new AnimationController(this, "puff", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.flying_pufferfish.inflate_loop", LoopType.LOOP));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
