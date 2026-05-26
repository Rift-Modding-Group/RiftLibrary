package anightdazingzoroark.riftlib.model.provider;

import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;

public abstract class GeoModelProvider<T> {
	public double seekTime;
	public double lastGameTickTime;
	public boolean shouldCrashOnMissing = false;

	public GeoModel getClientModel(ResourceLocation location) {
		return RiftLibCacheClient.getInstance().getGeoModels().get(location);
	}

	public GeoModel getServerModel(ResourceLocation location) {
		return RiftLibCacheServer.getInstance().getGeoModels().get(location);
	}

	public abstract ResourceLocation getModelLocation(T object);

	public abstract ResourceLocation getTextureLocation(T object);
}
