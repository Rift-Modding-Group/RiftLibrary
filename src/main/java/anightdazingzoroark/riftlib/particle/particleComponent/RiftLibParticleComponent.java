package anightdazingzoroark.riftlib.particle.particleComponent;

import anightdazingzoroark.riftlib.core.ConstantValue;
import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.HashMap;
import java.util.Map;

public abstract class RiftLibParticleComponent {
    public abstract void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException;

    public abstract void applyComponent(RiftLibParticleEmitter emitter);

    protected IValue parseExpression(MolangParser parser, RawParticleComponent.ComponentValue componentValue) throws MolangException {
        //component value was a string
        if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) return parser.parseExpression(componentValue.string);
            //component value was a double
        else return ConstantValue.fromDouble(componentValue.number);
    }

    protected IValue[] parseExpressionArray(MolangParser parser, int intendedSize, RawParticleComponent.ComponentValue componentValue) throws MolangException {
        if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
            if (componentValue.array.size() != intendedSize) throw new InvalidValueException("Invalid array length!");

            IValue[] toReturn = new IValue[componentValue.array.size()];
            for (int i = 0; i < toReturn.length; i++) {
                RawParticleComponent.ComponentValue value = componentValue.array.get(i);
                toReturn[i] = parseExpression(parser, value);
            }
            return toReturn;
        }
        else throw new InvalidValueException("Component was not an array!");
    }
}
