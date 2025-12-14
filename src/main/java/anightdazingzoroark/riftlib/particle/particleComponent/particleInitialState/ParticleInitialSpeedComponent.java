package anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleInitialSpeedComponent extends RiftLibParticleComponent {
    private IValue initialSpeed = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("$value")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("$value");
            this.initialSpeed = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.initialSpeed = this.initialSpeed;
    }
}
