package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.proxy.ServerProxy;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateParticle;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class RiftLibParticleHelper {
    public static boolean isRiftLibParticle(String name) {
        return getParticleIdentifiers().contains(name);
    }

    @NotNull
    public static List<String> getParticleIdentifiers() {
        return RiftLibCacheServer.getInstance().getParticleIdentifiers().stream().sorted().toList();
    }

    public static void createParticle(String name, double x, double y, double z, @NotNull String... variables) {
        validateVariableArray(variables);
        ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibCreateParticle(name, x, y, z, variables));
    }

    public static void createParticle(String name, double x, double y, double z, double rotationX, double rotationY, @NotNull String... variables) {
        validateVariableArray(variables);
        ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibCreateParticle(name, x, y, z, rotationX, rotationY, variables));
    }

    private static void validateVariableArray(@NotNull String[] variables) {
        if (variables.length % 2 == 0) return;
        throw new IllegalArgumentException("Emitter variables must be supplied as name-expression pairs");
    }

    @Nullable
    public static ParticleBuilder getParticleBuilder(String name) {
        Collection<ParticleBuilder> particleBuilders = RiftLibCacheClient.getInstance().getParticleBuilders().values();

        for (ParticleBuilder particleBuilder : particleBuilders) {
            if (particleBuilder.identifier != null && particleBuilder.identifier.equals(name)) return particleBuilder;
        }
        return null;
    }
}
