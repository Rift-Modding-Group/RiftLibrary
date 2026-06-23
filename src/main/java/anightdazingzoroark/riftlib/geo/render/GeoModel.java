package anightdazingzoroark.riftlib.geo.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;

public class GeoModel {
	public List<GeoBone> topLevelBones = new ArrayList<>();
	public RawGeoModel.RawModelDescription description;

	public GeoModel copy() {
		GeoModel copy = new GeoModel();
		copy.description = this.description;

		for (GeoBone bone : this.topLevelBones) {
			copy.topLevelBones.add(copyBone(bone, null));
		}

		return copy;
	}

	private static GeoBone copyBone(GeoBone source, GeoBone parent) {
		GeoBone copy = new GeoBone();
		copy.parent = parent;
		copy.name = source.name;
		copy.mirror = source.mirror;
		copy.inflate = source.inflate;
		copy.dontRender = source.dontRender;
		copy.reset = source.reset;
		copy.extraData = source.extraData;

		copy.setHidden(source.isHidden(), source.childBonesAreHiddenToo());
		copy.setCubesHidden(source.cubesAreHidden());
		copy.setScaleX(source.getScaleX());
		copy.setScaleY(source.getScaleY());
		copy.setScaleZ(source.getScaleZ());
		copy.setPositionX(source.getPositionX());
		copy.setPositionY(source.getPositionY());
		copy.setPositionZ(source.getPositionZ());
		copy.setRotationX(source.getRotationX());
		copy.setRotationY(source.getRotationY());
		copy.setRotationZ(source.getRotationZ());
		copy.setPivotX(source.getPivotX());
		copy.setPivotY(source.getPivotY());
		copy.setPivotZ(source.getPivotZ());

		copy.childCubes.addAll(source.childCubes);

		for (GeoLocator locator : source.childLocators) {
			copy.childLocators.add(copyLocator(locator, copy));
		}

		for (GeoBone child : source.childBones) {
			copy.childBones.add(copyBone(child, copy));
		}

		return copy;
	}

	private static GeoLocator copyLocator(GeoLocator source, GeoBone parent) {
		GeoLocator copy = new GeoLocator(parent, source.name);
		copy.setHidden(source.isHidden(), source.childBonesAreHiddenToo());
		copy.setCubesHidden(source.cubesAreHidden());
		copy.setPositionX(source.getPositionX());
		copy.setPositionY(source.getPositionY());
		copy.setPositionZ(source.getPositionZ());
		copy.setRotationX(source.getRotationX());
		copy.setRotationY(source.getRotationY());
		copy.setRotationZ(source.getRotationZ());
		return copy;
	}

	public Optional<GeoBone> getBone(String name) {
		for (GeoBone bone : topLevelBones) {
			GeoBone optionalBone = getBoneRecursively(name, bone);
			if (optionalBone != null) {
				return Optional.of(optionalBone);
			}
		}
		return Optional.empty();
	}

	private GeoBone getBoneRecursively(String name, GeoBone bone) {
		if (bone.name.equals(name)) {
			return bone;
		}
		for (GeoBone childBone : bone.childBones) {
			if (childBone.name.equals(name)) {
				return childBone;
			}
			GeoBone optionalBone = getBoneRecursively(name, childBone);
			if (optionalBone != null) {
				return optionalBone;
			}
		}
		return null;
	}

	public List<GeoBone> getAllBones() {
		List<GeoBone> listToReturn = new ArrayList<>();
		for (GeoBone bone : this.topLevelBones) {
			this.collectAll(bone, listToReturn);
		}
		return listToReturn;
	}

	private void collectAll(GeoBone current, List<GeoBone> result) {
		result.add(current);
		for (GeoBone child : current.childBones) {
			collectAll(child, result);
		}
	}

	public List<GeoLocator> getAllLocators() {
		List<GeoLocator> listToReturn = new ArrayList<>();
		for (GeoBone bone : this.getAllBones()) {
			for (GeoLocator locator : bone.childLocators) {
				if (!listToReturn.contains(locator)) listToReturn.add(locator);
			}
		}
		return listToReturn;
	}
}
