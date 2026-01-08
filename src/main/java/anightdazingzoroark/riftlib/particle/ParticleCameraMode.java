package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import org.apache.logging.log4j.Level;

public enum ParticleCameraMode {
    ROTATE_XYZ,
    ROTATE_Y,
    LOOKAT_XYZ,
    LOOKAT_Y,
    LOOKAT_DIRECTION,
    //everything else below isnt supported... yet
    DIRECTION_X,
    DIRECTION_Y,
    DIRECTION_Z,
    EMITTER_TRANSFORM_XY,
    EMITTER_TRANSFORM_XZ,
    EMITTER_TRANSFORM_YZ;

    public static ParticleCameraMode getCameraModeFromString(String value) {
        switch (value) {
            case "rotate_xyz":
                return ROTATE_XYZ;
            case "rotate_y":
                return ROTATE_Y;
            case "lookat_xyz":
                return LOOKAT_XYZ;
            case "lookat_y":
                return LOOKAT_Y;
            case "direction_x":
            case "direction_y":
            case "direction_z":
            case "emitter_transform_xy":
            case "emitter_transform_xz":
            case "emitter_transform_yz":
                RiftLib.LOGGER.warn("Unsupported particle camera mode, defaulting to rotate_xyz");
                return ROTATE_XYZ;
            default:
                throw new InvalidValueException("Invalid particle camera mode");
        }
    }
}
