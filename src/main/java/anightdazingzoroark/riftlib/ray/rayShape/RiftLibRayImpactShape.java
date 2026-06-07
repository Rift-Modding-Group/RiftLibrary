package anightdazingzoroark.riftlib.ray.rayShape;

import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.ray.RiftLibRaySegment;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidth;
import anightdazingzoroark.riftlib.ray.rayWidth.RiftLibRayWidthRange;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibBoxShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibCylinderShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibEllipsoidShape;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibThreeDimShape;
import anightdazingzoroark.riftlib.util.MathUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base behavior for impact-only rays. Concrete subclasses define which portion
 * of the impact sphere can be reached.
 * */
public abstract class RiftLibRayImpactShape extends RiftLibRayShape {
    @Override
    public void onCreateSegment(@NotNull RiftLibRaySegment segment) {
        RiftLibRayWidthRange widthRange = segment.builder.getRayWidthRange();
        RiftLibRayWidth initialWidth = widthRange.isThreeDim()
                ? new RiftLibRayWidth(
                        widthRange.getStartWidth(Axis.X),
                        widthRange.getStartWidth(Axis.Y),
                        widthRange.getStartWidth(Axis.Z)
                )
                : new RiftLibRayWidth(widthRange.getStartWidth());
        RiftLibRayWidth maxWidth = widthRange.isThreeDim()
                ? new RiftLibRayWidth(
                        widthRange.getEndWidth(Axis.X),
                        widthRange.getEndWidth(Axis.Y),
                        widthRange.getEndWidth(Axis.Z)
                )
                : new RiftLibRayWidth(widthRange.getEndWidth());
        double decayStart = -Math.max(1D, Math.max(0D, segment.builder.getRaySpeed()));

        segment.setImpacting();
        segment.setImpactMaxWidth(maxWidth);
        segment.setImpactCurrentWidth(initialWidth);
        segment.setImpactDepth(Math.max(1D, initialWidth.isThreeDim() ? initialWidth.getMaxWidth() : initialWidth.getWidth()));
        segment.setImpactDecayWidth(widthRange.isThreeDim() ? new RiftLibRayWidth(decayStart, decayStart, decayStart) : new RiftLibRayWidth(decayStart));
        segment.getSegmentImpactPositions().clear();
        segment.getExpiredImpactPositions().clear();
        segment.getImpactFrontier().clear();

        segment.setImpactOriginPos(new BlockPos(segment.initPos));
        this.setImpactShape(
                segment,
                this.createImpactShape(segment, segment.initPos, segment.getImpactCurrentWidth(), segment.getImpactDepth()),
                segment.initPos
        );
        this.addImpactPositionsWithinCurrentShape(segment);
    }

    @Override
    public void updateImpact(@NotNull RiftLibRaySegment segment) {
        RiftLibRayWidth.Mutable currentWidth = segment.getImpactCurrentWidth();
        RiftLibRayWidth maxWidth = segment.getImpactMaxWidth();
        double growthSpeed = Math.max(0D, segment.builder.getRaySpeed());
        boolean widthChanged;

        if (currentWidth.isThreeDim()) {
            double xWidth = Math.min(maxWidth.getWidth(Axis.X), currentWidth.getWidth(Axis.X) + growthSpeed);
            double yWidth = Math.min(maxWidth.getWidth(Axis.Y), currentWidth.getWidth(Axis.Y) + growthSpeed);
            double zWidth = Math.min(maxWidth.getWidth(Axis.Z), currentWidth.getWidth(Axis.Z) + growthSpeed);
            widthChanged = xWidth != currentWidth.getWidth(Axis.X) || yWidth != currentWidth.getWidth(Axis.Y) || zWidth != currentWidth.getWidth(Axis.Z);
            currentWidth.setWidth(Axis.X, xWidth);
            currentWidth.setWidth(Axis.Y, yWidth);
            currentWidth.setWidth(Axis.Z, zWidth);
        }
        else {
            double width = Math.min(maxWidth.getWidth(), currentWidth.getWidth() + growthSpeed);
            widthChanged = width != currentWidth.getWidth();
            currentWidth.setWidth(width);
        }

        if (widthChanged) {
            this.setImpactShape(
                    segment,
                    this.createImpactShape(segment, segment.initPos, currentWidth, segment.getImpactDepth()),
                    segment.initPos
            );
            this.addImpactPositionsWithinCurrentShape(segment);
        }

        segment.decayImpactPositions();

        boolean widthAtMax = currentWidth.isThreeDim()
                ? currentWidth.getWidth(Axis.X) >= maxWidth.getWidth(Axis.X)
                && currentWidth.getWidth(Axis.Y) >= maxWidth.getWidth(Axis.Y)
                && currentWidth.getWidth(Axis.Z) >= maxWidth.getWidth(Axis.Z)
                : currentWidth.getWidth() >= maxWidth.getWidth();
        if (widthAtMax && !segment.hasImpactPositions()) {
            segment.killSegment();
        }
    }

    @Override
    public boolean isWithinImpactDecayFront(@NotNull RiftLibRaySegment segment, @NotNull BlockPos pos) {
        if (segment.getImpactDecayRadius() <= 0D) return false;
        return this.blockIntersectsShape(
                segment,
                pos,
                this.createImpactShape(segment, segment.initPos, segment.getImpactDecayWidth(), segment.getImpactDepth())
        );
    }

