package anightdazingzoroark.riftlib.util;

//helper class thats a mutable variant of aabb
public class MutableAxisAlignedBB {
    private double minX, minY, minZ, maxX, maxY, maxZ;

    public void set(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean intersects(double inMinX, double inMinY, double inMinZ, double inMaxX, double inMaxY, double inMaxZ) {
        return this.maxX > inMinX && this.minX < inMaxX
                && this.maxY > inMinY && this.minY < inMaxY
                && this.maxZ > inMinZ && this.minZ < inMaxZ;
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMinZ() {
        return this.minZ;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getMaxZ() {
        return this.maxZ;
    }
}
