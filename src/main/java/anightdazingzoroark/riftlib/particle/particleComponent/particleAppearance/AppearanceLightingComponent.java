package anightdazingzoroark.riftlib.particle.particleComponent.particleAppearance;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class AppearanceLightingComponent extends RiftLibParticleComponent {
    @Override
    public void parseRawComponent(Map.@NonNull Entry<String, RawParticleComponent> rawComponent, @NonNull MolangParser parser) {
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticle particle) {
        particle.enableLocalLighting();
    }

    public static class Builder extends RiftLibParticleComponent.Builder<AppearanceLightingComponent> {
        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:particle_appearance_lighting";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of();
                }
            };
        }

        @Override
        @NotNull
        public AppearanceLightingComponent build(@NotNull MolangParser parser) {
            return new AppearanceLightingComponent();
        }
    }
}
