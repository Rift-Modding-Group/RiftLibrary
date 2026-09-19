package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.core.manager.AnimationDataBlock;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GeoBlockRendererTicker {
    public static final GeoBlockRendererTicker INSTANCE = new GeoBlockRendererTicker();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void render(Entity cameraEntity, ICamera camera, float partialTicks) {
        if (MinecraftForgeClient.getRenderPass() != 0 || !AnimatedBlockRegistry.INSTANCE.hasRenderers()) return;
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<AnimatedBlockRegistry.Entry>> chunks = AnimatedBlockRegistry.INSTANCE.getChunks(cameraEntity.world);
        if (chunks == null || chunks.isEmpty()) return;
        double cameraX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * partialTicks;
        double cameraY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * partialTicks;
        double cameraZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * partialTicks;
        for (Long2ObjectOpenHashMap<AnimatedBlockRegistry.Entry> entries : chunks.values()) {
            for (AnimatedBlockRegistry.Entry entry : entries.values()) {
                if (!entry.isValid()) continue;
                GeoBlockRenderer renderer = GeoBlockRenderer.getRenderer(entry.getBlockState().getBlock());
                if (renderer == null) continue;
                BlockPos pos = entry.getPos();
                if (pos.distanceSqToCenter(cameraX, cameraY, cameraZ) > renderer.getRenderDistanceSquared()) continue;
                if (entry.getRenderBoundingBox() == null) entry.setRenderBoundingBox(renderer.getRenderBoundingBox(entry.getBlockState(), pos));
                if (!camera.isBoundingBoxInFrustum(entry.getRenderBoundingBox())) continue;
                AnimatedBlockStateHolder holder = entry.getOrCreateHolder(cameraEntity.world);
                renderer.render(holder, pos.getX() - cameraX, pos.getY() - cameraY, pos.getZ() - cameraZ, partialTicks);
            }
        }
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || minecraft.world == null) return;
        Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<AnimatedBlockRegistry.Entry>> chunks = AnimatedBlockRegistry.INSTANCE.getChunks(minecraft.world);
        if (chunks == null) return;
        for (Long2ObjectOpenHashMap<AnimatedBlockRegistry.Entry> entries : chunks.values()) {
            for (AnimatedBlockRegistry.Entry entry : entries.values()) {
                AnimatedBlockStateHolder holder = entry.getHolder();
                if (holder == null || !holder.isValid()) continue;
                AnimationDataBlock data = holder.getAnimationData();
                if (data.isServerSynced() || ServerModelRegistry.hasServerModel(holder)) continue;
                if (minecraft.isGamePaused() && !data.shouldPlayWhilePaused) continue;
                data.tick++;
                data.tickAnimatedLocators();
            }
        }
    }
}
