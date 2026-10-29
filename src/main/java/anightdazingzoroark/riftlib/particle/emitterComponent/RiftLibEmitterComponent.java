package anightdazingzoroark.riftlib.particle.emitterComponent;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class RiftLibEmitterComponent {
    /**
     * Parse a component from raw json
     * */
    public abstract void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser);

    /**
     * Apply a component to the particle
     * */
    public abstract void applyComponent(@NotNull RiftLibParticleEmitter emitter);

    /**
     * JSON representation of component
     * */
    public abstract static class RawComponent {
        @NotNull
        public abstract String identifier();

        @NotNull
        public abstract Map<String, Object> fields();
    }

    public abstract static class Builder<T extends RiftLibEmitterComponent> {
        @NotNull
        public abstract T build(@NotNull MolangParser parser);

        /**
         * Convert the component definition to JSON, mostly for packets.
         */
        @NotNull
        public abstract RawComponent toRawComponent();
    }
}