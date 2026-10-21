package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps pathing body rotation independent of the entity's body helper implementation.
 * */
@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin {
    @Inject(method = "updateDistance(FF)F", at = @At("RETURN"), require = 1)
    private void riftlib$alignPathingBody(float distance, float limbSwing, @NotNull CallbackInfoReturnable<Float> callback) {
        EntityLiving entity = (EntityLiving) (Object) this;
        if (!(entity instanceof IAnimatable<?> animatable) || !(animatable.getAnimationData() instanceof AnimationDataEntityLiving)) return;
        if (entity.isAIDisabled() || entity.isRiding() || entity.isBeingRidden()) return;
        if (entity.getNavigator().noPath() && !entity.getMoveHelper().isUpdating()) return;

        //Run after the body helper so custom helper implementations run normally.
        //Follow the movement heading even before the entity has been displaced.
        entity.renderYawOffset = entity.rotationYaw;
        float headYaw = MathHelper.wrapDegrees(entity.rotationYawHead - entity.renderYawOffset);
        entity.rotationYawHead = entity.renderYawOffset + Math.clamp(headYaw, -75f, 75f);
    }
}
