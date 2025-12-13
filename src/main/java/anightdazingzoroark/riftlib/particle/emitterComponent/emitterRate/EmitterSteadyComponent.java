package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.Map;

public class EmitterSteadyComponent extends RiftLibEmitterRateComponent {
    public IValue spawnRate = MolangParser.ONE;
    public IValue maxParticleCount = new Constant(50);

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("spawn_rate")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("spawn_rate");
            this.spawnRate = parseExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("max_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_particles");
            this.maxParticleCount = parseExpression(parser, componentValue);
        }
    }
}
