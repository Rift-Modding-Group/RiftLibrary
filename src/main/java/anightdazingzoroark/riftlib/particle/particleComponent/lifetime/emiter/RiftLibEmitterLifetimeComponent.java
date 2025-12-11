package anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

public abstract class RiftLibEmitterLifetimeComponent extends RiftLibParticleComponent {
    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterLifetime = this;
    }
}
