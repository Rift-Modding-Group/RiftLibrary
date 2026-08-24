package anightdazingzoroark.riftlib.jsonParsing;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.jsonParsing.constructor.AnimationConstructor;
import anightdazingzoroark.riftlib.jsonParsing.constructor.ParticleConstructor;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationChannel;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationFile;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawLoopType;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.*;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.geo.GeoModel;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiftLibLoader {
    @NotNull
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawMolangValue.class, new RawMolangValue.Deserializer())
            .registerTypeAdapter(RawUVUnion.class, new RawUVUnion.Deserializer())
            .registerTypeAdapter(RawAnimationChannel.class, new RawAnimationChannel.Deserializer())
            .registerTypeAdapter(RawParticleComponent.class, new RawParticleComponent.Deserializer())
            .registerTypeAdapter(RawModelLocatorList.class, new RawModelLocatorList.Deserialize())
            .registerTypeAdapter(RawModelBoundingBoxList.class, new RawModelBoundingBoxList.Deserializer())
            .registerTypeAdapter(RawLoopType.class, new RawLoopType.Deserializer())
            .create();
    @NotNull
    private final AnimationConstructor animationConstructor = new AnimationConstructor(); //create animations
    @NotNull
    private final ParticleConstructor particleConstructor = new ParticleConstructor(); //create particles

    @NotNull
	public Map<String, GeoModel> loadGeoModels(RiftLibResourceReader resourceReader, ResourceLocation location) {
		try {
            Map<String, GeoModel> toReturn = new HashMap<>();

			//Deserialize from json into basic json objects, bones are still stored as a flat list
			RawGeoModel rawModel = this.gson.fromJson(this.getResourceAsString(location, resourceReader), RawGeoModel.class);

            //evaluate over each geometry
            for (RawGeoModel.MinecraftGeometry geometry : rawModel.geometry) {
                //Get and validate the description
                RawGeoModel.RawModelDescription modelDescription = geometry.description; //is temporary, will acknowledge multiple models soon
                if (modelDescription.identifier == null) {
                    throw new IllegalStateException(location + " has no identifier!");
                }
                if (modelDescription.texture_width == null || modelDescription.texture_height == null) {
                    throw new IllegalStateException(location + " has no texture size set!");
                }
                if (modelDescription.visible_bounds_width == null || modelDescription.visible_bounds_height == null) {
                    throw new IllegalStateException(location + " has no visible bounds size set!");
                }
                if (modelDescription.visible_bounds_offset == null) {
                    throw new IllegalStateException(location + " has no visible bounds offset!");
                }
                if (modelDescription.visible_bounds_offset.length != 3) {
                    throw new IllegalStateException("Visible bounds offset in " + location + " must have 3 values!");
                }

                //Parse the flat list of bones into a raw hierarchical tree of "BoneGroup"s
                RawGeometryTree rawGeometryTree = new RawGeometryTree(geometry, location);

                //Build the quads and cubes from the raw tree into a built and ready to be rendered GeoModel
                toReturn.put(modelDescription.identifier, new GeoModel(modelDescription, rawGeometryTree));
            }
            return toReturn;
		}
        catch (Exception e) {
			RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
			throw (new RuntimeException(e));
		}
	}

    @NotNull
    public AnimationFile loadAnimationFile(RiftLibResourceReader resourceReader, ResourceLocation location) {
        try {
            AnimationFile animationFile = new AnimationFile();

            RawAnimationFile rawAnimationFile = this.gson.fromJson(this.getResourceAsString(location, resourceReader), RawAnimationFile.class);
            Map<String, RawAnimationFile.RawAnimation> rawAnimationMap = rawAnimationFile.rawAnimations;
            for (Map.Entry<String, RawAnimationFile.RawAnimation> rawAnimation : rawAnimationMap.entrySet()) {
                Animation animation = this.animationConstructor.getAnimationFromRawAnimationEntry(rawAnimation);
                animationFile.putAnimation(rawAnimation.getKey(), animation);
            }

            return animationFile;
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    @NotNull
    public ParticleBuilder loadParticle(MolangParser parser, RiftLibResourceReader resourceReader, ResourceLocation location) {
        try {
            RawParticle rawParticle = this.gson.fromJson(this.getResourceAsString(location, resourceReader), RawParticle.class);
            return this.particleConstructor.createParticleBuilder(rawParticle, parser);
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    @NotNull
    private String getResourceAsString(ResourceLocation location, RiftLibResourceReader resourceReader) {
        try (InputStream inputStream = resourceReader.open(location)) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        }
        catch (Exception e) {
            String message = "Couldn't load " + location;
            RiftLib.LOGGER.error(message, e);
            throw new RuntimeException(new FileNotFoundException(location.toString()));
        }
    }
}
