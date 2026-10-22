package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Animation data for entities extending EntityLivingBase.
 * */
public class AnimationDataEntityLiving extends AbstractAnimationDataAdvancedEntity<EntityLivingBase, AnimationDataEntityLiving> {
    @NotNull
    private final MovementSpeedTracker movementSpeedTracker = new MovementSpeedTracker();
    @NotNull
    private final BodyRotationTracker serverBodyRotationTracker = new BodyRotationTracker();
    @NotNull
    private final HeadPitchTracker serverHeadPitchTracker = new HeadPitchTracker();
    private boolean hasServerBodyRotation;
    private float serverBodyYaw;
    private float previousServerBodyYaw;
    private boolean serverBodyRotationPending;
    private boolean hasServerHeadPitch;
    private float serverHeadPitch;
    private float previousServerHeadPitch;
    private boolean serverHeadPitchPending;

    public AnimationDataEntityLiving(@NotNull EntityLivingBase holder) {
        super(holder, getAnimatable(holder));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static IAnimatable<AnimationDataEntityLiving> getAnimatable(@NotNull EntityLivingBase holder) {
        if (holder instanceof IAnimatable<?> animatable) return (IAnimatable<AnimationDataEntityLiving>) animatable;
        throw new IllegalArgumentException("AnimationDataEntityLiving holder must implement IAnimatable");
    }

    @Override
    public void updateOnDataTick() {
        super.updateOnDataTick();

        this.movementSpeedTracker.update(
                this.getHolder().ticksExisted,
                this.getHolder().posX - this.getHolder().prevPosX,
                this.getVerticalSpeed(),
                this.getHolder().posZ - this.getHolder().prevPosZ
        );
    }

    @Override
    @NotNull
    public NBTTagCompound getNBT() {
        NBTTagCompound data = super.getNBT();

        NBTTagCompound bodyRotation = new NBTTagCompound();
        bodyRotation.setFloat("Yaw", this.getHolder().renderYawOffset);
        bodyRotation.setFloat("PreviousYaw", this.getHolder().prevRenderYawOffset);
        data.setTag("BodyRotation", bodyRotation);

        NBTTagCompound headRotation = new NBTTagCompound();
        headRotation.setFloat("Pitch", this.getHolder().rotationPitch);
        headRotation.setFloat("PreviousPitch", this.getHolder().prevRotationPitch);
        data.setTag("HeadRotation", headRotation);
        return data;
    }

    @Override
    public void readNBT(@NotNull NBTTagCompound data) {
        super.readNBT(data);

        NBTTagCompound bodyRotation = data.getCompoundTag("BodyRotation");
        this.hasServerBodyRotation = bodyRotation.hasKey("Yaw", 99) && bodyRotation.hasKey("PreviousYaw", 99);
        if (!this.hasServerBodyRotation) {
            this.serverBodyRotationPending = false;
            this.serverBodyRotationTracker.reset();
        }
        else {
            this.serverBodyYaw = bodyRotation.getFloat("Yaw");
            this.previousServerBodyYaw = bodyRotation.getFloat("PreviousYaw");
            this.serverBodyRotationPending = true;
            this.applyServerBodyRotation();
        }

        NBTTagCompound headRotation = data.getCompoundTag("HeadRotation");
        this.hasServerHeadPitch = headRotation.hasKey("Pitch", 99) && headRotation.hasKey("PreviousPitch", 99);
        if (!this.hasServerHeadPitch) {
            this.serverHeadPitchPending = false;
            this.serverHeadPitchTracker.reset();
        }
        else {
            this.serverHeadPitch = headRotation.getFloat("Pitch");
            this.previousServerHeadPitch = headRotation.getFloat("PreviousPitch");
            this.serverHeadPitchPending = true;
        }
        //live clients commit both targets at render start, after queued packets and ticks.
        if (this.getWorld() == null || !this.getWorld().isRemote) this.prepareBodyRotationFrame(0f);
    }

    /**
     * Accepts body and head-pitch targets on the same clock used to render this frame.
     * */
    public void prepareBodyRotationFrame(float partialTicks) {
        if (this.serverHeadPitchPending) {
            this.serverHeadPitchTracker.update(
                    this.previousServerHeadPitch, this.serverHeadPitch, this.getRotationTime(partialTicks)
            );
            this.serverHeadPitchPending = false;
        }
        if (!this.serverBodyRotationPending) return;
        this.serverBodyRotationTracker.update(
                this.previousServerBodyYaw, this.serverBodyYaw, this.getRotationTime(partialTicks)
        );
        this.serverBodyRotationPending = false;
    }

    private double getRotationTime(float partialTicks) {
        if (this.getWorld() == null) return this.getHolder().ticksExisted + (double) partialTicks;
        if (this.getWorld().isRemote) return LivingBodyRotationTicker.Client.getBodyRotationTime(partialTicks);
        return this.getWorld().getTotalWorldTime() + (double) partialTicks;
    }

    private boolean shouldUseServerBodyRotation() {
        return this.hasServerBodyRotation && !this.getHolder().isBeingRidden() && !this.getHolder().isRiding();
    }

    /**
     * Reapplies the server body pose after client body helpers, leaving mounted steering local.
     * */
    public boolean applyServerBodyRotation() {
        if (!this.shouldUseServerBodyRotation()) return false;
        this.getHolder().renderYawOffset = this.serverBodyYaw;
        this.getHolder().prevRenderYawOffset = this.previousServerBodyYaw;
        return true;
    }

    @Override
    public float getModelYaw() {
        if (this.getHolder().isBeingRidden() && this.getHolder() instanceof IDynamicRideUser<?> rideUser) {
            return rideUser.getRenderYaw(1f);
        }
        if (this.shouldUseServerBodyRotation()) return this.serverBodyYaw;
        return this.getHolder().renderYawOffset;
    }

    @Override
    public float getModelYaw(float partialTicks) {
        if (this.getHolder().isBeingRidden() && this.getHolder() instanceof IDynamicRideUser<?> rideUser) {
            return rideUser.getRenderYaw(partialTicks);
        }
        if (this.shouldUseServerBodyRotation()) {
            if (!this.serverBodyRotationTracker.initialized) {
                return Interpolations.lerpYaw(this.previousServerBodyYaw, this.serverBodyYaw, partialTicks);
            }
            return this.serverBodyRotationTracker.getYaw(this.getRotationTime(partialTicks));
        }
        return Interpolations.lerpYaw(this.getHolder().prevRenderYawOffset, this.getHolder().renderYawOffset, partialTicks);
    }

    /**
     * Head pitch in Blockbench degrees, positive when looking down.
     * */
    public float getHeadPitch(float partialTicks) {
        if (this.hasServerHeadPitch && !this.getHolder().isBeingRidden() && !this.getHolder().isRiding()) {
            //movement packets interpolate pitch independently and can retain an old attack pose.
            if (!this.serverHeadPitchTracker.initialized) {
                return Interpolations.lerp(this.previousServerHeadPitch, this.serverHeadPitch, partialTicks);
            }
            return this.serverHeadPitchTracker.getPitch(this.getRotationTime(partialTicks));
        }
        return Interpolations.lerp(this.getHolder().prevRotationPitch, this.getHolder().rotationPitch, partialTicks);
    }

    /**
     * Head yaw in Blockbench degrees relative to the rendered model, in [-180, 180).
     * */
    public float getHeadYaw(float partialTicks) {
        float modelYaw = this.getModelYaw(partialTicks);
        float headYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYawHead, this.getHolder().rotationYawHead, partialTicks);
        if (this.getHolder().isBeingRidden() && this.getHolder() instanceof IDynamicRideUser<?> rideUser) {
            modelYaw = rideUser.getRenderYaw(partialTicks);
            Entity controller = this.getHolder().getControllingPassenger();
            if (rideUser.canRotateMounted() && controller != null) {
                //use the same render-time rider input as steering, rather than the AI's delayed head yaw.
                headYaw = Interpolations.lerpYaw(controller.prevRotationYaw, controller.rotationYaw, partialTicks);
            }
        }
        return MathHelper.wrapDegrees(headYaw - modelYaw);
    }

