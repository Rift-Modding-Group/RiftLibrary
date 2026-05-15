package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

public class RiftLibRay {
    @NotNull
    private final AbstractAnimationData<?> animData;
    private final double maxRayLength;
    private final double rayWidth;
    private final double rayCreationTime;
    private final double rayFadeOutTime;

    //this represents the phase the ray is currently in
    @NotNull
    private CreationPhase creationPhase;
    //these are anim dependent displacements due to animations
    @NotNull
    private double[] animPosOffset = new double[]{0, 0, 0};
    @NotNull
    private Quaternion animQuaternionOffset = new Quaternion();
    //how much time has passed since the change in creation phase in ticks
    private int tick;

    public RiftLibRay(@NotNull AbstractAnimationData<?> animData, double maxRayLength, double rayWidth, double rayCreationTime, double rayFadeOutTime) {
        this.animData = animData;
        this.maxRayLength = maxRayLength;
        this.rayWidth = rayWidth;
        this.rayCreationTime = rayCreationTime * 20D;
        this.rayFadeOutTime = rayFadeOutTime * 20D;

        this.creationPhase = CreationPhase.CREATE;
    }

    public void onUpdate() {}

    //better way to change the creation phase
    private void changeCreationPhase(@NotNull CreationPhase phase) {
        this.creationPhase = phase;
        this.tick = 0;
    }

    //set additional position offset of the origin of the ray based on changes in
    //animation. to be used in AnimatedGeoModel.
    public void setAnimPosOffset(double x, double y, double z) {
        this.animPosOffset = new double[]{x, y, z};
    }

    //set additional quaternion offset for the orientation of the ray based on
    //changes in animation. to be used in AnimatedGeoModel.
    public void setAnimQuaternionOffset(@NotNull Quaternion quaternion) {
        this.animQuaternionOffset = quaternion;
    }

    private enum CreationPhase {
        CREATE,
        RUN,
        FADE_OUT;
    }

    public enum RayShape {
        BEAM, //ray is an ever extending beam with fixed width
        CONE; //ray is a cone which reaches its max width at max length
    }
}
