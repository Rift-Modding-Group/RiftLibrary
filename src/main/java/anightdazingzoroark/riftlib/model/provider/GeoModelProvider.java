package anightdazingzoroark.riftlib.model.provider;

import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class GeoModelProvider<T> {
	public double seekTime;
	public boolean shouldCrashOnMissing = false;

	public GeoModel getModel(ResourceLocation location) {
		return FMLCommonHandler.instance().getSide().isClient() ?
				RiftLibCacheClient.getInstance().getGeoModels().get(location) : RiftLibCacheServer.getInstance().getGeoModels().get(location);
	}

	public abstract ResourceLocation getModelLocation(T object);

	public abstract ResourceLocation getTextureLocation(T object);
}
