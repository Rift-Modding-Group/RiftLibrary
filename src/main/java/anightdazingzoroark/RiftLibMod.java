package anightdazingzoroark;

import anightdazingzoroark.example.client.renderer.entity.*;
import anightdazingzoroark.example.entity.*;
import anightdazingzoroark.riftlib.RiftLibConfig;
import anightdazingzoroark.riftlib.command.RiftLibMobFamily;
import anightdazingzoroark.riftlib.mobFamily.MobFamily;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyCreator;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import anightdazingzoroark.example.registry.ItemRegistry;

import java.io.File;

@Mod(modid = RiftLib.ModID, version = RiftLib.VERSION)
public class RiftLibMod {
    @SidedProxy(clientSide = "anightdazingzoroark.ClientProxy", serverSide = "anightdazingzoroark.ServerProxy")
    public static ServerProxy PROXY;
	public static Configuration configMain;
	public static boolean DISABLE_IN_DEV = false;
	private static CreativeTabs riftlibItemGroup;
	public static boolean DEOBF_ENVIRONMENT = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    private static MobFamilyManager MOB_FAMILY_MANAGER;

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
		//config is to be initialized here
		File directory = event.getModConfigurationDirectory();
		configMain = new Configuration(new File(directory.getPath(), "riftlib.cfg"));
		RiftLibConfig.readConfig();

        //default mob families are to be made here
        MOB_FAMILY_MANAGER = MobFamilyCreator.createManager(directory, "mob_families.json");
        MOB_FAMILY_MANAGER.addMobFamily(new MobFamily("animal").addToFamilyMembers(
                "minecraft:pig",
                "minecraft:chicken",
                "minecraft:cow",
                "minecraft:sheep",
                "minecraft:rabbit",
                "minecraft:horse",
                "minecraft:donkey",
                "minecraft:mule",
                "minecraft:llama",
                "minecraft:polar_bear",
                "minecraft:wolf",
                "minecraft:ocelot"
        ));
        MOB_FAMILY_MANAGER.addMobFamily(new MobFamily("monster").addToFamilyMembers(
                "minecraft:cave_spider",
                "minecraft:enderman",
                "minecraft:spider",
                "minecraft:zombie_pigman",
                "minecraft:blaze",
                "minecraft:creeper",
                "minecraft:elder_guardian",
                "minecraft:endermite",
                "minecraft:evoker",
                "minecraft:ghast",
                "minecraft:guardian",
                "minecraft:husk",
                "minecraft:magma_cube",
                "minecraft:shulker",
                "minecraft:silverfish",
                "minecraft:skeleton",
                "minecraft:slime",
                "minecraft:stray",
                "minecraft:vex",
                "minecraft:vindicator",
                "minecraft:witch",
                "minecraft:wither_skeleton",
                "minecraft:zombie",
                "minecraft:zombie_villager"
        ));
        MOB_FAMILY_MANAGER.addMobFamily(new MobFamily("human").addToFamilyMembers(
                "minecraft:player",
                "minecraft:villager",
                "minecraft:evoker",
                "minecraft:vindicator",
                "minecraft:villager"
        ));
        MOB_FAMILY_MANAGER.addMobFamily(new MobFamily("arthropod").addToFamilyMembers(
                "minecraft:spider",
                "minecraft:cave_spider",
                "minecraft:silverfish",
                "minecraft:endermite"
        ));

        // Load or generate file
        MOB_FAMILY_MANAGER.load();

		//other essentials, like message registry and removing hitbox textures
		//go here too
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
        event.registerServerCommand(new RiftLibMobFamily());
    }
}
