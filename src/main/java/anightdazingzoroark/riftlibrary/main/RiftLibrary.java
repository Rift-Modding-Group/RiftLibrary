package anightdazingzoroark.riftlibrary.main;

import anightdazingzoroark.riftlibrary.main.resource.ResourceListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.FutureTask;

public class RiftLibrary {
    private static boolean hasInitialized;

    public static void initialize() {
        if (!hasInitialized) {
            FMLCommonHandler.callFuture(new FutureTask<>(() -> {
                if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                    doOnlyOnClient();
                }
            }, null));
        }
        hasInitialized = true;
    }

    @SideOnly(Side.CLIENT)
    private static void doOnlyOnClient() {
        ResourceListener.registerReloadListener();
    }

    public static boolean isInitialized() {
        return hasInitialized;
    }
}
