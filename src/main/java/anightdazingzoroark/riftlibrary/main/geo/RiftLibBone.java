package anightdazingzoroark.riftlibrary.main.geo;

import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawFaceUVUser;
import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawModel;
import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawUVUnion;
import org.lwjglx.util.vector.Vector3f;

public class RiftLibBone {
    public GeoQuad[] quads = new GeoQuad[6];
    public Vector3f pivot;
    public Vector3f rotation;
    public Vector3f size = new Vector3f();
    public double inflate;
    public Boolean mirror;

    public RiftLibBone(RawModel.RawModelCube cube, RawModel.RawModelDescription description, Double boneInflate, Boolean mirror) {
        if (cube.size.length >= 3) this.size.set((float) cube.size[0], (float) cube.size[1], (float) cube.size[2]);

        RawUVUnion uvUnion = cube.uv;
        RawFaceUVUser faces = uvUnion.faceUV;
        boolean isBoxUV = uvUnion.isBoxUV();
        this.mirror = cube.mirror;
        this.inflate = cube.inflate == null ? (boneInflate == null ? 0 : boneInflate) : cube.inflate / 16;;

        float textureHeight = description.texture_height;
        float textureWidth = description.texture_width;

        Vector3f size = VectorUtils.fromArray(cube.size);
        Vector3f origin = VectorUtils.fromArray(cube.origin);
        origin = new Vector3f(-(origin.x + size.x) / 16, origin.y / 16, origin.z / 16);

        size.x *= 0.0625f;
        size.y *= 0.0625f;
        size.z *= 0.0625f;

        Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(cube.rotation));
        rotation.x *= -1;
        rotation.y *= -1;

