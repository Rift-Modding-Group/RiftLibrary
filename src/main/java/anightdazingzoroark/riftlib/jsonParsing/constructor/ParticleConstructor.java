package anightdazingzoroark.riftlib.jsonParsing.constructor;

import anightdazingzoroark.riftlib.core.ConstantValue;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponentRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class ParticleConstructor {
    public static ParticleBuilder createParticleBuilder(MolangParser parser, String namespace, RawParticle rawParticle) throws NumberFormatException, MolangException {
        ParticleBuilder toReturn = new ParticleBuilder();

        //get name
        toReturn.identifier = rawParticle.rawParticleEffect.description.identifier;

        //get texture
        String textureLocation = rawParticle.rawParticleEffect.description.basicRenderParameters.texture;
        if (!textureLocation.endsWith(".png")) textureLocation = textureLocation.concat(".png");
        toReturn.texture = new ResourceLocation(namespace, textureLocation);

        //get material
        toReturn.material = ParticleMaterial.getMaterialFromString(rawParticle.rawParticleEffect.description.basicRenderParameters.material);

        //get molang parser
        toReturn.molangParser = parser;

        //get components
        Map<String, RawParticleComponent> particleComponents = rawParticle.rawParticleEffect.components;
        for (Map.Entry<String, RawParticleComponent> rawComponent : particleComponents.entrySet()) {
            RiftLibParticleComponent component = RiftLibParticleComponentRegistry.createComponent(rawComponent.getKey());
            if (component != null) {
                component.parseRawComponent(rawComponent, parser);
                toReturn.particleComponents.add(component);
            }
        }

        return toReturn;
    }
}
