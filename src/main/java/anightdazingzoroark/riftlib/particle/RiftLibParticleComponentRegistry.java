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

public class RiftLibParticleComponentRegistry {
    private static final Map<String, Class<? extends RiftLibEmitterComponent>> emitterComponentMap = new HashMap<>();
    private static final Map<String, Class<? extends RiftLibParticleComponent>> particleComponentMap = new HashMap<>();

    public static void initializeMap() {
        //-----initialize the emitter component map-----
        emitterComponentMap.put("minecraft:emitter_rate_instant", EmitterInstantComponent.class);
        emitterComponentMap.put("minecraft:emitter_rate_steady", EmitterSteadyComponent.class);

        emitterComponentMap.put("minecraft:emitter_shape_sphere", EmitterShapeSphereComponent.class);
        emitterComponentMap.put("minecraft:emitter_shape_box", EmitterShapeBoxComponent.class);
        emitterComponentMap.put("minecraft:emitter_shape_point", EmitterShapePointComponent.class);
        emitterComponentMap.put("minecraft:emitter_shape_disc", EmitterShapeDiscComponent.class);
        emitterComponentMap.put("minecraft:emitter_shape_custom", EmitterShapeCustomComponent.class);

        emitterComponentMap.put("minecraft:emitter_initialization", EmitterInitializationComponent.class);

        emitterComponentMap.put("minecraft:emitter_lifetime_expression", EmitterLifetimeExpressionComponent.class);
        emitterComponentMap.put("minecraft:emitter_lifetime_looping", EmitterLifetimeLoopingComponent.class);
        emitterComponentMap.put("minecraft:emitter_lifetime_once", EmitterLifetimeOnceComponent.class);

        //-----initialize the particle component map-----
        particleComponentMap.put("minecraft:particle_appearance_billboard", AppearanceBillboardComponent.class);
        particleComponentMap.put("minecraft:particle_appearance_tinting", AppearanceTintingComponent.class);
        particleComponentMap.put("minecraft:particle_appearance_lighting", AppearanceLightingComponent.class);

        particleComponentMap.put("minecraft:particle_lifetime_expression", ParticleLifetimeExpressionComponent.class);
        particleComponentMap.put("minecraft:particle_expire_if_in_blocks", ParticleExpireInBlockComponent.class);
        particleComponentMap.put("minecraft:particle_expire_if_not_in_blocks", ParticleExpireNotInBlockComponent.class);

        particleComponentMap.put("minecraft:particle_initial_speed", ParticleInitialSpeedComponent.class);
        particleComponentMap.put("minecraft:particle_initial_spin", ParticleInitialSpinComponent.class);

        particleComponentMap.put("minecraft:particle_motion_dynamic", ParticleMotionDynamicComponent.class);
        particleComponentMap.put("minecraft:particle_motion_collision", ParticleMotionCollisionComponent.class);
    }

    public static boolean isEmitterComponent(String id) {
        for (Map.Entry<String, Class<? extends RiftLibEmitterComponent>> emitterComponent : emitterComponentMap.entrySet()) {
            if (emitterComponent.getKey().equals(id)) return true;
        }
        return false;
    }

    public static RiftLibEmitterComponent createEmitterComponent(String id) {
        Class<? extends RiftLibEmitterComponent> clazz = emitterComponentMap.get(id);
        if (clazz == null) return null;

        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isParticleComponent(String id) {
        for (Map.Entry<String, Class<? extends RiftLibParticleComponent>> emitterComponent : particleComponentMap.entrySet()) {
            if (emitterComponent.getKey().equals(id)) return true;
        }
        return false;
    }

    public static RiftLibParticleComponent createParticleComponent(String id) {
        Class<? extends RiftLibParticleComponent> clazz = particleComponentMap.get(id);
        if (clazz == null) return null;

        try {
            return clazz.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
