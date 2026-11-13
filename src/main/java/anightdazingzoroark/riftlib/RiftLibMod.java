package anightdazingzoroark.riftlib;

import anightdazingzoroark.riftlib.command.RiftLibCommandParticle;
import anightdazingzoroark.riftlib.command.RiftLibMobFamily;
import anightdazingzoroark.riftlib.command.RiftLibShowBoundingBox;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import anightdazingzoroark.example.registry.ItemRegistry;

@Mod(modid = RiftLib.ModID, version = RiftLib.VERSION)
public class RiftLibMod {
    @SidedProxy(clientSide = "anightdazingzoroark.riftlib.proxy.ClientProxy", serverSide = "anightdazingzoroark.riftlib.proxy.ServerProxy")
    public static ServerProxy PROXY;
	public static Configuration configMain;
	public static boolean DISABLE_IN_DEV = false;
	private static CreativeTabs riftlibItemGroup;
	public static boolean DEOBF_ENVIRONMENT = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

	public static CreativeTabs getRiftlibItemGroup() {
		if (riftlibItemGroup == null) {
			riftlibItemGroup = new CreativeTabs(CreativeTabs.getNextID(), "riftlib_examples") {
				@Override
				public ItemStack createIcon() {
					return new ItemStack(ItemRegistry.BOMB);
				}
			};
		}

		return riftlibItemGroup;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		PROXY.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
        if (DEOBF_ENVIRONMENT && !DISABLE_IN_DEV) RiftLib.initialize();
        PROXY.init(event);
    }


	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		//for config
		if (configMain.hasChanged()) configMain.save();
	}

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
		MinecraftServer server = event.getServer();
		ServerCommandManager manager = (ServerCommandManager) server.getCommandManager();
		manager.registerCommand(new RiftLibCommandParticle());

        event.registerServerCommand(new RiftLibMobFamily());
		event.registerServerCommand(new RiftLibShowBoundingBox());
    }
}
