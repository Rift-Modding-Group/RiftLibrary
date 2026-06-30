package anightdazingzoroark.riftlib.jsonParsing.constructor;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import anightdazingzoroark.riftlib.core.ExpressionValue;
import anightdazingzoroark.riftlib.geo.*;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.*;

import anightdazingzoroark.riftlib.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

public class GeoConstructor {
	public static GeoModel constructGeoModel(RawGeometryTree geometryTree) {
		GeoModel model = new GeoModel();
		model.description = geometryTree.description;
		for (RawModelBoneGroup rawBone : geometryTree.topLevelBones.values()) {
			model.topLevelBones.add(constructBone(rawBone, geometryTree.description, null));
		}
		return model;
	}

	public static GeoBone constructBone(RawModelBoneGroup bone, RawGeoModel.RawModelDescription description, @Nullable GeoBone parent) {
		RawGeoModel.RawModelBone rawBone = bone.selfBone;
		GeoBone geoBone = new GeoBone(parent, rawBone.name);

		Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.rotation));
		Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.pivot));
		rotation.x *= -1;
		rotation.y *= -1;

		geoBone.mirror = rawBone.mirror;
		//geoBone.dontRender = rawBone.getNeverRender();
		//geoBone.reset = rawBone.getReset();
		geoBone.inflate = rawBone.inflate;

		geoBone.getRotation().set(
				(float) Math.toRadians(rotation.getX()),
				(float) Math.toRadians(rotation.getY()),
				(float) Math.toRadians(rotation.getZ())
		);

		geoBone.getPivot().set(-pivot.getX(), pivot.getY(), pivot.getZ());

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

				toAdd.getPosition().set(
						(float) -rawLocator.offset[0],
						(float) rawLocator.offset[1],
						(float) rawLocator.offset[2]
				);

				toAdd.getRotation().set(
						(float) Math.toRadians(-rawLocator.rotation[0]),
						(float) Math.toRadians(-rawLocator.rotation[1]),
						(float) Math.toRadians(rawLocator.rotation[2])
				);

				geoBone.childLocators.add(toAdd);
			}
		}

		//add bounding boxes
		if (rawBone.boundingBoxes != null && !rawBone.boundingBoxes.list.isEmpty()) {
			for (RawModelBoundingBoxList.RawBoundingBox rawBoundingBox : rawBone.boundingBoxes.list) {
				GeoBoundingBox toAdd = new GeoBoundingBox(geoBone, rawBoundingBox.name);

				toAdd.getPosition().set(
						(float) -rawBoundingBox.origin[0],
						(float) rawBoundingBox.origin[1],
						(float) rawBoundingBox.origin[2]
				);

				toAdd.setSize((float) rawBoundingBox.size[0], (float) rawBoundingBox.size[1]);

				toAdd.canCollide = rawBoundingBox.collision;
				toAdd.tags = rawBoundingBox.tags;
				if (rawBoundingBox.damageMultiplier != null) toAdd.damageMultiplier = new ExpressionValue(rawBoundingBox.damageMultiplier);

				geoBone.childBoundingBoxes.add(toAdd);
			}
		}

		//create bones
		for (RawModelBoneGroup child : bone.children.values()) {
			geoBone.childBones.add(constructBone(child, description, geoBone));
		}

		return geoBone;
	}
}
