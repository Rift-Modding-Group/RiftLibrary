package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateOrDestroyRay;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateRayInServer;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RayTicker {
    public static final List<ImmutablePair<IRayCreator<?>, RiftLibRay>> RAY_PAIR_LIST = new ArrayList<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        //do not tick if the game is paused
        if (Minecraft.getMinecraft().isGamePaused()) return;

        //iterate over ray list
        Iterator<ImmutablePair<IRayCreator<?>, RiftLibRay>> it = RAY_PAIR_LIST.iterator();
        while (it.hasNext()) {
            ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair = it.next();

            //update the ray
            rayPair.getRight().onUpdate();

            //get the ray info as an AABB list
            List<AxisAlignedBB> rayAABB = rayPair.getRight().createAABBListFromRay();

            //send aabb list to server
            String rayName = null;
            for (Map.Entry<String, RiftLibRay> rayNameEntry : rayPair.getLeft().getRays().entrySet()) {
                if (rayNameEntry.getValue() == rayPair.getRight()) {
                    rayName = rayNameEntry.getKey();
                    break;
                }
            }
            if (rayName != null) {
                ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibCreateRayInServer(rayPair.getLeft(), rayName, rayAABB));
            }

            //remove from list of rays if dead
            if (rayPair.getRight().isDead()) it.remove();
        }
    }

    /**
     * Show the ray collisions when F3+B is enabled
     * */
    @SubscribeEvent
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

        for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RAY_PAIR_LIST) {
            if (rayPair.getRight().isDead()) continue;

            for (AxisAlignedBB aabb : rayPair.getRight().createAABBListFromRay()) {
                RenderGlobal.drawSelectionBoundingBox(
                        aabb.offset(-viewerX, -viewerY, -viewerZ).grow(0.002D),
                        1f,
                        0.25f,
                        0f,
                        1f
                );
            }
        }

        GL11.glLineWidth(1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) RAY_PAIR_LIST.clear();
    }
}
