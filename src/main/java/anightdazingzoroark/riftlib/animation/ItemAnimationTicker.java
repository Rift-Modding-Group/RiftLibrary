package anightdazingzoroark.riftlib.animation;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ItemAnimationTicker {
    private static final Map<ImmutablePair<ItemStack, Integer>, Integer> renderedItemStackMap = new HashMap<>();

    @SubscribeEvent
    public void tickRenderedStack(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Set<Map.Entry<ImmutablePair<ItemStack, Integer>, Integer>> renderedItemStackSet = new HashMap<>(renderedItemStackMap).entrySet();
        for (Map.Entry<ImmutablePair<ItemStack, Integer>, Integer> entry : renderedItemStackSet) {
            int newTickValue = entry.getValue() + 1;
            if (newTickValue >= 1) {
                Item item = entry.getKey().left.getItem();
                if (!(item instanceof IAnimatable)) continue;
                IAnimatable animatable = (IAnimatable) item;
                animatable.getFactory().removeAnimationData(entry.getKey().right);
                renderedItemStackMap.remove(entry.getKey());
            }
            else renderedItemStackMap.put(entry.getKey(), newTickValue);
        }
    }

    public static void refreshRenderedStackEntry(ItemStack itemStack, Integer uniqueID) {
        //first of all check if the pair exists as a key in the map
        boolean pairNotFoundFlag = true;
        Set<Map.Entry<ImmutablePair<ItemStack, Integer>, Integer>> renderedItemStackSet = new HashMap<>(renderedItemStackMap).entrySet();
        for (Map.Entry<ImmutablePair<ItemStack, Integer>, Integer> entry : renderedItemStackSet) {
            ImmutablePair<ItemStack, Integer> pairToFind = entry.getKey();
            if (pairToFind.left == itemStack && Objects.equals(pairToFind.right, uniqueID)) {
                renderedItemStackMap.put(entry.getKey(), 0);
                pairNotFoundFlag = false;
                break;
            }
        }

        //if there no pair found, add it
        if (pairNotFoundFlag) {
            ImmutablePair<ItemStack, Integer> pairToAdd = new ImmutablePair<>(itemStack, uniqueID);
            if (itemStack != null) renderedItemStackMap.put(pairToAdd, 0);
        }
    }
}
