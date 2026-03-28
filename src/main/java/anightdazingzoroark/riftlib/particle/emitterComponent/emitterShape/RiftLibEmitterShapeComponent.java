package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.util.math.Vec3d;

public abstract class RiftLibEmitterShapeComponent extends RiftLibEmitterComponent {
    protected IValue[] offset = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterShape = this;
    }

    public abstract Vec3d defineParticleOffset(RiftLibParticleEmitter emitter);

    public abstract Vec3d defineDirection(RiftLibParticleEmitter emitter, double emissionX, double emissionY, double emissionZ);
}
