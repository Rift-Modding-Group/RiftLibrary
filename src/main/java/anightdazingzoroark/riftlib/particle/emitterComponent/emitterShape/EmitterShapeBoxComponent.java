package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;

import java.util.Map;

public class EmitterShapeBoxComponent extends RiftLibEmitterShapeComponent {
    public boolean surfaceOnly;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("surface_only")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("surface_only");
            this.surfaceOnly = componentValue.bool == Boolean.TRUE;
        }
    }
}
