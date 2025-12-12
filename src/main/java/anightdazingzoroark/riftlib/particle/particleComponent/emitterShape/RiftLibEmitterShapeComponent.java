package anightdazingzoroark.riftlib.particle.particleComponent.emitterShape;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

public abstract class RiftLibEmitterShapeComponent extends RiftLibParticleComponent {
    public IValue[] offset = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterShape = this;
    }
}
