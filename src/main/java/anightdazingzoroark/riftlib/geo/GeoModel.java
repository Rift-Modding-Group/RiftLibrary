package anightdazingzoroark.riftlib.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.*;
import anightdazingzoroark.riftlib.util.VectorUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

/**
 * Immutable class containing info for a parsed model
 * */
public class GeoModel {
	@NotNull
	public final RawGeoModel.RawModelDescription description;
	@NotNull
	private final RawGeometryTree geometryTree;

	@NotNull
	private final List<GeoBone> topLevelBones = new ArrayList<>();
	@NotNull
	private final Map<String, GeoBone> allBones = new HashMap<>();
	@NotNull
	private final List<GeoLocator> allLocators = new ArrayList<>();
	@NotNull
	private final List<GeoBoundingBox> allBoundingBoxes = new ArrayList<>();

	public GeoModel(@NotNull RawGeoModel.RawModelDescription modelDescription, @NotNull RawGeometryTree geometryTree) {
		this.description = modelDescription;
		this.geometryTree = geometryTree; //preserved for copying

		//define other lists and maps
		for (RawModelBoneGroup rawBone : geometryTree.topLevelBones.values()) {
			this.topLevelBones.add(this.constructBone(rawBone, null));
		}
	}

	//-----part creation operations-----
	@NotNull
	private GeoBone constructBone(@NotNull RawModelBoneGroup bone, @Nullable GeoBone parentBone) {
		RawGeoModel.RawModelBone rawBone = bone.selfBone;
		GeoBone geoBone = new GeoBone(parentBone, rawBone.name);
		this.allBones.put(geoBone.getName(), geoBone);

		Vector3f rotation = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.rotation));
		Vector3f pivot = VectorUtils.convertDoubleToFloat(VectorUtils.fromArray(rawBone.pivot));
		rotation.x *= -1;
		rotation.y *= -1;

		geoBone.mirror = rawBone.mirror;
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
						cube, this.description,
						geoBone.inflate == null ? null : geoBone.inflate / 16D,
						geoBone.mirror
				));
			}
		}

		//add locators
		if (rawBone.locators != null && !rawBone.locators.list.isEmpty()) {
			for (RawModelLocatorList.RawModelLocator rawLocator : rawBone.locators.list) {
				GeoLocator toAdd = new GeoLocator(geoBone, rawLocator.name);

				//---add to bone---
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

				//---add to locator list on model---
				this.allLocators.add(toAdd);
			}
		}

		//add bounding boxes
		if (rawBone.boundingBoxes != null && !rawBone.boundingBoxes.list.isEmpty()) {
			for (RawModelBoundingBoxList.RawBoundingBox rawBoundingBox : rawBone.boundingBoxes.list) {
				GeoBoundingBox toAdd = new GeoBoundingBox(geoBone, rawBoundingBox.name);

				//---add to bone---
				toAdd.getPosition().set(
						(float) -rawBoundingBox.origin[0],
						(float) rawBoundingBox.origin[1],
						(float) rawBoundingBox.origin[2]
				);

				toAdd.setSize((float) rawBoundingBox.size[0], (float) rawBoundingBox.size[1]);

				toAdd.canCollide = rawBoundingBox.collision;
				toAdd.tags = rawBoundingBox.tags;

				geoBone.childBoundingBoxes.add(toAdd);

				//---add to bounding box list on model---
				this.allBoundingBoxes.add(toAdd);
			}
		}

		//create bones
		for (RawModelBoneGroup child : bone.children.values()) {
			geoBone.childBones.add(this.constructBone(child, geoBone));
		}

		return geoBone;
	}

	//-----getters-----
	@NotNull
	public String getIdentifier() {
		return this.description.identifier;
	}

	public int[] getTextureSize() {
		return new int[]{this.description.texture_width, this.description.texture_height};
	}

	public double[] getVisibleBoundsSize() {
		return new double[]{this.description.visible_bounds_width, this.description.visible_bounds_height};
	}

	public double[] getVisibleBoundsOffset() {
		return this.description.visible_bounds_offset.clone();
	}

	@NotNull
	public List<GeoBone> getTopLevelBones() {
		return List.copyOf(this.topLevelBones);
	}

	@NotNull
	public Map<String, GeoBone> getAllBones() {
		return Map.copyOf(this.allBones);
	}

	@NotNull
	public List<GeoLocator> getAllLocators() {
		return List.copyOf(this.allLocators);
	}

	@NotNull
	public List<GeoBoundingBox> getAllBoundingBoxes() {
		return List.copyOf(this.allBoundingBoxes);
	}

	//-----other operations-----
	@NotNull
	public GeoModel copy() {
		return new GeoModel(this.description, this.geometryTree);
	}
}
