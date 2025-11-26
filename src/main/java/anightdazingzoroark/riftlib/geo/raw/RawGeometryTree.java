package anightdazingzoroark.riftlib.geo.raw;

import anightdazingzoroark.riftlib.RiftLib;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RawGeometryTree {
    public final HashMap<String, RawModelBoneGroup> topLevelBones = new HashMap<>();
    public final RawGeoModel.RawModelDescription description;

    public RawGeometryTree(RawGeoModel model, ResourceLocation location) {
        RawGeoModel.MinecraftGeometry geometry = model.geometry.get(0);

        this.description = geometry.description;

        List<RawGeoModel.RawModelBone> rawBones = new ArrayList<>(geometry.bones);
        int index = rawBones.size() - 1;
        int loopsWithoutChange = 0;
        while (true) {
            loopsWithoutChange++;
            if (loopsWithoutChange > 10000) {
                RiftLib.LOGGER.warn("Some rawBones in " + location.toString() + " do not have existing parents: ");
                RiftLib.LOGGER.warn(rawBones.stream().map(b -> b.name).collect(Collectors.joining(", ")));
                break;
            }

            RawGeoModel.RawModelBone rawBone = rawBones.get(index);
            if (!this.hasParent(rawBone)) {
                this.topLevelBones.put(rawBone.name, new RawModelBoneGroup(rawBone));
                rawBones.remove(rawBone);
                loopsWithoutChange = 0;
            }
            else {
                RawModelBoneGroup groupFromHierarchy = this.getGroupFromHierarchy(rawBone.parent);
                if (groupFromHierarchy != null) {
                    groupFromHierarchy.children.put(rawBone.name, new RawModelBoneGroup(rawBone));
                    rawBones.remove(rawBone);
                    loopsWithoutChange = 0;
                }
            }

            if (index == 0) {
                index = rawBones.size() - 1;
                if (index == -1) break;
            }
            else index--;
        }
    }

    public RawModelBoneGroup getGroupFromHierarchy(String bone) {
        HashMap<String, RawModelBoneGroup> flatList = new HashMap<>();
        for (RawModelBoneGroup group : this.topLevelBones.values()) {
            flatList.put(group.selfBone.name, group);
            this.traverse(flatList, group);
        }
        return flatList.get(bone);
    }

    public void traverse(HashMap<String, RawModelBoneGroup> flatList, RawModelBoneGroup group) {
        for (RawModelBoneGroup child : group.children.values()) {
            flatList.put(child.selfBone.name, child);
            this.traverse(flatList, child);
        }
    }

    public boolean hasParent(RawGeoModel.RawModelBone bone) {
        return bone.parent != null;
    }
}
