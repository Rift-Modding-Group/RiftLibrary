package anightdazingzoroark.riftlib.geo.raw;


import java.util.HashMap;

public class RawModelBoneGroup {
    public HashMap<String, RawModelBoneGroup> children = new HashMap<>();
    public RawGeoModel.RawModelBone selfBone;

    public RawModelBoneGroup(RawGeoModel.RawModelBone bone) {
        this.selfBone = bone;
    }
}