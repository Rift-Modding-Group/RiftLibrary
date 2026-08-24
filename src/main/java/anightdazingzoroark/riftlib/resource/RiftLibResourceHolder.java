package anightdazingzoroark.riftlib.resource;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.jsonParsing.RiftLibLoader;
import anightdazingzoroark.riftlib.molang.MolangParser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * On client and server, this is meant to hold the resources to use.
 * Side-specific subclasses are responsible for discovering and opening files.
 */
public abstract class RiftLibResourceHolder {
    @NotNull
    protected final RiftLibLoader loader;
    @NotNull
    public final MolangParser parser = new MolangParser();

    protected RiftLibResourceHolder() {
        this.loader = new RiftLibLoader();
    }

    /**
     * animations are stored per mod id in cache, then by identifier
     * */
    public abstract Map<String, Map<String, Animation>> getAnimations();

    /**
     * same here
     * */
    public abstract Map<String, Map<String, GeoModel>> getGeoModels();
}
