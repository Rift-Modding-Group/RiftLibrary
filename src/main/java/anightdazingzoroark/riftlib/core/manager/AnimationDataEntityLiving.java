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

    private float getHeadRotationPartialTicks() {
        if (this.getWorld() == null || !this.getWorld().isRemote) return 1f;
        return Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    protected void createMolangQueries() {
        super.createMolangQueries();
        this.registerMolangQuery("head_x_rotation", (values, animData) -> (double) this.getHeadPitch(this.getHeadRotationPartialTicks()));
        this.registerMolangQuery("head_y_rotation", (values, animData) -> (double) this.getHeadYaw(this.getHeadRotationPartialTicks()));
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
}