package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * This class holds information on the positions involving ride positions on a
 * IDynamicRideUser. It allows for transforming AnimatedLocators into vectors that
 * directly represent where you are positioned on when you ride.
 * */
public class DynamicRidePosList {
    @NotNull
    private final AnimationDataEntity animData;
    @NotNull
    private final Map<String, AnimatedLocator> riderPosMap = new HashMap<>();
    @Nullable
    private Vec3d controllerPos;
    @Nullable
    private Vec3d prevControllerPos;
    @NotNull
    private List<Vec3d> passengerPositions = new ArrayList<>();
    @NotNull
    private List<Vec3d> prevPassengerPositions = new ArrayList<>();

    public DynamicRidePosList(@NotNull AnimationDataEntity animData) {
        this.animData = animData;
    }

    /**
     * Continuously update this dynamic ride position list to update the rider positions
     * based on animated locators provided by animation data. Use this in the
     * Entity.onUpdate() method of the entity you're implementing IDynamicRideUser
     * to, and make sure its on both client and server.
     * */
    public void onUpdate() {
        //---define set for existing position names---
        Set<String> existingPosNames = new HashSet<>();

        //---create map---
        for (AnimatedLocator locator : this.animData.getAnimatedLocators()) {
            //skip locators that cannot be used as rider positions
            if (!DynamicRidePosUtils.locatorCanBeRidePos(locator.getName())) continue;

            existingPosNames.add(locator.getName());

            //if valid locator name cannot be found in riderPosMap, time to add it
            if (!this.riderPosMap.containsKey(locator.getName())) {
                this.riderPosMap.put(locator.getName(), locator);
            }
            //otherwise, test equality between the held locator in the map
            //and the locator given by animData
            else {
                AnimatedLocator oldLocator = this.riderPosMap.get(locator.getName());
                if (oldLocator != locator) {
                    this.riderPosMap.put(locator.getName(), locator);
                }
            }
        }

        //---remove locators from this.riderPosMap that don't exist in existingPosNames---
        this.riderPosMap.keySet().removeIf(posName -> !existingPosNames.contains(posName));

        //---update controller position---
        this.prevControllerPos = this.controllerPos;
        if (this.riderPosMap.containsKey(DynamicRidePosUtils.controllerLocatorName)) {
            this.controllerPos = this.getWorldRidePos(DynamicRidePosUtils.controllerLocatorName);
        }
        else this.controllerPos = null;

        //---set passenger position---
        //order them first however
        Map<String, AnimatedLocator> tempRiderPosMap = new HashMap<>(this.riderPosMap);
        tempRiderPosMap.remove(DynamicRidePosUtils.controllerLocatorName);
        List<String> orderedPassengerPosNames = new ArrayList<>(tempRiderPosMap.keySet());
        orderedPassengerPosNames.sort(Comparator.comparingInt(DynamicRidePosUtils::locatorRideIndex));

        //now change passengerPositions and prevPassengerPositions with this in mind
        this.prevPassengerPositions = new ArrayList<>(this.passengerPositions);
        List<Vec3d> newPassengerPositions = new ArrayList<>();
        for (String posName : orderedPassengerPosNames) {
            newPassengerPositions.add(this.getWorldRidePos(posName));
        }
        this.passengerPositions = newPassengerPositions;
    }

    /**
     * Get the model-space position of an AnimatedLocator and, using information involving
     * the holder entity, transform it into a valid rider position.
     */
    @Nullable
    private Vec3d getWorldRidePos(String locatorName) {
        if (!this.riderPosMap.containsKey(locatorName)) return null;
        AnimatedLocator animLocator = this.riderPosMap.get(locatorName);
        IDynamicRideUser<?> dynamicRideUser = (IDynamicRideUser<?>) this.animData.getHolder();

        //correct locator positions first
        Vec3d posVec = new Vec3d(
                -(float) animLocator.getModelSpacePosition().x / 16f,
                (float) animLocator.getModelSpacePosition().y / 16f,
                -(float) animLocator.getModelSpacePosition().z / 16f
        );

        //change based on scale
        posVec = new Vec3d(
                posVec.x * dynamicRideUser.dynamicRiderUserScale(),
                posVec.y * dynamicRideUser.dynamicRiderUserScale(),
                posVec.z * dynamicRideUser.dynamicRiderUserScale()
        );

        //determine yaw
        double normalYawRadians = -Math.toRadians(this.animData.getHolder().rotationYawHead);
        double riddenYawRadians = -Math.toRadians(this.animData.getHolder().rotationYaw);
        double finalYawRadians = this.animData.getHolder().isBeingRidden() ? riddenYawRadians : normalYawRadians;

        //rotate vector around yaw
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //orient to user position
        posVec = new Vec3d(
                posVec.x + this.animData.getHolder().posX,
                posVec.y + this.animData.getHolder().posY,
                posVec.z + this.animData.getHolder().posZ
        );

        //return
        return posVec;
    }

    @Nullable
    public Vec3d getControllerPos() {
        return this.controllerPos;
    }

    @NotNull
    public List<Vec3d> getPassengerPositions() {
        return this.passengerPositions;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public Vec3d getLerpedControllerPos(float partialTicks) {
        if (this.controllerPos == null || this.prevControllerPos == null) return null;

        double lerpedX = Interpolations.lerp(this.prevControllerPos.x, this.controllerPos.x, partialTicks);
        double lerpedY = Interpolations.lerp(this.prevControllerPos.y, this.controllerPos.y, partialTicks);
        double lerpedZ = Interpolations.lerp(this.prevControllerPos.z, this.controllerPos.z, partialTicks);

        return new Vec3d(lerpedX, lerpedY, lerpedZ);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    public List<Vec3d> getLerpedPassengerPositions(float partialTicks) {
        if (this.passengerPositions.isEmpty() || this.prevPassengerPositions.isEmpty()) return List.of();

        List<Vec3d> toReturn = new ArrayList<>();
        int smallerSize = Math.max(this.passengerPositions.size(), this.prevPassengerPositions.size());
        for (int index = 0; index < smallerSize; index++) {
            Vec3d currentPos = this.passengerPositions.get(index);
            Vec3d prevPos = this.prevPassengerPositions.get(index);

            double lerpedX = Interpolations.lerp(prevPos.x, currentPos.x, partialTicks);
            double lerpedY = Interpolations.lerp(prevPos.y, currentPos.y, partialTicks);
            double lerpedZ = Interpolations.lerp(prevPos.z, currentPos.z, partialTicks);

            toReturn.add(new Vec3d(lerpedX, lerpedY, lerpedZ));
        }
        return toReturn;
    }

    /**
     * If true, there's no rideable positions to use.
     * */
    public boolean isEmpty() {
        return this.controllerPos == null && this.passengerPositions.isEmpty();
    }
}
