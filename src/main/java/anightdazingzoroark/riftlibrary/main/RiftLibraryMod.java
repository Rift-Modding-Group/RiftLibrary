package anightdazingzoroark.riftlibrary.main;

import anightdazingzoroark.riftlibrary.main.proxy.IProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = RiftLibraryMod.MODID, name = RiftLibraryMod.MODNAME, version = RiftLibraryMod.MODVERSION)
public class RiftLibraryMod {
    public static final String MODID = "riftlibrary";
    public static final String MODNAME = "RiftLibrary";
    public static final String MODVERSION = "2.0.0";
    public static final Logger LOGGER = LogManager.getLogger(RiftLibraryMod.MODNAME);
    @SidedProxy(
            modId = RiftLibraryMod.MODID, clientSide = "anightdazingzoroark.riftlibrary.main.proxy.ClientProxy",
            serverSide = "anightdazingzoroark.riftlibrary.main.proxy.CommonProxy"
    )
    public static IProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}
}
