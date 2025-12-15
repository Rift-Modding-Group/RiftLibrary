package anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleInitialSpinComponent extends RiftLibParticleComponent {
    private IValue initialRotation = MolangParser.ZERO;
    private IValue rotationRate = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("rotation")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation");
            this.initialRotation = this.parseExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_rate")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_rate");
            this.rotationRate = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.initialRotation = this.initialRotation;
        particle.rotationRate = this.rotationRate;
    }
}
