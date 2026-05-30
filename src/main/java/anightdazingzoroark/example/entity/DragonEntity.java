package anightdazingzoroark.example.entity;

import anightdazingzoroark.example.entity.ai.DragonAttackAI;
import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosList;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class DragonEntity extends EntityCreature implements IAnimatable<DragonEntity, AnimationDataEntity>, IRayCreator<DragonEntity>, IMultiHitboxUser<DragonEntity>, IDynamicRideUser<DragonEntity> {
    private static final DataParameter<Boolean> BREATHING_FIRE = EntityDataManager.createKey(DragonEntity.class, DataSerializers.BOOLEAN);
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);
    private final DynamicRidePosList ridePositions;
    private final Map<String, RiftLibRay.Builder> rayMap;
    private HitboxDefinitionList hitboxDefinitionList;
    private Entity[] hitboxes = {};

    public DragonEntity(World worldIn) {
        super(worldIn);
        this.setSize(4f, 4f);
        this.enablePersistence();
        this.ridePositions = new DynamicRidePosList(this, this.animationData);
        this.rayMap = Map.of(
                "breatheFire", new RiftLibRay.Builder(this, "fireLocator")
                        .setRaySpeed(0.5D)
                        .setShapeSpray(8D, 1D)
                        .setSpreadOnHitBlock()
        );
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BREATHING_FIRE, false);
    }

    @Override
    protected void initEntityAI() {
        //this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityCow.class, true));

        //this.tasks.addTask(1, new DragonAttackAI(this, 1.0D));
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
    public DragonEntity getMultiHitboxUser() {
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
    public HitboxDefinitionList getHitboxDefinitionList() {
        return this.hitboxDefinitionList;
    }

    @Override
    public void setHitboxDefinitionList(HitboxDefinitionList hitboxDefinitionList) {
        this.hitboxDefinitionList = hitboxDefinitionList;
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
    public void updatePassenger(Entity passenger) {
        IDynamicRideUser.super.updatePassenger(passenger);
    }
    //ride pos stuff ends here

    //ride management stuff starts here
    public DragonEntity getDynamicRideUser() {
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
    public Map<String, RiftLibRay.Builder> getRayBuilders() {
        return this.rayMap;
    }

    @Override
    public void applyRaySegments(String rayName, BlockPos originPos, RiftLibRay.RayHitResult rayHitResult) {
        if (rayName.equals("breatheFire")) {
            for (Entity hitEntity : rayHitResult.hitEntities()) {
                if (hitEntity instanceof EntityLivingBase) {
                    hitEntity.setFire(5);
                    DamageSource dragonFireDamageSrc = DamageSource.causeMobDamage(this);
                    dragonFireDamageSrc.setFireDamage();
                    hitEntity.attackEntityFrom(dragonFireDamageSrc, (float) ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
                    this.setLastAttackedEntity(hitEntity);
                }
            }

            int fireChecks = 0;
            int fireBlocksPlaced = 0;
            for (BlockPos pos : rayHitResult.hitBlockPositions()) {
                //make sure that only 4 fire blocks are placed every tick
                //and for the amount of fire checks done to be up to 48 times
                if (fireChecks >= 48 || fireBlocksPlaced >= 4) break;
                fireChecks++;

                //1 in 8 chance to burn block
                if (this.world.rand.nextInt(8) != 0) continue;

                //skip if theres already fire at the pos
                IBlockState iBlockState = this.world.getBlockState(pos);
                if (iBlockState.getBlock() == Blocks.FIRE) continue;

                //create fire when blockpos is air and fire can exist there
                Material blockMaterial = iBlockState.getMaterial();
                if (blockMaterial == Material.AIR && Blocks.FIRE.canPlaceBlockAt(this.world, pos)) {
                    this.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                    fireBlocksPlaced++;
                }
            }
        }
    }
    //ray management stuff ends here

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
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public List<AnimationController<DragonEntity, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                /*
                new AnimationController<DragonEntity, AnimationDataEntity>(
                        this, "test", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.dragon.test")
                )
                 */
                new AnimationController<DragonEntity, AnimationDataEntity>(
                        this, "movement", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.dragon.flying")
                ),
                new AnimationController<DragonEntity, AnimationDataEntity>(
                        this, "attack", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addStateTransition("fireBreathAttack", data -> this.isBreathingFire()),
                        new AnimationControllerState<AnimationDataEntity>("fireBreathAttack")
                                .addAnimation("animation.dragon.breathe_fire_while_flying")
                                .addStateTransition("default", data -> data.allAnimationsFinished("attack"))
                                .addExitEffect(new AnimatableValue("'endBreathUse'"))
                )
        );
    }

    @Override
    public Map<String, AnimatableRunValue> createAnimationMessageEffects() {
        return Map.of(
                "startFireBreath", new AnimatableRunValue(() -> {
                    RiftLibRayHelper.createRay(this, "breatheFire");
                }, Side.SERVER),
                "endFireBreath", new AnimatableRunValue(() -> {
                    RiftLibRayHelper.killRay(this, "breatheFire");
                }, Side.SERVER),
                "endBreathUse", new AnimatableRunValue(() -> this.setBreathingFire(false), Side.CLIENT, Side.SERVER)
        );
    }
}
