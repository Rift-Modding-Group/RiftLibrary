package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class EmitterLifetimeOnceComponent extends RiftLibEmitterLifetimeComponent {
    private IValue activeTime = new Constant(10);

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("active_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("active_time");
            this.activeTime = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public boolean canCreateParticles(RiftLibParticleEmitter emitter) {
        return emitter.getAge() < this.activeTime.get() * 20;
    }

    @Override
    public boolean canExpire(RiftLibParticleEmitter emitter) {
        return emitter.getAge() >= this.activeTime.get() * 20;
    }
}
