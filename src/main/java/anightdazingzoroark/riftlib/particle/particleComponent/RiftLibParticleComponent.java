package anightdazingzoroark.riftlib.particle.particleComponent;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class RiftLibParticleComponent {
    /**
     * Parse a component from raw json
     * */
    public abstract void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser);

    /**
     * Apply a component to the particle
     * */
    public abstract void applyComponent(@NotNull RiftLibParticle particle);

    /**
     * JSON representation of component
     * */
    public abstract static class RawComponent {
        @NotNull
        public abstract String identifier();

        @NotNull
        public abstract Map<String, Object> fields();
    }

    /**
     * A builder class for the component, a great option if u dont wanna make a json file
     * */
    public abstract static class Builder<T extends RiftLibParticleComponent> {
        @NotNull
        public abstract T build(@NotNull MolangParser parser);

        /**
         * Convert the component definition to JSON, mostly for packets.
         */
        @NotNull
        public abstract RawComponent toRawComponent();
    }
}
