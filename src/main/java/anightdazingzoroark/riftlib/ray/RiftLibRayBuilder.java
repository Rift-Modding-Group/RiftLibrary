package anightdazingzoroark.riftlib.ray;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.ray.rayShape.*;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidthRange;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A ray builder is like a template but for the rays to create.
 * */
public class RiftLibRayBuilder {
    private RayType rayType;
    private Supplier<RiftLibRayShape> rayShapeSupplier;
    private double maxLength = 1;
    @NotNull
    private RiftLibRayWidthRange rayWidthRange = new RiftLibRayWidthRange(1D);
    private double raySpeed = 1D;
    private boolean spreadOnHitBlock;
    private boolean onlyOneSegment;
    @NotNull
    private BiFunction<IRayCreator<?>, BlockPos, Boolean> breakBlockCondition = (rayCreator, pos) -> false;

    //-----setters-----
    //---shape setters---
    public RiftLibRayBuilder setShapeSpray(double maxLength, double width) {
        return this.setShapeSpray(maxLength, width, width);
    }

    public RiftLibRayBuilder setShapeSpray(double maxLength, double minWidth, double maxWidth) {
        if (this.rayType != null) {
            RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
            return this;
        }
        if (maxWidth < minWidth) {
            RiftLib.LOGGER.error("Ray max width {} is smaller than min width {}. Skipping...", maxWidth, minWidth);
            return this;
        }
        this.rayType = RayType.SPRAY;
        this.rayShapeSupplier = RiftLibRayMovingShape::new;
        this.maxLength = maxLength;
        this.rayWidthRange = new RiftLibRayWidthRange(minWidth, maxWidth);
        return this;
    }

    public RiftLibRayBuilder setShapeBeam(double maxLength, double width) {
        return this.setShapeBeam(maxLength, width, width);
    }

    public RiftLibRayBuilder setShapeBeam(double maxLength, double minWidth, double maxWidth) {
        if (this.rayType != null) {
            RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
            return this;
        }
        if (maxWidth < minWidth) {
            RiftLib.LOGGER.error("Ray max width {} is smaller than min width {}. Skipping...", maxWidth, minWidth);
            return this;
        }
        this.rayType = RayType.BEAM;
        this.rayShapeSupplier = RiftLibRayMovingShape::new;
        this.maxLength = maxLength;
        this.rayWidthRange = new RiftLibRayWidthRange(minWidth, maxWidth);
        return this;
    }

    public RiftLibRayBuilder setShapeImpactSphere(double startRadius, double endRadius) {
        return this.setShapeImpactSphere(startRadius, endRadius, false);
    }

    public RiftLibRayBuilder setShapeImpactSphere(double startRadius, double endRadius, boolean upperOnly) {
        return this.setShapeImpactEllipse(
                startRadius, startRadius, startRadius,
                endRadius, endRadius, endRadius,
                upperOnly
        );
    }

    public RiftLibRayBuilder setShapeImpactEllipse(
            double startXZRadius, double startYRadius,
            double endXZRadius, double endYRadius
    ) {
        return this.setShapeImpactEllipse(startXZRadius, startYRadius, endXZRadius, endYRadius, false);
    }

    public RiftLibRayBuilder setShapeImpactEllipse(
            double startXZRadius, double startYRadius,
            double endXZRadius, double endYRadius,
            boolean upperOnly
    ) {
        return this.setShapeImpactEllipse(startXZRadius, startYRadius, startXZRadius, endXZRadius, endYRadius, endXZRadius, upperOnly);
    }

    public RiftLibRayBuilder setShapeImpactEllipse(
            double startXRadius, double startYRadius, double startZRadius,
            double endXRadius, double endYRadius, double endZRadius
    ) {
        return this.setShapeImpactEllipse(startXRadius, startYRadius, startZRadius, endXRadius, endYRadius, endZRadius, false);
    }

    public RiftLibRayBuilder setShapeImpactEllipse(
            double startXRadius, double startYRadius, double startZRadius,
            double endXRadius, double endYRadius, double endZRadius,
            boolean upperOnly
    ) {
        if (this.rayType != null) {
            RiftLib.LOGGER.warn("This ray already has its shape defined as {}. Skipping...", this.rayType);
            return this;
        }
        if (endXRadius < startXRadius || endYRadius < startYRadius || endZRadius < startZRadius) {
            RiftLib.LOGGER.error(
                    "Impact ray end radii [{}, {}, {}] must not be smaller than start radii [{}, {}, {}]. Skipping...",
                    endXRadius, endYRadius, endZRadius, startXRadius, startYRadius, startZRadius
            );
            return this;
        }

        this.rayType = RayType.IMPACT;
        this.rayShapeSupplier = () -> new RiftLibRayEllipsoidImpactShape(upperOnly);
        this.rayWidthRange = new RiftLibRayWidthRange(
                startXRadius, startYRadius, startZRadius,
                endXRadius, endYRadius, endZRadius
        );
        return this;
    }

    //---other setters---
    public RiftLibRayBuilder setSpreadOnHitBlock() {
        if (this.rayType == RayType.IMPACT) {
            RiftLib.LOGGER.warn("Impact type rays do not travel, so using setSpreadOnHitBlock() is unnecessary.");
            return this;
        }
        this.spreadOnHitBlock = true;
        return this;
    }

    public RiftLibRayBuilder setRaySpeed(double value) {
        this.raySpeed = value;
        return this;
    }

    public RiftLibRayBuilder setBreakBlockCondition(@NotNull BiFunction<IRayCreator<?>, BlockPos, Boolean> value) {
        this.breakBlockCondition = value;
        return this;
    }

    public RiftLibRayBuilder setOnlyOneSegment() {
        this.onlyOneSegment = true;
        return this;
    }

    //-----getters-----
    public RiftLibRayShape getRayShape() {
        return this.rayShapeSupplier.get();
    }

    @NotNull
    public RiftLibRayWidthRange getRayWidthRange() {
        return this.rayWidthRange;
    }

    public double getRayMaxLength() {
        return this.maxLength;
    }

    public double getRaySpeed() {
        return this.raySpeed;
    }

    public boolean getSpreadOnHitBlock() {
        return this.spreadOnHitBlock || this.rayType == RayType.IMPACT;
    }

    public boolean isImpact() {
        return this.rayType == RayType.IMPACT;
    }

    public boolean getImpactIsThreeDim() {
        return this.rayType == RayType.IMPACT;
    }

    public boolean followUserRotation() {
        return this.rayType == RayType.BEAM;
    }

    @NotNull
    public BiFunction<IRayCreator<?>, BlockPos, Boolean> getBreakBlockCondition() {
        return this.breakBlockCondition;
    }

    public boolean getOnlyOneSegment() {
        return this.onlyOneSegment;
    }

    /**
     * Test validity of builder before creation
     * */
    public boolean isValid() {
        return this.rayType != null && this.rayShapeSupplier != null;
    }

    /**
     * This is for the shape of the ray.
     * */
    public enum RayType {
        /**
         * A spray is made of AABBs traveling along a straight line, and will not
         * instantly rotate alongside the user's rotations.
         * */
        SPRAY,
        /**
         * A fixed beam is made of AABBs forming a fixed straight line, and will
         * instantly rotate alongside the user's rotations.
         * */
        BEAM,
        /**
         * Impact only, does not move.
         */
        IMPACT
    }
}
