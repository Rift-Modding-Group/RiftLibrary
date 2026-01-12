package anightdazingzoroark.riftlib.jsonParsing;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.hitboxLogic.HitboxDefinitionList;
import anightdazingzoroark.riftlib.exceptions.GeoModelException;
import anightdazingzoroark.riftlib.jsonParsing.constructor.AnimationConstructor;
import anightdazingzoroark.riftlib.jsonParsing.constructor.HitboxConstructor;
import anightdazingzoroark.riftlib.jsonParsing.constructor.ParticleConstructor;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationChannel;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationFile;
import anightdazingzoroark.riftlib.jsonParsing.raw.geo.*;
import anightdazingzoroark.riftlib.jsonParsing.raw.hitbox.RawHitboxDefinition;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.jsonParsing.constructor.GeoConstructor;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class RiftLibLoader {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawUVUnion.class, new RawUVUnion.Deserializer())
            .registerTypeAdapter(RawAnimationChannel.class, new RawAnimationChannel.Deserializer())
            .registerTypeAdapter(RawParticleComponent.class, new RawParticleComponent.Deserializer())
            .registerTypeAdapter(RawModelLocatorList.class, new RawModelLocatorList.Deserialize())
            .create();

	public GeoModel loadGeoModel(IResourceManager resourceManager, ResourceLocation location) {
		try {
			// Deserialize from json into basic json objects, bones are still stored as a
			// flat list
			RawGeoModel rawModel = this.gson.fromJson(getResourceAsString(location, resourceManager), RawGeoModel.class);;

            if (FormatVersion.forValue(rawModel.format_version) != FormatVersion.VERSION_1_12_0) {
				throw new GeoModelException(location, "Wrong geometry json version, expected 1.12.0");
			}

			// Parse the flat list of bones into a raw hierarchical tree of "BoneGroup"s
			RawGeometryTree rawGeometryTree = new RawGeometryTree(rawModel, location);

			// Build the quads and cubes from the raw tree into a built and ready to be
			// rendered GeoModel
			return GeoConstructor.constructGeoModel(rawGeometryTree);
		}
        catch (Exception e) {
			RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
			throw (new RuntimeException(e));
		}
	}

    public AnimationFile loadAnimationFile(MolangParser parser, IResourceManager manager, ResourceLocation location) {
        try {
            AnimationFile animationFile = new AnimationFile();

            RawAnimationFile rawAnimationFile = this.gson.fromJson(getResourceAsString(location, manager), RawAnimationFile.class);
            Map<String, RawAnimationFile.RawAnimation> rawAnimationMap = rawAnimationFile.rawAnimations;
            for (Map.Entry<String, RawAnimationFile.RawAnimation> rawAnimation : rawAnimationMap.entrySet()) {
                Animation animation = AnimationConstructor.getAnimationFromRawAnimationEntry(rawAnimation);
                animationFile.putAnimation(rawAnimation.getKey(), animation);
            }

            return animationFile;
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    public HitboxDefinitionList loadHitboxDefinitionList(IResourceManager resourceManager, ResourceLocation location) {
        try {
            RawHitboxDefinition rawHitboxDefinition = this.gson.fromJson(getResourceAsString(location, resourceManager), RawHitboxDefinition.class);
            return HitboxConstructor.createHitboxDefinitionList(rawHitboxDefinition);
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    public ParticleBuilder loadParticle(MolangParser parser, IResourceManager resourceManager, ResourceLocation location) {
        try {
            RawParticle rawParticle = this.gson.fromJson(getResourceAsString(location, resourceManager), RawParticle.class);
            return ParticleConstructor.createParticleBuilder(location.getNamespace(), rawParticle, parser);
        }
        catch (Exception e) {
            RiftLib.LOGGER.error(String.format("Error parsing %S", location), e);
            throw (new RuntimeException(e));
        }
    }

    private String getResourceAsString(ResourceLocation location, IResourceManager manager) {
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
