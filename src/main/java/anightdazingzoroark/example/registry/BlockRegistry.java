package anightdazingzoroark.example.registry;

import anightdazingzoroark.example.block.MerryGoRoundBlock;
import anightdazingzoroark.example.block.SprinklerBlock;
import anightdazingzoroark.example.client.renderer.block.MerryGoRoundRenderer;
import anightdazingzoroark.example.client.renderer.block.SprinklerRenderer;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import net.minecraft.block.Block;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

public class BlockRegistry {
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static boolean blocksRegistered;

    public static MerryGoRoundBlock MERRY_GO_ROUND_BLOCK;
    public static SprinklerBlock SPRINKLER_BLOCK;

    public static void registerBlocks() {
        if (blocksRegistered) throw new IllegalStateException("Blocks have already been registered!");

        MERRY_GO_ROUND_BLOCK = registerBlock(new MerryGoRoundBlock(), "merry_go_round_block");
        SPRINKLER_BLOCK = registerBlock(new SprinklerBlock(), "sprinkler");

        blocksRegistered = true;
    }

    private static <T extends Block> T registerBlock(T block, String name) {
        block.setCreativeTab(RiftLibMod.getRiftlibItemGroup());
        block.setRegistryName(name);
        block.setTranslationKey(RiftLib.ModID+"."+name);
        BLOCKS.add(block);
        return block;
    }

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> reg = event.getRegistry();
        reg.registerAll(BLOCKS.toArray(new Block[0]));
    }
}
