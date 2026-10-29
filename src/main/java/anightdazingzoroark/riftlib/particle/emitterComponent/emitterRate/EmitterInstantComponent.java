package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

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

public class EmitterInstantComponent extends RiftLibEmitterRateComponent {
    public IValue particleCount = new Constant(10f);

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("num_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("num_particles");
            this.particleCount = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void createParticles(RiftLibParticleEmitter emitter) {
        double particleCount = this.particleCount.get();
        while (emitter.getParticleCount() < particleCount) {
            emitter.addParticle(emitter.createParticle());
            emitter.setParticleCount(emitter.getParticleCount() + 1);
        }
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterInstantComponent> {
        private final EmitterInstantComponent toReturn = new EmitterInstantComponent();

        @NotNull
        public Builder setParticleCount(@NotNull IValue value) {
            this.toReturn.particleCount = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setParticleCount(double value) {
            return this.setParticleCount(new Constant(value));
        }

        @NotNull
        public Builder setParticleCount(@NotNull String expression) {
            return this.setParticleCount(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setParticleCount(@NotNull RawMolangValue value) {
            return this.setParticleCount(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_rate_instant";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "num_particles", Builder.this.toReturn.particleCount
                    );
                }
            };
        }

        @Override
        @NotNull
        public EmitterInstantComponent build(@NotNull MolangParser parser) {
            EmitterInstantComponent result = new EmitterInstantComponent();
            result.particleCount = RawMolangValue.copy(this.toReturn.particleCount, parser);
            return result;
        }
    }
}