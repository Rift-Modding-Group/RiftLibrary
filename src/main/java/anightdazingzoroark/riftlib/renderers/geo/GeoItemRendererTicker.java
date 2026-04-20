package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

//cleaning up all itemrenderers happens here
public class GeoItemRendererTicker {
	public static final Set<GeoItemRenderer<?>> ITEM_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		long now = Minecraft.getSystemTime();
		for (GeoItemRenderer<?> renderer : ITEM_RENDERERS) {
			renderer.cleanupHolderCache(now);
		}
	}
}
