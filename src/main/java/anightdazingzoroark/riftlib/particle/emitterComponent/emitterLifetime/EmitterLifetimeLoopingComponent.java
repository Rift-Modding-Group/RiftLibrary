package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.Map;

public class EmitterLifetimeLoopingComponent extends RiftLibEmitterLifetimeComponent {
    public IValue emitterActiveTime = new Constant(10);
    public IValue emitterSleepTime = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        //amount of time the looping emitter will be active
        if (rawComponent.getValue().componentValues.containsKey("active_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("active_time");
            this.emitterActiveTime = parseExpression(parser, componentValue);
        }
        //when emitter active time ends, it will sleep for time as defined here
        if (rawComponent.getValue().componentValues.containsKey("sleep_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("sleep_time");
            this.emitterSleepTime = parseExpression(parser, componentValue);
        }
    }
}
