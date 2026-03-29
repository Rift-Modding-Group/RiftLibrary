package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class EmitterSteadyComponent extends RiftLibEmitterRateComponent {
    public IValue spawnRate = MolangParser.ONE;
    public IValue maxParticleCount = new Constant(50);

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("spawn_rate")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("spawn_rate");
            this.spawnRate = this.parseExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("max_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_particles");
            this.maxParticleCount = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public void createParticles(RiftLibParticleEmitter emitter) {
        int maxParticleCount = (int) this.maxParticleCount.get();
        //turn particles per second into particles per tick
        double particleRate = this.spawnRate.get() / 20D;

        emitter.setParticleCount(emitter.getParticleCount() + particleRate);
        while (emitter.getParticleCount() >= 1 && emitter.getParticles().size() < maxParticleCount) {
            emitter.getParticles().add(emitter.createParticle());
            emitter.setParticleCount(emitter.getParticleCount() - 1);
        }

        if (emitter.getParticles().size() >= maxParticleCount) {
            emitter.setParticleCount(Math.min(emitter.getParticleCount(), 1));
        }
    }
}
