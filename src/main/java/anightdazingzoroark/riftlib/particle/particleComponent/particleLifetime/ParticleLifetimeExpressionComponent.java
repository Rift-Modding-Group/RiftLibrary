package anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime;

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

public class ParticleLifetimeExpressionComponent extends RiftLibParticleComponent {
    //time until particle expires
    public IValue expirationValue = MolangParser.ZERO;
    //condition in which a particle expires
    public IValue lifetimeValue = new Constant(Integer.MAX_VALUE);

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        //this expression makes the particle expire when true (non-zero)
        //evaluated once per particle
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
            this.expirationValue = componentValue.asMolang().compile(parser);
        }
        //particle will expire after this much time, evaluated once
        if (rawComponent.getValue().componentValues.containsKey("max_lifetime")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_lifetime");
            this.lifetimeValue = componentValue.asMolang().compile(parser);
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setLifetimeExpressions(this.lifetimeValue, this.expirationValue);
    }

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleLifetimeExpressionComponent> {
        private final ParticleLifetimeExpressionComponent toReturn = new ParticleLifetimeExpressionComponent();

        @NotNull
        public Builder setExpirationExpression(@NotNull IValue value) {
            this.toReturn.expirationValue = Objects.requireNonNull(value);
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

        @NotNull
        public Builder setMaxLifetime(@NotNull IValue value) {
            this.toReturn.lifetimeValue = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setMaxLifetime(double value) {
            return this.setMaxLifetime(new Constant(value));
        }

        @NotNull
        public Builder setMaxLifetime(@NotNull String expression) {
            return this.setMaxLifetime(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setMaxLifetime(@NotNull RawMolangValue value) {
            return this.setMaxLifetime(value.compile(new MolangParser()));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_lifetime_expression";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "expiration_expression", Builder.this.toReturn.expirationValue,
                            "max_lifetime", Builder.this.toReturn.lifetimeValue
                    );
                }
            };
        }

        @Override
        @NotNull
        public ParticleLifetimeExpressionComponent build(@NotNull MolangParser parser) {
            ParticleLifetimeExpressionComponent result = new ParticleLifetimeExpressionComponent();
            result.expirationValue = RawMolangValue.copy(this.toReturn.expirationValue, parser);
            result.lifetimeValue = RawMolangValue.copy(this.toReturn.lifetimeValue, parser);
            return result;
        }
    }
}
