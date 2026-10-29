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

public class EmitterLifetimeOnceComponent extends RiftLibEmitterLifetimeComponent {
    private IValue activeTime = new Constant(10);

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("active_time")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("active_time");
            this.activeTime = componentValue.asMolang().compile(parser);
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

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterLifetimeOnceComponent> {
        private final EmitterLifetimeOnceComponent toReturn = new EmitterLifetimeOnceComponent();

        @NotNull
        public Builder setActiveTime(@NotNull IValue value) {
            this.toReturn.activeTime = Objects.requireNonNull(value);
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

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_lifetime_once";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "active_time", Builder.this.toReturn.activeTime
                    );
                }
            };
        }

        @Override
        @NotNull
        public EmitterLifetimeOnceComponent build(@NotNull MolangParser parser) {
            EmitterLifetimeOnceComponent result = new EmitterLifetimeOnceComponent();
            result.activeTime = RawMolangValue.copy(this.toReturn.activeTime, parser);
            return result;
        }
    }
}
