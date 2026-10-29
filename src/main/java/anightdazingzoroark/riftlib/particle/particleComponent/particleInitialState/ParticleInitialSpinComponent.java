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

public class ParticleInitialSpinComponent extends RiftLibParticleComponent {
    private IValue initialRotation = MolangParser.ZERO;
    private IValue rotationRate = MolangParser.ZERO;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("rotation")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation");
            this.initialRotation = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_rate")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_rate");
            this.rotationRate = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setInitialSpin(this.initialRotation, this.rotationRate);
    }

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleInitialSpinComponent> {
        private final ParticleInitialSpinComponent toReturn = new ParticleInitialSpinComponent();

        @NotNull
        public Builder setInitialRotation(@NotNull IValue value) {
            this.toReturn.initialRotation = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setInitialRotation(double value) {
            return this.setInitialRotation(new Constant(value));
        }

        @NotNull
        public Builder setInitialRotation(@NotNull String expression) {
            return this.setInitialRotation(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setInitialRotation(@NotNull RawMolangValue value) {
            return this.setInitialRotation(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setRotationRate(@NotNull IValue value) {
            this.toReturn.rotationRate = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setRotationRate(double value) {
            return this.setRotationRate(new Constant(value));
        }

        @NotNull
        public Builder setRotationRate(@NotNull String expression) {
            return this.setRotationRate(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setRotationRate(@NotNull RawMolangValue value) {
            return this.setRotationRate(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_initial_spin";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "rotation", Builder.this.toReturn.initialRotation,
                            "rotation_rate", Builder.this.toReturn.rotationRate
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.initialRotation = this.toReturn.initialRotation;
            result.toReturn.rotationRate = this.toReturn.rotationRate;
            return result;
        }

        @Override
        @NotNull
        public ParticleInitialSpinComponent build(@NotNull MolangParser parser) {
            ParticleInitialSpinComponent result = new ParticleInitialSpinComponent();
            result.initialRotation = RawMolangValue.copy(this.toReturn.initialRotation, parser);
            result.rotationRate = RawMolangValue.copy(this.toReturn.rotationRate, parser);
            return result;
        }
    }
}
