package anightdazingzoroark.riftlib.animation;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.*;

/**
 * This event class's purpose is to properly tick instances of
 * each item and delete their relevant data if they ever stop
 * existing for whatever reason
 * **/
public class ItemAnimationTicker {
    private static final List<ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType>> currentRenderedItemStackList = new ArrayList<>();
    private static final Map<ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType>, Integer> cachedRenderedItemStackMap = new HashMap<>();

    /**
     * This is executed at the end of every tick after rendering everything to
     * ensure removal of data pertaining to items that are not rendered. It does
     * this by adding currently rendered triples to a cache and assigning them
     * a tick. Then, the tick on the cache gets updated, where items that are
     * no longer rendered get removed, while those that are rendered get to
     * stay.
     * **/
    @SubscribeEvent
    public void tickRenderedStack(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        //iterate over current rendered item stack map
        for (ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType> entry : currentRenderedItemStackList) {
            cachedRenderedItemStackMap.put(entry, 0);
        }

        //tick the cache
        Set<Map.Entry<ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType>, Integer>> cachedItemStackSet = new HashMap<>(cachedRenderedItemStackMap).entrySet();
        for (Map.Entry<ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType>, Integer> entry : cachedItemStackSet) {
            boolean updateFlag = true;
            if (entry.getValue() >= 1) {
                updateFlag = false;
                Item item = entry.getKey().left.getItem();
                if (!(item instanceof IAnimatable)) continue;
                IAnimatable animatable = (IAnimatable) item;
                animatable.getFactory().removeAnimationData(entry.getKey().middle);
                cachedRenderedItemStackMap.remove(entry.getKey());
            }

            if (updateFlag) {
                int newTickValue = entry.getValue() + 1;
                cachedRenderedItemStackMap.put(entry.getKey(), newTickValue);
            }
        }

        //reset current rendered list
        currentRenderedItemStackList.clear();
    }

    /**
     * Remove all rendering data upon unloading the world
     * **/
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            currentRenderedItemStackList.clear();
            cachedRenderedItemStackMap.clear();
        }
    }

    /**
     * This is executed in GeoItemRenderer, its purpose is to update currently
     * rendered items to ensure they do not get deleted from the ticker. It
     * adds them as triples, which have the itemStack, uniqueID, and transformType
     * **/
    public static void refreshRenderedStackEntry(ItemStack itemStack, Integer uniqueID, ItemCameraTransforms.TransformType transformType) {
        //add currently rendered item to current rendered map
        ImmutableTriple<ItemStack, Integer, ItemCameraTransforms.TransformType> tripleToAdd = new ImmutableTriple<>(itemStack, uniqueID, transformType);
        if (!currentRenderedItemStackList.contains(tripleToAdd)) {
            currentRenderedItemStackList.add(tripleToAdd);
        }
    }
}