    private float getAnimationPartialTicks() {
        if (this.getWorld() == null || !this.getWorld().isRemote) return 1f;
        return Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    protected void createMolangQueries() {
        super.createMolangQueries();
        this.registerMolangQuery("head_x_rotation", (values, animData) -> (double) this.getHeadPitch(this.getAnimationPartialTicks()));
        this.registerMolangQuery("head_y_rotation", (values, animData) -> (double) this.getHeadYaw(this.getAnimationPartialTicks()));
        this.registerMolangQuery("modified_move_speed", (values, animData) -> {
            if (this.getHolder().isRiding()) return 0D;
            return this.movementSpeedTracker.getAmount(this.getAnimationPartialTicks());
        });
        this.registerMolangQuery("health", (values, animData) -> {
            return (double) this.getHolder().getHealth();
        });
        this.registerMolangQuery("max_health", (values, animData) -> {
            return (double) this.getHolder().getMaxHealth();
        });
        this.registerMolangQuery("is_riding", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
        this.registerMolangQuery("is_sprinting", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isSprinting());
        });
    }

    /**
     * Linear pitch interpolation; unlike yaw, vertical angles must not wrap.
     * */
    private static class HeadPitchTracker {
        private boolean initialized;
        private double transitionStart;
        private float startPitch;
        private float targetPitch;

