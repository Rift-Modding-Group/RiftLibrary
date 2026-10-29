package anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class ParticleInitialSpeedComponent extends RiftLibParticleComponent {
    private IValue initialSpeed = MolangParser.ZERO;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("$value")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("$value");
            this.initialSpeed = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setInitialSpeed(this.initialSpeed);
    }

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleInitialSpeedComponent> {
        private final ParticleInitialSpeedComponent toReturn = new ParticleInitialSpeedComponent();

        @NotNull
        public Builder setInitialSpeed(@NotNull IValue value) {
            this.toReturn.initialSpeed = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setInitialSpeed(double value) {
            return this.setInitialSpeed(new Constant(value));
        }

        @NotNull
        public Builder setInitialSpeed(@NotNull String expression) {
            return this.setInitialSpeed(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setInitialSpeed(@NotNull RawMolangValue value) {
            return this.setInitialSpeed(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_initial_speed";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "$value", Builder.this.toReturn.initialSpeed
                    );
                }
            };
        }

        @Override
        @NotNull
        public ParticleInitialSpeedComponent build(@NotNull MolangParser parser) {
            ParticleInitialSpeedComponent result = new ParticleInitialSpeedComponent();
            result.initialSpeed = RawMolangValue.copy(this.toReturn.initialSpeed, parser);
            return result;
        }
    }
}
