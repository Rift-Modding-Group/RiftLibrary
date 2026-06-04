package anightdazingzoroark.riftlib.nbtStorageUser.tileEntity;

import anightdazingzoroark.riftlib.inventory.RiftLibInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 * For tile entities that store items or liquids.
 * */
public abstract class RiftTileEntityContainer extends RiftTileEntity implements ISidedInventory {
    protected final Map<String, RiftLibInventoryHandler> inventoryMap = new HashMap<>();
    private final Map<String, FluidTank> fluidTankMap = new HashMap<>();
    protected final InventorySideContainer inventorySideInfo = new InventorySideContainer();

    public RiftTileEntityContainer() {
        super();
        this.registerInventories();
        this.registerFluidTanks();
    }

    //-----initialization and registration of inventories-----
    public abstract void registerInventories();

    protected void finalizeInventorySidingInfo() {
        this.inventorySideInfo.finalizeInfo();
    }

    protected void registerInventory(String key, int size) {
        this.registerInventory(key, new RiftLibInventoryHandler(size));
    }

    protected void registerInventory(String key, RiftLibInventoryHandler invHandler) {
        //check if the inventory already exists, if it already does, skip
        if (this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" already exists in this tile entity!");
        }

        this.inventoryMap.put(key, invHandler);
    }

    //for dealing with how inventories can be inserted into or extracted from by hoppers or pipes
    //depending on the side, must be put in registerInventories after registering inventories
    protected void registerInventorySiding(@NotNull String key, SideInvInteraction sideInvInteraction, @NotNull EnumFacing... sides) {
        //check if the inventory already exists, if it doesn't, skip
        if (!this.inventoryMap.containsKey(key)) {
            throw new UnsupportedOperationException("Inventory "+key+" does not exist in this tile entity!");
        }

        this.inventorySideInfo.addKey(key, this.inventoryMap.get(key).getSlots(), sideInvInteraction, sides);
    }

    public List<RiftLibInventoryHandler> getInventories() {
        return new ArrayList<>(this.inventoryMap.values());
    }

    //-----getting inventory-----
    public RiftLibInventoryHandler getInventory(String key) {
        return this.inventoryMap.get(key);
    }

    //-----initialization and registration of fluidtanks-----
    public abstract void registerFluidTanks();

    protected void registerFluidTank(String key, int volume) {
        //check if the fluid tank already exists, if it doesn't, skip
        if (this.fluidTankMap.containsKey(key)) {
            throw new UnsupportedOperationException("FluidTank "+key+" already not exist in this tile entity!");
        }

        FluidTank tank = new FluidTank(volume) {
            @Override
            protected void onContentsChanged() {
                markDirty();
                if (!world.isRemote) getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(pos), getWorld().getBlockState(pos), 2);
            }
        };
        tank.setTileEntity(this);
        this.fluidTankMap.put(key, tank);
    }

    //-----getting fluid tank-----
    public FluidTank getFluidTank(String key) {
        return this.fluidTankMap.get(key);
    }

    //-----inventory operations that take advantage of inventorySidingMap, for use in anything that has ISidedInventory-----
    @Override
    public int[] getSlotsForFace(EnumFacing facing) {
        return this.inventorySideInfo.slotsAndDirections.get(facing).stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing facing) {
        return this.inventorySideInfo.interactionAtSide.containsKey(facing)
                && this.inventorySideInfo.slotsAndDirections.containsKey(facing)
                && this.inventorySideInfo.interactionAtSide.get(facing) == SideInvInteraction.INSERT
                && this.inventorySideInfo.slotsAndDirections.get(facing).contains(index)
                && this.itemIsFiltered(index, itemStackIn);
    }

    private boolean itemIsFiltered(int index, ItemStack itemStackIn) {
        String invAtIndex = this.inventorySideInfo.inventoryKeyAtPos(index);
        if (invAtIndex == null) return false;
        RiftLibInventoryHandler inventoryHandler = this.inventoryMap.get(invAtIndex);
        if (inventoryHandler == null) return false;
        if (inventoryHandler.getItemFilter() == null) return true;
        return inventoryHandler.getItemFilter().apply(itemStackIn);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing facing) {
        return this.inventorySideInfo.interactionAtSide.containsKey(facing)
                && this.inventorySideInfo.slotsAndDirections.containsKey(facing)
                && this.inventorySideInfo.interactionAtSide.get(facing) == SideInvInteraction.EXTRACT
                && this.inventorySideInfo.slotsAndDirections.get(facing).contains(index);
    }

    @Override
    public int getSizeInventory() {
        return this.inventorySideInfo.totalSize;
    }

    @Override
    public boolean isEmpty() {
        for (String key : this.inventorySideInfo.orderedKeys) {
            if (!this.inventoryMap.get(key).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return this.inventoryMap.get(slotAndInv.getLeft()).getStackInSlot(slotAndInv.getRight());
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return ItemStackHelper.getAndSplit(
                this.inventoryMap.get(slotAndInv.getLeft()).getItemStackList(),
                slotAndInv.getRight(), count
        );
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return ItemStack.EMPTY;

        return ItemStackHelper.getAndRemove(
                this.inventoryMap.get(slotAndInv.getLeft()).getItemStackList(),
                slotAndInv.getRight()
        );
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) { //slot is relative to InventorySideContainer.totalSize
        ImmutablePair<String, Integer> slotAndInv = this.getSlotAndInv(slot);
        if (slotAndInv == null || slotAndInv.getLeft() == null) return;

        this.inventoryMap.get(slotAndInv.getLeft()).setStackInSlot(slot, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    protected ImmutablePair<String, Integer> getSlotAndInv(int slotRelativeToSidingInfo) {
        String matchedInventory = null;
        int matchedSlot = slotRelativeToSidingInfo;

        if (slotRelativeToSidingInfo >= this.inventorySideInfo.totalSize) return null;

        int prevInvSize = 0;
        for (String key : this.inventorySideInfo.orderedKeys) {
            int invSize = this.inventoryMap.get(key).getSlots();
            if (matchedSlot >= invSize) {
                matchedSlot =- prevInvSize;
                prevInvSize = invSize;
                matchedInventory = key;
            }
            else {
                matchedInventory = key;
                break;
            }
        }
        return new ImmutablePair<>(matchedInventory, matchedSlot);
    }
    //-----inventory stuff ends here-----

    //-----saving and updating nbt-----
    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        //nbt for inventories
        for (Map.Entry<String, RiftLibInventoryHandler> inventoryEntry : this.inventoryMap.entrySet()) {
            NBTTagCompound inventoryNBT = compound.getCompoundTag(inventoryEntry.getKey());
            inventoryEntry.getValue().deserializeNBT(inventoryNBT);
        }

        //nbt for fluid tanks
        for (Map.Entry<String, FluidTank> fluidTankEntry : this.fluidTankMap.entrySet()) {
            NBTTagCompound fluidNBT = compound.getCompoundTag(fluidTankEntry.getKey());
            fluidTankEntry.getValue().readFromNBT(fluidNBT);
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        //nbt for inventories
        for (Map.Entry<String, RiftLibInventoryHandler> inventoryEntry : this.inventoryMap.entrySet()) {
            NBTTagCompound inventoryNBT = inventoryEntry.getValue().serializeNBT();
            compound.setTag(inventoryEntry.getKey(), inventoryNBT);
        }
        //nbt for fluid tanks
        for (Map.Entry<String, FluidTank> fluidTankEntry : this.fluidTankMap.entrySet()) {
            NBTTagCompound fluidNBT = new NBTTagCompound();
            fluidTankEntry.getValue().writeToNBT(fluidNBT);
            compound.setTag(fluidTankEntry.getKey(), fluidNBT);
        }
        return compound;
    }

    //-----subclasses for inventory siding-----
    protected record InventorySideInfo(String key, int size, SideInvInteraction sideInvInteraction, List<EnumFacing> directions) {}

    //for info on restricting inventory extraction via pipes or hoppers or whatever depending on the side
    //of the tile entity it was placed next to
    protected static class InventorySideContainer {
        //non finalized info
        private final List<InventorySideInfo> inventorySideInfos = new ArrayList<>();

        //finalized info
        private int totalSize;
        private final HashMap<String, List<Integer>> displacedIndexes = new HashMap<>();
        private final HashMap<EnumFacing, List<Integer>> slotsAndDirections = new HashMap<>();
        private final HashMap<EnumFacing, SideInvInteraction> interactionAtSide = new HashMap<>();
        private final List<String> orderedKeys = new ArrayList<>();

        public void addKey(String key, int inventorySize, SideInvInteraction sideInvInteraction, EnumFacing[] directions) {
            if (!this.orderedKeys.contains(key)) this.orderedKeys.add(key);
            this.inventorySideInfos.add(new InventorySideInfo(key, inventorySize, sideInvInteraction, new ArrayList<>(Arrays.asList(directions))));
        }

        //must be run at the end of registerInventories() after registering all inventories
        public void finalizeInfo() {
            this.totalSize = this.getTotalSize();
            this.displaceIndexes();
            this.setSlotsAndDirections();
            this.setInteractionAtSide();
        }

        private int getTotalSize() {
            int toReturn = 0;
            List<String> foundKeys = new ArrayList<>();
            for (String key : this.orderedKeys) {
                if (foundKeys.contains(key)) continue;

                List<InventorySideInfo> matches = this.inventorySideInfos.stream()
                        .filter(info -> info.key.equals(key))
                        .toList();
                if (matches.isEmpty()) continue;
                toReturn += matches.getFirst().size;
                foundKeys.add(key);
            }
            return toReturn;
        }

        private void displaceIndexes() {
            int accumulativeSize = 0;
            for (String key : this.orderedKeys) {
                if (this.displacedIndexes.containsKey(key)) continue;

                List<InventorySideInfo> matches = this.inventorySideInfos.stream()
                        .filter(info -> info.key.equals(key))
                        .toList();
                if (matches.isEmpty()) continue;

                InventorySideInfo match = matches.getFirst();
                List<Integer> indexes = IntStream.range(accumulativeSize, accumulativeSize + match.size).boxed().collect(Collectors.toList());
                this.displacedIndexes.put(key, indexes);
                accumulativeSize += match.size;
            }
        }

        private void setSlotsAndDirections() {
            for (EnumFacing direction : EnumFacing.values()) {
                for (InventorySideInfo invMatch : this.inventorySideInfos) {
                    if (!invMatch.directions.contains(direction)) continue;

                    //check first for presence in this.slotsAndDirections
                    if (this.slotsAndDirections.containsKey(direction)) {
                        List<Integer> oldDisplacedIndexes = this.slotsAndDirections.get(direction);

                        Set<Integer> union = new HashSet<>(oldDisplacedIndexes);
                        union.addAll(this.displacedIndexes.get(invMatch.key));
                        this.slotsAndDirections.put(direction, new ArrayList<>(union));
                    }
                    else {
                        List<Integer> displacedIndexes = this.displacedIndexes.get(invMatch.key);
                        this.slotsAndDirections.put(direction, new ArrayList<>(displacedIndexes));
                    }
                }
            }
        }

        private void setInteractionAtSide() {
            for (InventorySideInfo invSideInfo : this.inventorySideInfos) {
                for (EnumFacing direction : invSideInfo.directions()) {
                    this.interactionAtSide.put(direction, invSideInfo.sideInvInteraction());
                }
            }
        }

        public String inventoryKeyAtPos(int pos) {
            for (Map.Entry<String, List<Integer>> entry : this.displacedIndexes.entrySet()) {
                if (entry.getValue().contains(pos)) return entry.getKey();
            }
            return null;
        }
    }

    public enum SideInvInteraction {
        EXTRACT,
        INSERT
    }
}
