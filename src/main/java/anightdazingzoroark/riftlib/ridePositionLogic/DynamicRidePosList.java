package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

public class DynamicRidePosList {
    @NotNull
    private final IDynamicRideUser<?> dynamicRideUser;
    @NotNull
    private final AnimationDataEntity animData;
    @NotNull
    private final Map<String, AnimatedLocator> riderPosMap = new HashMap<>();

    //positions for controller
    @Nullable
    private Vec3d controllerWorldPos;

    //positions for other passengers
    @NotNull
    private List<Vec3d> passengerWorldPositions = new ArrayList<>();
    @NotNull
    public final DynamicRidePosSnapshot snapshot;

    public DynamicRidePosList(@NotNull IDynamicRideUser<?> dynamicRideUser, @NotNull AnimationDataEntity animData) {
        this.dynamicRideUser = dynamicRideUser;
        this.animData = animData;
        this.snapshot = new DynamicRidePosSnapshot(dynamicRideUser);
    }

    /**
     * Continuously update usable AnimatedLocators for use in
     * ride positions.
     * */
    public void updateUsableLocators() {
        EntityLivingBase dynamicRideEntity = this.dynamicRideUser.getDynamicRideUser();
        if (!dynamicRideEntity.world.isRemote) {
            ServerModelRegistry.requireServerModel((IAnimatable<?, ?>) dynamicRideEntity, "server ride positions");
        }

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
    }

    public void updatePositions() {
        //---update controller position---
        if (this.riderPosMap.containsKey(DynamicRidePosUtils.controllerLocatorName)) {
            this.controllerWorldPos = this.getRidePosFromLocator(DynamicRidePosUtils.controllerLocatorName);
        }
        else this.controllerWorldPos = null;

        //---set passenger position---
        //first, order passenger pos names
        Map<String, AnimatedLocator> tempRiderPosMap = new HashMap<>(this.riderPosMap);
        tempRiderPosMap.remove(DynamicRidePosUtils.controllerLocatorName);
        List<String> orderedPassengerPosNames = new ArrayList<>(tempRiderPosMap.keySet());
        orderedPassengerPosNames.sort(Comparator.comparingInt(DynamicRidePosUtils::locatorRideIndex));

        //now set them
        List<Vec3d> newPassengerWorldPositions = new ArrayList<>();
        for (String posName : orderedPassengerPosNames) {
            Vec3d passengerRidePos = this.getRidePosFromLocator(posName);
            if (passengerRidePos == null) continue;
            newPassengerWorldPositions.add(passengerRidePos);
        }
        this.passengerWorldPositions = newPassengerWorldPositions;
    }

    /**
     * This transforms an AnimatedLocator's model-space position into a preview position.
     * */
    @Nullable
    private Vec3d getRidePosFromLocator(String locatorName) {
        if (!this.riderPosMap.containsKey(locatorName)) return null;
        AnimatedLocator animLocator = this.riderPosMap.get(locatorName);
        EntityLivingBase dynamicRideUser = this.dynamicRideUser.getDynamicRideUser();

        //correct locator position first
        Vec3d modelSpacePos = animLocator.getModelSpacePosition();
        Vec3d posVec = new Vec3d(
                -(float) modelSpacePos.x / 16f,
                (float) modelSpacePos.y / 16f,
                -(float) modelSpacePos.z / 16f
        );

        //change based on scale
        posVec = new Vec3d(
                posVec.x * this.dynamicRideUser.dynamicRiderUserScale(),
                posVec.y * this.dynamicRideUser.dynamicRiderUserScale(),
                posVec.z * this.dynamicRideUser.dynamicRiderUserScale()
        );

        //change based on yaw of ridden mob
        double yawRadians = -Math.toRadians(dynamicRideUser.rotationYaw);
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, yawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //reposition to ridden mob
        posVec = new Vec3d(
                posVec.x + dynamicRideUser.posX,
                posVec.y + dynamicRideUser.posY,
                posVec.z + dynamicRideUser.posZ
        );

        //return
        return posVec;
    }

    //todo: remove this and make passenger position application be here instead
    @NotNull
    public List<Vec3d> getPassengerWorldPositions() {
        return this.passengerWorldPositions;
    }

    @Nullable
    public Vec3d getControllerWorldPos() {
        return this.controllerWorldPos;
    }

    /**
     * If true, there's no rideable positions to use.
     * */
    public boolean isEmpty() {
        return this.controllerWorldPos == null && this.passengerWorldPositions.isEmpty();
    }
}
