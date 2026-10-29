package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class EmitterLifetimeLoopingComponent extends RiftLibEmitterLifetimeComponent {
    private IValue emitterActiveTime = new Constant(10);
    private IValue emitterSleepTime = MolangParser.ZERO;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        //amount of time the looping emitter will be active
        if (rawComponent.getValue().componentValues.containsKey("active_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("active_time");
            this.emitterActiveTime = componentValue.asMolang().compile(parser);
        }
        //when emitter active time ends, it will sleep for time as defined here
        if (rawComponent.getValue().componentValues.containsKey("sleep_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("sleep_time");
            this.emitterSleepTime = componentValue.asMolang().compile(parser);
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

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterLifetimeLoopingComponent> {
        private final EmitterLifetimeLoopingComponent toReturn = new EmitterLifetimeLoopingComponent();

        @NotNull
        public Builder setActiveTime(@NotNull IValue value) {
            this.toReturn.emitterActiveTime = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setActiveTime(double value) {
            return this.setActiveTime(new Constant(value));
        }

        @NotNull
        public Builder setActiveTime(@NotNull String expression) {
            return this.setActiveTime(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setActiveTime(@NotNull RawMolangValue value) {
            return this.setActiveTime(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setSleepTime(@NotNull IValue value) {
            this.toReturn.emitterSleepTime = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setSleepTime(double value) {
            return this.setSleepTime(new Constant(value));
        }

        @NotNull
        public Builder setSleepTime(@NotNull String expression) {
            return this.setSleepTime(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setSleepTime(@NotNull RawMolangValue value) {
            return this.setSleepTime(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_lifetime_looping";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "active_time", Builder.this.toReturn.emitterActiveTime,
                            "sleep_time", Builder.this.toReturn.emitterSleepTime
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.emitterActiveTime = this.toReturn.emitterActiveTime;
            result.toReturn.emitterSleepTime = this.toReturn.emitterSleepTime;
            return result;
        }

        @Override
        @NotNull
        public EmitterLifetimeLoopingComponent build(@NotNull MolangParser parser) {
            EmitterLifetimeLoopingComponent result = new EmitterLifetimeLoopingComponent();
            result.emitterActiveTime = RawMolangValue.copy(this.toReturn.emitterActiveTime, parser);
            result.emitterSleepTime = RawMolangValue.copy(this.toReturn.emitterSleepTime, parser);
            return result;
        }
    }
}