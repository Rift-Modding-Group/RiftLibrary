package anightdazingzoroark.riftlib.model;

import java.util.*;

import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;

import anightdazingzoroark.riftlib.internalMessage.RiftLibTickClientFromServer;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
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

@SuppressWarnings({"unchecked" })
public abstract class AnimatedGeoModel<T extends IAnimatable<?>> extends GeoModelProvider<T> implements IAnimatableModel<T>, IAnimatableModelProvider<T> {
	private static final long MAX_SERVER_SYNC_PREDICTION_TICKS = 3L;
	private static final Map<AbstractAnimationData<?, ?>, Map<ResourceLocation, GeoModel>> SERVER_SYNCED_MODEL_COPIES = new WeakHashMap<>();
	private final AnimationProcessor animationProcessor;
	//only relevant for server side models
	private final Map<AbstractAnimationData<?, ?>, ServerTickCheckpoint> serverTickCheckpoints = new WeakHashMap<>();
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
		AbstractAnimationData<?, ?> animData = entity.getAnimationData();

		//helper flag for checking for server model
		boolean hasServerModel = this.hasServerModel(entity) || animData.isServerSynced();
		if (hasServerModel) this.prepareServerSyncedAnimationData(entity);

		//if there is no server model, the client will be the main authority in
		//ticking animations.
		if (!hasServerModel && animData.clientTicker == null) {
			AnimationTicker ticker = new AnimationTicker(animData);
			animData.clientTicker = ticker;
			MinecraftForge.EVENT_BUS.register(ticker);
		}

		//update the seek time as long as the game is not paused (unless
		//explicitly set in the anim data)
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			this.seekTime = hasServerModel ?
					this.getServerSyncedSeekTime(animData, partialTicks) :
					animData.tick + partialTicks;
		}
		else this.seekTime = animData.tick;

		//update molang related information while the entity is rendered and game is not
		//paused (unless explicitly set in the anim data)
		if (!Minecraft.getMinecraft().isGamePaused() || animData.shouldPlayWhilePaused) {
			if (!hasServerModel) animData.updateAnimationVariables();
			animData.updateOnDataTick();
		}

		//process animations
		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, this.seekTime,
					this.shouldCrashOnMissing,
					true
			);
		}
	}

	//small but useful helper method
	@SideOnly(Side.CLIENT)
	private boolean hasServerModel(T animatable) {
		return ServerModelRegistry.hasServerModel(animatable);
	}

	@SideOnly(Side.CLIENT)
	private double getServerSyncedSeekTime(AbstractAnimationData<?, ?> animData, float partialTicks) {
		ServerTickCheckpoint checkpoint = this.serverTickCheckpoints.computeIfAbsent(
				animData, data -> new ServerTickCheckpoint(data.tick, this.getClientWorldTime(data))
		);

		if (animData.tick > checkpoint.serverTick) {
			checkpoint.serverTick = animData.tick;
			checkpoint.clientWorldTime = this.getClientWorldTime(animData);
		}

		long elapsedClientTicks = Math.clamp(
				this.getClientWorldTime(animData) - checkpoint.clientWorldTime, 0L,
                MAX_SERVER_SYNC_PREDICTION_TICKS
		);
		double seekTime = checkpoint.serverTick + elapsedClientTicks + partialTicks;

		checkpoint.lastSeekTime = Math.max(seekTime, checkpoint.lastSeekTime);
		return checkpoint.lastSeekTime;
	}

	@SideOnly(Side.CLIENT)
	private long getClientWorldTime(AbstractAnimationData<?, ?> animData) {
		return animData.getWorld() != null ? animData.getWorld().getTotalWorldTime() : 0L;
	}
	//-----client only stuff ends here-----

	//-----server only stuff starts here-----
	public void setServerAnimations(T entity) {
		AbstractAnimationData<?, ?> animData = entity.getAnimationData();
		animData.setServerSynced(true);

		//model is set here
		GeoModel model = this.getServerSyncedModel(animData, this.getModelLocation(entity));
		if (model != this.currentModel) this.setCurrentModel(model);

		//create and update locators on the model
		this.createAndUpdateAnimatedLocators(entity);

		//tick anim data
		animData.tick++;
		animData.tickAnimatedLocators();
		animData.updateAnimationVariables();
		animData.updateOnDataTick();

		//send tick to all clients after the server has advanced the model
		ServerProxy.SERVER_MODEL_MESSAGE_WRAPPER.sendToAll(new RiftLibTickClientFromServer(animData));

		//process animations
		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			this.animationProcessor.tickAnimation(
					entity, animData.tick,
					this.shouldCrashOnMissing,
					false
			);
		}
	}

	public void prepareServerSyncedAnimationData(T entity) {
		this.getModel(entity);
		this.createAndUpdateAnimatedLocators(entity);
	}
	//-----server only stuff ends here-----

	public void createAndUpdateAnimatedLocators(T entity) {
		AbstractAnimationData<?, ?> animData = entity.getAnimationData();
		animData.createAnimatedLocators(this.currentModel);
		animData.updateAnimatedLocators();
	}

	public GeoModel getModel(T object) {
		AbstractAnimationData<?, ?> animData = object.getAnimationData();
		ResourceLocation location = this.getModelLocation(object);
		if (ServerModelRegistry.hasServerModel(object) || animData.isServerSynced()) {
			GeoModel model = this.getServerSyncedModel(animData, location);
			if (model != this.currentModel) this.setCurrentModel(model);
			return model;
		}
		return this.getModel(location);
	}

	@Override
	public GeoModel getModel(ResourceLocation location) {
		GeoModel model = super.getModel(location);
		if (model == null) throw new GeoModelException(location, "Could not find model.");

		//change current model
		if (model != this.currentModel) this.setCurrentModel(model);

		return model;
	}

	private GeoModel getServerSyncedModel(AbstractAnimationData<?, ?> animData, ResourceLocation location) {
		synchronized (SERVER_SYNCED_MODEL_COPIES) {
			Map<ResourceLocation, GeoModel> models = SERVER_SYNCED_MODEL_COPIES.computeIfAbsent(animData, data -> new HashMap<>());
			GeoModel model = models.get(location);
			if (model != null) return model;

			GeoModel sharedModel = super.getModel(location);
			if (sharedModel == null) throw new GeoModelException(location, "Could not find model.");

			model = sharedModel.copy();
			models.put(location, model);
			return model;
		}
	}

	@Override
	public Animation getAnimations(String name, IAnimatable<?> animatable) {
		Map<ResourceLocation, AnimationFile> animations = FMLCommonHandler.instance().getSide().isClient() ?
				RiftLibCacheClient.getInstance().getAnimations() : RiftLibCacheServer.getInstance().getAnimations();
		return animations.get(this.getAnimationFileLocation((T) animatable)).getAnimation(name);
	}

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

	private static class ServerTickCheckpoint {
		private double serverTick;
		private long clientWorldTime;
		private double lastSeekTime;

		private ServerTickCheckpoint(double serverTick, long clientWorldTime) {
			this.serverTick = serverTick;
			this.clientWorldTime = clientWorldTime;
			this.lastSeekTime = serverTick;
		}
	}
}
