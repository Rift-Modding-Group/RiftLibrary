package anightdazingzoroark.riftlib.particle.particleComponent.particleMotion;

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

public class ParticleMotionDynamicComponent extends RiftLibParticleComponent {
    private IValue[] linearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    private IValue linearDragCoefficient = MolangParser.ZERO;
    private IValue rotationAcceleration = MolangParser.ZERO;
    private IValue rotationDragCoefficient = MolangParser.ZERO;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("linear_acceleration")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("linear_acceleration");
            this.linearAcceleration = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
        }
        if (rawComponent.getValue().componentValues.containsKey("linear_drag_coefficient")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("linear_drag_coefficient");
            this.linearDragCoefficient = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_acceleration")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_acceleration");
            this.rotationAcceleration = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_drag_coefficient")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_drag_coefficient");
            this.rotationDragCoefficient = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setDynamicMotion(
                this.linearAcceleration,
                this.linearDragCoefficient,
                this.rotationAcceleration,
                this.rotationDragCoefficient
        );
    }

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleMotionDynamicComponent> {
        private final ParticleMotionDynamicComponent toReturn = new ParticleMotionDynamicComponent();

        @NotNull
        public Builder setLinearAcceleration(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.linearAcceleration = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
            return this;
        }

        @NotNull
        public Builder setLinearAcceleration(double x, double y, double z) {
            return this.setLinearAcceleration(new Constant(x), new Constant(y), new Constant(z));
        }

        @NotNull
        public Builder setLinearDragCoefficient(@NotNull IValue value) {
            this.toReturn.linearDragCoefficient = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setLinearDragCoefficient(double value) {
            return this.setLinearDragCoefficient(new Constant(value));
        }

        @NotNull
        public Builder setLinearDragCoefficient(@NotNull String expression) {
            return this.setLinearDragCoefficient(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setLinearDragCoefficient(@NotNull RawMolangValue value) {
            return this.setLinearDragCoefficient(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setRotationAcceleration(@NotNull IValue value) {
            this.toReturn.rotationAcceleration = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setRotationAcceleration(double value) {
            return this.setRotationAcceleration(new Constant(value));
        }

        @NotNull
        public Builder setRotationAcceleration(@NotNull String expression) {
            return this.setRotationAcceleration(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setRotationAcceleration(@NotNull RawMolangValue value) {
            return this.setRotationAcceleration(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setRotationDragCoefficient(@NotNull IValue value) {
            this.toReturn.rotationDragCoefficient = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setRotationDragCoefficient(double value) {
            return this.setRotationDragCoefficient(new Constant(value));
        }

        @NotNull
        public Builder setRotationDragCoefficient(@NotNull String expression) {
            return this.setRotationDragCoefficient(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setRotationDragCoefficient(@NotNull RawMolangValue value) {
            return this.setRotationDragCoefficient(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_motion_dynamic";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "linear_acceleration", Builder.this.toReturn.linearAcceleration,
                            "linear_drag_coefficient", Builder.this.toReturn.linearDragCoefficient,
                            "rotation_acceleration", Builder.this.toReturn.rotationAcceleration,
                            "rotation_drag_coefficient", Builder.this.toReturn.rotationDragCoefficient
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.linearAcceleration = this.toReturn.linearAcceleration.clone();
            result.toReturn.linearDragCoefficient = this.toReturn.linearDragCoefficient;
            result.toReturn.rotationAcceleration = this.toReturn.rotationAcceleration;
            result.toReturn.rotationDragCoefficient = this.toReturn.rotationDragCoefficient;
            return result;
        }

        @Override
        @NotNull
        public ParticleMotionDynamicComponent build(@NotNull MolangParser parser) {
            ParticleMotionDynamicComponent result = new ParticleMotionDynamicComponent();
            result.linearAcceleration = RawMolangValue.copy(this.toReturn.linearAcceleration, parser);
            result.linearDragCoefficient = RawMolangValue.copy(this.toReturn.linearDragCoefficient, parser);
            result.rotationAcceleration = RawMolangValue.copy(this.toReturn.rotationAcceleration, parser);
            result.rotationDragCoefficient = RawMolangValue.copy(this.toReturn.rotationDragCoefficient, parser);
            return result;
        }
    }
}
