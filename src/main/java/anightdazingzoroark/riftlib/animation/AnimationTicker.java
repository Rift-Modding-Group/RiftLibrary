package anightdazingzoroark.riftlib.animation;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AnimationTicker {
	private final AbstractAnimationData<?> data;

	public AnimationTicker(AbstractAnimationData<?> data) {
		this.data = data;
	}

	@SubscribeEvent
	public void tickEvent(TickEvent.ClientTickEvent event) {
		if (Minecraft.getMinecraft().isGamePaused() && !data.shouldPlayWhilePaused) {
			return;
		}

		if (event.phase == TickEvent.Phase.END) {
			//tick animations
			this.data.tick++;

			//5 tick delay to remove
			this.data.tickAnimatedLocators();
		}
	}
}
