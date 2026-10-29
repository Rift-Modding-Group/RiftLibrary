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

public class EmitterLifetimeExpressionComponent extends RiftLibEmitterLifetimeComponent {
    //condition in which emitter activates
    private IValue emitterActivationValue = MolangParser.ONE;
    //condition in which emitter expires
    private IValue emitterExpirationValue = MolangParser.ZERO;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        //when the expression is non-zero, the emitter will emit particles.
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("activation_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("activation_expression");
            this.emitterActivationValue = componentValue.asMolang().compile(parser);
        }
        //will expire if the expression is non-zero.
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
            this.emitterExpirationValue = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public boolean canCreateParticles(RiftLibParticleEmitter emitter) {
        return this.emitterActivationValue.get() != 0;
    }

    @Override
    public boolean canExpire(RiftLibParticleEmitter emitter) {
        return this.emitterExpirationValue.get() != 0;
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterLifetimeExpressionComponent> {
        private final EmitterLifetimeExpressionComponent toReturn = new EmitterLifetimeExpressionComponent();

        @NotNull
        public Builder setActivationExpression(@NotNull IValue value) {
            this.toReturn.emitterActivationValue = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setActivationExpression(double value) {
            return this.setActivationExpression(new Constant(value));
        }

        @NotNull
        public Builder setActivationExpression(@NotNull String expression) {
            return this.setActivationExpression(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setActivationExpression(@NotNull RawMolangValue value) {
            return this.setActivationExpression(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setExpirationExpression(@NotNull IValue value) {
            this.toReturn.emitterExpirationValue = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setExpirationExpression(double value) {
            return this.setExpirationExpression(new Constant(value));
        }

        @NotNull
        public Builder setExpirationExpression(@NotNull String expression) {
            return this.setExpirationExpression(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setExpirationExpression(@NotNull RawMolangValue value) {
            return this.setExpirationExpression(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_lifetime_expression";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "activation_expression", Builder.this.toReturn.emitterActivationValue,
                            "expiration_expression", Builder.this.toReturn.emitterExpirationValue
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.emitterActivationValue = this.toReturn.emitterActivationValue;
            result.toReturn.emitterExpirationValue = this.toReturn.emitterExpirationValue;
            return result;
        }

        @Override
        @NotNull
        public EmitterLifetimeExpressionComponent build(@NotNull MolangParser parser) {
            EmitterLifetimeExpressionComponent result = new EmitterLifetimeExpressionComponent();
            result.emitterActivationValue = RawMolangValue.copy(this.toReturn.emitterActivationValue, parser);
            result.emitterExpirationValue = RawMolangValue.copy(this.toReturn.emitterExpirationValue, parser);
            return result;
        }
    }
}
