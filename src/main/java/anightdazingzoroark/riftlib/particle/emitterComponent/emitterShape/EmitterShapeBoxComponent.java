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

public class EmitterShapeBoxComponent extends RiftLibEmitterShapeComponent {
    public boolean surfaceOnly;
    public IValue[] halfDimensions = new IValue[]{MolangParser.ONE, MolangParser.ONE, MolangParser.ONE};
    public String particleDirection = "outwards";
    public IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
        }
        if (rawComponent.getValue().componentValues.containsKey("half_dimensions")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("half_dimensions");
            this.halfDimensions = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
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
        //in cubes, |x|, |y|, and |z| are less than or equal to associated dimension length
        double randomX = emitter.nextRandomDouble() * this.halfDimensions[0].get() * 2 - this.halfDimensions[0].get();
        double randomY = emitter.nextRandomDouble() * this.halfDimensions[1].get() * 2 - this.halfDimensions[1].get();
        double randomZ = emitter.nextRandomDouble() * this.halfDimensions[2].get() * 2 - this.halfDimensions[2].get();

        if (this.surfaceOnly) {
            int face = emitter.nextRandomInt(6);

            return switch (face) {
                //positive x
                case 0 -> new Vec3d(
                        this.halfDimensions[0].get() + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //negative x
                case 1 -> new Vec3d(
                        -this.halfDimensions[0].get() + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //positive y
                case 2 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        this.halfDimensions[1].get() + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //negative y
                case 3 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        -this.halfDimensions[1].get() + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //positive z
                case 4 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        this.halfDimensions[2].get() + this.offset[2].get()
                );
                //negative z
                case 5 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        -this.halfDimensions[2].get() + this.offset[2].get()
                );
                default -> Vec3d.ZERO;
            };
        } else return new Vec3d(
                randomX + this.offset[0].get(),
                randomY + this.offset[1].get(),
                randomZ + this.offset[2].get()
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

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterShapeBoxComponent> {
        private final EmitterShapeBoxComponent toReturn = new EmitterShapeBoxComponent();

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
        public Builder setHalfDimensions(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.halfDimensions = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
            return this;
        }

        @NotNull
        public Builder setHalfDimensions(double x, double y, double z) {
            return this.setHalfDimensions(new Constant(x), new Constant(y), new Constant(z));
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
                    return "minecraft:emitter_shape_box";
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
                            "half_dimensions", Builder.this.toReturn.halfDimensions,
                            "surface_only", Builder.this.toReturn.surfaceOnly,
                            "direction", direction
                    );
                }
            };
        }

        @Override
        @NotNull
        public EmitterShapeBoxComponent build(@NotNull MolangParser parser) {
            EmitterShapeBoxComponent result = new EmitterShapeBoxComponent();
            result.offset = RawMolangValue.copy(this.toReturn.offset, parser);
            result.halfDimensions = RawMolangValue.copy(this.toReturn.halfDimensions, parser);
            result.surfaceOnly = this.toReturn.surfaceOnly;
            result.particleDirection = this.toReturn.particleDirection;
            result.customParticleDirection = RawMolangValue.copy(this.toReturn.customParticleDirection, parser);
            return result;
        }
    }
}
