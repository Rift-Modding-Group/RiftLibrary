package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.MolangException;
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

public class EmitterShapeDiscComponent extends RiftLibEmitterShapeComponent {
    private IValue[] planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
    private IValue radius = MolangParser.ZERO;
    private boolean surfaceOnly;
    private String particleDirection = "outwards";
    private IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("plane_normal")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("plane_normal");

            //assuming its a string, we have to choose between x, y, and z directions
            if (componentValue.isString()) {
                switch (componentValue.asString()) {
                    case "x":
                        this.planeNormal = new IValue[]{MolangParser.ONE, MolangParser.ZERO, MolangParser.ZERO};
                        break;
                    case "y":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
                        break;
                    case "z":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ONE};
                        break;
                    default:
                        throw new RuntimeException(new MolangException("Invalid value for plane direction!"));
                }
            } else if (componentValue.isArray()) {
                this.planeNormal = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
            }
        }
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
        //disc formula is x^2 + y^2 = r^2
        Vec3d vecNormal = new Vec3d(
                this.planeNormal[0].get(),
                this.planeNormal[1].get(),
                this.planeNormal[2].get()
        ).normalize();

        //orthonormal basis spanning the disc plane
        Vec3d helper = (Math.abs(vecNormal.y) < 1) ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        Vec3d vecX = vecNormal.crossProduct(helper).normalize();
        Vec3d vecY = vecNormal.crossProduct(vecX).normalize();

        double radius = this.surfaceOnly ? this.radius.get() : emitter.nextRandomDouble() * this.radius.get();
        double theta = 2 * Math.PI * emitter.nextRandomDouble();

        Vec3d inPlane = vecX.scale(radius * Math.cos(theta)).add(vecY.scale(radius * Math.sin(theta)));

        return new Vec3d(
                inPlane.x + this.offset[0].get(),
                inPlane.y + this.offset[1].get(),
                inPlane.z + this.offset[2].get()
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

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterShapeDiscComponent> {
        private final EmitterShapeDiscComponent toReturn = new EmitterShapeDiscComponent();

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
        public Builder setPlaneNormal(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.planeNormal = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
            return this;
        }

        @NotNull
        public Builder setPlaneNormal(double x, double y, double z) {
            return this.setPlaneNormal(new Constant(x), new Constant(y), new Constant(z));
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
                    return "minecraft:emitter_shape_disc";
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
                            "plane_normal", Builder.this.toReturn.planeNormal,
                            "radius", Builder.this.toReturn.radius,
                            "surface_only", Builder.this.toReturn.surfaceOnly,
                            "direction", direction
                    );
                }
            };
        }

        @Override
        @NotNull
        public EmitterShapeDiscComponent build(@NotNull MolangParser parser) {
            EmitterShapeDiscComponent result = new EmitterShapeDiscComponent();
            result.offset = RawMolangValue.copy(this.toReturn.offset, parser);
            result.planeNormal = RawMolangValue.copy(this.toReturn.planeNormal, parser);
            result.radius = RawMolangValue.copy(this.toReturn.radius, parser);
            result.surfaceOnly = this.toReturn.surfaceOnly;
            result.particleDirection = this.toReturn.particleDirection;
            result.customParticleDirection = RawMolangValue.copy(this.toReturn.customParticleDirection, parser);
            return result;
        }
    }
}
