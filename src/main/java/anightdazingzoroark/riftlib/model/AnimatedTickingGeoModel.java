package anightdazingzoroark.riftlib.model;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimationTickable;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.resource.RiftLibCache;

@SuppressWarnings({ "rawtypes", "unchecked" })
/**
 * I don't think anyone's gonna give 10 shits about this class
 * And neither might anyone miss this class's deprecation and deletion
 * :trollface:
 */
@Deprecated
public abstract class AnimatedTickingGeoModel<T extends IAnimatable & IAnimationTickable> extends AnimatedGeoModel<T> {
	public AnimatedTickingGeoModel() {}

	public boolean isInitialized() {
		return !this.getAnimationProcessor().getModelRendererList().isEmpty();
	}

	@Override
	public void setLivingAnimations(T entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AnimationData manager = entity.getFactory().getOrCreateAnimationData(uniqueID);
		if (manager.startTick == null) {
			manager.startTick = (double) (entity.tickTimer() + Minecraft.getMinecraft().getRenderPartialTicks());
		}

		if (!Minecraft.getMinecraft().isGamePaused() || manager.shouldPlayWhilePaused) {
			manager.tick = (entity.tickTimer() + Minecraft.getMinecraft().getRenderPartialTicks());
			double gameTick = manager.tick;
			double deltaTicks = gameTick - this.lastGameTickTime;
			this.seekTime += deltaTicks;
			this.lastGameTickTime = gameTick;
		}

		AnimationEvent<T> predicate;
		if (customPredicate == null) {
			predicate = new AnimationEvent<T>(entity, 0, 0, 0, false, Collections.emptyList());
		}
		else predicate = customPredicate;

		predicate.animationTick = this.seekTime;

		//update molang related information while the entity is rendered
		if (!Minecraft.getMinecraft().isGamePaused() || manager.shouldPlayWhilePaused) {
			manager.updateAnimationVariables();
			manager.updateMolangQueries();
		}

		if (!this.getAnimationProcessor().getModelRendererList().isEmpty()) {
			getAnimationProcessor().tickAnimation(entity, uniqueID, seekTime, predicate,
					RiftLibCache.getInstance().parser, shouldCrashOnMissing);
		}

		if (!Minecraft.getMinecraft().isGamePaused() || manager.shouldPlayWhilePaused) {
			this.codeAnimations(entity, uniqueID, customPredicate);
		}
	}

	public void codeAnimations(T entity, Integer uniqueID, AnimationEvent<?> customPredicate) {

	}
}