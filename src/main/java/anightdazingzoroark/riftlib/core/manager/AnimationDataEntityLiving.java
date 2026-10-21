package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Animation data for entities extending EntityLivingBase.
 * */
public class AnimationDataEntityLiving extends AbstractAnimationDataAdvancedEntity<EntityLivingBase, AnimationDataEntityLiving> {
    @NotNull
    private final MovementSpeedTracker movementSpeedTracker = new MovementSpeedTracker();

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
    public float getModelYaw() {
        if (this.getHolder().isBeingRidden()) return super.getModelYaw();
        return this.getHolder().renderYawOffset;
    }

    @Override
    public float getModelYaw(float partialTicks) {
        if (this.getHolder().isBeingRidden()) return super.getModelYaw(partialTicks);
        return Interpolations.lerpYaw(this.getHolder().prevRenderYawOffset, this.getHolder().renderYawOffset, partialTicks);
    }

    /**
     * Head pitch in Blockbench degrees, positive when looking down.
     * */
    public float getHeadPitch(float partialTicks) {
        return Interpolations.lerp(this.getHolder().prevRotationPitch, this.getHolder().rotationPitch, partialTicks);
    }

    /**
     * Head yaw in Blockbench degrees relative to the rendered model, in [-180, 180).
     * */
    public float getHeadYaw(float partialTicks) {
        float modelYaw = this.getModelYaw(partialTicks);
        if (this.getHolder().isBeingRidden() && this.getHolder() instanceof IDynamicRideUser<?> rideUser) {
            modelYaw = rideUser.getRenderYaw(partialTicks);
        }
        float headYaw = Interpolations.lerpYaw(this.getHolder().prevRotationYawHead, this.getHolder().rotationYawHead, partialTicks);
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