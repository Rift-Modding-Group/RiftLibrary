package anightdazingzoroark.riftlib.newGeo.modelRaw;


import java.util.HashMap;

public class RawModelBoneGroup {
    public HashMap<String, RawModelBoneGroup> children = new HashMap<>();
    public RawGeoModel.RawModelBone selfBone;

    public RawModelBoneGroup(RawGeoModel.RawModelBone bone) {
        this.selfBone = bone;
    }
}