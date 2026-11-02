package anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Position inheritance for minecraft:emitter_local_space.
 * */
public class EmitterLocalSpaceComponent extends RiftLibEmitterComponent {
    private boolean position;

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        RawParticleComponent.ComponentValue value = rawComponent.getValue().componentValues.get("position");
        this.position = value != null && value.asBoolean();
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticleEmitter emitter) {
        emitter.setLocalSpacePosition(this.position);
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterLocalSpaceComponent> {
        private boolean position;

        @NotNull
        public Builder setPosition(boolean position) {
            this.position = position;
            return this;
        }

        @Override
        @NotNull
        public Builder copy() {
            return new Builder().setPosition(this.position);
        }

        @Override
        @NotNull
        public EmitterLocalSpaceComponent build(@NotNull MolangParser parser) {
            EmitterLocalSpaceComponent result = new EmitterLocalSpaceComponent();
            result.position = this.position;
            return result;
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_local_space";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    return Map.of("position", Builder.this.position);
                }
            };
        }
    }
}
