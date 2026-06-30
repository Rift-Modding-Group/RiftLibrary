package anightdazingzoroark.riftlib.resource;

import anightdazingzoroark.riftlib.animation.AnimationFile;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.jsonParsing.RiftLibLoader;
import anightdazingzoroark.riftlib.molang.MolangParser;
import net.minecraft.util.ResourceLocation;
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

    public abstract Map<ResourceLocation, AnimationFile> getAnimations();

    public abstract Map<ResourceLocation, GeoModel> getGeoModels();
}
