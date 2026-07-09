package anightdazingzoroark.riftlib.renderers.debug;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class WorldSpaceBoundingBoxRenderer {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || !this.hasAnyDisplayedAABB(minecraft)) return;

        double viewerX = minecraft.getRenderManager().viewerPosX;
        double viewerY = minecraft.getRenderManager().viewerPosY;
        double viewerZ = minecraft.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0f, 1f, 0.35f, 1f);

        GL11.glBegin(GL11.GL_LINES);
        for (Entity entity : minecraft.world.loadedEntityList) {
            AnimationDataEntity animData = this.getAnimationData(entity);
            if (animData == null) continue;

            for (AxisAlignedBB aabb : animData.getDisplayedWorldSpaceAABBs().values()) {
                this.drawBoundingBox(aabb.offset(-viewerX, -viewerY, -viewerZ));
            }
        }
        GL11.glEnd();

        GL11.glLineWidth(1f);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private boolean hasAnyDisplayedAABB(Minecraft minecraft) {
        for (Entity entity : minecraft.world.loadedEntityList) {
            AnimationDataEntity animData = this.getAnimationData(entity);
            if (animData != null && animData.hasDisplayedWorldSpaceAABBs()) return true;
        }
        return false;
    }

    private AnimationDataEntity getAnimationData(Entity entity) {
        if (!(entity instanceof IAnimatable<?> animatable)) return null;
        if (animatable.getAnimationData() instanceof AnimationDataEntity animData) return animData;
        return null;
    }

    private void drawBoundingBox(AxisAlignedBB aabb) {
        this.drawLine(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.minZ);
        this.drawLine(aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        this.drawLine(aabb.maxX, aabb.minY, aabb.maxZ, aabb.minX, aabb.minY, aabb.maxZ);
        this.drawLine(aabb.minX, aabb.minY, aabb.maxZ, aabb.minX, aabb.minY, aabb.minZ);

        this.drawLine(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ);
        this.drawLine(aabb.maxX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
        this.drawLine(aabb.maxX, aabb.maxY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.maxZ);
        this.drawLine(aabb.minX, aabb.maxY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.minZ);

        this.drawLine(aabb.minX, aabb.minY, aabb.minZ, aabb.minX, aabb.maxY, aabb.minZ);
        this.drawLine(aabb.maxX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.minZ);
        this.drawLine(aabb.maxX, aabb.minY, aabb.maxZ, aabb.maxX, aabb.maxY, aabb.maxZ);
        this.drawLine(aabb.minX, aabb.minY, aabb.maxZ, aabb.minX, aabb.maxY, aabb.maxZ);
    }

    private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }
}
