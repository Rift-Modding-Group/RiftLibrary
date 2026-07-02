package anightdazingzoroark.riftlib.geo;

import anightdazingzoroark.riftlib.core.ExpressionValue;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.vecmath.Vector3f;

/**
 * A raw bounding box from le model
 * */
public class GeoBoundingBox {
    @NotNull
    public final GeoBone parent;
    @NotNull
    public final String name;

    public boolean canCollide;
    public String[] tags = {};

    @NotNull
    private final Vector3f position = new Vector3f();
    private float[] size = new float[]{1f, 1f};

    public GeoBoundingBox(@NonNull GeoBone parent, @NotNull String name) {
        this.parent = parent;
        this.name = name;
    }

    @NotNull
    public Vector3f getPosition() {
        return this.position;
    }

    public float[] getSize() {
        return this.size;
    }

    public void setSize(float width, float height) {
        this.size = new float[]{width, height};
    }
}
