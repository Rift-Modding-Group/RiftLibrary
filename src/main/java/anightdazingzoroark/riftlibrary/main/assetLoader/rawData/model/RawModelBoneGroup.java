package anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model;

import java.util.HashMap;

public class RawModelBoneGroup {
    public HashMap<String, RawModelBoneGroup> children = new HashMap<>();
    public RawModel.RawModelBone selfBone;

    public RawModelBoneGroup(RawModel.RawModelBone bone) {
        this.selfBone = bone;
    }
}
