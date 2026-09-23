package anightdazingzoroark.example.registry;

import anightdazingzoroark.example.client.renderer.item.*;
import anightdazingzoroark.example.item.BombItem;
import anightdazingzoroark.example.item.BubbleGunItem;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibMod;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class ItemRegistry {
    private static final List<Item> ITEMS = new ArrayList<>();
    private static boolean itemsRegistered;

	public static Item BOMB;

	public static ItemArmor GREEN_HEAD;
	public static ItemArmor GREEN_CHEST;
	public static ItemArmor GREEN_LEGGINGS;
	public static ItemArmor GREEN_BOOTS;

    public static ItemArmor SATELLITE_DISH_HELMET;

    public static Item MERRY_GO_ROUND;
    public static Item SPRINKLER;
    public static Item BUBBLE_GUN;
    public static Item FIDGET_SPINNER;
    public static Item FIREWORK_STICK;

    public static void registerItems() {
        if (itemsRegistered) throw new IllegalStateException("Items have already been registered!");

        BOMB = registerItem(new BombItem().setMaxStackSize(1), "bomb");
        BUBBLE_GUN = registerItem(new BubbleGunItem().setMaxStackSize(1), "bubble_gun");
        FIDGET_SPINNER = registerItem(new Item().setMaxStackSize(1), "fidget_spinner");
        FIREWORK_STICK = registerItem(new Item().setMaxStackSize(1), "firework_stick");

        GREEN_HEAD = registerItem(new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 0, EntityEquipmentSlot.HEAD), "green_helmet");
        GREEN_CHEST = registerItem(new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 0, EntityEquipmentSlot.CHEST), "green_chest");
        GREEN_LEGGINGS = registerItem(new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 0, EntityEquipmentSlot.LEGS), "green_leggings");
        GREEN_BOOTS = registerItem(new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 0, EntityEquipmentSlot.FEET), "green_boots");

        SATELLITE_DISH_HELMET = registerItem(new ItemArmor(ItemArmor.ArmorMaterial.IRON, 0, EntityEquipmentSlot.HEAD), "satellite_dish_helmet");

        MERRY_GO_ROUND = registerItem(new ItemBlock(BlockRegistry.MERRY_GO_ROUND_BLOCK), "merry_go_round");
        SPRINKLER = registerItem(new ItemBlock(BlockRegistry.SPRINKLER_BLOCK), "sprinkler");

        itemsRegistered = true;
    }

    private static <T extends Item> T registerItem(T item, String name) {
        item.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
        item.setRegistryName(name);
        item.setTranslationKey(RiftLib.ModID+"."+name);
        ITEMS.add(item);
        return item;
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();
        reg.registerAll(ITEMS.toArray(new Item[0]));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        //---vanilla item rendering for positions---
        ModelLoader.setCustomModelResourceLocation(
                BOMB, 0,
                new ModelResourceLocation(RiftLib.ModID+":bomb", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                BUBBLE_GUN, 0,
                new ModelResourceLocation(RiftLib.ModID+":bubble_gun", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                FIDGET_SPINNER, 0,
                new ModelResourceLocation(RiftLib.ModID+":fidget_spinner", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                FIREWORK_STICK, 0,
                new ModelResourceLocation(RiftLib.ModID+":firework_stick", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                MERRY_GO_ROUND, 0,
                new ModelResourceLocation(RiftLib.ModID+":merry_go_round", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                SPRINKLER, 0,
                new ModelResourceLocation(RiftLib.ModID+":sprinkler", "inventory")
        );

        ModelLoader.setCustomModelResourceLocation(
                GREEN_HEAD, 0,
                new ModelResourceLocation(RiftLib.ModID + ":green_helmet", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GREEN_CHEST, 0,
                new ModelResourceLocation(RiftLib.ModID + ":green_chest", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GREEN_LEGGINGS, 0,
                new ModelResourceLocation(RiftLib.ModID + ":green_leggings", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                GREEN_BOOTS, 0,
                new ModelResourceLocation(RiftLib.ModID + ":green_boots", "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
                SATELLITE_DISH_HELMET, 0,
                new ModelResourceLocation(RiftLib.ModID + ":satellite_dish_helmet", "inventory")
        );

        //---riftlib item rendering--
        BOMB.setTileEntityItemStackRenderer(new BombRenderer());
        BUBBLE_GUN.setTileEntityItemStackRenderer(new BubbleGunRenderer());
        FIDGET_SPINNER.setTileEntityItemStackRenderer(new FidgetSpinnerRenderer());
        FIREWORK_STICK.setTileEntityItemStackRenderer(new FireworkStickRenderer());

        MERRY_GO_ROUND.setTileEntityItemStackRenderer(new MerryGoRoundItemRenderer());
        SPRINKLER.setTileEntityItemStackRenderer(new SprinklerItemRenderer());
    }
}
