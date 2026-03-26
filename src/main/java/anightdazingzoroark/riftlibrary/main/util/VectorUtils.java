package anightdazingzoroark.riftlibrary.main.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.lwjglx.util.vector.Quaternion;
import org.lwjglx.util.vector.Vector3f;

public class VectorUtils {
    public static Vector3f fromArray(float[] array) {
        Validate.validIndex(ArrayUtils.toObject(array), 2);
        return new Vector3f(array[0], array[1], array[2]);
    }

    public static javax.vecmath.Vector3f convertToJavaVec3f(Vector3f vector3f) {
        return new javax.vecmath.Vector3f(vector3f.x, vector3f.y, vector3f.z);
    }

    public static Quaternion convertToQuaternion(Vector3f vector3f) {
        return new Quaternion(vector3f.x, vector3f.y, vector3f.z, 0);
    }
}
