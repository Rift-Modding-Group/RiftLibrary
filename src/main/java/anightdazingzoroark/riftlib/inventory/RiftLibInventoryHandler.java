package anightdazingzoroark.riftlib.inventory;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An abstraction for inventories in a GeckoLib fork??? omaga
 * its also modularui friendly.
 * */
public class RiftLibInventoryHandler extends ItemStackHandler {
    //fallback value for item searching if nothing can be found
    private static final ItemSearchResult noItemFound = new ItemSearchResult(false, ItemStack.EMPTY, -1);
    @Nullable
    private final Function<ItemStack, Boolean> itemFilter;

    public RiftLibInventoryHandler() {
        this(1);
    }

    public RiftLibInventoryHandler(int size) {
        this(size, null);
    }

    public RiftLibInventoryHandler(int size, @Nullable Function<ItemStack, Boolean> itemFilter) {
        super(size);
        this.itemFilter = itemFilter;
    }

    @Nullable
    public Function<ItemStack, Boolean> getItemFilter() {
        return this.itemFilter;
    }

    //check if all slots are empty
    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (!itemStack.isEmpty()) return false;
        }
        return true;
    }

    //this adds an itemstack to the inventory and edits itemStackToAdd
    public ItemStack addItem(ItemStack itemStackToAdd) {
        //block based on filter
        if (this.itemFilter != null && !this.itemFilter.apply(itemStackToAdd)) return ItemStack.EMPTY;

        ItemSearchResult similarItemResult = this.findItem(
                ItemSearchDirection.FIRST_TO_LAST,
                itemStack -> {
                    return this.itemStacksEqual(itemStack, itemStackToAdd)
                        && itemStack.isStackable()
                        && itemStack.getCount() < itemStackToAdd.getMaxStackSize();
                }
        );
        ItemSearchResult emptySpaceResult = this.findItem(ItemSearchDirection.FIRST_TO_LAST, ItemStack.EMPTY);

        if (similarItemResult.successful) {
            int maxStackSize = itemStackToAdd.getMaxStackSize();
            if (similarItemResult.foundStack.getCount() + itemStackToAdd.getCount() < maxStackSize) {
                similarItemResult.foundStack.setCount(similarItemResult.foundStack.getCount() + itemStackToAdd.getCount());
                return ItemStack.EMPTY;
            }
            else {
                int remainingSize = similarItemResult.foundStack.getCount() + itemStackToAdd.getCount() - maxStackSize;
                similarItemResult.foundStack.setCount(maxStackSize);
                itemStackToAdd.setCount(remainingSize);
                return this.addItem(itemStackToAdd);
            }
        }
        else if (emptySpaceResult.successful) {
            this.setStackInSlot(emptySpaceResult.slot, itemStackToAdd);
            return ItemStack.EMPTY;
        }
        else return itemStackToAdd;
    }

    //similar to addItem but does not edit the itemstack given as parameter
    public void insertItem(ItemStack itemToInsert) {
        //block based on filter
        if (this.itemFilter != null && !this.itemFilter.apply(itemToInsert)) return;

        int count = itemToInsert.getCount();
        for (int index = 0; index < this.stacks.size(); index++) {
            ItemStack itemStack = this.stacks.get(index);

            if (itemStack.isEmpty()) {
                this.stacks.set(index, itemToInsert.copy());
                break;
            }
            else if (ItemStack.areItemsEqual(itemStack, itemToInsert)) {
                int countSum = count + itemStack.getCount();

                if (countSum <= itemStack.getMaxStackSize()) {
                    itemStack.setCount(countSum);
                    break;
                }
                else {
                    count = countSum - itemStack.getMaxStackSize();
                    itemStack.setCount(itemStack.getMaxStackSize());
                }
            }

            if (count <= 0) break;
        }
    }

    public void removeItem(@NotNull ItemSearchDirection itemSearchDirection, ItemStack itemStackToRemove) {
        ItemSearchResult similarItemResult = this.findItem(itemSearchDirection, itemStackToRemove);

        if (similarItemResult.successful) {
            int subtractResult = similarItemResult.foundStack.getCount() - itemStackToRemove.getCount();
            if (subtractResult >= 0) similarItemResult.foundStack.setCount(subtractResult);
            else {
                itemStackToRemove.setCount(Math.abs(subtractResult));
                this.removeItem(itemSearchDirection, itemStackToRemove);
            }
        }
    }

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, ItemStack itemToSearch) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (this.itemStacksEqual(itemToSearch, itemStack)) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (this.itemStacksEqual(itemToSearch, itemStack)) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return noItemFound;
        }
        return noItemFound;
    }

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, Item itemToSearch) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemStack.getItem() == itemToSearch) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemStack.getItem() == itemToSearch) {
                    return new ItemSearchResult(true, itemStack, i);
                }
            }
            return noItemFound;
        }
        return noItemFound;
    }

    public ItemSearchResult findItem(@NotNull ItemSearchDirection searchDirection, Function<ItemStack, Boolean> itemSearchFunction) {
        if (searchDirection == ItemSearchDirection.FIRST_TO_LAST) {
            for (int i = 0; i < this.stacks.size(); i++) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemSearchFunction.apply(itemStack)) return new ItemSearchResult(true, itemStack, i);
            }
            return noItemFound;
        }
        else if (searchDirection == ItemSearchDirection.LAST_TO_FIRST) {
            for (int i = this.stacks.size() - 1; i >= 0; i--) {
                ItemStack itemStack = this.stacks.get(i);
                if (itemSearchFunction.apply(itemStack)) return new ItemSearchResult(true, itemStack, i);
            }
            return noItemFound;
        }
        return noItemFound;
    }

    public void dropAllItems(World world, BlockPos pos) {
        if (world.isRemote) return;

        //drop all items in this creatures inventory
        for (int i = 0; i < this.stacks.size(); i++) {
            ItemStack itemStack = this.stacks.get(i);
            if (itemStack.isEmpty()) continue;

            //create itemStack as an entity and spawn it
            EntityItem droppedItem = new EntityItem(world);
            droppedItem.setItem(itemStack);
            droppedItem.setPosition(pos.getX(), pos.getY() + 0.5D, pos.getZ());
            world.spawnEntity(droppedItem);

            //clear up after dropping
            this.stacks.set(i, ItemStack.EMPTY);
        }
    }

   public NonNullList<ItemStack> getItemStackList() {
        return this.stacks;
   }

   public boolean canInsertItem(ItemStack itemToInsert) {
        int count = itemToInsert.getCount();
        for (ItemStack itemStack : this.stacks) {
            if (itemStack.isEmpty()) count = 0;
            else if (ItemStack.areItemsEqual(itemStack, itemToInsert)) {
                int countSum = count + itemStack.getCount();

                if (countSum <= itemStack.getMaxStackSize()) count = 0;
                else count = countSum - itemStack.getMaxStackSize();
            }

            if (count <= 0) return true;
        }
        return false;
   }

   private boolean itemStacksEqual(ItemStack itemStackOne, ItemStack itemStackTwo) {
       return itemStackOne.getItem() == itemStackTwo.getItem() && itemStackOne.getMetadata() == itemStackTwo.getMetadata();
   }

    @Override
    public String toString() {
        return this.stacks.toString();
    }

    public record ItemSearchResult(boolean successful, ItemStack foundStack, int slot) {}

    public enum ItemSearchDirection {
        FIRST_TO_LAST,
        LAST_TO_FIRST;
    }
}
