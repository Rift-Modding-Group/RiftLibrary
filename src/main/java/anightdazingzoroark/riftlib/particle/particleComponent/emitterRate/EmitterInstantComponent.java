package anightdazingzoroark.riftlib.particle.particleComponent.emitterRate;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class EmitterInstantComponent extends RiftLibEmitterRateComponent {
    public IValue particleCount = new Constant(10f);

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("num_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("num_particles");
            this.particleCount = parseExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterRate = this;
        emitter.maxParticleCount = this.particleCount;
    }
}
