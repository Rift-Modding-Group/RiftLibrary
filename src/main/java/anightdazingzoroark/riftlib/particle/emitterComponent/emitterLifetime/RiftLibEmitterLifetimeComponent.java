package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

public abstract class RiftLibEmitterLifetimeComponent extends RiftLibEmitterComponent {
    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterLifetime = this;
    }
}
