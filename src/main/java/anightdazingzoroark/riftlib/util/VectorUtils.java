package anightdazingzoroark.riftlib.util;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.lwjgl.util.vector.Quaternion;

public class VectorUtils {
	public static Vector3d fromArray(double[] array) {
		Validate.validIndex(ArrayUtils.toObject(array), 2);
		return new Vector3d(array[0], array[1], array[2]);
	}

	public static Vector3f fromArray(float[] array) {
		Validate.validIndex(ArrayUtils.toObject(array), 2);
		return new Vector3f(array[0], array[1], array[2]);
	}

	public static Vector3f convertDoubleToFloat(Vector3d vector) {
		return new Vector3f((float) vector.x, (float) vector.y, (float) vector.z);
	}

	public static Vector3d convertFloatToDouble(Vector3f vector) {
		return new Vector3d(vector.getX(), vector.getY(), vector.getZ());
	}

    public static Quaternion convertToQuaternion(Vec3d vec3d) {
        return new Quaternion((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 0);
    }

    public static Vec3d convertQuaternionToVec(Quaternion quaternion) {
        return new Vec3d(quaternion.x, quaternion.y, quaternion.z);
    }

    //rotate a Vec3d with an already existing quaternion
    public static Vec3d rotateVectorWithQuaternion(Vec3d vec3d, Quaternion quaternion) {
        //conjugate quaternion
        Quaternion conjQuaternion = new Quaternion();
        Quaternion.negate(quaternion, conjQuaternion);

        //turn vector into quaternion
        Quaternion vecQuaternion = new Quaternion((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 0);

        //now multiply
        Quaternion toReturn = new Quaternion();
        Quaternion.mul(quaternion, vecQuaternion, toReturn);
        Quaternion.mul(toReturn, conjQuaternion, toReturn);

        return new Vec3d(toReturn.x, toReturn.y, toReturn.z);
    }

    //this uses yxz
    public static Vec3d rotateVectorYXZ(Vec3d vec3d, double xRotation, double yRotation, double zRotation) {
        //create quaternion
        double cosY = Math.cos(yRotation / 2);
        double sinY = Math.sin(yRotation / 2);
        double cosX = Math.cos(xRotation / 2);
        double sinX = Math.sin(xRotation / 2);
        double cosZ = Math.cos(zRotation / 2);
        double sinZ = Math.sin(zRotation / 2);
        Quaternion rotationQuaternion = new Quaternion(
                (float) (sinX * cosY * cosZ + cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ - sinX * cosY * sinZ),
                (float) (sinX * sinY * cosZ + cosX * cosY * sinZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );

        //conjugate
        Quaternion conjRotationQuaternion = new Quaternion();
        Quaternion.negate(rotationQuaternion, conjRotationQuaternion);

        //turn vector into quaternion
        Quaternion vecQuaternion = new Quaternion((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 0);

        //now multiply
        Quaternion toReturn = new Quaternion();
        Quaternion.mul(rotationQuaternion, vecQuaternion, toReturn);
        Quaternion.mul(toReturn, conjRotationQuaternion, toReturn);

        return new Vec3d(toReturn.x, toReturn.y, toReturn.z);
    }

    //this uses yzx
    public static Vec3d rotateVectorYZX(Vec3d vec3d, double xRotation, double yRotation, double zRotation) {
        //create quaternion
        double cosY = Math.cos(yRotation / 2);
        double sinY = Math.sin(yRotation / 2);
        double cosZ = Math.cos(zRotation / 2);
        double sinZ = Math.sin(zRotation / 2);
        double cosX = Math.cos(xRotation / 2);
        double sinX = Math.sin(xRotation / 2);
        Quaternion rotationQuaternion = new Quaternion(
                (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ - sinX * sinY * sinZ)
        );

        //conjugate
        Quaternion conjRotationQuaternion = new Quaternion();
        Quaternion.negate(rotationQuaternion, conjRotationQuaternion);

        //turn vector into quaternion
        Quaternion vecQuaternion = new Quaternion((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 0);

        //now multiply
        Quaternion toReturn = new Quaternion();
        Quaternion.mul(rotationQuaternion, vecQuaternion, toReturn);
        Quaternion.mul(toReturn, conjRotationQuaternion, toReturn);

        return new Vec3d(toReturn.x, toReturn.y, toReturn.z);
    }

    //this uses xyz
    //note to self: this is equal to zyx
    public static Vec3d rotateVectorXYZ(Vec3d vec3d, double xRotation, double yRotation, double zRotation) {
        //create quaternion
        double cosX = Math.cos(xRotation / 2);
        double sinX = Math.sin(xRotation / 2);
        double cosY = Math.cos(yRotation / 2);
        double sinY = Math.sin(yRotation / 2);
        double cosZ = Math.cos(zRotation / 2);
        double sinZ = Math.sin(zRotation / 2);
        Quaternion rotationQuaternion = new Quaternion(
                (float) (sinX * cosY * cosZ - cosX * sinY * sinZ),
                (float) (cosX * sinY * cosZ + sinX * cosY * sinZ),
                (float) (cosX * cosY * sinZ - sinX * sinY * cosZ),
                (float) (cosX * cosY * cosZ + sinX * sinY * sinZ)
        );

        //conjugate
        Quaternion conjRotationQuaternion = new Quaternion();
        Quaternion.negate(rotationQuaternion, conjRotationQuaternion);

        //turn vector into quaternion
        Quaternion vecQuaternion = new Quaternion((float) vec3d.x, (float) vec3d.y, (float) vec3d.z, 0);

        //now multiply
        Quaternion toReturn = new Quaternion();
        Quaternion.mul(rotationQuaternion, vecQuaternion, toReturn);
        Quaternion.mul(toReturn, conjRotationQuaternion, toReturn);

        return new Vec3d(toReturn.x, toReturn.y, toReturn.z);
    }
}
