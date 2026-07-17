package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeOnceComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape.*;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceLightingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState.EmitterInitializationComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.emitterComponent.emitterLifetime.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpinComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleExpireInBlockComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleExpireNotInBlockComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleLifetime.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionCollisionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RiftLibParticleComponentRegistry {
    private static final Map<String, Supplier<RiftLibEmitterComponent>> emitterComponentMap = new HashMap<>();
    private static final Map<String, Supplier<RiftLibParticleComponent>> particleComponentMap = new HashMap<>();

    public static void initializeMap() {
        //-----initialize the emitter component map-----
        emitterComponentMap.put("minecraft:emitter_rate_instant", EmitterInstantComponent::new);
        emitterComponentMap.put("minecraft:emitter_rate_steady", EmitterSteadyComponent::new);

        emitterComponentMap.put("minecraft:emitter_shape_sphere", EmitterShapeSphereComponent::new);
        emitterComponentMap.put("minecraft:emitter_shape_box", EmitterShapeBoxComponent::new);
        emitterComponentMap.put("minecraft:emitter_shape_point", EmitterShapePointComponent::new);
        emitterComponentMap.put("minecraft:emitter_shape_disc", EmitterShapeDiscComponent::new);
        emitterComponentMap.put("minecraft:emitter_shape_custom", EmitterShapeCustomComponent::new);

        emitterComponentMap.put("minecraft:emitter_initialization", EmitterInitializationComponent::new);

        emitterComponentMap.put("minecraft:emitter_lifetime_expression", EmitterLifetimeExpressionComponent::new);
        emitterComponentMap.put("minecraft:emitter_lifetime_looping", EmitterLifetimeLoopingComponent::new);
        emitterComponentMap.put("minecraft:emitter_lifetime_once", EmitterLifetimeOnceComponent::new);

        //-----initialize the particle component map-----
        particleComponentMap.put("minecraft:particle_appearance_billboard", AppearanceBillboardComponent::new);
        particleComponentMap.put("minecraft:particle_appearance_tinting", AppearanceTintingComponent::new);
        particleComponentMap.put("minecraft:particle_appearance_lighting", AppearanceLightingComponent::new);

        particleComponentMap.put("minecraft:particle_lifetime_expression", ParticleLifetimeExpressionComponent::new);
        particleComponentMap.put("minecraft:particle_expire_if_in_blocks", ParticleExpireInBlockComponent::new);
        particleComponentMap.put("minecraft:particle_expire_if_not_in_blocks", ParticleExpireNotInBlockComponent::new);

        particleComponentMap.put("minecraft:particle_initial_speed", ParticleInitialSpeedComponent::new);
        particleComponentMap.put("minecraft:particle_initial_spin", ParticleInitialSpinComponent::new);

        particleComponentMap.put("minecraft:particle_motion_dynamic", ParticleMotionDynamicComponent::new);
        particleComponentMap.put("minecraft:particle_motion_collision", ParticleMotionCollisionComponent::new);
    }

    public static boolean isEmitterComponent(String id) {
        for (String emitterComponent : emitterComponentMap.keySet()) {
            if (emitterComponent.equals(id)) return true;
        }
        return false;
    }

    public static RiftLibEmitterComponent createEmitterComponent(String id) {
        try {
            return emitterComponentMap.get(id).get();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid emitter component "+id+" provided!");
        }
    }

    public static boolean isParticleComponent(String id) {
        for (String emitterComponent : particleComponentMap.keySet()) {
            if (emitterComponent.equals(id)) return true;
        }
        return false;
    }

    public static RiftLibParticleComponent createParticleComponent(String id) {
        try {
            return particleComponentMap.get(id).get();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid particle component "+id+" provided!");
        }
    }
}
