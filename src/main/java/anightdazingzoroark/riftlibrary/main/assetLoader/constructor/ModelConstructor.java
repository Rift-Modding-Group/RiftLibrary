package anightdazingzoroark.riftlibrary.main.assetLoader.constructor;

import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawGeometryTree;
import anightdazingzoroark.riftlibrary.main.assetLoader.rawData.model.RawModelBoneGroup;
import anightdazingzoroark.riftlibrary.main.geo.RiftLibModel;

import javax.vecmath.Vector3f;

public class ModelConstructor {
    public static RiftLibModel constructGeoModel(RawGeometryTree geometryTree) {
        RiftLibModel toReturn = new RiftLibModel();
        toReturn.description = geometryTree.description;
        for (RawModelBoneGroup rawBone : geometryTree.topLevelBones.values()) {
            toReturn.topLevelBones.add(constructBone(rawBone, geometryTree.description, null));
        }
        return toReturn;
    }

    public static GeoBone constructBone(RawModelBoneGroup bone, RawGeoModel.RawModelDescription description, GeoBone parent) {
        GeoBone geoBone = new GeoBone();

        RawGeoModel.RawModelBone rawBone = bone.selfBone;
        Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.rotation));
        Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.pivot));
        rotation.x *= -1;
        rotation.y *= -1;

        geoBone.mirror = rawBone.mirror;
        //geoBone.dontRender = rawBone.getNeverRender();
        //geoBone.reset = rawBone.getReset();
        geoBone.inflate = rawBone.inflate;
        geoBone.parent = parent;
        geoBone.setModelRendererName(rawBone.name);

        geoBone.setRotationX((float) Math.toRadians(rotation.getX()));
        geoBone.setRotationY((float) Math.toRadians(rotation.getY()));
        geoBone.setRotationZ((float) Math.toRadians(rotation.getZ()));

        geoBone.rotationPointX = -pivot.getX();
        geoBone.rotationPointY = pivot.getY();
        geoBone.rotationPointZ = pivot.getZ();

        //add cubes
        if (rawBone.cubes != null && !rawBone.cubes.isEmpty()) {
            for (RawGeoModel.RawModelCube cube : rawBone.cubes) {
                geoBone.childCubes.add(new GeoCube(
                        cube,
                        description,
                        geoBone.inflate == null ? null : geoBone.inflate / 16,
                        geoBone.mirror
                ));
            }
        }

        //add locators
        if (rawBone.locators != null && !rawBone.locators.list.isEmpty()) {
            for (RawModelLocatorList.RawModelLocator rawLocator : rawBone.locators.list) {
                GeoLocator toAdd = new GeoLocator(geoBone, rawLocator.name);

                toAdd.setPositionX((float) rawLocator.offset[0]);
                toAdd.setPositionY((float) rawLocator.offset[1]);
                toAdd.setPositionZ((float) rawLocator.offset[2]);

                toAdd.setRotationX((float) Math.toRadians(rawLocator.rotation[0]));
                toAdd.setRotationY((float) Math.toRadians(rawLocator.rotation[1]));
                toAdd.setRotationZ((float) Math.toRadians(rawLocator.rotation[2]));

                toAdd.setPivotX((float) rawLocator.offset[0]);
                toAdd.setPivotY((float) rawLocator.offset[1]);
                toAdd.setPivotZ((float) rawLocator.offset[2]);

                geoBone.childLocators.add(toAdd);
            }
        }

        //create bones
        for (RawModelBoneGroup child : bone.children.values()) {
            geoBone.childBones.add(constructBone(child, description, geoBone));
        }

        return geoBone;
    }
}
