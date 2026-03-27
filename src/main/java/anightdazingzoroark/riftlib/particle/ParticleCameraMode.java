package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.util.QuadFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.function.IOQuadFunction;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public enum ParticleCameraMode {
    ROTATE_XYZ((scaleX, scaleY, partialTicks,rotation) -> {
        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();

        //compute 4 corners (in camera space)
        Vec3d pointOne = new Vec3d(
                -rotationX * scaleX - rotationYZ * scaleX,
                -rotationXZ * scaleY,
                -rotationZ * scaleX - rotationXY * scaleX
        );
        Vec3d pointTwo = new Vec3d(
                -rotationX * scaleX + rotationYZ * scaleX,
                rotationXZ * scaleY,
                -rotationZ * scaleX + rotationXY * scaleX
        );
        Vec3d pointThree = new Vec3d(
                rotationX * scaleX + rotationYZ * scaleX,
                rotationXZ * scaleY,
                rotationZ * scaleX + rotationXY * scaleX
        );
        Vec3d pointFour = new Vec3d(
                rotationX * scaleX - rotationYZ * scaleX,
                -rotationXZ * scaleY,
                rotationZ * scaleX - rotationXY * scaleX
        );

        //edit points further based on rotation
        if (rotation != 0) {
            Vec3d axis = new Vec3d(0, 0, 1);

            pointOne = rotateAroundAxis(pointOne, axis, rotation);
            pointTwo = rotateAroundAxis(pointTwo, axis, rotation);
            pointThree = rotateAroundAxis(pointThree, axis, rotation);
            pointFour = rotateAroundAxis(pointFour, axis, rotation);
        }

        Vec3d center = pointOne.add(pointTwo).add(pointThree).add(pointFour);
        pointOne = pointOne.subtract(center);
        pointTwo = pointTwo.subtract(center);
        pointThree = pointThree.subtract(center);
        pointFour = pointFour.subtract(center);

        return Arrays.asList(pointOne, pointTwo, pointThree, pointFour);
    }),
    ROTATE_Y((scaleX, scaleY, partialTicks,rotation) -> {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO);

        float yaw = (float) Math.toRadians(camera.prevRotationYaw + (camera.rotationYaw - camera.prevRotationYaw) * partialTicks);

        float cos = MathHelper.cos(yaw);
        float sin = MathHelper.sin(yaw);

        Vec3d horizontalVec = new Vec3d(-cos, 0, -sin);
        Vec3d verticalVec = new Vec3d(0,  1,  0);

        //compute 4 corners (in camera space)
        Vec3d pointOne = new Vec3d(
                -horizontalVec.x * scaleX + verticalVec.x * scaleY,
                -horizontalVec.y * scaleX + verticalVec.y * scaleY,
                -horizontalVec.z * scaleX + verticalVec.z * scaleY
        );
        Vec3d pointTwo = new Vec3d(
                -horizontalVec.x * scaleX - verticalVec.x * scaleY,
                -horizontalVec.y * scaleX - verticalVec.y * scaleY,
                -horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointThree = new Vec3d(
                horizontalVec.x * scaleX - verticalVec.x * scaleY,
                horizontalVec.y * scaleX - verticalVec.y * scaleY,
                horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointFour = new Vec3d(
                horizontalVec.x * scaleX + verticalVec.x * scaleY,
                horizontalVec.y * scaleX + verticalVec.y * scaleY,
                horizontalVec.z * scaleX + verticalVec.z * scaleY
        );

        //edit points further based on rotation
        if (rotation != 0) {
            Vec3d axis = horizontalVec.crossProduct(verticalVec);
            double lenSq = axis.lengthSquared();
            if (lenSq > 1e-8) axis = axis.normalize();
            else axis = new Vec3d(0, 0, 1);

            pointOne = rotateAroundAxis(pointOne, axis, rotation);
            pointTwo = rotateAroundAxis(pointTwo, axis, rotation);
            pointThree = rotateAroundAxis(pointThree, axis, rotation);
            pointFour = rotateAroundAxis(pointFour, axis, rotation);
        }

        Vec3d center = pointOne.add(pointTwo).add(pointThree).add(pointFour);
        pointOne = pointOne.subtract(center);
        pointTwo = pointTwo.subtract(center);
        pointThree = pointThree.subtract(center);
        pointFour = pointFour.subtract(center);

        return Arrays.asList(pointOne, pointTwo, pointThree, pointFour);
    }),
    LOOKAT_XYZ((scaleX, scaleY, partialTicks,rotation) -> {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO);
        Vec3d look = camera.getLook(partialTicks);
        Vec3d upWorld = new Vec3d(0, 1, 0);

        Vec3d horizontalVec = look.crossProduct(upWorld);

        //mainly to make the particle be visible from above
        double rightLenSq = horizontalVec.lengthSquared();
        if (rightLenSq < 1e-6) {
            upWorld = new Vec3d(0, 0, 1);
            horizontalVec = look.crossProduct(upWorld);
            rightLenSq = horizontalVec.lengthSquared();
            if (rightLenSq < 1e-6) {
                horizontalVec = new Vec3d(1, 0, 0);
            }
        }

        horizontalVec = horizontalVec.normalize();
        Vec3d verticalVec = horizontalVec.crossProduct(look).normalize();

        //compute 4 corners (in camera space)
        Vec3d pointOne = new Vec3d(
                -horizontalVec.x * scaleX + verticalVec.x * scaleY,
                -horizontalVec.y * scaleX + verticalVec.y * scaleY,
                -horizontalVec.z * scaleX + verticalVec.z * scaleY
        );
        Vec3d pointTwo = new Vec3d(
                -horizontalVec.x * scaleX - verticalVec.x * scaleY,
                -horizontalVec.y * scaleX - verticalVec.y * scaleY,
                -horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointThree = new Vec3d(
                horizontalVec.x * scaleX - verticalVec.x * scaleY,
                horizontalVec.y * scaleX - verticalVec.y * scaleY,
                horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointFour = new Vec3d(
                horizontalVec.x * scaleX + verticalVec.x * scaleY,
                horizontalVec.y * scaleX + verticalVec.y * scaleY,
                horizontalVec.z * scaleX + verticalVec.z * scaleY
        );

        //edit points further based on rotation
        if (rotation != 0) {
            Vec3d axis = horizontalVec.crossProduct(verticalVec);
            double lenSq = axis.lengthSquared();
            if (lenSq > 1e-8) axis = axis.normalize();
            else axis = new Vec3d(0, 0, 1);

            pointOne = rotateAroundAxis(pointOne, axis, rotation);
            pointTwo = rotateAroundAxis(pointTwo, axis, rotation);
            pointThree = rotateAroundAxis(pointThree, axis, rotation);
            pointFour = rotateAroundAxis(pointFour, axis, rotation);
        }

        Vec3d center = pointOne.add(pointTwo).add(pointThree).add(pointFour);
        pointOne = pointOne.subtract(center);
        pointTwo = pointTwo.subtract(center);
        pointThree = pointThree.subtract(center);
        pointFour = pointFour.subtract(center);

        return Arrays.asList(pointOne, pointTwo, pointThree, pointFour);
    }),
    LOOKAT_Y((scaleX, scaleY, partialTicks,rotation) -> {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) return Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO);
        Vec3d look = camera.getLook(partialTicks);
        Vec3d upWorld = new Vec3d(0, 1, 0);

        Vec3d horizontalVec = look.crossProduct(upWorld).normalize();
        Vec3d verticalVec = new Vec3d(0, 1, 0);

        //compute 4 corners (in camera space)
        Vec3d pointOne = new Vec3d(
                -horizontalVec.x * scaleX + verticalVec.x * scaleY,
                -horizontalVec.y * scaleX + verticalVec.y * scaleY,
                -horizontalVec.z * scaleX + verticalVec.z * scaleY
        );
        Vec3d pointTwo = new Vec3d(
                -horizontalVec.x * scaleX - verticalVec.x * scaleY,
                -horizontalVec.y * scaleX - verticalVec.y * scaleY,
                -horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointThree = new Vec3d(
                horizontalVec.x * scaleX - verticalVec.x * scaleY,
                horizontalVec.y * scaleX - verticalVec.y * scaleY,
                horizontalVec.z * scaleX - verticalVec.z * scaleY
        );
        Vec3d pointFour = new Vec3d(
                horizontalVec.x * scaleX + verticalVec.x * scaleY,
                horizontalVec.y * scaleX + verticalVec.y * scaleY,
                horizontalVec.z * scaleX + verticalVec.z * scaleY
        );

        //edit points further based on rotation
        if (rotation != 0) {
            Vec3d axis = horizontalVec.crossProduct(verticalVec);
            double lenSq = axis.lengthSquared();
            if (lenSq > 1e-8) axis = axis.normalize();
            else axis = new Vec3d(0, 0, 1);

            pointOne = rotateAroundAxis(pointOne, axis, rotation);
            pointTwo = rotateAroundAxis(pointTwo, axis, rotation);
            pointThree = rotateAroundAxis(pointThree, axis, rotation);
            pointFour = rotateAroundAxis(pointFour, axis, rotation);
        }

        Vec3d center = pointOne.add(pointTwo).add(pointThree).add(pointFour);
        pointOne = pointOne.subtract(center);
        pointTwo = pointTwo.subtract(center);
        pointThree = pointThree.subtract(center);
        pointFour = pointFour.subtract(center);

        return Arrays.asList(pointOne, pointTwo, pointThree, pointFour);
    }),
    //everything else below isnt supported... yet
    LOOKAT_DIRECTION((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    DIRECTION_X((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    DIRECTION_Y((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    DIRECTION_Z((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    EMITTER_TRANSFORM_XY((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    EMITTER_TRANSFORM_XZ((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO)),
    EMITTER_TRANSFORM_YZ((scaleX, scaleY, partialTicks,rotation) -> Arrays.asList(Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO));
    private final QuadFunction<Float, Float, Float, Double, List<Vec3d>> pointsCreator;

    ParticleCameraMode(QuadFunction<Float, Float, Float, Double, List<Vec3d>> pointsCreator) {
        this.pointsCreator = pointsCreator;
    }

    public List<Vec3d> getPoints(float scaleX, float scaleY, float partialTicks, double rotation) {
        return this.pointsCreator.apply(scaleX, scaleY, partialTicks, rotation);
    }

    public static ParticleCameraMode getCameraModeFromString(String value) {
        return switch (value) {
            case "rotate_xyz" -> ROTATE_XYZ;
            case "rotate_y" -> ROTATE_Y;
            case "lookat_xyz" -> LOOKAT_XYZ;
            case "lookat_y" -> LOOKAT_Y;
            case "direction_x", "direction_y", "direction_z", "emitter_transform_xy", "emitter_transform_xz",
                 "emitter_transform_yz" -> {
                RiftLib.LOGGER.warn("Unsupported particle camera mode, defaulting to rotate_xyz");
                yield ROTATE_XYZ;
            }
            default -> throw new InvalidValueException("Invalid particle camera mode");
        };
    }

    private static Vec3d rotateAroundAxis(Vec3d vec, Vec3d axisUnit, double rotation) {
        //convert current rotation into rads
        double radians = Math.toRadians(rotation);

        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        Vec3d termOne = vec.scale(cos);
        Vec3d termTwo = axisUnit.crossProduct(vec).scale(sin);
        Vec3d termThree = axisUnit.scale(axisUnit.dotProduct(vec) * (1 - cos));
        return termOne.add(termTwo).add(termThree);
    }
}
