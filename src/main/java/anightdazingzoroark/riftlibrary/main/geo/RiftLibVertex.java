package anightdazingzoroark.riftlibrary.main.geo;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.lwjglx.util.vector.Vector3f;

public class RiftLibVertex {
    public final Vector3f position;
    public float textureU;
    public float textureV;

    public RiftLibVertex(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    public RiftLibVertex(double x, double y, double z) {
        this.position = new Vector3f((float) x, (float) y, (float) z);
    }

    public RiftLibVertex setTextureUV(float texU, float texV) {
        return new RiftLibVertex(this.position, texU, texV);
    }

    public RiftLibVertex setTextureUV(double[] array) {
        Validate.validIndex(ArrayUtils.toObject(array), 1);
        return new RiftLibVertex(this.position, (float) array[0], (float) array[1]);
    }

    public RiftLibVertex(Vector3f posIn, float texU, float texV) {
        this.position = posIn;
        this.textureU = texU;
        this.textureV = texV;
    }
}