package anightdazingzoroark.riftlib.particle.particleComponent.particleMotion;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ParticleMotionCollisionComponent extends RiftLibParticleComponent {
    private IValue enabled = MolangParser.ONE;
    private float collisionDrag;
    private float coeffOfRestitution;
    private Float collisionRadius;
    private boolean expireOnContact;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("enabled")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("enabled");
            if (componentValue instanceof RawParticleComponent.BooleanValue(boolean value)) {
                this.enabled = value ? MolangParser.ONE : MolangParser.ZERO;
            } else this.enabled = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("collision_drag")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("collision_drag");
            this.collisionDrag = (float) componentValue.asNumber();
        }
        if (rawComponent.getValue().componentValues.containsKey("coefficient_of_restitution")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("coefficient_of_restitution");
            this.coeffOfRestitution = (float) componentValue.asNumber();
        }
        if (rawComponent.getValue().componentValues.containsKey("collision_radius")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("collision_radius");
            this.collisionRadius = (float) componentValue.asNumber();
        }
        if (rawComponent.getValue().componentValues.containsKey("expire_on_contact")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expire_on_contact");
            this.expireOnContact = componentValue.asBoolean();
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.setCollision(
                this.enabled,
                this.collisionDrag,
                this.coeffOfRestitution,
                this.collisionRadius,
                this.expireOnContact
        );
    }

    public static class Builder extends RiftLibParticleComponent.Builder<ParticleMotionCollisionComponent> {
        private final ParticleMotionCollisionComponent toReturn = new ParticleMotionCollisionComponent();

        @NotNull
        public Builder setEnabled(@NotNull IValue value) {
            this.toReturn.enabled = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setEnabled(double value) {
            return this.setEnabled(new Constant(value));
        }

        @NotNull
        public Builder setEnabled(@NotNull String expression) {
            return this.setEnabled(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setEnabled(@NotNull RawMolangValue value) {
            return this.setEnabled(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setEnabled(boolean value) {
            return this.setEnabled(value ? MolangParser.ONE : MolangParser.ZERO);
        }

        @NotNull
        public Builder setCollisionDrag(float value) {
            if (value < 0F || Float.compare(value, Float.MAX_VALUE) > 0)
                throw new IllegalArgumentException("Value must be finite and nonnegative");
            this.toReturn.collisionDrag = value;
            return this;
        }

        @NotNull
        public Builder setCoeffOfRestitution(float value) {
            if (value < 0F || Float.compare(value, Float.MAX_VALUE) > 0)
                throw new IllegalArgumentException("Value must be finite and nonnegative");
            this.toReturn.coeffOfRestitution = value;
            return this;
        }

        @NotNull
        public Builder setCollisionRadius(float value) {
            if (value < 0F || Float.compare(value, Float.MAX_VALUE) > 0)
                throw new IllegalArgumentException("Value must be finite and nonnegative");
            this.toReturn.collisionRadius = value;
            return this;
        }

        @NotNull
        public Builder setExpireOnContact(boolean value) {
            this.toReturn.expireOnContact = value;
            return this;
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_motion_collision";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    Map<String, Object> fields = new LinkedHashMap<>();
                    fields.put("enabled", Builder.this.toReturn.enabled);
                    fields.put("collision_drag", Builder.this.toReturn.collisionDrag);
                    fields.put("coefficient_of_restitution", Builder.this.toReturn.coeffOfRestitution);
                    if (Builder.this.toReturn.collisionRadius != null)
                        fields.put("collision_radius", Builder.this.toReturn.collisionRadius);
                    fields.put("expire_on_contact", Builder.this.toReturn.expireOnContact);
                    return fields;
                }
            };
        }

        @Override
        @NotNull
        public ParticleMotionCollisionComponent build(@NotNull MolangParser parser) {
            ParticleMotionCollisionComponent result = new ParticleMotionCollisionComponent();
            result.enabled = RawMolangValue.copy(this.toReturn.enabled, parser);
            result.collisionDrag = this.toReturn.collisionDrag;
            result.coeffOfRestitution = this.toReturn.coeffOfRestitution;
            result.collisionRadius = this.toReturn.collisionRadius;
            result.expireOnContact = this.toReturn.expireOnContact;
            return result;
        }
    }
}
