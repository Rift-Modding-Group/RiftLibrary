package anightdazingzoroark.riftlib.model.provider;

import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

public abstract class GeoModelProvider<T> {
	public double seekTime;
	public boolean shouldCrashOnMissing = false;

	public GeoModel getModel(@NotNull String identifier) {
		if (FMLCommonHandler.instance().getSide().isClient()) {
			return RiftLibCacheClient.getInstance().getGeoModels().get(this.getModId()).get(identifier);
		}
		else return RiftLibCacheServer.getInstance().getGeoModels().get(this.getModId()).get(identifier);
	}

	@NotNull
	public abstract String getModId();

	public abstract String getModelIdentifier(T object);

	public abstract ResourceLocation getTextureLocation(T object);
}
