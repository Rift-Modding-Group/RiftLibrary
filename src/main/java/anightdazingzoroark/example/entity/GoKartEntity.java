package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerNew;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GoKartEntity extends EntityCreature implements IAnimatable<AnimationDataEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);

    public GoKartEntity(World worldIn) {
        super(worldIn);
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.isRiding()) {
            this.getNavigator().clearPath();
            player.startRiding(this, true);
        }
        return super.processInteract(player, hand);
    }

    //ride management stuff starts here
    //also because of how simple this entity is its not an IDynamicRideUser
    @Override
    public void updatePassenger(Entity passenger) {
        if (!this.isPassenger(passenger)) return;

        this.rotationYaw = passenger.rotationYaw;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch = passenger.rotationPitch * 0.5f;
        this.setRotation(this.rotationYaw, this.rotationPitch);
        this.renderYawOffset = this.rotationYaw;

        passenger.setPosition(this.posX, this.posY - 0.25D, this.posZ);
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) return passenger;
        }
        return null;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBeingRidden()) {
            EntityLivingBase controller = (EntityLivingBase)this.getControllingPassenger();
            if (controller != null) {
                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;

                if (forward <= 0f) forward *= 0.25f;

                //movement
                this.stepHeight = 1f;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1f;
                this.fallDistance = 0;
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = (float)Math.max(0, this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() - riderSpeed);
                this.setAIMoveSpeed(moveSpeed);

                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5f;
            this.jumpMovementFactor = 0.02f;
            super.travel(strafe, vertical, forward);
        }
    }
    //ride management stuff ends here

    @Override
    public List<AnimationControllerNew<?, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                new AnimationControllerNew<GoKartEntity, AnimationDataEntity>(
                        this, "movement", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.go_kart.move", AnimationDataEntity::isMoving)
                )
        );
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
