package anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import org.jetbrains.annotations.NotNull;

public abstract class RiftLibEmitterRateComponent extends RiftLibEmitterComponent {
    @Override
    public void applyComponent(@NotNull RiftLibParticleEmitter emitter) {
        emitter.setEmitterRate(this);
    }

    public abstract void createParticles(RiftLibParticleEmitter emitter);
}
