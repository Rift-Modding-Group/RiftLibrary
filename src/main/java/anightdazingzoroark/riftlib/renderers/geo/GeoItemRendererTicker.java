package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import anightdazingzoroark.riftlib.item.AnimatedItemStackHolder;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class GeoItemRendererTicker {
	public static final Set<GeoItemRenderer<?>> ITEM_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());
	public static final Set<GeoArmorRenderer<?>> ARMOR_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());

	//cleaning up all stack-backed renderer holder caches happens here
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		long now = Minecraft.getSystemTime();
		for (GeoItemRenderer<?> renderer : ITEM_RENDERERS) {
			renderer.updateEquipSessions();
			renderer.cleanupHolderCache(now);
		}
		for (GeoArmorRenderer<?> renderer : ARMOR_RENDERERS) {
			renderer.cleanupHolderCache(now);
		}
	}

	//exposure of transition for item switch in first person happens here
	@SubscribeEvent
	public void onRenderSpecificHand(RenderSpecificHandEvent event) {
		Minecraft minecraft = Minecraft.getMinecraft();
		ItemStack stack = event.getItemStack();
		if (minecraft.player == null || stack.isEmpty()) return;

		TileEntityItemStackRenderer itemRenderer = stack.getItem().getTileEntityItemStackRenderer();
		if (!(itemRenderer instanceof GeoItemRenderer<?> geoItemRenderer)) return;

		EnumHandSide handSide = event.getHand() == EnumHand.MAIN_HAND
				? minecraft.player.getPrimaryHand()
				: minecraft.player.getPrimaryHand().opposite();
		ItemCameraTransforms.TransformType transformType = handSide == EnumHandSide.RIGHT
				? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
				: ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
		AnimatedItemStackHolder holder = geoItemRenderer.getOrCreateHolder(stack, transformType);
		holder.setFirstPersonEquipProgress(event.getEquipProgress());
	}
}
