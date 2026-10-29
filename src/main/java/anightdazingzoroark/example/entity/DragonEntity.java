package anightdazingzoroark.example.entity;

import anightdazingzoroark.example.entity.ai.DragonAttackAI;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.hitbox.MultiHitboxList;
import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import anightdazingzoroark.riftlib.ray.*;
import anightdazingzoroark.riftlib.ray.rayShape.motion.RiftLibRayBoxMotionShape;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.List;

public class DragonEntity extends EntityCreature implements IAnimatable<AnimationDataEntityLiving>, IMultiHitboxUser<DragonEntity>, IDynamicRideUser<DragonEntity> {
    private static final DataParameter<Boolean> BREATHING_FIRE = EntityDataManager.createKey(DragonEntity.class, DataSerializers.BOOLEAN);
    @NotNull
    private final AnimationDataEntityLiving animationData = new AnimationDataEntityLiving(this);
    private final MultiHitboxList<DragonEntity> multiHitboxList;
    @NotNull
    private final DynamicRidePosList ridePositions;
    @NotNull
    private static final RiftLibRayBuilder FIRE_BREATH_RAY = new RiftLibRayBuilder("riftlib:dragon_fire_breath")
            .setMotionThenImpact()
            .setMotionSpeed(1D)
            .setMaxMotionDistance(12D)
            .setMovementShape(() -> new RiftLibRayBoxMotionShape(
                    0.1D, 4D,
                    false, false
            ))
            .setImpactShape(RiftLibRaySphereImpactShape::new)
            .setHitHandler((rayCreator, originPos, rayHitResult) -> {
                if (!(rayCreator instanceof DragonEntity dragon)) return;
                for (Entity hitEntity : rayHitResult.hitEntities()) {
                    if (hitEntity instanceof EntityLivingBase) {
                        hitEntity.setFire(5);
                        DamageSource dragonFireDamageSrc = DamageSource.causeMobDamage(dragon);
                        dragonFireDamageSrc.setFireDamage();
                        hitEntity.attackEntityFrom(dragonFireDamageSrc, (float) ((int) dragon.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
                        dragon.setLastAttackedEntity(hitEntity);
                    }
                }
            });

    public DragonEntity(World worldIn) {
        super(worldIn);
        this.setSize(4f, 4f);
        this.enablePersistence();
        this.multiHitboxList = new MultiHitboxList<>(this, this.animationData);
        this.ridePositions = new DynamicRidePosList(this, this.animationData);
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BREATHING_FIRE, false);
    }

    @Override
    protected void initEntityAI() {
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCow.class, true));

        this.tasks.addTask(1, new DragonAttackAI(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWanderAvoidWater(this, 1D) {
            //-----no moving around when ridden cause fuck you xd-----
            @Override
            public boolean shouldExecute() {
                if (this.entity.isBeingRidden()) return false;
                return super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                if (this.entity.isBeingRidden()) return false;
                return super.shouldExecute();
            }
        });
        this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8f));
        this.tasks.addTask(4, new EntityAILookIdle(this) {
            private DragonEntity dragonEntity;

            private EntityAILookIdle initAnonymous(DragonEntity dragonEntity) {
                this.dragonEntity = dragonEntity;
                return this;
            }

            @Override
            public boolean shouldExecute() {
                if (this.dragonEntity.isBeingRidden()) return false;
                return super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                if (this.dragonEntity.isBeingRidden()) return false;
                return super.shouldExecute();
            }
        }.initAnonymous(this)); //thank you stackoverflow xd
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
            if (!this.world.isRemote) {
                this.getNavigator().clearPath();
                this.setAttackTarget(null);
            }
            player.startRiding(this, true);
        }
        return super.processInteract(player, hand);
    }

    //hitbox stuff starts here
    @Override
    @NotNull
    public DragonEntity getMultiHitboxUser() {
        return this;
    }

    @Override
    @NotNull
    public MultiHitboxList<DragonEntity> getMultiHitboxList() {
        return this.multiHitboxList;
    }

    @Override
    public float hitboxDamageMultiplier(RiftLibCollisionHitbox<DragonEntity> collisionHitbox, DamageSource source) {
        return collisionHitbox.hasHitboxTag("head") ? 5f : 1;
    }

    @Override
    public Entity[] getParts() {
        return this.multiHitboxList.getHitboxesAsArray();
    }
    //hitbox stuff ends here

    //ride pos stuff starts here
    @Override
    @NotNull
    public DynamicRidePosList ridePosList() {
        return this.ridePositions;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        IDynamicRideUser.super.updatePassenger(passenger);
    }
    //ride pos stuff ends here

    //ride management stuff starts here
    public DragonEntity getDynamicRideUser() {
        return this;
    }

    @Override
    @NotNull
    public List<String> locatorRidePositions() {
        return List.of("passengerRidePosition");
    }

    @Override
    @Nullable
    public String locatorControllerPosition() {
        return "controllerRidePosition";
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

    public boolean isBreathingFire() {
        return this.dataManager.get(BREATHING_FIRE);
    }

    public void setBreathingFire(boolean value) {
        this.dataManager.set(BREATHING_FIRE, value);
    }

    public float attackWidth() {
        return 8f;
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataEntityLiving animationData) {
        /*
        animationData.addAnimationController(new AnimationController<DragonEntity, AnimationDataEntityLiving>(
                this, "test", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default")
                        .addAnimation("animation.dragon.test")
        ));
         */
        //---scaling---
        animationData.setScale(3f);

        //---animations---
        animationData.addAnimation("animation.dragon.flying");
        animationData.addAnimationController(new AnimationController<DragonEntity, AnimationDataEntityLiving>(
                this, "attack", "default",
                new AnimationControllerState<AnimationDataEntityLiving>("default")
                        .addStateTransition("fireBreathAttack", data -> this.isBreathingFire()),
                new AnimationControllerState<AnimationDataEntityLiving>("fireBreathAttack")
                        .addAnimation("animation.dragon.breathe_fire_while_flying")
                        .addStateTransition("default", data -> data.allAnimationsFinished("attack"))
                        .addExitEffect(data -> data.sendMessage("endBreathUse"))
        ));

        animationData.addAnimationMessageEffect("startFireBreath", new AnimatableRunValue(() -> RiftLibRayHelper.createRay(FIRE_BREATH_RAY, "fireLocator", this), Side.SERVER));
        animationData.addAnimationMessageEffect("endFireBreath", new AnimatableRunValue(() -> RiftLibRayHelper.killRay(this, FIRE_BREATH_RAY.getName()), Side.SERVER));
        animationData.addAnimationMessageEffect("endBreathUse", new AnimatableRunValue(() -> this.setBreathingFire(false), Side.CLIENT, Side.SERVER));
    }

    @Override
    @NotNull
    public AnimationDataEntityLiving getAnimationData() {
        return this.animationData;
    }
}
