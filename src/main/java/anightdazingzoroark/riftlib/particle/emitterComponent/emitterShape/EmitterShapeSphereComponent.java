package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class EmitterShapeSphereComponent extends RiftLibEmitterShapeComponent {
    private IValue radius = MolangParser.ONE;
    private String particleDirection = "outwards";
    private boolean surfaceOnly;
    private IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
        }
        if (rawComponent.getValue().componentValues.containsKey("radius")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("radius");
            this.radius = componentValue.asMolang().compile(parser);
        }
        if (rawComponent.getValue().componentValues.containsKey("surface_only")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("surface_only");
            this.surfaceOnly = componentValue.asBoolean();
        }
        if (rawComponent.getValue().componentValues.containsKey("direction")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
            //direction as string
            if (componentValue.isString()) {
                if (componentValue.asString().equals("inwards") || componentValue.asString().equals("outwards")) {
                    this.particleDirection = componentValue.asString();
                } else
                    throw new IllegalArgumentException("Invalid value " + componentValue.asString() + " for direction!");
            }
            //custom direction
            else if (componentValue.isArray()) {
                this.customParticleDirection = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
            }
        }
    }

    @Override
    public Vec3d defineParticleOffset(RiftLibParticleEmitter emitter) {
        double radius = this.surfaceOnly ? this.radius.get() : (2 * emitter.nextRandomDouble() - 1) * this.radius.get();
        double offsetY = (2 * emitter.nextRandomDouble() - 1) * radius;
        double radiusAtY = Math.sqrt(radius * radius - offsetY * offsetY);
        double theta = 2 * Math.PI * emitter.nextRandomDouble();
        double offsetX = radiusAtY * Math.cos(theta);
        double offsetZ = radiusAtY * Math.sin(theta);
        return new Vec3d(
                offsetX + this.offset[0].get(),
                offsetY + this.offset[1].get(),
                offsetZ + this.offset[2].get()
        );
    }

    @Override
    public Vec3d defineDirection(RiftLibParticleEmitter emitter, double emissionX, double emissionY, double emissionZ) {
        //if it has custom particle direction, just return it instead
        if (this.customParticleDirection != null) {
            return new Vec3d(
                    this.customParticleDirection[0].get(),
                    this.customParticleDirection[1].get(),
                    this.customParticleDirection[2].get()
            ).normalize();
        } else {
            //generally the direction to go towards should be the same as its xyz offset from the center of sphere emitter
            int pointer = this.particleDirection.equals("outwards") ? 1 : this.particleDirection.equals("inwards") ? -1 : 0;
            return new Vec3d(
                    pointer * (emissionX - emitter.getX()),
                    pointer * (emissionY - emitter.getY()),
                    pointer * (emissionZ - emitter.getZ())
            ).normalize();
        }
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterShapeSphereComponent> {
        private final EmitterShapeSphereComponent toReturn = new EmitterShapeSphereComponent();

        @NotNull
        public Builder setOffset(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.offset = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
            return this;
        }

        @NotNull
        public Builder setOffset(double x, double y, double z) {
            return this.setOffset(new Constant(x), new Constant(y), new Constant(z));
        }

        @NotNull
        public Builder setRadius(@NotNull IValue value) {
            this.toReturn.radius = Objects.requireNonNull(value);
            return this;
        }

        @NotNull
        public Builder setRadius(double value) {
            return this.setRadius(new Constant(value));
        }

        @NotNull
        public Builder setRadius(@NotNull String expression) {
            return this.setRadius(RawMolangValue.compileExpression(expression));
        }

        @NotNull
        public Builder setRadius(@NotNull RawMolangValue value) {
            return this.setRadius(value.compile(new MolangParser()));
        }

        @NotNull
        public Builder setSurfaceOnly(boolean value) {
            this.toReturn.surfaceOnly = value;
            return this;
        }

        @NotNull
        public Builder setDirection(@NotNull String value) {
            if (!value.equals("inwards") && !value.equals("outwards")) {
                throw new IllegalArgumentException("Direction must be inwards or outwards");
            }
            this.toReturn.particleDirection = value;
            this.toReturn.customParticleDirection = null;
            return this;
        }

        @NotNull
        public Builder setDirection(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.customParticleDirection = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
            this.toReturn.particleDirection = "outwards";
            return this;
        }

        @NotNull
        public Builder setDirection(double x, double y, double z) {
            return this.setDirection(new Constant(x), new Constant(y), new Constant(z));
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_shape_sphere";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    Object direction = Builder.this.toReturn.particleDirection;
                    if (Builder.this.toReturn.customParticleDirection != null) {
                        direction = Builder.this.toReturn.customParticleDirection;
                    }
                    return Map.of(
                            "offset", Builder.this.toReturn.offset,
                            "radius", Builder.this.toReturn.radius,
                            "surface_only", Builder.this.toReturn.surfaceOnly,
                            "direction", direction
                    );
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.offset = this.toReturn.offset.clone();
            result.toReturn.radius = this.toReturn.radius;
            result.toReturn.surfaceOnly = this.toReturn.surfaceOnly;
            result.toReturn.particleDirection = this.toReturn.particleDirection;
            result.toReturn.customParticleDirection = this.toReturn.customParticleDirection == null ? null : this.toReturn.customParticleDirection.clone();
            return result;
        }

        @Override
        @NotNull
        public EmitterShapeSphereComponent build(@NotNull MolangParser parser) {
            EmitterShapeSphereComponent result = new EmitterShapeSphereComponent();
            result.offset = RawMolangValue.copy(this.toReturn.offset, parser);
            result.radius = RawMolangValue.copy(this.toReturn.radius, parser);
            result.surfaceOnly = this.toReturn.surfaceOnly;
            result.particleDirection = this.toReturn.particleDirection;
            result.customParticleDirection = RawMolangValue.copy(this.toReturn.customParticleDirection, parser);
            return result;
        }
    }
}
