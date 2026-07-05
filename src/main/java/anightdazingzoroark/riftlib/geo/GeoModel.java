package anightdazingzoroark.riftlib.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;

import javax.annotation.Nullable;

public class GeoModel {
	public RawGeoModel.RawModelDescription description;
	public final List<GeoBone> topLevelBones = new ArrayList<>();
	public final Map<String, GeoBone> allBones = new HashMap<>();
	public final List<GeoLocator> allLocators = new ArrayList<>();
	public final List<GeoBoundingBox> allBoundingBoxes = new ArrayList<>();

	public GeoModel copy() {
		GeoModel copyModel = new GeoModel();
		copyModel.description = this.description;

		for (GeoBone bone : this.topLevelBones) {
			copyModel.topLevelBones.add(this.copyBone(copyModel, bone, null));
		}

		return copyModel;
	}

	private GeoBone copyBone(GeoModel copyModel, GeoBone sourceBone, @Nullable GeoBone copyParentBone) {
		GeoBone copyBone = new GeoBone(copyParentBone, sourceBone.getName());
		copyModel.allBones.put(copyBone.getName(), copyBone);

		copyBone.mirror = sourceBone.mirror;
		copyBone.inflate = sourceBone.inflate;
		copyBone.dontRender = sourceBone.dontRender;

		copyBone.setHidden(sourceBone.isHidden(), sourceBone.childBonesAreHiddenToo());
		copyBone.setCubesHidden(sourceBone.cubesAreHidden());
		copyBone.getScale().set(sourceBone.getScale());
		copyBone.getPosition().set(sourceBone.getPosition());
		copyBone.getRotation().set(sourceBone.getRotation());
		copyBone.getPivot().set(sourceBone.getPivot());

		copyBone.childCubes.addAll(sourceBone.childCubes);

		for (GeoLocator locator : sourceBone.childLocators) {
			GeoLocator copyLocator = this.copyLocator(locator, copyBone);
			copyBone.childLocators.add(copyLocator);
			copyModel.allLocators.add(copyLocator);
		}

		for (GeoBoundingBox boundingBox : sourceBone.childBoundingBoxes) {
			GeoBoundingBox copyBoundingBox = this.copyBoundingBox(boundingBox, copyBone);
			copyBone.childBoundingBoxes.add(copyBoundingBox);
			copyModel.allBoundingBoxes.add(copyBoundingBox);
		}

		for (GeoBone child : sourceBone.childBones) {
			copyBone.childBones.add(this.copyBone(copyModel, child, copyBone));
		}

		return copyBone;
	}

	private GeoLocator copyLocator(GeoLocator source, GeoBone parent) {
		GeoLocator copy = new GeoLocator(parent, source.name);
		copy.setHidden(source.isHidden(), source.childBonesAreHiddenToo());
		copy.setCubesHidden(source.cubesAreHidden());
		copy.getPosition().set(source.getPosition());
		copy.getRotation().set(source.getRotation());
		return copy;
	}

	private GeoBoundingBox copyBoundingBox(GeoBoundingBox source, GeoBone parent) {
		GeoBoundingBox copy = new GeoBoundingBox(parent, source.name);
		copy.getPosition().set(source.getPosition());
		copy.setSize(source.getSize()[0], source.getSize()[1]);
		copy.canCollide = source.canCollide;
		copy.tags = source.tags;
		return copy;
	}
}
