package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.entity.EntityLivingBase;
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
        return this.getHolder().rotationYawHead;
    }

    @Override
    public float getModelYaw(float partialTicks) {
        if (this.getHolder().isBeingRidden()) return super.getModelYaw(partialTicks);
        return Interpolations.lerpYaw(this.getHolder().prevRotationYawHead, this.getHolder().rotationYawHead, partialTicks);
    }

    @Override
    protected void createMolangQueries() {
        super.createMolangQueries();
        //---normal stuff---
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