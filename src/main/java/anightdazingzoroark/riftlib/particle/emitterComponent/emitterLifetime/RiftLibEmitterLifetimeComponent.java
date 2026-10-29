package anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime;

import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import org.jetbrains.annotations.NotNull;

public abstract class RiftLibEmitterLifetimeComponent extends RiftLibEmitterComponent {
    @Override
    public void applyComponent(@NotNull RiftLibParticleEmitter emitter) {
        emitter.setEmitterLifetime(this);
    }

    public abstract boolean canCreateParticles(RiftLibParticleEmitter emitter);

    public abstract boolean canExpire(RiftLibParticleEmitter emitter);
}
