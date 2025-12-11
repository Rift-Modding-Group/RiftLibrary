package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;

public enum ParticleCameraMode {
    ROTATE_XYZ,
    ROTATE_Y,
    LOOKAT_XYZ,
    LOOKAT_Y,
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
            default:
                throw new InvalidValueException("Invalid particle camera mode");
        }
    }
}
