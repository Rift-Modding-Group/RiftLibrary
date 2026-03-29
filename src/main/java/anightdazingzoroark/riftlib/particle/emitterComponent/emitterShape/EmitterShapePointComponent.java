package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class EmitterShapePointComponent extends RiftLibEmitterShapeComponent {
    public IValue[] particleDirection = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("direction")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
            this.particleDirection = parseExpressionArray(parser, 3, componentValue);
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
}