    //-----debug grid line helpers-----
    @Override
    @NotNull
    protected List<RiftLibRaySegment.DebugLine> createDebugGridLines(@NotNull RiftLibRaySegment segment) {
        return this.createImpactSphereGridLines(segment, false);
    }

    @NotNull
    protected List<RiftLibRaySegment.DebugLine> createImpactSphereGridLines(@NotNull RiftLibRaySegment segment, boolean upperOnly) {
        if (this.impactOriginVec == null) return Collections.emptyList();

        List<RiftLibRaySegment.DebugLine> lines = new ArrayList<>();
        int radialSteps = 24;
        int verticalSteps = upperOnly ? 6 : 12;
        RiftLibRayWidth width = segment.getImpactCurrentWidth();
        double xRadius = width.isThreeDim() ? Math.max(0.001D, width.getWidth(Axis.X)) : Math.max(0.001D, width.getWidth());
        double yRadius = width.isThreeDim() ? Math.max(0.001D, width.getWidth(Axis.Y)) : xRadius;
        double zRadius = width.isThreeDim() ? Math.max(0.001D, width.getWidth(Axis.Z)) : xRadius;
        double minTheta = upperOnly ? 0D : -Math.PI / 2D;
        double maxTheta = Math.PI / 2D;

        for (int lat = 0; lat <= verticalSteps; lat++) {
            double theta = MathUtils.slopeResult(lat, true, 0D, verticalSteps, minTheta, maxTheta);
            double y = Math.sin(theta) * yRadius;
            double ringScale = Math.cos(theta);

            for (int i = 0; i < radialSteps; i++) {
                double angleA = Math.PI * 2D * i / radialSteps;
                double angleB = Math.PI * 2D * (i + 1) / radialSteps;
                lines.add(new RiftLibRaySegment.DebugLine(
                        this.impactOriginVec.add(Math.cos(angleA) * ringScale * xRadius, y, Math.sin(angleA) * ringScale * zRadius),
                        this.impactOriginVec.add(Math.cos(angleB) * ringScale * xRadius, y, Math.sin(angleB) * ringScale * zRadius)
                ));
            }
        }

        for (int i = 0; i < radialSteps; i += 2) {
            double angle = Math.PI * 2D * i / radialSteps;
            Vec3d previousPoint = null;

            for (int lat = 0; lat <= verticalSteps; lat++) {
                double theta = MathUtils.slopeResult(lat, true, 0D, verticalSteps, minTheta, maxTheta);
                double y = Math.sin(theta) * yRadius;
                double ringScale = Math.cos(theta);
                Vec3d currentPoint = this.impactOriginVec.add(Math.cos(angle) * ringScale * xRadius, y, Math.sin(angle) * ringScale * zRadius);

                if (previousPoint != null) lines.add(new RiftLibRaySegment.DebugLine(previousPoint, currentPoint));
                previousPoint = currentPoint;
            }
        }

        return lines;
    }

    //-----other helpers-----
    protected void setImpactShape(@NotNull RiftLibRaySegment segment, @NotNull RiftLibThreeDimShape impactShape, @NotNull Vec3d impactOriginVec) {
        this.impactShape = impactShape;
        this.impactOriginVec = impactOriginVec;
        segment.setSegmentAABB(this.createCenteredAABB(impactOriginVec, this.getShapeBoundingRadius(segment, impactShape)));
    }

    @NotNull
    private AxisAlignedBB createCenteredAABB(@NotNull Vec3d origin, double radius) {
        return new AxisAlignedBB(
                origin.x - radius,
                origin.y - radius,
                origin.z - radius,
                origin.x + radius,
                origin.y + radius,
                origin.z + radius
        ).grow(0.001D);
    }

    private double getShapeBoundingRadius(@NotNull RiftLibRaySegment segment, @NotNull RiftLibThreeDimShape shape) {
        return switch (shape) {
            case RiftLibEllipsoidShape ellipsoidShape -> MathUtils.max(
                    ellipsoidShape.getXRadius(),
                    ellipsoidShape.getYRadius(),
                    ellipsoidShape.getZRadius()
            );
            case RiftLibCylinderShape cylinderShape -> {
                double yRadius = cylinderShape.getOriginIsCenter() ? cylinderShape.getHeight() / 2D : cylinderShape.getHeight();
                yield Math.sqrt(cylinderShape.getRadius() * cylinderShape.getRadius() + yRadius * yRadius);
            }
            case RiftLibBoxShape boxShape -> {
                double xRadius = boxShape.getXLength() / 2D;
                double yRadius = boxShape.getOriginIsCenter() ? boxShape.getYLength() / 2D : boxShape.getYLength();
                double zRadius = boxShape.getZLength() / 2D;
                yield Math.sqrt(xRadius * xRadius + yRadius * yRadius + zRadius * zRadius);
            }
            default -> Math.max(segment.getImpactCurrentRadius(), segment.getImpactMaxRadius());
        };
    }

    @NotNull
    protected abstract RiftLibThreeDimShape createImpactShape(@NotNull RiftLibRaySegment segment, @NotNull Vec3d origin, @NotNull RiftLibRayWidth width, double depth);
}
