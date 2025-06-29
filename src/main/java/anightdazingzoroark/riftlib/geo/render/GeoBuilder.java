package anightdazingzoroark.riftlib.geo.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import anightdazingzoroark.riftlib.geo.raw.pojo.LocatorValue;
import anightdazingzoroark.riftlib.geo.render.built.GeoLocator;
import org.apache.commons.lang3.ArrayUtils;

import anightdazingzoroark.riftlib.geo.raw.pojo.Bone;
import anightdazingzoroark.riftlib.geo.raw.pojo.Cube;
import anightdazingzoroark.riftlib.geo.raw.pojo.ModelProperties;
import anightdazingzoroark.riftlib.geo.raw.tree.RawBoneGroup;
import anightdazingzoroark.riftlib.geo.raw.tree.RawGeometryTree;
import anightdazingzoroark.riftlib.geo.render.built.GeoBone;
import anightdazingzoroark.riftlib.geo.render.built.GeoCube;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;
import anightdazingzoroark.riftlib.util.VectorUtils;

public class GeoBuilder implements IGeoBuilder {
	private static Map<String, IGeoBuilder> moddedGeoBuilders = new HashMap<>();
	private static IGeoBuilder defaultBuilder = new GeoBuilder();

	public static void registerGeoBuilder(String modID, IGeoBuilder builder) {
		moddedGeoBuilders.put(modID, builder);
	}

	public static IGeoBuilder getGeoBuilder(String modID) {
		IGeoBuilder builder = moddedGeoBuilders.get(modID);
		return builder == null ? defaultBuilder : builder;
	}

	@Override
	public GeoModel constructGeoModel(RawGeometryTree geometryTree) {
		GeoModel model = new GeoModel();
		model.properties = geometryTree.properties;
		for (RawBoneGroup rawBone : geometryTree.topLevelBones.values()) {
			model.topLevelBones.add(this.constructBone(rawBone, geometryTree.properties, null));
		}
		return model;
	}

	@Override
	public GeoBone constructBone(RawBoneGroup bone, ModelProperties properties, GeoBone parent) {
		GeoBone geoBone = new GeoBone();

		Bone rawBone = bone.selfBone;
		Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.getRotation()));
		Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.getPivot()));
		rotation.x *= -1;
		rotation.y *= -1;

		geoBone.mirror = rawBone.getMirror();
		geoBone.dontRender = rawBone.getNeverRender();
		geoBone.reset = rawBone.getReset();
		geoBone.inflate = rawBone.getInflate();
		geoBone.parent = parent;
		geoBone.setModelRendererName(rawBone.getName());

		geoBone.setRotationX((float) Math.toRadians(rotation.getX()));
		geoBone.setRotationY((float) Math.toRadians(rotation.getY()));
		geoBone.setRotationZ((float) Math.toRadians(rotation.getZ()));

		geoBone.rotationPointX = -pivot.getX();
		geoBone.rotationPointY = pivot.getY();
		geoBone.rotationPointZ = pivot.getZ();

		//add cubes
		if (!ArrayUtils.isEmpty(rawBone.getCubes())) {
			for (Cube cube : rawBone.getCubes()) {
				geoBone.childCubes.add(GeoCube.createFromPojoCube(cube, properties,
						geoBone.inflate == null ? null : geoBone.inflate / 16, geoBone.mirror));
			}
		}

		//add locators
		if (rawBone.getLocators() != null && !rawBone.getLocators().isEmpty()) {
			for (Map.Entry<String, LocatorValue> entry: rawBone.getLocators().entrySet()) {
				geoBone.childLocators.add(new GeoLocator(
						geoBone,
						entry.getKey(),
						(float) entry.getValue().doubleArrayValue[0] / 16f,
						(float) entry.getValue().doubleArrayValue[1] / 16f,
						(float) entry.getValue().doubleArrayValue[2] / 16f
				));
			}
		}

		//create bones
		for (RawBoneGroup child : bone.children.values()) {
			geoBone.childBones.add(constructBone(child, properties, geoBone));
		}

		return geoBone;
	}
}
