package anightdazingzoroark.riftlib.particle.particleComponent.emitterRate;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

public abstract class RiftLibEmitterRateComponent extends RiftLibParticleComponent {

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterRate = this;
    }
}
