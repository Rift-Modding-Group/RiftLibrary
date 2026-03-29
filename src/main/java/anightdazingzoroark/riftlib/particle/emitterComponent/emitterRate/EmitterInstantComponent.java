package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class EmitterInstantComponent extends RiftLibEmitterRateComponent {
    public IValue particleCount = new Constant(10f);

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("num_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("num_particles");
            this.particleCount = parseExpression(parser, componentValue);
        }
    }

    @Override
    public void createParticles(RiftLibParticleEmitter emitter) {
        double particleCount = this.particleCount.get();
        while (emitter.getParticleCount() < particleCount) {
            emitter.getParticles().add(emitter.createParticle());
            emitter.setParticleCount(emitter.getParticleCount() + 1);
        }
    }
}
