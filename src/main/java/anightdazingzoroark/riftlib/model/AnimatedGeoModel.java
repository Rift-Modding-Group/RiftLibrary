package anightdazingzoroark.riftlib.model;

import java.util.*;

import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateRayPos;
import anightdazingzoroark.riftlib.proxy.ServerProxy;

import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RayTicker;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import anightdazingzoroark.riftlib.animation.AnimationTicker;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.processor.AnimationProcessor;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.exceptions.GeoModelException;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.model.provider.IAnimatableModelProvider;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjglx.util.vector.Quaternion;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AnimatedGeoModel<T extends IAnimatable<?>> extends GeoModelProvider<T> implements IAnimatableModel<T>, IAnimatableModelProvider<T> {
	private final AnimationProcessor animationProcessor;
	protected GeoModel currentModel;

	protected AnimatedGeoModel() {
		this.animationProcessor = new AnimationProcessor();
	}

	public void registerBone(GeoBone bone) {
        this.registerModelRenderer(bone);

		for (GeoLocator childLocator : bone.childLocators) {
			this.registerModelRenderer(childLocator);
		}

		for (GeoBone childBone : bone.childBones) {
			this.registerBone(childBone);
		}
	}

	//-----client only stuff starts here-----
	@SideOnly(Side.CLIENT)
	@Override
	public void setClientAnimations(T entity) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AbstractAnimationData<?> animData = entity.getAnimationData();

		if (animData.ticker == null) {
			AnimationTicker ticker = new AnimationTicker(animData);
			animData.ticker = ticker;
			MinecraftForge.EVENT_BUS.register(ticker);
		}
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
            this.seekTime = animData.tick + Minecraft.getMinecraft().getRenderPartialTicks();
		}
        else this.seekTime = animData.tick;


		//update molang related information while the entity is rendered
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
			animData.updateAnimationVariables();
			animData.updateOnDataTick();
		}

		//update based on animations
		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, this.seekTime,
					RiftLibCacheClient.getInstance().parser,
					this.shouldCrashOnMissing
			);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Animation getClientAnimations(String name, IAnimatable<?> animatable) {
		Map<ResourceLocation, AnimationFile> animations = RiftLibCacheClient.getInstance().getAnimations();
		return animations.get(this.getAnimationFileLocation((T) animatable)).getAnimation(name);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GeoModel getClientModel(ResourceLocation location) {
		GeoModel model = super.getClientModel(location);
		if (model == null) throw new GeoModelException(location, "Could not find model.");

		//change current model
		if (model != this.currentModel) this.setCurrentModel(model);

		return model;
	}
	//-----client only stuff ends here-----

	//-----server only stuff starts here-----
	public void setServerAnimations(T entity) {
		AbstractAnimationData<?> animData = entity.getAnimationData();

		//model is set here
		GeoModel model = this.getServerModel(this.getModelLocation(entity));
		if (model != this.currentModel) this.setCurrentModel(model);

		//create and update locators on the model
		this.createAndUpdateAnimatedLocators(entity);

		animData.tick++;
		animData.tickAnimatedLocators();
		animData.updateAnimationVariables();
		animData.updateOnDataTick();

		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, animData.tick,
					animData.getParser(),
					this.shouldCrashOnMissing,
					false
			);
		}
	}

	@Override
	public Animation getServerAnimations(String name, IAnimatable<?> animatable) {
		Map<ResourceLocation, AnimationFile> animations = RiftLibCacheServer.getInstance().getAnimations();
		return animations.get(this.getAnimationFileLocation((T) animatable)).getAnimation(name);
	}

	@Override
	public GeoModel getServerModel(ResourceLocation location) {
		GeoModel model = super.getServerModel(location);
		if (model == null) throw new GeoModelException(location, "Could not find model.");

		//change current model
		if (model != this.currentModel) this.setCurrentModel(model);
		return model;
	}
	//-----server only stuff ends here-----

	public void createAndUpdateAnimatedLocators(T entity) {
		AbstractAnimationData<?> animData = entity.getAnimationData();
		animData.createAnimatedLocators(this.currentModel);
		animData.updateAnimatedLocators();
	}

	/*
	@Deprecated
	private void setRayDisplacements(T entity, AbstractAnimationData<?> animData) {
		if (!(entity instanceof IRayCreator<?> rayCreator)) return;

		for (AnimatedLocator locator : animData.getAnimatedLocators()) {
			for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.Client.RAY_PAIR_LIST) {
				if (rayCreator != rayPair.getLeft() || !locator.getName().equals(rayPair.getRight().parentLocatorName)) continue;
				Vec3d modelSpacePos = locator.getModelSpacePosition();
				Quaternion modelSpaceQuat = locator.computeModelSpaceYXZQuaternion();

				rayPair.getRight().displaceByAnim(modelSpacePos);
				rayPair.getRight().displaceQuatByAnim(modelSpaceQuat);

				ServerProxy.RAY_MESSAGE_WRAPPER.sendToServer(new RiftLibUpdateRayPos(
						rayCreator, rayPair.getRight().rayName,
						modelSpacePos, modelSpaceQuat
				));
			}
		}
	}

	@Deprecated
	private void setServerRayDisplacements(T entity, AbstractAnimationData<?> animData) {
		if (!(entity instanceof IRayCreator<?> rayCreator)) return;

		for (AnimatedLocator locator : animData.getAnimatedLocators()) {
			for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.Server.RAY_PAIR_LIST) {
				if (rayCreator != rayPair.getLeft() || !locator.getName().equals(rayPair.getRight().parentLocatorName)) continue;

				rayPair.getRight().displaceByAnim(locator.getModelSpacePosition());
				rayPair.getRight().displaceQuatByAnim(locator.computeModelSpaceYXZQuaternion());
			}
		}
	}
	 */

	@Override
	public AnimationProcessor getAnimationProcessor() {
		return this.animationProcessor;
	}

	private void registerModelRenderer(IBone modelRenderer) {
        this.animationProcessor.registerModelRenderer(modelRenderer);
	}

	private void setCurrentModel(GeoModel model) {
		this.animationProcessor.clearModelRendererList();
		for (GeoBone bone : model.topLevelBones) {
			this.registerBone(bone);
		}
		this.currentModel = model;
	}

	@Override //no use, might delete?
	public double getCurrentTick() {
		return (Minecraft.getSystemTime() / 50d);
	}
}
