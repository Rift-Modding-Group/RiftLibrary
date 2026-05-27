package anightdazingzoroark.riftlib.ridePositionLogic;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;

/**
 * <p>
 * This class holds information on the positions involving ride positions on a
 * IDynamicRideUser. It allows for transforming AnimatedLocators into vectors that
 * directly represent where you are positioned on when you ride.
 * </p>
 * <br />
 * <h3>Dictionary</h3>
 * <ul>
 *     <li>
 *         <b>Preview Position</b> - refers to a partially initialized ride position. It is transformed from
 *         the Animated Locator's model space position, which includes inversion of x and y positions,
 *         dividing by 16, and multiplying by the entity's scale. It is to be then transformed later
 *         depending on the context.
 *     </li>
 *     <li>
 *         <b>World Position</b> - refers to the ride position in the game's server. It's the main authority
 *         when it comes to where a passenger/controller is in the world while its riding the entity.
 *         It is transformed from a preview position.
 *     </li>
 *     <li>
 *         <b>Render Position</b> - refers to the ride position on a client. It's what a player sees when they're
 *         riding the entity. It has no specific variables here, only functions to use in DynamicRidePosTicker.
 *     </li>
 * </ul>
 * */
public class DynamicRidePosList {
    @NotNull
    private final AnimationDataEntity animData;
    @NotNull
    private final Map<String, AnimatedLocator> riderPosMap = new HashMap<>();

    //positions for controller
    @Nullable
    private Vec3d controllerWorldPos;
    @Nullable
    private Vec3d controllerPreviewPos;
    @Nullable
    private Vec3d prevControllerPreviewPos;

    //positions for other passengers
    @NotNull
    private List<Vec3d> passengerWorldPositions = new ArrayList<>();
    @NotNull
    private List<Vec3d> passengerPreviewPositions = new ArrayList<>();
    @NotNull
    private List<Vec3d> prevPassengerPreviewPositions = new ArrayList<>();

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
        this.prevControllerPreviewPos = this.controllerPreviewPos;
        if (this.riderPosMap.containsKey(DynamicRidePosUtils.controllerLocatorName)) {
            this.controllerPreviewPos = this.getPreviewRidePos(DynamicRidePosUtils.controllerLocatorName);
            this.controllerWorldPos = this.getWorldRidePos(this.controllerPreviewPos);
        }
        else {
            this.controllerPreviewPos = null;
            this.controllerWorldPos = null;
        }