        private void update(float previousPitch, float pitch, double time) {
            if (!this.initialized) {
                this.initialized = true;
                this.startPitch = previousPitch;
                this.targetPitch = pitch;
                this.transitionStart = Math.floor(time);
                return;
            }
            if (pitch == this.targetPitch) return;
            this.startPitch = this.getPitch(time);
            this.targetPitch = pitch;
            this.transitionStart = time;
        }

        private float getPitch(double time) {
            float progress = (float) Math.clamp(time - this.transitionStart, 0D, 1D);
            if (progress == 1f) return this.targetPitch;
            return Interpolations.lerp(this.startPitch, this.targetPitch, progress);
        }

        private void reset() {
            this.initialized = false;
        }
    }

    /**
     * Interpolates network body targets without replaying an old turn when partial ticks wrap.
     * */
    public static class BodyRotationTracker {
        private boolean initialized;
        private double transitionStart;
        private float startYaw;
        private float targetYaw;

        public void update(float previousYaw, float yaw, double time) {
            if (!this.initialized) {
                this.initialized = true;
                this.startYaw = previousYaw;
                this.targetYaw = yaw;
                this.transitionStart = Math.floor(time);
                return;
            }
            if (MathHelper.wrapDegrees(yaw - this.targetYaw) == 0f) return;
            this.startYaw = this.getYaw(time);
            this.targetYaw = yaw;
            this.transitionStart = time;
        }

        public float getYaw(double time) {
            float progress = (float) Math.clamp(time - this.transitionStart, 0D, 1D);
            if (progress == 1f) return this.targetYaw;
            return Interpolations.lerpYaw(this.startYaw, this.targetYaw, progress);
        }

        public void reset() {
            this.initialized = false;
        }
    }

    /**
     * Return interpolated total speed of entity, for query.modified_move_speed
     */
    public static class MovementSpeedTracker {
        private boolean initialized;
        private int lastTick;
        private float previousAmount;
        private float amount;

        public void update(int tick, double dx, double dy, double dz) {
            if (this.initialized && tick == this.lastTick) return;

            this.initialized = true;
            this.lastTick = tick;
            this.previousAmount = this.amount;
            float targetAmount = Math.clamp((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 4f, 0f, 1f);
            this.amount += (targetAmount - this.amount) * 0.4f;
        }

        public float getAmount(float partialTicks) {
            return this.previousAmount + (this.amount - this.previousAmount) * Math.clamp(partialTicks, 0f, 1f);
        }
    }
}