        rotation.setX((float) Math.toRadians(rotation.getX()));
        rotation.setY((float) Math.toRadians(rotation.getY()));
        rotation.setZ((float) Math.toRadians(rotation.getZ()));

        Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(cube.pivot));
        pivot.x *= -1;

        this.pivot = pivot;
        this.rotation = rotation;


        RiftLibVertex P1 = new RiftLibVertex(origin.x - this.inflate, origin.y - this.inflate, origin.z - this.inflate);
        RiftLibVertex P2 = new RiftLibVertex(origin.x - this.inflate, origin.y - this.inflate,
                origin.z + size.z + this.inflate);
        RiftLibVertex P3 = new RiftLibVertex(origin.x - this.inflate, origin.y + size.y + this.inflate,
                origin.z - this.inflate);
        RiftLibVertex P4 = new RiftLibVertex(origin.x - this.inflate, origin.y + size.y + this.inflate,
                origin.z + size.z + this.inflate);
        RiftLibVertex P5 = new RiftLibVertex(origin.x + size.x + this.inflate, origin.y - this.inflate,
                origin.z - this.inflate);
        RiftLibVertex P6 = new RiftLibVertex(origin.x + size.x + this.inflate, origin.y - this.inflate,
                origin.z + size.z + this.inflate);
        RiftLibVertex P7 = new RiftLibVertex(origin.x + size.x + this.inflate, origin.y + size.y + this.inflate,
                origin.z - this.inflate);
        RiftLibVertex P8 = new RiftLibVertex(origin.x + size.x + this.inflate, origin.y + size.y + this.inflate,
                origin.z + size.z + this.inflate);

        GeoQuad quadWest;
        GeoQuad quadEast;
        GeoQuad quadNorth;
        GeoQuad quadSouth;
        GeoQuad quadUp;
        GeoQuad quadDown;

        if (!isBoxUV) {
            RawFaceUVUser.RawUVFace west = faces.westUV;
            RawFaceUVUser.RawUVFace east = faces.eastUV;
            RawFaceUVUser.RawUVFace north = faces.northUV;
            RawFaceUVUser.RawUVFace south = faces.southUV;
            RawFaceUVUser.RawUVFace up = faces.upUV;
            RawFaceUVUser.RawUVFace down = faces.downUV;
            // Pass in vertices starting from the top right corner, then going
            // counter-clockwise
            quadWest = west == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P4, P3, P1, P2 },
                    west.uv, west.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.WEST
            );
            quadEast = east == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P7, P8, P6, P5 },
                    east.uv, east.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.EAST
            );
            quadNorth = north == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P3, P7, P5, P1 },
                    north.uv, north.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.NORTH
            );
            quadSouth = south == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P8, P4, P2, P6 },
                    south.uv, south.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.SOUTH
            );
            quadUp = up == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P4, P8, P7, P3 },
                    up.uv, up.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.UP
            );
            quadDown = down == null ? null
                    : new GeoQuad(
                    new RiftLibVertex[] { P1, P5, P6, P2 },
                    down.uv, down.uv_size, textureWidth,
                    textureHeight, cube.mirror, EnumFacing.DOWN
            );

            if (cube.mirror || mirror) {
                quadWest = west == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P7, P8, P6, P5 },
                        west.uv, west.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.WEST
                );
                quadEast = east == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P4, P3, P1, P2 },
                        east.uv, east.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.EAST
                );
                quadNorth = north == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P3, P7, P5, P1 },
                        north.uv, north.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.NORTH
                );
                quadSouth = south == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P8, P4, P2, P6 },
                        south.uv, south.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.SOUTH
                );
                quadUp = up == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P1, P5, P6, P2 },
                        up.uv, up.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.UP
                );
                quadDown = down == null ? null
                        : new GeoQuad(
                        new RiftLibVertex[] { P4, P8, P7, P3 },
                        down.uv, down.uv_size, textureWidth,
                        textureHeight, cube.mirror, EnumFacing.DOWN
                );
            }
        }
        else {
            int[] UV = cube.uv.boxUV;
            Vector3d UVSize = VectorUtils.fromArray(cube.size);
            UVSize = new Vector3d(Math.floor(UVSize.x), Math.floor(UVSize.y), Math.floor(UVSize.z));

            quadWest = new GeoQuad(
                    new RiftLibVertex[] { P4, P3, P1, P2 },
                    new int[] { (int) (UV[0] + UVSize.z + UVSize.x), (int) (UV[1] + UVSize.z) },
                    new int[] { (int) UVSize.z, (int) UVSize.y },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.WEST
            );
            quadEast = new GeoQuad(
                    new RiftLibVertex[] { P7, P8, P6, P5 },
                    new int[] { (int) UV[0], (int) (UV[1] + UVSize.z) },
                    new int[] { (int) UVSize.z, (int) UVSize.y },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.EAST
            );
            quadNorth = new GeoQuad(
                    new RiftLibVertex[] { P3, P7, P5, P1 },
                    new int[] { (int) (UV[0] + UVSize.z), (int) (UV[1] + UVSize.z) },
                    new int[] { (int) UVSize.x, (int) UVSize.y },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.NORTH
            );
            quadSouth = new GeoQuad(
                    new RiftLibVertex[] { P8, P4, P2, P6 },
                    new int[] { (int) (UV[0] + UVSize.z + UVSize.x + UVSize.z), (int) (UV[1] + UVSize.z) },
                    new int[] { (int) UVSize.x, (int) UVSize.y },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.SOUTH
            );
            quadUp = new GeoQuad(
                    new RiftLibVertex[] { P4, P8, P7, P3 },
                    new int[] { (int) (UV[0] + UVSize.z), (int) UV[1] },
                    new int[] { (int) UVSize.x, (int) UVSize.z },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.UP
            );
            quadDown = new GeoQuad(
                    new RiftLibVertex[] { P2, P6, P5, P1 },
                    new int[] { (int) (UV[0] + UVSize.z + UVSize.x), (int) UV[1] },
                    new int[] { (int) UVSize.x, (int) UVSize.z },
                    textureWidth, textureHeight, cube.mirror, EnumFacing.DOWN
            );

            if (cube.mirror == Boolean.TRUE) {
                quadWest = new GeoQuad(
                        new RiftLibVertex[] { P7, P8, P6, P5 },
                        new int[] { (int) (UV[0] + UVSize.z + UVSize.x), (int) (UV[1] + UVSize.z) },
                        new int[] { (int) UVSize.z, (int) UVSize.y },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.WEST
                );
                quadEast = new GeoQuad(
                        new RiftLibVertex[] { P4, P3, P1, P2 },
                        new int[] { (int) UV[0], (int) (UV[1] + UVSize.z) },
                        new int[] { (int) UVSize.z, (int) UVSize.y },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.EAST
                );
                quadNorth = new GeoQuad(
                        new RiftLibVertex[] { P3, P7, P5, P1 },
                        new int[] { (int) (UV[0] + UVSize.z), (int) (UV[1] + UVSize.z) },
                        new int[] { (int) UVSize.x, (int) UVSize.y },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.NORTH
                );
                quadSouth = new GeoQuad(
                        new RiftLibVertex[] { P8, P4, P2, P6 },
                        new int[] { (int) (UV[0] + UVSize.z + UVSize.x + UVSize.z), (int) (UV[1] + UVSize.z) },
                        new int[] { (int) UVSize.x, (int) UVSize.y },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.SOUTH
                );
                quadUp = new GeoQuad(
                        new RiftLibVertex[] { P4, P8, P7, P3 },
                        new int[] { (int) (UV[0] + UVSize.z), (int) UV[1] },
                        new int[] { (int) UVSize.x, (int) UVSize.z },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.UP
                );
                quadDown = new GeoQuad(
                        new RiftLibVertex[] { P1, P5, P6, P2 },
                        new int[] { (int) (UV[0] + UVSize.z + UVSize.x), (int) (UV[1] + UVSize.z) },
                        new int[] { (int) UVSize.x, (int) (-UVSize.z) },
                        textureWidth, textureHeight, cube.mirror, EnumFacing.DOWN
                );
            }
        }

        this.quads[0] = quadWest;
        this.quads[1] = quadEast;
        this.quads[2] = quadNorth;
        this.quads[3] = quadSouth;
        this.quads[4] = quadUp;
        this.quads[5] = quadDown;
    }
}
