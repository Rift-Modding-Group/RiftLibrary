package anightdazingzoroark.riftlibrary.main.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

public class ResourceListener {
    public static void registerReloadListener() {
        if (Minecraft.getMinecraft().getResourceManager() == null) {
            throw new RuntimeException(
                    "RiftLibrary was initialized too early!");
        }
        IReloadableResourceManager reloadable = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        reloadable.registerReloadListener(RiftLibraryCache.getInstance());
    }
}
