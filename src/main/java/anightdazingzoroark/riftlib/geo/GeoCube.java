package anightdazingzoroark.riftlib.geo;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawFaceUVUser;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;
import net.minecraft.util.EnumFacing;
import anightdazingzoroark.riftlib.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public class GeoCube {
	private final GeoQuad[] quads; //length of 6 as geocubes represent rectangular prisms/cubes
    @NotNull
    private final Vector3f size;
    @NotNull
	private final Vector3f pivot;
    @NotNull
	private final Vector3f rotation;
	public double inflate;
	public Boolean mirror;

    public GeoCube(@NotNull RawGeoModel.RawModelCube cube, @NotNull RawGeoModel.RawModelDescription description, double boneInflate, boolean mirror) {
        this.size = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(cube.size));
        this.mirror = cube.mirror;
        this.inflate = cube.inflate == null ? boneInflate : cube.inflate / 16;;

        Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(cube.pivot));
        pivot.x *= -1;
        this.pivot = pivot;

        Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(cube.rotation));
        rotation.setX((float) Math.toRadians(-rotation.getX()));
        rotation.setY((float) Math.toRadians(-rotation.getY()));
        rotation.setZ((float) Math.toRadians(rotation.getZ()));
        this.rotation = rotation;

        GeoVertex[] vertices = this.createVertices(cube);
        this.quads = cube.uv.isBoxUV()
                ? this.createBoxUVQuads(cube, description, vertices)
                : this.createFaceUVQuads(cube, description, vertices, mirror);
    }

    //-----quad creation operations-----
    private GeoVertex[] createVertices(@NotNull RawGeoModel.RawModelCube cube) {
        Vector3d size = VectorUtils.fromArray(cube.size);
        Vector3d origin = VectorUtils.fromArray(cube.origin);

        origin = new Vector3d(-(origin.x + size.x) / 16, origin.y / 16, origin.z / 16);
        size.scale(0.0625f);

        double x1 = origin.x - this.inflate;
        double y1 = origin.y - this.inflate;
        double z1 = origin.z - this.inflate;

        double x2 = origin.x + size.x + this.inflate;
        double y2 = origin.y + size.y + this.inflate;
        double z2 = origin.z + size.z + this.inflate;

        return new GeoVertex[] {
                new GeoVertex(x1, y1, z1), // P1
                new GeoVertex(x1, y1, z2), // P2
                new GeoVertex(x1, y2, z1), // P3
                new GeoVertex(x1, y2, z2), // P4
                new GeoVertex(x2, y1, z1), // P5
                new GeoVertex(x2, y1, z2), // P6
                new GeoVertex(x2, y2, z1), // P7
                new GeoVertex(x2, y2, z2)  // P8
        };
    }

    private GeoVertex[] vertices(GeoVertex[] v, int a, int b, int c, int d) {
        return new GeoVertex[] {v[a], v[b], v[c], v[d]};
    }

    //---face uv quads---
    private GeoQuad[] createFaceUVQuads(@NotNull RawGeoModel.RawModelCube cube, @NotNull RawGeoModel.RawModelDescription description, GeoVertex[] v, boolean boneMirror) {
        RawFaceUVUser faces = cube.uv.faceUV;

        float textureWidth = description.texture_width;
        float textureHeight = description.texture_height;

        if (Boolean.TRUE.equals(this.mirror) || boneMirror) {
            return new GeoQuad[] {
                    this.face(faces.westUV,  this.vertices(v, 6, 7, 5, 4), textureWidth, textureHeight, EnumFacing.WEST),
                    this.face(faces.eastUV,  this.vertices(v, 3, 2, 0, 1), textureWidth, textureHeight, EnumFacing.EAST),
                    this.face(faces.northUV, this.vertices(v, 2, 6, 4, 0), textureWidth, textureHeight, EnumFacing.NORTH),
                    this.face(faces.southUV, this.vertices(v, 7, 3, 1, 5), textureWidth, textureHeight, EnumFacing.SOUTH),
                    this.face(faces.upUV,    this.vertices(v, 0, 4, 5, 1), textureWidth, textureHeight, EnumFacing.UP),
                    this.face(faces.downUV,  this.vertices(v, 3, 7, 6, 2), textureWidth, textureHeight, EnumFacing.DOWN)
            };
        }
        else {
            return new GeoQuad[] {
                    this.face(faces.westUV,  this.vertices(v, 3, 2, 0, 1), textureWidth, textureHeight, EnumFacing.WEST),
                    this.face(faces.eastUV,  this.vertices(v, 6, 7, 5, 4), textureWidth, textureHeight, EnumFacing.EAST),
                    this.face(faces.northUV, this.vertices(v, 2, 6, 4, 0), textureWidth, textureHeight, EnumFacing.NORTH),
                    this.face(faces.southUV, this.vertices(v, 7, 3, 1, 5), textureWidth, textureHeight, EnumFacing.SOUTH),
                    this.face(faces.upUV,    this.vertices(v, 3, 7, 6, 2), textureWidth, textureHeight, EnumFacing.UP),
                    this.face(faces.downUV,  this.vertices(v, 0, 4, 5, 1), textureWidth, textureHeight, EnumFacing.DOWN)
            };
        }
    }

    @NotNull
    private GeoQuad face(@NotNull RawFaceUVUser.RawUVFace face, GeoVertex[] vertices, float textureWidth, float textureHeight, EnumFacing facing) {
        return new GeoQuad(vertices, face.uv, face.uv_size, textureWidth, textureHeight, this.mirror, facing);
    }

    //---box uv quads---
    private GeoQuad[] createBoxUVQuads(@NotNull RawGeoModel.RawModelCube cube, @NotNull RawGeoModel.RawModelDescription description, GeoVertex[] v) {
        int[] uv = cube.uv.boxUV;

        Vector3d s = VectorUtils.fromArray(cube.size);
        double x = Math.floor(s.x);
        double y = Math.floor(s.y);
        double z = Math.floor(s.z);

        float textureWidth = description.texture_width;
        float textureHeight = description.texture_height;

        if (Boolean.TRUE.equals(this.mirror)) {
            return new GeoQuad[] {
                    this.box(this.vertices(v, 6, 7, 5, 4), uv[0] + z + x, uv[1] + z, z, y, textureWidth, textureHeight, EnumFacing.WEST),
                    this.box(this.vertices(v, 3, 2, 0, 1), uv[0], uv[1] + z, z, y, textureWidth, textureHeight, EnumFacing.EAST),
                    this.box(this.vertices(v, 2, 6, 4, 0), uv[0] + z, uv[1] + z, x, y, textureWidth, textureHeight, EnumFacing.NORTH),
                    this.box(this.vertices(v, 7, 3, 1, 5), uv[0] + z + x + z, uv[1] + z, x, y, textureWidth, textureHeight, EnumFacing.SOUTH),
                    this.box(this.vertices(v, 3, 7, 6, 2), uv[0] + z, uv[1],     x, z, textureWidth, textureHeight, EnumFacing.UP),
                    this.box(this.vertices(v, 0, 4, 5, 1), uv[0] + z + x, uv[1] + z, x, -z, textureWidth, textureHeight, EnumFacing.DOWN)
            };
        }
        else {
            return new GeoQuad[] {
                    this.box(this.vertices(v, 3, 2, 0, 1), uv[0] + z + x,     uv[1] + z, z, y, textureWidth, textureHeight, EnumFacing.WEST),
                    this.box(this.vertices(v, 6, 7, 5, 4), uv[0],             uv[1] + z, z, y, textureWidth, textureHeight, EnumFacing.EAST),
                    this.box(this.vertices(v, 2, 6, 4, 0), uv[0] + z,         uv[1] + z, x, y, textureWidth, textureHeight, EnumFacing.NORTH),
                    this.box(this.vertices(v, 7, 3, 1, 5), uv[0] + z + x + z, uv[1] + z, x, y, textureWidth, textureHeight, EnumFacing.SOUTH),
                    this.box(this.vertices(v, 3, 7, 6, 2), uv[0] + z,         uv[1],     x, z, textureWidth, textureHeight, EnumFacing.UP),
                    this.box(this.vertices(v, 1, 5, 4, 0), uv[0] + z + x,     uv[1],     x, z, textureWidth, textureHeight, EnumFacing.DOWN)
            };
        }
    }

    @NotNull
    private GeoQuad box(GeoVertex[] vertices, double u, double v, double width, double height, float textureWidth, float textureHeight, EnumFacing facing) {
        return new GeoQuad(
                vertices,
                new int[]{(int) u, (int) v},
                new int[]{(int) width, (int) height},
                textureWidth, textureHeight, this.mirror, facing
        );
    }

    //-----getters-----
    public GeoQuad[] getGeoQuads() {
        return this.quads.clone();
    }

    @NotNull
    public Vector3f getPivot() {
        return this.pivot;
    }

    @NotNull
    public Vector3f getRotation() {
        return this.rotation;
    }

    @NotNull
    public Vector3f getSize() {
        return this.size;
    }
}
