package anightdazingzoroark.riftlibrary.main.assetLoader;

import anightdazingzoroark.riftlibrary.main.geo.RiftLibModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class AssetLoader {
    private final Gson gson = new GsonBuilder().create();

    public RiftLibModel loadModel(IResourceManager resourceManager, ResourceLocation location) {

    }
}
