package anightdazingzoroark.riftlib.jsonParsing;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.file.AnimationFile;
import anightdazingzoroark.riftlib.geo.GeoModelException;
import anightdazingzoroark.riftlib.jsonParsing.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationChannel;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationFile;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.FormatVersion;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeoModel;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawGeometryTree;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.RawUVUnion;
import anightdazingzoroark.riftlib.molang.MolangParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.jsonParsing.builder.GeoBuilder;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class RiftLibLoader {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawUVUnion.class, new RawUVUnion.Deserializer())
            .registerTypeAdapter(RawAnimationChannel.class, new RawAnimationChannel.Deserializer())
            .create();

	public static GeoModel loadGeoModel(IResourceManager resourceManager, ResourceLocation location) {
		try {
			// Deserialize from json into basic json objects, bones are still stored as a
			// flat list
			RawGeoModel rawModel = gson.fromJson(getResourceAsString(location, resourceManager), RawGeoModel.class);;

            if (FormatVersion.forValue(rawModel.format_version) != FormatVersion.VERSION_1_12_0) {
				throw new GeoModelException(location, "Wrong geometry json version, expected 1.12.0");
			}

			// Parse the flat list of bones into a raw hierarchical tree of "BoneGroup"s
			RawGeometryTree rawGeometryTree = new RawGeometryTree(rawModel, location);

			// Build the quads and cubes from the raw tree into a built and ready to be
			// rendered GeoModel
			return GeoBuilder.constructGeoModel(rawGeometryTree);
		}
        catch (Exception e) {
			RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
			throw (new RuntimeException(e));
		}
	}

    public static AnimationFile loadAnimationFile(MolangParser parser, IResourceManager manager, ResourceLocation location) {
        try {
            AnimationFile animationFile = new AnimationFile();

            RawAnimationFile rawAnimationFile = gson.fromJson(getResourceAsString(location, manager), RawAnimationFile.class);
            Map<String, RawAnimationFile.RawAnimation> rawAnimationMap = rawAnimationFile.rawAnimations;
            for (Map.Entry<String, RawAnimationFile.RawAnimation> rawAnimation : rawAnimationMap.entrySet()) {
                Animation animation = AnimationBuilder.getAnimationFromRawAnimationEntry(rawAnimation, parser);
                animationFile.putAnimation(rawAnimation.getKey(), animation);
            }

            return animationFile;
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    public static String getResourceAsString(ResourceLocation location, IResourceManager manager) {
        try (InputStream inputStream = manager.getResource(location).getInputStream()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
        catch (Exception e) {
            String message = "Couldn't load " + location;
            RiftLib.LOGGER.error(message, e);
            throw new RuntimeException(new FileNotFoundException(location.toString()));
        }
    }
}
