package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.geo.raw.RawGeoModel;
import anightdazingzoroark.riftlib.geo.raw.RawModelBoneGroup;
import anightdazingzoroark.riftlib.geo.raw.RawGeometryTree;
import anightdazingzoroark.riftlib.geo.render.built.GeoBone;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;

public interface IGeoBuilder {
	GeoModel constructGeoModel(RawGeometryTree geometryTree);

	GeoBone constructBone(RawModelBoneGroup boneGroup, RawGeoModel.RawModelDescription description, GeoBone parent);
}
