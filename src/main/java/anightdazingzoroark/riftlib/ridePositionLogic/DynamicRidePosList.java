package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class DynamicRidePosList {
    private Vec3d controllerPos = null;
    private final HashMap<Integer, Vec3d> passengerPosMap = new HashMap<>();

    public DynamicRidePosList() {}

    //this only matters in packets
    public DynamicRidePosList(NBTTagCompound nbtTagCompound) {
        //decode controller pos
        if (nbtTagCompound.hasKey("ControllerPos")) {
            NBTTagCompound controllerPosNBT = nbtTagCompound.getCompoundTag("ControllerPos");
            this.controllerPos = new Vec3d(
                    controllerPosNBT.getDouble("ControllerPosX"),
                    controllerPosNBT.getDouble("ControllerPosY"),
                    controllerPosNBT.getDouble("ControllerPosZ")
            );
        }

        //decode passenger pos map
        if (nbtTagCompound.hasKey("PassengerPosList")) {
            NBTTagList passengerPosNBTList = nbtTagCompound.getTagList("PassengerPosList", 10);
            for (int i = 0; i < passengerPosNBTList.tagCount(); i++) {
                NBTTagCompound passengerPosNBT = passengerPosNBTList.getCompoundTagAt(i);
                int key = passengerPosNBT.getInteger("PassengerPosKey");
                Vec3d pos = new Vec3d(
                        passengerPosNBT.getDouble("PassengerPosX"),
                        passengerPosNBT.getDouble("PassengerPosY"),
                        passengerPosNBT.getDouble("PassengerPosZ")
                );
                this.passengerPosMap.put(key, pos);
            }
        }
    }

    public Vec3d getControllerPos() {
        return this.controllerPos;
    }

    public HashMap<Integer, Vec3d> getPassengerPosMap() {
        return this.passengerPosMap;
    }

    public List<Vec3d> getOrderedPassengerPosList() {
        if (this.passengerPosMap.isEmpty()) return new ArrayList<>();

        List<Vec3d> toReturn = new ArrayList<>();

        //order the indexes first
        int[] indexArray = new int[this.passengerPosMap.size()];
        Object[] keys = this.passengerPosMap.keySet().toArray();

        //get integers first
        for (int x = 0; x < this.passengerPosMap.size(); x++) {
            indexArray[x] = (int) keys[x];
        }

        //now sort the numbers
        for (int x = 0; x < indexArray.length; x++) {
            for (int y = x; y < indexArray.length; y++) {
                if (indexArray[y] < indexArray[x]) {
                    int temp = indexArray[x];
                    indexArray[x] = indexArray[y];
                    indexArray[y] = temp;
                }
            }
        }

        //create the final ordered position list
        for (int index : indexArray) {
            toReturn.add(this.passengerPosMap.get(index));
        }

        return toReturn;
    }

    public void addPosition(AnimatedLocator locator) {
        if (locator == null) return;
        if (DynamicRidePosUtils.locatorCanBeControllerPos(locator.getName())) {
            Vec3d modelSpacePos = locator.getModelSpacePosition();
            this.controllerPos = new Vec3d(
                    modelSpacePos.x / 16f,
                    modelSpacePos.y / 16f,
                    -modelSpacePos.z / 16f
            );
        }
        else if (DynamicRidePosUtils.locatorCanBeRidePos(locator.getName())) {
            int index = DynamicRidePosUtils.locatorRideIndex(locator.getName());
            if (index >= 0) {
                Vec3d modelSpacePos = locator.getModelSpacePosition();
                Vec3d finalNewPos = new Vec3d(
                        modelSpacePos.x / 16f,
                        modelSpacePos.y / 16f,
                        -modelSpacePos.z / 16f
                );
                this.passengerPosMap.put(index, finalNewPos);
            }
        }
    }

    public boolean isEmpty() {
        return this.controllerPos == null && this.passengerPosMap.isEmpty();
    }

    //this only matters for use in packets
    public NBTTagCompound getAsNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //encode controller
        if (this.controllerPos != null) {
            NBTTagCompound controllerNBT = new NBTTagCompound();
            controllerNBT.setDouble("ControllerPosX", this.controllerPos.x);
            controllerNBT.setDouble("ControllerPosY", this.controllerPos.y);
            controllerNBT.setDouble("ControllerPosZ", this.controllerPos.z);
            toReturn.setTag("ControllerPos", controllerNBT);
        }

        //encode passenger positions
        if (!this.passengerPosMap.isEmpty()) {
            NBTTagList passengerPosNBTList = new NBTTagList();
            Set<Map.Entry<Integer, Vec3d>> passengerPosEntrySet = this.passengerPosMap.entrySet();
            for (Map.Entry<Integer, Vec3d> passengerPosEntry : passengerPosEntrySet) {
                NBTTagCompound passengerPosNBT = new NBTTagCompound();
                passengerPosNBT.setInteger("PassengerPosKey", passengerPosEntry.getKey());
                passengerPosNBT.setDouble("PassengerPosX", passengerPosEntry.getValue().x);
                passengerPosNBT.setDouble("PassengerPosY", passengerPosEntry.getValue().y);
                passengerPosNBT.setDouble("PassengerPosZ", passengerPosEntry.getValue().z);
                passengerPosNBTList.appendTag(passengerPosNBT);
            }
            toReturn.setTag("PassengerPosList", passengerPosNBTList);
        }

        return toReturn;
    }
}
