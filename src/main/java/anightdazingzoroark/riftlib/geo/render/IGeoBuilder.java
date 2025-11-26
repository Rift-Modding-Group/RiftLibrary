package anightdazingzoroark.riftlib.geo.render;

import anightdazingzoroark.riftlib.newGeo.modelRaw.RawGeoModel;
import anightdazingzoroark.riftlib.newGeo.modelRaw.RawModelBoneGroup;
import anightdazingzoroark.riftlib.newGeo.modelRaw.RawGeometryTree;
import anightdazingzoroark.riftlib.geo.render.built.GeoBone;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;

public interface IGeoBuilder {
	GeoModel constructGeoModel(RawGeometryTree geometryTree);

	GeoBone constructBone(RawModelBoneGroup boneGroup, RawGeoModel.RawModelDescription description, GeoBone parent);
}
