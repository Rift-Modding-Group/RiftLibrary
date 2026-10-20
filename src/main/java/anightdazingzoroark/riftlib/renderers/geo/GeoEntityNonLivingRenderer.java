package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityNonLiving;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosSnapshot;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import anightdazingzoroark.riftlib.util.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders animated entities that do not extend EntityLivingBase.
 * */
@SuppressWarnings("unchecked")
public class GeoEntityNonLivingRenderer<A extends Entity & IAnimatable<AnimationDataEntityNonLiving>> extends Render<A> implements IGeoRenderer<A> {
    static {
        AnimationController.addModelFetcher(object -> {
            if (object instanceof Entity entity) {
                return (IAnimatableModel<Object>) AnimationUtils.getGeoModelForEntity(entity);
            }
            return null;
        });
    }

    protected AnimatedGeoModel<A> modelProvider;
    protected List<GeoEntityNonLivingLayerRenderer<A>> layerRenderers = new ArrayList<>();
    private float locatorPartialTicks;

    public GeoEntityNonLivingRenderer(@NotNull RenderManager renderManager, @NotNull AnimatedGeoModel<A> modelProvider) {
        super(renderManager);
        this.modelProvider = modelProvider;
    }

    @Override
    public void doRender(A entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.getRidingEntity() instanceof IDynamicRideUser<?> mount && !mount.ridePosList().snapshot.isRenderingPassenger(entity)) return;

        GeoModel model = this.modelProvider.getModel(entity);
        DynamicRidePosSnapshot snapshot = null;
        float yaw = entity.getAnimationData().getModelYaw(partialTicks);
        if (entity.isBeingRidden() && entity instanceof IDynamicRideUser<?> rideUser) {
            snapshot = rideUser.ridePosList().snapshot;
            yaw = rideUser.getRenderYaw(partialTicks);
        }

        if (snapshot == null || !snapshot.consumeClientAnimationPrepared(entity.world.getTotalWorldTime(), partialTicks)) {
            this.modelProvider.prepareClientAnimationPose(entity);
        }
        if (snapshot != null) {
            snapshot.storeSnapshot(partialTicks, yaw);
            snapshot.renderOriginVec = new Vec3d(entity.posX - x, entity.posY - y, entity.posZ - z);
            try {
                snapshot.cachePassengerRidePositions();
            }
            catch (RuntimeException | Error exception) {
                snapshot.restoreSnapshot();
                throw exception;
            }
            snapshot.restoreSnapshot();
        }

        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, z);
            this.applyRotations(entity, yaw, partialTicks);
            float scale = entity.getAnimationData().getScale();
            GlStateManager.scale(scale, scale, scale);
            this.bindEntityTexture(entity);
            Color color = this.getRenderColor(entity, partialTicks);
            if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().player)) {
                this.render(
                        model, entity, partialTicks,
                        color.getRed() / 255f, color.getGreen() / 255f,
                        color.getBlue() / 255f, color.getAlpha() / 255f
                );
            }
            for (GeoEntityNonLivingLayerRenderer<A> layer : this.layerRenderers) {
                layer.render(entity, partialTicks, entity.ticksExisted + partialTicks, color);
            }
        }
        catch (RuntimeException | Error exception) {
            GlStateManager.popMatrix();
            throw exception;
        }
        GlStateManager.popMatrix();
        if (snapshot != null) GeoEntityRenderHelper.renderPassengers(this.renderManager, entity, snapshot, yaw, partialTicks);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected void applyRotations(@NotNull A entity, float rotationYaw, float partialTicks) {
        GlStateManager.rotate(180f - rotationYaw, 0f, 1f, 0f);
    }

    @Override
    public void render(GeoModel model, A entity, float partialTicks, float red, float green, float blue, float alpha) {
        this.locatorPartialTicks = partialTicks;
        IGeoRenderer.super.render(model, entity, partialTicks, red, green, blue, alpha);
    }

    @Override
    public void repositionAnimatedLocators(A entity) {
        if (entity instanceof IDynamicRideUser<?>) {
            GeoEntityRenderHelper.repositionAnimatedLocators(entity, this.locatorPartialTicks);
        }
        else IGeoRenderer.super.repositionAnimatedLocators(entity);
    }

    @Override
    public boolean shouldRender(A entity, ICamera camera, double camX, double camY, double camZ) {
        if (entity instanceof IDynamicRideUser<?> && entity.isBeingRidden()) return true;
        if (super.shouldRender(entity, camera, camX, camY, camZ)) return true;
        Entity[] parts = entity.getParts();
        if (parts != null) {
            for (Entity part : parts) {
                if (camera.isBoundingBoxInFrustum(part.getEntityBoundingBox())) return true;
            }
        }
        return false;
    }

    public boolean addLayer(@NotNull GeoEntityNonLivingLayerRenderer<A> layer) {
        return this.layerRenderers.add(layer);
    }

    @Override
    @NotNull
    public ResourceLocation getEntityTexture(A entity) {
        return this.getTextureLocation(entity);
    }

    @Override
    @NotNull
    public GeoModelProvider<A> getGeoModelProvider() {
        return this.modelProvider;
    }
}
