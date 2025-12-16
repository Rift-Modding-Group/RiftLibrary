package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.Map;

public class EmitterShapeDiscComponent extends RiftLibEmitterShapeComponent {
    public IValue[] planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
    public IValue radius = MolangParser.ZERO;
    public boolean surfaceOnly;
    public String particleDirection;
    public IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("plane_normal")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("plane_normal");

            //assuming its a string, we have to choose between x, y, and z directions
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                switch (componentValue.string) {
                    case "x":
                        this.planeNormal = new IValue[]{MolangParser.ONE, MolangParser.ZERO, MolangParser.ZERO};
                        break;
                    case "y":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
                        break;
                    case "z":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ONE};
                        break;
                    default:
                        throw new MolangException("Invalid value for plane direction!");
                }
            }
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                this.planeNormal = this.parseExpressionArray(parser, 3, componentValue);
            }
        }
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("radius")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("radius");
            this.radius = parseExpression(parser, componentValue);
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
