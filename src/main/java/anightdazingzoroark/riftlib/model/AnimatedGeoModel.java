package anightdazingzoroark.riftlib.model;

import java.util.*;

import javax.annotation.Nullable;

import anightdazingzoroark.riftlib.RiftLibConfig;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import anightdazingzoroark.riftlib.hitbox.EntityHitbox;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateRiderPos;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateHitboxPos;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateHitboxSize;

import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosList;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosUtils;
import anightdazingzoroark.riftlib.ridePositionLogic.IDynamicRideUser;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import anightdazingzoroark.riftlib.animation.AnimationTicker;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.processor.AnimationProcessor;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.exceptions.GeoModelException;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.model.provider.IAnimatableModelProvider;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AnimatedGeoModel<T extends IAnimatable<?>> extends GeoModelProvider<T>
		implements IAnimatableModel<T>, IAnimatableModelProvider<T> {
	private final AnimationProcessor animationProcessor;
	protected GeoModel currentModel;

	protected AnimatedGeoModel() {
		this.animationProcessor = new AnimationProcessor();
	}

	public void registerBone(GeoBone bone) {
        this.registerModelRenderer(bone);

		for (GeoBone childBone : bone.childBones) {
			this.registerBone(childBone);
		}
	}

	@Override
	public void setLivingAnimations(T entity, @Nullable AnimationEvent customPredicate) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AbstractAnimationData<?> animData = entity.getAnimationData();
		animData.initialize();

		if (animData.ticker == null) {
			AnimationTicker ticker = new AnimationTicker(animData);
			animData.ticker = ticker;
			MinecraftForge.EVENT_BUS.register(ticker);
		}
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
            this.seekTime = animData.tick + Minecraft.getMinecraft().getRenderPartialTicks();
		}
        else this.seekTime = animData.tick;

		AnimationEvent predicate = Objects.requireNonNullElseGet(
				customPredicate, () -> new AnimationEvent((float) (animData.tick - this.lastGameTickTime), Collections.emptyList())
		);
		predicate.animationTick = this.seekTime;

		//update molang related information while the entity is rendered
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
			animData.updateAnimationVariables();
			animData.updateMolangQueries();
		}

		//update based on animations
		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, this.seekTime,
					predicate, RiftLibCache.getInstance().parser,
					this.shouldCrashOnMissing
			);
		}

		//update hitboxes
		this.setDynamicHitboxes(entity, animData);

		//update dynamic hitbox positions
		this.setDynamicRidePositions(entity, animData);
	}

	public void createAndUpdateAnimatedLocators(T entity) {
		AbstractAnimationData<?> animData = entity.getAnimationData();
		animData.initialize();
		animData.createAnimatedLocators(this.currentModel);
		animData.updateAnimatedLocators();
	}

	private void setDynamicHitboxes(T entity, AbstractAnimationData<?> animData) {
		if (!(entity instanceof IMultiHitboxUser multiHitboxUser)) return;
		List<AnimatedLocatorNew> animatedLocators = animData.getAnimatedLocators();
		for (AnimatedLocatorNew animatedLocator : animatedLocators) {
			if (!HitboxUtils.locatorCanBeHitbox(animatedLocator.getName())) continue;
			String hitboxName = HitboxUtils.locatorHitboxToHitbox(animatedLocator.getName());

			//get parent bone
			GeoBone parentBone = animatedLocator.getParentBone();

			//get hitbox associated with the locator
			EntityHitbox hitbox = multiHitboxUser.getHitboxByName(hitboxName);

			//skip if there's no hitbox
			if (hitbox == null) continue;

			//skip when hitbox is set to not be affected by animation
			if (!hitbox.affectedByAnim) continue;

			//set point in which the packets are to be sent from
			NetworkRegistry.TargetPoint targetPoint = new NetworkRegistry.TargetPoint(
					multiHitboxUser.getMultiHitboxUser().dimension,
					multiHitboxUser.getMultiHitboxUser().posX,
					multiHitboxUser.getMultiHitboxUser().posY,
					multiHitboxUser.getMultiHitboxUser().posZ,
					RiftLibConfig.HITBOX_UPDATE_RANGE
			);

			//packets for hitbox updates will not be sent if their total change
			//is too miniscule
			//get positions
			Vec3d modelSpacePos = animatedLocator.getModelSpacePosition();
			float newHitboxX = (float) modelSpacePos.x / 16f;
			float newHitboxY = (float) modelSpacePos.y / 16f - (hitbox.fixedHeight / 2f) - (parentBone.getScaleY() - 1) / 3;
			float newHitboxZ = -(float) modelSpacePos.z / 16f;

			//get magnitude of displacement
			double dPosTotal = Math.sqrt(Math.pow(newHitboxX - hitbox.getDisplacementVec().x, 2) + Math.pow(newHitboxY - hitbox.getDisplacementVec().y, 2) + Math.pow(newHitboxZ - hitbox.getDisplacementVec().z, 2));

			//update positions
			if (dPosTotal > RiftLibConfig.HITBOX_DISPLACEMENT_TOLERANCE) {
				RiftLibUpdateHitboxPos updateHitboxPos = new RiftLibUpdateHitboxPos(
						(Entity) entity,
						hitboxName,
						newHitboxX,
						newHitboxY,
						newHitboxZ
				);

				ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToAllTracking(updateHitboxPos, targetPoint);
				ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToServer(updateHitboxPos);
			}

			//get sizes
			float newHitboxWidthScale = Math.max(parentBone.getScaleX(), parentBone.getScaleZ());
			float newHitboxHeightScale = parentBone.getScaleY();

			//get magnitude of resizing
			double dSizeTotal = Math.sqrt(Math.pow(newHitboxWidthScale - hitbox.getWidthScaleDisplacement(), 2) + Math.pow(newHitboxHeightScale - hitbox.getHeightScaleDisplacement(), 2));

			//update sizes
			if (dSizeTotal > RiftLibConfig.HITBOX_RESIZING_TOLERANCE) {
				RiftLibUpdateHitboxSize updateHitboxSize = new RiftLibUpdateHitboxSize(
						(Entity) entity,
						hitboxName,
						Math.max(parentBone.getScaleX(), parentBone.getScaleZ()),
						parentBone.getScaleY()
				);

				ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToAllTracking(updateHitboxSize, targetPoint);
				ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToServer(updateHitboxSize);
			}
		}
	}

	private void setDynamicRidePositions(T entity, AbstractAnimationData<?> animData) {
		if (!(entity instanceof IDynamicRideUser)) return;

		//make a rideposdef list for changine ride positions
		DynamicRidePosList definitionList = new DynamicRidePosList();

		//make a definition list of dynamic ride positions and put in it the new positions based on the new locators positions
		for (AnimatedLocatorNew locator : animData.getAnimatedLocators()) {
			if (!DynamicRidePosUtils.locatorCanBeRidePos(locator.getName())) continue;
			definitionList.addPosition(locator);
		}

		//apply changes to ride positions
		if (definitionList.isEmpty()) return;
		ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibUpdateRiderPos(
				(Entity) entity, definitionList
		));
		ServerProxy.MESSAGE_WRAPPER.sendToServer(new RiftLibUpdateRiderPos(
				(Entity) entity, definitionList
		));
	}

	@Override
	public AnimationProcessor getAnimationProcessor() {
		return this.animationProcessor;
	}

	public void registerModelRenderer(IBone modelRenderer) {
        this.animationProcessor.registerModelRenderer(modelRenderer);
	}

	@Override
	public Animation getAnimation(String name, IAnimatable<?> animatable) {
		return RiftLibCache.getInstance().getAnimations()
                .get(this.getAnimationFileLocation((T) animatable))
				.getAnimation(name);
	}

	//this is where the model is attached to the entity
    @Override
	public GeoModel getModel(ResourceLocation location) {
		GeoModel model = super.getModel(location);
		if (model == null) {
			throw new GeoModelException(location, "Could not find model.");
		}
		if (model != this.currentModel) {
            //change current model
			this.animationProcessor.clearModelRendererList();
			for (GeoBone bone : model.topLevelBones) {
                this.registerBone(bone);
			}
			this.currentModel = model;
		}
		return model;
	}

	@Override
	public double getCurrentTick() {
		return (Minecraft.getSystemTime() / 50d);
	}
}
