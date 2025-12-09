package anightdazingzoroark.riftlib.particle.particleComponent.particleMotion;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleMotionDynamicComponent extends RiftLibParticleComponent {
    private IValue[] particleLinearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("linear_acceleration")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("linear_acceleration");
            this.particleLinearAcceleration = this.parseExpressionArray(parser, 3, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.particleLinearAcceleration = this.particleLinearAcceleration;
    }
}
