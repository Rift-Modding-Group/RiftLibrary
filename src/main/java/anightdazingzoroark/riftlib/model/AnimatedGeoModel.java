package anightdazingzoroark.riftlib.model;

import java.util.*;

import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
					this.shouldCrashOnMissing,
					true
			);
		}
	}
	//-----client only stuff ends here-----

	//-----server only stuff starts here-----
	public void setServerAnimations(T entity) {
		AbstractAnimationData<?> animData = entity.getAnimationData();

		//model is set here
		GeoModel model = this.getModel(this.getModelLocation(entity));
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
					this.shouldCrashOnMissing,
					false
			);
		}
	}
	//-----server only stuff ends here-----

	public void createAndUpdateAnimatedLocators(T entity) {
		AbstractAnimationData<?> animData = entity.getAnimationData();
		animData.createAnimatedLocators(this.currentModel);
		animData.updateAnimatedLocators();
	}

	@Override
	public GeoModel getModel(ResourceLocation location) {
		GeoModel model = super.getModel(location);
		if (model == null) throw new GeoModelException(location, "Could not find model.");

		//change current model
		if (model != this.currentModel) this.setCurrentModel(model);

		return model;
	}

	@Override
	public Animation getAnimations(String name, IAnimatable<?> animatable) {
		Map<ResourceLocation, AnimationFile> animations = FMLCommonHandler.instance().getSide().isClient() ?
				RiftLibCacheClient.getInstance().getAnimations() : RiftLibCacheServer.getInstance().getAnimations();
		return animations.get(this.getAnimationFileLocation((T) animatable)).getAnimation(name);
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
}
