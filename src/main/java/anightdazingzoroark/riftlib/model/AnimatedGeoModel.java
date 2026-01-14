package anightdazingzoroark.riftlib.model;

import java.util.*;

import javax.annotation.Nullable;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.molang.MolangParser;

import anightdazingzoroark.riftlib.molang.MolangScope;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import anightdazingzoroark.riftlib.animation.AnimationTicker;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.processor.AnimationProcessor;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.exceptions.GeoModelException;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.model.provider.IAnimatableModelProvider;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import anightdazingzoroark.riftlib.util.MolangUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AnimatedGeoModel<T extends IAnimatable> extends GeoModelProvider<T>
		implements IAnimatableModel<T>, IAnimatableModelProvider<T> {
	private final AnimationProcessor animationProcessor;
	protected GeoModel currentModel;

	protected AnimatedGeoModel() {
		this.animationProcessor = new AnimationProcessor(this);
	}

	public void registerBone(GeoBone bone) {
        this.registerModelRenderer(bone);

		for (GeoBone childBone : bone.childBones) {
			this.registerBone(childBone);
		}
	}

	@Override
	public void setLivingAnimations(T entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AnimationData manager = entity.getFactory().getOrCreateAnimationData(uniqueID);

        //create animated locators
        manager.createAnimatedLocators(this.currentModel);


		if (manager.ticker == null) {
			AnimationTicker ticker = new AnimationTicker(manager);
			manager.ticker = ticker;
			MinecraftForge.EVENT_BUS.register(ticker);
		}
		if (!Minecraft.getMinecraft().isGamePaused() || manager.shouldPlayWhilePaused) {
            this.seekTime = manager.tick + Minecraft.getMinecraft().getRenderPartialTicks();
		}
        else this.seekTime = manager.tick;

		AnimationEvent<T> predicate;
		if (customPredicate == null) {
			predicate = new AnimationEvent<T>(entity, 0, 0, (float) (manager.tick - lastGameTickTime), false, Collections.emptyList());
		}
        else predicate = customPredicate;

		predicate.animationTick = this.seekTime;

		//update molang related information while the entity is rendered
		manager.updateAnimationVariables();
		manager.updateMolangQueries();

		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, uniqueID, this.seekTime,
					predicate, RiftLibCache.getInstance().parser,
					this.shouldCrashOnMissing
			);
		}
	}

	@Override
	public AnimationProcessor getAnimationProcessor() {
		return this.animationProcessor;
	}

	public void registerModelRenderer(IBone modelRenderer) {
        this.animationProcessor.registerModelRenderer(modelRenderer);
	}

	@Override
	public Animation getAnimation(String name, IAnimatable animatable) {
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
