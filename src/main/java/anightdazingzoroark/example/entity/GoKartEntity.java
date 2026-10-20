package anightdazingzoroark.example.entity;

import anightdazingzoroark.example.registry.ItemRegistry;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class GoKartEntity extends Entity implements IAnimatable<AnimationDataEntityNonLiving> {
    @NotNull
    private final AnimationDataEntityNonLiving animationData = new AnimationDataEntityNonLiving(this);
    private int interpolationSteps;
    private double targetX;
    private double targetY;
    private double targetZ;
    private float targetYaw;
    private float targetPitch;

    public GoKartEntity(@NotNull World worldIn) {
        super(worldIn);
        this.setSize(1.125f, 0.75f);
        this.preventEntitySpawning = true;
        this.stepHeight = 0.5f;
    }

    @Override
    protected void entityInit() {}

    // Entity already saves position, rotation, motion, and passengers.
    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {}

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {}

    @Override
    public boolean processInitialInteract(@NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (player.isSneaking() || player.isSpectator() || player.isRiding() || this.isBeingRidden()) return false;
        return this.world.isRemote || player.startRiding(this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public boolean canBePushed() {
        return !this.isDead;
    }

    @Override
    @NotNull
    public ItemStack getPickedResult(@NotNull RayTraceResult target) {
        return new ItemStack(ItemRegistry.GO_KART);
    }

    @Override
    public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
        if (this.isDead || amount <= 0f || this.isEntityInvulnerable(source)) return false;
        if (source.getTrueSource() != null && this.isPassenger(source.getTrueSource())) return false;
        if (this.world.isRemote) return true;

        this.removePassengers();
        this.setDead();
        boolean creativeAttacker = source.getTrueSource() instanceof EntityPlayer player && player.capabilities.isCreativeMode;
        if (!creativeAttacker && this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.entityDropItem(new ItemStack(ItemRegistry.GO_KART), 0f);
        }
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.stepHeight = this.isBeingRidden() ? 1f : 0.5f;
        if (!this.canPassengerSteer()) {
            this.interpolatePosition();
            this.motionX = 0D;
            this.motionY = 0D;
            this.motionZ = 0D;
        }
        else {
            // The driver's client simulates a ridden kart; vanilla vehicle packets
            // relay its position to the server. Unoccupied karts move on the server.
            this.interpolationSteps = 0;
            float strafe = 0f;
            float forward = 0f;
            float movementSpeed = 0f;
            if (this.getControllingPassenger() instanceof EntityPlayer driver) {
                this.rotationYaw = driver.rotationYaw;
                this.rotationPitch = driver.rotationPitch * 0.5f;
                strafe = Math.clamp(driver.moveStrafing, -1f, 1f) * 0.5f;
                forward = Math.clamp(driver.moveForward, -1f, 1f);
                if (forward < 0f) forward *= 0.25f;
                movementSpeed = (float) Math.max(0D, 0.25D - driver.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
                this.fallDistance = 0f;
            }

            //proper ground movement
            float friction = this.getMovementFriction();
            float acceleration;
            double gravity;
            double verticalDrag;
            if (this.isInWater()) {
                acceleration = 0.02f;
                friction = 0.8f;
                gravity = 0.02D;
                verticalDrag = 0.8D;
            }
            else if (this.isInLava()) {
                acceleration = 0.02f;
                friction = 0.5f;
                gravity = 0.02D;
                verticalDrag = 0.5D;
            }
            else {
                if (this.onGround) acceleration = movementSpeed * 0.16277136f / (friction * friction * friction);
                else acceleration = movementSpeed * 0.1f;
                gravity = 0.08D;
                verticalDrag = 0.98D;
            }

            this.moveRelative(strafe, 0f, forward, acceleration);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            if (!this.hasNoGravity()) this.motionY -= gravity;
            this.motionY *= verticalDrag;
            this.motionX *= friction;
            this.motionZ *= friction;
        }
    }

    protected float getMovementFriction() {
        if (!this.onGround) return 0.91f;
        BlockPos groundPos = new BlockPos(this.posX, this.getEntityBoundingBox().minY - 1D, this.posZ);
        IBlockState ground = this.world.getBlockState(groundPos);
        return Math.max(0.01f, ground.getBlock().getSlipperiness(ground, this.world, groundPos, this) * 0.91f);
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int increments, boolean teleport) {
        if (teleport) {
            this.setPosition(x, y, z);
            this.setRotation(yaw, pitch);
            this.interpolationSteps = 0;
        }
        else {
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            this.targetYaw = yaw;
            this.targetPitch = pitch;
            this.interpolationSteps = Math.max(1, increments);
        }
    }

    private void interpolatePosition() {
        if (this.interpolationSteps <= 0) return;
        this.setPosition(
                this.posX + (this.targetX - this.posX) / this.interpolationSteps,
                this.posY + (this.targetY - this.posY) / this.interpolationSteps,
                this.posZ + (this.targetZ - this.posZ) / this.interpolationSteps
        );
        this.setRotation(
                this.rotationYaw + MathHelper.wrapDegrees(this.targetYaw - this.rotationYaw) / this.interpolationSteps,
                this.rotationPitch + (this.targetPitch - this.rotationPitch) / this.interpolationSteps
        );
        this.interpolationSteps--;
    }

    @Override
    public void updatePassenger(@NotNull Entity passenger) {
        if (!this.isPassenger(passenger)) return;
        if (passenger == this.getControllingPassenger()) {
            this.rotationYaw = passenger.rotationYaw;
            this.rotationPitch = passenger.rotationPitch * 0.5f;
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        passenger.setPosition(this.posX, this.posY + this.getMountedYOffset(), this.posZ);
    }

    @Override
    public double getMountedYOffset() {
        return -0.25D;
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
    public void initializeAnimationData(@NonNull AnimationDataEntityNonLiving animationData) {
        animationData.addAnimationController(new AnimationController<GoKartEntity, AnimationDataEntityNonLiving>(
                this, "movement", "default",
                new AnimationControllerState<AnimationDataEntityNonLiving>("default")
                        .addAnimation("animation.go_kart.move", AnimationDataEntityNonLiving::isMoving)
        ));
    }

    @Override
    @NotNull
    public AnimationDataEntityNonLiving getAnimationData() {
        return this.animationData;
    }
}
