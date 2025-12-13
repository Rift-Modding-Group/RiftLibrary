package anightdazingzoroark.riftlib.jsonParsing.constructor;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleMaterial;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.RiftLibParticleComponentRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class ParticleConstructor {
    public static ParticleBuilder createParticleBuilder(String namespace, RawParticle rawParticle) throws NumberFormatException, MolangException {
        ParticleBuilder toReturn = new ParticleBuilder();

        //make a molang parser
        MolangParser molangParser = new MolangParser();

        //get name
        toReturn.identifier = rawParticle.rawParticleEffect.description.identifier;

        //get texture
        String textureLocation = rawParticle.rawParticleEffect.description.basicRenderParameters.texture;
        if (!textureLocation.endsWith(".png")) textureLocation = textureLocation.concat(".png");
        toReturn.texture = new ResourceLocation(namespace, textureLocation);

        //get material
        toReturn.material = ParticleMaterial.getMaterialFromString(rawParticle.rawParticleEffect.description.basicRenderParameters.material);

        //make a molang parser
        toReturn.molangParser = molangParser;

        //get raw components
        Map<String, RawParticleComponent> particleComponents = rawParticle.rawParticleEffect.components;

        //evaluate raw components
        for (Map.Entry<String, RawParticleComponent> rawComponent : particleComponents.entrySet()) {
            //get emitter components
            if (RiftLibParticleComponentRegistry.isEmitterComponent(rawComponent.getKey())) {
                RiftLibEmitterComponent component = RiftLibParticleComponentRegistry.createEmitterComponent(rawComponent.getKey());
                if (component != null) {
                    component.parseRawComponent(rawComponent, molangParser);
                    toReturn.emitterComponents.add(component);
                }
            }

            //get particle components, dont parse them yet however
            if (RiftLibParticleComponentRegistry.isParticleComponent(rawComponent.getKey())) {
                toReturn.rawParticleComponents.add(rawComponent);
            }
        }

        return toReturn;
    }
}
