package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

public abstract class RiftLibEmitterRateComponent extends RiftLibEmitterComponent {

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.emitterRate = this;
    }
}
