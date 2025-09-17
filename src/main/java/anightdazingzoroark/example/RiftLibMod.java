/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.example;

import anightdazingzoroark.example.client.renderer.entity.*;
import anightdazingzoroark.example.entity.*;
import anightdazingzoroark.example.entity.hitboxLinker.DragonHitboxLinker;
import anightdazingzoroark.example.entity.hitboxLinker.FlyingPufferfishHitboxLinker;
import anightdazingzoroark.example.entity.ridePosLinker.DragonRidePosLinker;
import anightdazingzoroark.riftlib.RiftLibConfig;
import anightdazingzoroark.riftlib.RiftLibEvent;
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.command.RiftLibMobFamily;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitboxRenderer;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.mobFamily.MobFamily;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyCreator;
import anightdazingzoroark.riftlib.mobFamily.MobFamilyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import anightdazingzoroark.example.block.tile.BotariumTileEntity;
import anightdazingzoroark.example.block.tile.FertilizerTileEntity;
import anightdazingzoroark.example.client.renderer.armor.PotatoArmorRenderer;
import anightdazingzoroark.example.client.renderer.tile.BotariumTileRenderer;
import anightdazingzoroark.example.client.renderer.tile.FertilizerTileRenderer;
import anightdazingzoroark.example.item.PotatoArmorItem;
import anightdazingzoroark.example.registry.ItemRegistry;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoReplacedEntityRenderer;

import java.io.File;

@Mod(modid = RiftLib.ModID, version = RiftLib.VERSION)
public class RiftLibMod {
	public static Configuration configMain;
	public static boolean DISABLE_IN_DEV = false;
	private static CreativeTabs riftlibItemGroup;
	private boolean deobfuscatedEnvironment;
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

	public RiftLibMod() {
		deobfuscatedEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		if (deobfuscatedEnvironment && !DISABLE_IN_DEV) {
			MinecraftForge.EVENT_BUS.register(new CommonListener());
		}
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
                "minecraft:polar_bear"
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
		RenderingRegistry.registerEntityRenderingHandler(EntityHitbox.class, new EntityHitboxRenderer.Factory());
		RiftLibMessage.registerMessages();

		if (deobfuscatedEnvironment && !DISABLE_IN_DEV) {
			this.registerExamplePreInit();
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (deobfuscatedEnvironment && !DISABLE_IN_DEV) {
			RiftLib.initialize();
			this.registerExampleInit(event);
			MinecraftForge.EVENT_BUS.register(new RiftLibEvent());
		}
	}

	@SideOnly(Side.CLIENT)
	private void registerExamplePreInit() {
		//linkers
		RiftLibLinkerRegistry.registerEntityHitboxLinker(DragonEntity.class, new DragonHitboxLinker());
		RiftLibLinkerRegistry.registerDynamicRidePosLinker(DragonEntity.class, new DragonRidePosLinker());
		RiftLibLinkerRegistry.registerEntityHitboxLinker(FlyingPufferfishEntity.class, new FlyingPufferfishHitboxLinker());

		//entity renderers
		RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(FlyingPufferfishEntity.class, FlyingPufferfishRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(BombProjectile.class, BombProjectileRenderer::new);

		GeoArmorRenderer.registerArmorRenderer(PotatoArmorItem.class, new PotatoArmorRenderer());

		ClientRegistry.bindTileEntitySpecialRenderer(BotariumTileEntity.class, new BotariumTileRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(FertilizerTileEntity.class, new FertilizerTileRenderer());
	}

	@SideOnly(Side.CLIENT)
	public void registerExampleInit(FMLInitializationEvent event) {
		RiftLib.initialize();
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		ReplacedCreeperRenderer creeperRenderer = new ReplacedCreeperRenderer(renderManager);
		renderManager.entityRenderMap.put(EntityCreeper.class, creeperRenderer);
		GeoReplacedEntityRenderer.registerReplacedEntity(ReplacedCreeperEntity.class, creeperRenderer);
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
