package anightdazingzoroark.riftlibrary.main.renderer;

import anightdazingzoroark.riftlibrary.example.entity.RedDragonEntity;
import anightdazingzoroark.riftlibrary.main.animator.IAnimated;
import anightdazingzoroark.riftlibrary.main.geo.animated.AnimatedRiftLibModel;
import anightdazingzoroark.riftlibrary.main.geo.basic.RiftLibModel;
import anightdazingzoroark.riftlibrary.main.util.Color;
import anightdazingzoroark.riftlibrary.main.util.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.jspecify.annotations.Nullable;

public abstract class RiftLibEntityRenderer<T extends Entity & IAnimated> extends Render<T> implements IRiftLibRenderer<T> {
    private final AnimatedRiftLibModel<T> animatedModel;

    protected RiftLibEntityRenderer(RenderManager renderManager, AnimatedRiftLibModel<T> animatedModel) {
        super(renderManager);
        this.animatedModel = animatedModel;
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        //get model
        RiftLibModel model = this.animatedModel.getModel(this.animatedModel.getModelLocation(entity));

        //rest is good ol rendering code
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        boolean shouldSit = (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        /*
        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = entity.isChild();
         */

        float prevYawOffset = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).prevRenderYawOffset : entity.prevRotationYaw;
        float currentYawOffset = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).renderYawOffset : entity.rotationYaw;
        float yawOffset = Interpolations.lerpYaw(prevYawOffset, currentYawOffset, partialTicks);

        float prevHeadYaw = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).prevRotationYawHead : entity.prevRotationYaw;
        float currentHeadYaw = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead : entity.rotationYaw;
        float headYaw = Interpolations.lerpYaw(prevHeadYaw, currentHeadYaw, partialTicks);
        float netHeadYaw = headYaw - yawOffset;
        if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase livingentity) {
            yawOffset = Interpolations.lerpYaw(livingentity.prevRenderYawOffset, livingentity.renderYawOffset, partialTicks);
            netHeadYaw = headYaw - yawOffset;
            float f3 = MathHelper.wrapDegrees(netHeadYaw);
            if (f3 < -85.0F) f3 = -85.0F;

            if (f3 >= 85.0F) f3 = 85.0F;

            yawOffset = headYaw - f3;
            if (f3 * f3 > 2500.0F) yawOffset += f3 * 0.2F;

            netHeadYaw = headYaw - yawOffset;
        }

        float headPitch = Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTicks);
        /*
         * TODO: vanilla mobs can't sleep in beds in 1.12.2 and below if
         * (entity.getPose() == Pose.SLEEPING) { Direction direction =
         * entity.getBedDirection(); if (direction != null) { float f4 =
         * entity.getEyeHeight(Pose.STANDING) - 0.1F; stack.translate((double) ((float)
         * (-direction.getXOffset()) * f4), 0.0D, (double) ((float)
         * (-direction.getZOffset()) * f4)); } }
         */
        float f7 = this.handleRotationFloat(entity, partialTicks);
        if (entity instanceof EntityLivingBase entityLiving) this.applyRotations(entityLiving, f7, yawOffset, partialTicks);

        /*
        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isEntityAlive()) {
            limbSwingAmount = Interpolations.lerp(entity.prevLimbSwingAmount, entity.limbSwingAmount, partialTicks);
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);
            if (entity.isChild()) {
                limbSwing *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        entityModelData.headPitch = -headPitch;
        entityModelData.netHeadYaw = -netHeadYaw;
         */

        /*
        AnimationEvent predicate = new AnimationEvent(entity, limbSwing, limbSwingAmount, partialTicks,
                !(limbSwingAmount > -0.15F && limbSwingAmount < 0.15F), Collections.singletonList(entityModelData));
         */
        //this.modelProvider.setLivingAnimations(entity, uniqueID, predicate);
        //this.modelProvider.createAndUpdateAnimatedLocators(entity, uniqueID);

        GlStateManager.pushMatrix();
        //GlStateManager.scale(entity.scale(), entity.scale(), entity.scale());
        GlStateManager.translate(0, 0.01f, 0);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(entity));
        Color renderColor = this.getRenderColor(entity, partialTicks);

        boolean flag = entity instanceof EntityLivingBase entityLiving && this.setDoRenderBrightness(entityLiving, partialTicks);

        if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().player))
            render(model, entity, partialTicks,
                    (float) renderColor.getRed() / 255f,
                    (float) renderColor.getBlue() / 255f, (float) renderColor.getGreen() / 255f,
                    (float) renderColor.getAlpha() / 255);

        /*
        if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) {
            for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
                layerRenderer.render(entity, limbSwing, limbSwingAmount, partialTicks, limbSwing, netHeadYaw, headPitch,
                        renderColor);
            }
        }
         */

        /*
        if (entity instanceof EntityLiving) {
            Entity leashHolder = ((EntityLiving) entity).getLeashHolder();
            if (leashHolder != null) {
                this.renderLeash((EntityLiving) entity, x, y, z, entityYaw, partialTicks);
            }
        }
         */

        if (flag) RenderHurtColor.unset();

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    protected float handleRotationFloat(T livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    protected void applyRotations(EntityLivingBase entityLiving, float ageInTicks, float rotationYaw, float partialTicks) {
        if (!entityLiving.isPlayerSleeping()) {
            GlStateManager.rotate(180.0F - rotationYaw, 0, 1, 0);
        }

        if (entityLiving.deathTime > 0) {
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(entityLiving), 0, 0, 1);
        }
        else if (entityLiving.hasCustomName() || entityLiving instanceof EntityPlayer) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());
            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer)
                    || ((EntityPlayer) entityLiving).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0D, (double) (entityLiving.height + 0.1F), 0.0D);
                GlStateManager.rotate(180, 0, 0, 1);
            }
        }
    }

    protected float getDeathMaxRotation(EntityLivingBase entityLiving) {
        return 90f;
    }

    protected boolean setDoRenderBrightness(EntityLivingBase entityLivingBaseIn, float partialTicks) {
        return RenderHurtColor.set(entityLivingBaseIn, partialTicks);
    }

    @Override
    protected @Nullable ResourceLocation getEntityTexture(T entity) {
        return this.animatedModel.getTextureLocation(entity);
    }
}
