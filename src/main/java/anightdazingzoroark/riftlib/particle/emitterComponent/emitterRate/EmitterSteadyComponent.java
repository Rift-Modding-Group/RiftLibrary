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

public class EmitterSteadyComponent extends RiftLibEmitterRateComponent {
    public IValue spawnRate = MolangParser.ONE;
    public IValue maxParticleCount = new Constant(50);

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("spawn_rate")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("spawn_rate");
            this.spawnRate = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("max_particles")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_particles");
            this.maxParticleCount = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void createParticles(RiftLibParticleEmitter emitter) {
        int maxParticleCount = (int) this.maxParticleCount.get();
        //turn particles per second into particles per tick
        double particleRate = this.spawnRate.get() / 20D;

        emitter.setParticleCount(emitter.getParticleCount() + particleRate);
        while (emitter.getParticleCount() >= 1 && emitter.getParticleAmount() < maxParticleCount) {
            emitter.addParticle(emitter.createParticle());
            emitter.setParticleCount(emitter.getParticleCount() - 1);
        }

        if (emitter.getParticleAmount() >= maxParticleCount) {
            emitter.setParticleCount(Math.min(emitter.getParticleCount(), 1));
        }
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterSteadyComponent> {
        private final EmitterSteadyComponent toReturn = new EmitterSteadyComponent();

        @NotNull
        public Builder setSpawnRate(@NotNull IValue value) {
            this.toReturn.spawnRate = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setSpawnRate(double value) {
            return this.setSpawnRate(new Constant(value));
        }

        @NotNull
        public Builder setSpawnRate(@NotNull String expression) {
            return this.setSpawnRate(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setSpawnRate(@NotNull RawMolangValue value) {
            return this.setSpawnRate(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setMaxParticleCount(@NotNull IValue value) {
            this.toReturn.maxParticleCount = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setMaxParticleCount(double value) {
            return this.setMaxParticleCount(new Constant(value));
        }

        @NotNull
        public Builder setMaxParticleCount(@NotNull String expression) {
            return this.setMaxParticleCount(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setMaxParticleCount(@NotNull RawMolangValue value) {
            return this.setMaxParticleCount(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_rate_steady";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "spawn_rate", Builder.this.toReturn.spawnRate,
                            "max_particles", Builder.this.toReturn.maxParticleCount
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.spawnRate = this.toReturn.spawnRate;
            result.toReturn.maxParticleCount = this.toReturn.maxParticleCount;
            return result;
        }

        @Override
        @NotNull
        public EmitterSteadyComponent build(@NotNull MolangParser parser) {
            EmitterSteadyComponent result = new EmitterSteadyComponent();
            result.spawnRate = RawMolangValue.copy(this.toReturn.spawnRate, parser);
            result.maxParticleCount = RawMolangValue.copy(this.toReturn.maxParticleCount, parser);
            return result;
        }
    }
}
