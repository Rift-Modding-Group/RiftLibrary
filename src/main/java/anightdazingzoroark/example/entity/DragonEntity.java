package anightdazingzoroark.example.entity;

import anightdazingzoroark.example.entity.ai.DragonAttackAI;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosList;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonEntity extends EntityCreature implements IAnimatable<AnimationDataEntity>, IRayCreator<DragonEntity>, IMultiHitboxUser, IDynamicRideUser {
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(DragonEntity.class, DataSerializers.BOOLEAN);
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);
    private final Map<String, RiftLibRay> rayMap;
    private Entity[] hitboxes = {};
    private DynamicRidePosList ridePositions;

    public DragonEntity(World worldIn) {
        super(worldIn);
        this.setSize(4f, 4f);
        this.initializeHitboxes(this);
        this.enablePersistence();
        this.rayMap = Map.of(
                "breatheFire", new RiftLibRay(this, "fireLocator", 16D, 1D, 0.2D, 0.2D)
        );
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACKING, false);
    }

    @Override
    protected void initEntityAI() {
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCow.class, true));

        this.tasks.addTask(1, new DragonAttackAI(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(4, new EntityAILookIdle(this));
    }

    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.isRiding()) {
            this.getNavigator().clearPath();
            //this.setAttackTarget(null);
            player.startRiding(this, true);
        }
        return super.processInteract(player, hand);
    }

    //hitbox stuff starts here
    @Override
    public Entity getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return 3f;
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

    //ride pos stuff starts here
    @Override
    public DynamicRidePosList ridePosList() {
        return this.ridePositions;
    }

    @Override
    public float dynamicRiderUserScale() {
        return 3f;
    }

    @Override
    public void setRidePosition(DynamicRidePosList dynamicRidePosList) {
        this.ridePositions = dynamicRidePosList;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        IDynamicRideUser.super.updatePassenger(passenger);
    }
    //ride pos stuff ends here

    //ride management stuff starts here
    public EntityLiving getDynamicRideUser() {
        return this;
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
                if (this.getAttackTarget() != null) {
                    this.setAttackTarget(null);
                    this.getNavigator().clearPath();
                }

                strafe = controller.moveStrafing * 0.5f;
                forward = controller.moveForward;

                if (forward <= 0.0F) forward *= 0.25F;

                //movement
                this.stepHeight = 1.0F;
                this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;
                this.fallDistance = 0;
                float riderSpeed = (float) (controller.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                float moveSpeed = (float)Math.max(0, this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() - riderSpeed);
                this.setAIMoveSpeed(moveSpeed);

                super.travel(strafe, vertical, forward);
            }
        }
        else {
            this.stepHeight = 0.5F;
            this.jumpMovementFactor = 0.02F;
            super.travel(strafe, vertical, forward);
        }
    }
    //ride management stuff ends here

    //ray management stuff starts here


    @Override
    public float rayCreatorScale() {
        return 3f;
    }

    @Override
    public DragonEntity getRayCreator() {
        return this;
    }

    @Override
    public Map<String, RiftLibRay> getRays() {
        return this.rayMap;
    }

    @Override
    public void applyRayVectorResult(String rayName, List<AxisAlignedBB> beamCollisionBoxes) {

    }
    //ray management stuff ends here

    public boolean isAttacking() {
        return this.dataManager.get(ATTACKING);
    }

    public void setAttacking(boolean value) {
        this.dataManager.set(ATTACKING, value);
    }

    public float attackWidth() {
        return 5f;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (entityIn == null) return false;
        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (flag) {
            this.applyEnchantments(this, entityIn);
        }
        this.setLastAttackedEntity(entityIn);
        return flag;
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public List<AnimationController<?, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                new AnimationController<DragonEntity, AnimationDataEntity>(
                        this, "movement", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.dragon.flying")
                ),
                new AnimationController<DragonEntity, AnimationDataEntity>(
                        this, "attack", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addStateTransition("attack", data -> this.isAttacking()),
                        new AnimationControllerState<AnimationDataEntity>("attack")
                                .addAnimation("animation.dragon.attack_while_flying")
                                .addStateTransition("default", data -> data.allAnimationsFinished("attack"))
                                .addExitEffect(new AnimatableValue("'endAttack'"))
                )
        );
    }

    @Override
    public Map<String, AnimatableRunValue> createAnimationMessageEffects() {
        return Map.of(
                "performAttack", new AnimatableRunValue(() -> this.attackEntityAsMob(this.getAttackTarget()), Side.SERVER),
                "endAttack", new AnimatableRunValue(() -> this.setAttacking(false), Side.CLIENT, Side.SERVER)
        );
    }
}
