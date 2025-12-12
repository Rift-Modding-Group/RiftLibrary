package anightdazingzoroark.riftlib.particle.particleComponent;

import anightdazingzoroark.riftlib.particle.particleComponent.appearance.AppearanceBillboardComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.appearance.AppearanceLightingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.appearance.AppearanceTintingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterInitialState.EmitterInitializationComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.EmitterInstantComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterRate.EmitterSteadyComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeCustomComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapePointComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.emitterShape.EmitterShapeSphereComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter.EmitterLifetimeLoopingComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleInitialState.ParticleInitialSpeedComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter.EmitterLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.lifetime.particle.ParticleLifetimeExpressionComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.particleMotion.ParticleMotionDynamicComponent;

import java.util.HashMap;
import java.util.Map;

public class RiftLibParticleComponentRegistry {
    private static final Map<String, Class<? extends RiftLibParticleComponent>> componentMap = new HashMap<>();

    public static void initializeMap() {
        componentMap.put("minecraft:particle_appearance_billboard", AppearanceBillboardComponent.class);
        componentMap.put("minecraft:particle_appearance_tinting", AppearanceTintingComponent.class);
        componentMap.put("minecraft:particle_appearance_lighting", AppearanceLightingComponent.class);
        componentMap.put("minecraft:emitter_lifetime_expression", EmitterLifetimeExpressionComponent.class);
        componentMap.put("minecraft:emitter_lifetime_looping", EmitterLifetimeLoopingComponent.class);
        componentMap.put("minecraft:particle_lifetime_expression", ParticleLifetimeExpressionComponent.class);
        componentMap.put("minecraft:emitter_rate_instant", EmitterInstantComponent.class);
        componentMap.put("minecraft:emitter_rate_steady", EmitterSteadyComponent.class);
        componentMap.put("minecraft:emitter_shape_sphere", EmitterShapeSphereComponent.class);
        componentMap.put("minecraft:emitter_shape_point", EmitterShapePointComponent.class);
        componentMap.put("minecraft:emitter_shape_custom", EmitterShapeCustomComponent.class);
        componentMap.put("minecraft:emitter_initialization", EmitterInitializationComponent.class);
        componentMap.put("minecraft:particle_initial_speed", ParticleInitialSpeedComponent.class);
        componentMap.put("minecraft:particle_motion_dynamic", ParticleMotionDynamicComponent.class);
    }

    public static RiftLibParticleComponent createComponent(String id) {
        Class<? extends RiftLibParticleComponent> clazz = componentMap.get(id);
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
