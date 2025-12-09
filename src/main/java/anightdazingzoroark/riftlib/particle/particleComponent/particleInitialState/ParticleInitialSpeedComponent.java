package anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleInitialSpeedComponent extends RiftLibParticleComponent {
    private IValue[] particleInitialSpeed = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("$value")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("$value");
            //check if its a singular value
            //if its singular value, thats its y speed
            if (componentValue.number != null) {
                this.particleInitialSpeed = new IValue[]{
                        MolangParser.ZERO,
                        this.parseExpression(parser, componentValue),
                        MolangParser.ZERO
                };
            }
            //if its an array, those define its speed on each axis
            else if (componentValue.array != null) {
                this.particleInitialSpeed = this.parseExpressionArray(parser, 3, componentValue);
            }
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.particleInitialSpeed = this.particleInitialSpeed;
    }
}
