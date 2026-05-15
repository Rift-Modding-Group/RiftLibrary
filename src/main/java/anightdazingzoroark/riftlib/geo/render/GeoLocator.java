package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.util.math.Vec3d;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;

/**
 * GeoLocator is collected from raw model info and is just raw locator information.
 * */
public class GeoLocator {
    public final GeoBone parent;
    public final String name;
    private float positionX;
    private float positionY;
    private float positionZ;
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float pivotX;
    private float pivotY;
    private float pivotZ;

    public GeoLocator(GeoBone parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public float getPositionX() {
        return this.positionX;
    }

    public float getPositionY() {
        return this.positionY;
    }

    public float getPositionZ() {
        return this.positionZ;
    }

    public void setPositionX(float value) {
        this.positionX = value;
    }

    public void setPositionY(float value) {
        this.positionY = value;
    }

    public void setPositionZ(float value) {
        this.positionZ = value;
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }

    public void setRotationX(float value) {
        this.rotationX = value;
    }

    public void setRotationY(float value) {
        this.rotationY = value;
    }

    public void setRotationZ(float value) {
        this.rotationZ = value;
    }

    public float getPivotX() {
        return this.pivotX;
    }

    public float getPivotY() {
        return this.pivotY;
    }

    public float getPivotZ() {
        return this.pivotZ;
    }

    public void setPivotX(float value) {
        this.pivotX = value;
    }

    public void setPivotY(float value) {
        this.pivotY = value;
    }

    public void setPivotZ(float value) {
        this.pivotZ = value;
    }

    public String toString() {
        return "[name="+this.name+", offset=("+this.positionX+", "+this.positionY+", "+this.positionZ+")]";
    }
}