        //---set passenger position---
        this.prevPassengerPreviewPositions = new ArrayList<>(this.passengerPreviewPositions);
        List<Vec3d> newPassengerWorldPositions = new ArrayList<>();
        List<Vec3d> newPassengerPreviewPositions = new ArrayList<>();
        for (String posName : this.getOrderedPassengerPosNames()) {
            Vec3d passengerPreviewPos = this.getPreviewRidePos(posName);
            if (passengerPreviewPos == null) continue;

            newPassengerPreviewPositions.add(passengerPreviewPos);
            newPassengerWorldPositions.add(this.getWorldRidePos(passengerPreviewPos));
        }
        this.passengerPreviewPositions = newPassengerPreviewPositions;
        this.passengerWorldPositions = newPassengerWorldPositions;
    }

    /**
     * This transforms an AnimatedLocator's model-space position into a preview position.
     * */
    @Nullable
    private Vec3d getPreviewRidePos(String locatorName) {
        if (!this.riderPosMap.containsKey(locatorName)) return null;
        AnimatedLocator animLocator = this.riderPosMap.get(locatorName);
        IDynamicRideUser<?> dynamicRideUser = (IDynamicRideUser<?>) this.animData.getHolder();

        //correct locator position first
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

        //return
        return posVec;
    }

    /**
     * Gets passenger position names in their defined ride order.
     * */
    @NotNull
    private List<String> getOrderedPassengerPosNames() {
        Map<String, AnimatedLocator> tempRiderPosMap = new HashMap<>(this.riderPosMap);
        tempRiderPosMap.remove(DynamicRidePosUtils.controllerLocatorName);
        List<String> orderedPassengerPosNames = new ArrayList<>(tempRiderPosMap.keySet());
        orderedPassengerPosNames.sort(Comparator.comparingInt(DynamicRidePosUtils::locatorRideIndex));
        return orderedPassengerPosNames;
    }

    /**
     * This function is basically the final step in finalizing a preview ride position into either
     * world or render.
     * */
    @NotNull
    private Vec3d getFinalizedRidePos(@NotNull Vec3d previewRidePos, double x, double y, double z, double yawRadians) {
        //rotate vector around yaw
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, yawRadians, 0);
        Vec3d posVec = VectorUtils.rotateVectorWithQuaternion(previewRidePos, quaternion);

        //orient to user position
        return new Vec3d(
                posVec.x + x,
                posVec.y + y,
                posVec.z + z
        );
    }

    /**
     * Transform a preview position into a world position using only the preview position.
     */
    @NotNull
    private Vec3d getWorldRidePos(Vec3d previewRidePos) {
        double normalYawRadians = -Math.toRadians(this.animData.getHolder().rotationYawHead);
        double riddenYawRadians = -Math.toRadians(this.animData.getHolder().rotationYaw);
        double finalYawRadians = this.animData.getHolder().isBeingRidden() ? riddenYawRadians : normalYawRadians;

        return this.getFinalizedRidePos(
                previewRidePos,
                this.animData.getHolder().posX,
                this.animData.getHolder().posY,
                this.animData.getHolder().posZ,
                finalYawRadians
        );
    }

    /**
     * This function is used to finalize a preview ride position into a render ride position.
     * */
    @SideOnly(Side.CLIENT)
    @NotNull
    private Vec3d getRenderRidePos(@Nullable Vec3d prevPreviewPos, @NotNull Vec3d previewPos, float partialTicks) {
        Vec3d lerpedPreviewPos;
        if (prevPreviewPos == null) lerpedPreviewPos = previewPos;
        else {
            lerpedPreviewPos = new Vec3d(
                    Interpolations.lerp(prevPreviewPos.x, previewPos.x, partialTicks),
                    Interpolations.lerp(prevPreviewPos.y, previewPos.y, partialTicks),
                    Interpolations.lerp(prevPreviewPos.z, previewPos.z, partialTicks)
            );
        }

        double x = Interpolations.lerp(this.animData.getHolder().prevPosX, this.animData.getHolder().posX, partialTicks);
        double y = Interpolations.lerp(this.animData.getHolder().prevPosY, this.animData.getHolder().posY, partialTicks);
        double z = Interpolations.lerp(this.animData.getHolder().prevPosZ, this.animData.getHolder().posZ, partialTicks);
        double normalYawRadians = -Math.toRadians(Interpolations.lerpYaw(
                this.animData.getHolder().prevRotationYawHead,
                this.animData.getHolder().rotationYawHead,
                partialTicks
        ));
        double riddenYawRadians = -Math.toRadians(Interpolations.lerpYaw(
                this.animData.getHolder().prevRotationYaw,
                this.animData.getHolder().rotationYaw,
                partialTicks
        ));
        double finalYawRadians = this.animData.getHolder().isBeingRidden() ? riddenYawRadians : normalYawRadians;

        return this.getFinalizedRidePos(lerpedPreviewPos, x, y, z, finalYawRadians);
    }

    @Nullable
    public Vec3d getControllerWorldPos() {
        return this.controllerWorldPos;
    }

    @NotNull
    public List<Vec3d> getPassengerWorldPositions() {
        return this.passengerWorldPositions;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public Vec3d getControllerRenderPos(float partialTicks) {
        Vec3d livePreviewPos = this.getPreviewRidePos(DynamicRidePosUtils.controllerLocatorName);
        if (livePreviewPos != null) return this.getRenderRidePos(null, livePreviewPos, partialTicks);

        if (this.controllerPreviewPos == null) return null;
        return this.getRenderRidePos(this.prevControllerPreviewPos, this.controllerPreviewPos, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    public List<Vec3d> getPassengerRenderPositions(float partialTicks) {
        List<String> orderedPassengerPosNames = this.getOrderedPassengerPosNames();
        if (!orderedPassengerPosNames.isEmpty()) {
            List<Vec3d> toReturn = new ArrayList<>();
            for (String posName : orderedPassengerPosNames) {
                Vec3d livePreviewPos = this.getPreviewRidePos(posName);
                if (livePreviewPos == null) continue;

                toReturn.add(this.getRenderRidePos(null, livePreviewPos, partialTicks));
            }
            return toReturn;
        }

        if (this.passengerPreviewPositions.isEmpty()) return List.of();

        List<Vec3d> toReturn = new ArrayList<>();
        for (int index = 0; index < this.passengerPreviewPositions.size(); index++) {
            Vec3d currentPos = this.passengerPreviewPositions.get(index);
            Vec3d prevPos = index < this.prevPassengerPreviewPositions.size()
                    ? this.prevPassengerPreviewPositions.get(index)
                    : null;

            toReturn.add(this.getRenderRidePos(prevPos, currentPos, partialTicks));
        }
        return toReturn;
    }

    /**
     * If true, there's no rideable positions to use.
     * */
    public boolean isEmpty() {
        return this.controllerWorldPos == null && this.passengerWorldPositions.isEmpty();
    }
}
