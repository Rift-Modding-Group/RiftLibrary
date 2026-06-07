package anightdazingzoroark.riftlib.shape.twoDimShape;

import anightdazingzoroark.riftlib.shape.RiftLibShape;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Two-dimensional shapes by default are assumed to be lying on the ground
 * at rotations 0, 0, 0
 * */
public abstract class RiftLibTwoDimShape extends RiftLibShape {
    public RiftLibTwoDimShape(@NotNull Vec3d shapeOrigin) {
        super(shapeOrigin);
    }

    /**
     * Compute area of 2d shape
     * */
    public abstract double getArea();

    /**
     * Compute perimeter of 2d shape
     * */
    public abstract double getPerimeter();
}
