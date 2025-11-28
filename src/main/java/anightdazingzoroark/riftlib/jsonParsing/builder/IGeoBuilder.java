package anightdazingzoroark.riftlib.jsonParsing.builder;

import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawModelBoneGroup;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeometryTree;
import anightdazingzoroark.riftlib.geo.render.GeoBone;
import anightdazingzoroark.riftlib.geo.render.GeoModel;

public interface IGeoBuilder {
	GeoModel constructGeoModel(RawGeometryTree geometryTree);

	GeoBone constructBone(RawModelBoneGroup boneGroup, RawGeoModel.RawModelDescription description, GeoBone parent);
}
