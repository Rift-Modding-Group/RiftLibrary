package anightdazingzoroark.riftlib.jsonParsing.builder;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeometryTree;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawModelBoneGroup;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoCube;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.util.VectorUtils;

public class GeoBuilder {
	public static GeoModel constructGeoModel(RawGeometryTree geometryTree) {
		GeoModel model = new GeoModel();
		model.description = geometryTree.description;
		for (RawModelBoneGroup rawBone : geometryTree.topLevelBones.values()) {
			model.topLevelBones.add(constructBone(rawBone, geometryTree.description, null));
		}
		return model;
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
		if (rawBone.locators != null && !rawBone.locators.isEmpty()) {
			for (Map.Entry<String, double[]> entry: rawBone.locators.entrySet()) {
				geoBone.childLocators.add(new GeoLocator(
						geoBone,
						entry.getKey(),
						(float) entry.getValue()[0] / 16f,
						(float) entry.getValue()[1] / 16f,
						(float) entry.getValue()[2] / 16f
				));
			}
		}

		//create bones
		for (RawModelBoneGroup child : bone.children.values()) {
			geoBone.childBones.add(constructBone(child, description, geoBone));
		}

		return geoBone;
	}
}
