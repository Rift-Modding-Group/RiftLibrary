package anightdazingzoroark.riftlibrary.main.geo.basic;

import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RiftLibModel {
    public List<RiftLibBone> topLevelBones = new ArrayList<>();
    public RawModel.RawModelDescription description;

    public Optional<RiftLibBone> getBone(String name) {
        for (RiftLibBone bone : this.topLevelBones) {
            RiftLibBone optionalBone = this.getBoneRecursively(name, bone);
            if (optionalBone != null) {
                return Optional.of(optionalBone);
            }
        }
        return Optional.empty();
    }

    private RiftLibBone getBoneRecursively(String name, RiftLibBone bone) {
        if (bone.name.equals(name)) {
            return bone;
        }
        for (RiftLibBone childBone : bone.childBones) {
            if (childBone.name.equals(name)) {
                return childBone;
            }
            RiftLibBone optionalBone = getBoneRecursively(name, childBone);
            if (optionalBone != null) return optionalBone;
        }
        return null;
    }

    public List<RiftLibBone> getAllBones() {
        List<RiftLibBone> listToReturn = new ArrayList<>();
        for (RiftLibBone bone : this.topLevelBones) {
            this.collectAll(bone, listToReturn);
        }
        return listToReturn;
    }

    private void collectAll(RiftLibBone current, List<RiftLibBone> result) {
        result.add(current);
        for (RiftLibBone child : current.childBones) {
            collectAll(child, result);
        }
    }

    public List<RiftLibLocator> getAllLocators() {
        List<RiftLibLocator> listToReturn = new ArrayList<>();
        for (RiftLibBone bone : this.getAllBones()) {
            for (RiftLibLocator locator : bone.childLocators) {
                if (!listToReturn.contains(locator)) listToReturn.add(locator);
            }
        }
        return listToReturn;
    }
}
