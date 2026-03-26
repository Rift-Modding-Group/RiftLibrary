package anightdazingzoroark.riftlibrary.main.resource;

import anightdazingzoroark.riftlibrary.main.assetLoader.AssetLoader;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

@SuppressWarnings("deprecation")
public class RiftLibraryCache implements IResourceManagerReloadListener {
    private static RiftLibraryCache INSTANCE;

    private final AssetLoader loader;

    protected RiftLibraryCache() {
        this.loader = new AssetLoader();
    }
    public static RiftLibraryCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RiftLibraryCache();
            return INSTANCE;
        }
        return INSTANCE;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
