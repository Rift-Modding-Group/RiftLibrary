package anightdazingzoroark.riftlib.particle.particleComponent;

import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.EmitterLifetimeComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.ParticleLifetimeComponent;

import java.util.HashMap;
import java.util.Map;

public class RiftLibParticleComponentRegistry {
    public static final Map<String, RiftLibParticleComponent> componentMap = new HashMap<>();

    public static void initializeMap() {
        componentMap.put("minecraft:particle_appearance_billboard", new ParticleAppearanceBillboardComponent());
        componentMap.put("minecraft:emitter_lifetime_expression", new EmitterLifetimeComponent());
        componentMap.put("minecraft:particle_lifetime_expression", new ParticleLifetimeComponent());
        componentMap.put("minecraft:emitter_rate_instant", new EmitterInstantComponent());
        componentMap.put("minecraft:emitter_shape_sphere", new EmitterShapeSphereComponent());
    }
}
