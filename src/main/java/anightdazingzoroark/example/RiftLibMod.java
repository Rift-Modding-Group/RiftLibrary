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
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

@Mod(modid = RiftLib.ModID, version = RiftLib.VERSION)
public class RiftLibMod {
	public static boolean DISABLE_IN_DEV = false;
	private static CreativeTabs riftlibItemGroup;
	private boolean deobfuscatedEnvironment;

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

	@SideOnly(Side.CLIENT)
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (deobfuscatedEnvironment && !DISABLE_IN_DEV) {
			RiftLib.initializePre();
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
	}

	@SideOnly(Side.CLIENT)
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		if (deobfuscatedEnvironment && !DISABLE_IN_DEV) {
			RiftLib.initializeMiddle();
			RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
			ReplacedCreeperRenderer creeperRenderer = new ReplacedCreeperRenderer(renderManager);
			renderManager.entityRenderMap.put(EntityCreeper.class, creeperRenderer);
			GeoReplacedEntityRenderer.registerReplacedEntity(ReplacedCreeperEntity.class, creeperRenderer);
		}
	}
}
