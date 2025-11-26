package anightdazingzoroark.riftlib.file;

import anightdazingzoroark.riftlib.newGeo.FormatVersion;
import anightdazingzoroark.riftlib.newGeo.Converter;
import anightdazingzoroark.riftlib.newGeo.GeoModelException;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.newGeo.modelRaw.RawGeoModel;
import anightdazingzoroark.riftlib.newGeo.modelRaw.RawGeometryTree;
import anightdazingzoroark.riftlib.geo.render.GeoBuilder;
import anightdazingzoroark.riftlib.geo.render.built.GeoModel;

public class GeoModelLoader {
	public GeoModel loadModel(IResourceManager resourceManager, ResourceLocation location) {
		try {
			// Deserialize from json into basic json objects, bones are still stored as a
			// flat list
			RawGeoModel rawModel = Converter.convertModelJSONToRawGeoModel(AnimationFileLoader.getResourceAsString(location, resourceManager));

            if (FormatVersion.forValue(rawModel.format_version) != FormatVersion.VERSION_1_12_0) {
				throw new GeoModelException(location, "Wrong geometry json version, expected 1.12.0");
			}

			// Parse the flat list of bones into a raw hierarchical tree of "BoneGroup"s
			RawGeometryTree rawGeometryTree = new RawGeometryTree(rawModel, location);

			// Build the quads and cubes from the raw tree into a built and ready to be
			// rendered GeoModel
			return GeoBuilder.getGeoBuilder(location.getNamespace()).constructGeoModel(rawGeometryTree);
		}
        catch (Exception e) {
			RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
			throw (new RuntimeException(e));
		}
	}
}
