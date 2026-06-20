package anightdazingzoroark.riftlib.ray;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RayTicker {
    private static void updateRays(List<RiftLibRay> rayPairList) {
        //iterate over ray list
        Iterator<RiftLibRay> it = rayPairList.iterator();
        while (it.hasNext()) {
            RiftLibRay ray = it.next();

            //update the ray
            ray.onUpdate();

            //remove from list of rays if dead
            if (ray.isDead()) it.remove();
        }
    }

    public static class Server {
        public static final List<RiftLibRay> RAY_LIST = new ArrayList<>();

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            updateRays(RAY_LIST);
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (event.getWorld().isRemote) return;
            RAY_LIST.clear();
        }
    }

    public static class Client {
        public static final List<RiftLibRay> RAY_LIST = new ArrayList<>();

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onClientTick(TickEvent.ClientTickEvent event) {
            //do not tick if the game is paused
            if (Minecraft.getMinecraft().isGamePaused()) return;

            if (event.phase != TickEvent.Phase.END) return;
            updateRays(RAY_LIST);
        }

        /**
         * Show the ray boundaries when F3+B is enabled
         * */
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onRenderWorldLast(RenderWorldLastEvent event) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (!minecraft.getRenderManager().isDebugBoundingBox()) return;

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

            GL11.glColor4f(1f, 0.25f, 0f, 1f);
            GL11.glBegin(GL11.GL_LINES);
            for (RiftLibRay ray : RAY_LIST) {
                if (ray.isDead()) continue;

                ray.forEachDebugLine(event.getPartialTicks(), (start, end) -> {
                    GL11.glVertex3d(start.x - viewerX, start.y - viewerY, start.z - viewerZ);
                    GL11.glVertex3d(end.x - viewerX, end.y - viewerY, end.z - viewerZ);
                });
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

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            RAY_LIST.clear();
        }
    }
}
