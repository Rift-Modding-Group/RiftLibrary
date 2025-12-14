package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.Map;

public class EmitterShapeBoxComponent extends RiftLibEmitterShapeComponent {
    public boolean surfaceOnly;
    public IValue[] halfDimensions;
    public String particleDirection;
    public IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("half_dimensions")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("half_dimensions");
            this.halfDimensions = this.parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("surface_only")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("surface_only");
            this.surfaceOnly = componentValue.bool == Boolean.TRUE;
        }
        if (rawComponent.getValue().componentValues.containsKey("direction")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
            //direction as string
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                if (componentValue.string.equals("inwards") || componentValue.string.equals("outwards")) {
                    this.particleDirection = componentValue.string;
                }
                else throw new InvalidValueException("Invalid value "+componentValue.string+" for direction!");
            }
            //custom direction
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                this.customParticleDirection = parseExpressionArray(parser, 3, componentValue);
            }
        }
    }
}
