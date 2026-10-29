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

public class EmitterShapePointComponent extends RiftLibEmitterShapeComponent {
    public IValue[] particleDirection = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
        }
        if (rawComponent.getValue().componentValues.containsKey("direction")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
            this.particleDirection = RawMolangValue.compileArray(parser, componentValue.asMolangArray(3));
        }
    }

    @Override
    public Vec3d defineParticleOffset(RiftLibParticleEmitter emitter) {
        return new Vec3d(
                this.offset[0].get(),
                this.offset[1].get(),
                this.offset[2].get()
        );
    }

    @Override
    public Vec3d defineDirection(RiftLibParticleEmitter emitter, double emissionX, double emissionY, double emissionZ) {
        return new Vec3d(
                this.particleDirection[0].get(),
                this.particleDirection[1].get(),
                this.particleDirection[2].get()
        ).normalize();
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterShapePointComponent> {
        private final EmitterShapePointComponent toReturn = new EmitterShapePointComponent();

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
        public Builder setDirection(@NotNull IValue x, @NotNull IValue y, @NotNull IValue z) {
            this.toReturn.particleDirection = new IValue[]{Objects.requireNonNull(x), Objects.requireNonNull(y), Objects.requireNonNull(z)};
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
                    return "minecraft:emitter_shape_point";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of(
                            "offset", Builder.this.toReturn.offset,
                            "direction", Builder.this.toReturn.particleDirection
                    );
                }
            };
        }

        @Override
        @NotNull
        public EmitterShapePointComponent build(@NotNull MolangParser parser) {
            EmitterShapePointComponent result = new EmitterShapePointComponent();
            result.offset = RawMolangValue.copy(this.toReturn.offset, parser);
            result.particleDirection = RawMolangValue.copy(this.toReturn.particleDirection, parser);
            return result;
        }
    }
}
