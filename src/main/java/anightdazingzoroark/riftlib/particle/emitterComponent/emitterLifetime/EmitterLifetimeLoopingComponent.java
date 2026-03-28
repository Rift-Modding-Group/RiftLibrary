package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;

import java.util.Map;

public class EmitterLifetimeLoopingComponent extends RiftLibEmitterLifetimeComponent {
    private IValue emitterActiveTime = new Constant(10);
    private IValue emitterSleepTime = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        //amount of time the looping emitter will be active
        if (rawComponent.getValue().componentValues.containsKey("active_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("active_time");
            this.emitterActiveTime = this.parseExpression(parser, componentValue);
        }
        //when emitter active time ends, it will sleep for time as defined here
        if (rawComponent.getValue().componentValues.containsKey("sleep_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("sleep_time");
            this.emitterSleepTime = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public boolean canCreateParticles(RiftLibParticleEmitter emitter) {
        double activeTimeValue = this.emitterActiveTime.get();
        double sleepTimeValue = this.emitterSleepTime.get();

        double totalTimeValue = activeTimeValue + sleepTimeValue;
        double sleepTimePercent = activeTimeValue / totalTimeValue;
        double currentTimePercent = (emitter.getAge() % totalTimeValue) / totalTimeValue;

        return currentTimePercent <= sleepTimePercent;
    }

    @Override
    public boolean canExpire(RiftLibParticleEmitter emitter) {
        return false;
    }
}
