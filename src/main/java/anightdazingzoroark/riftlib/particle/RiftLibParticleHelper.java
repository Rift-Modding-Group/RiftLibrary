package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.internalMessage.RiftLibAttachedParticle;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateBuiltParticle;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateParticle;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class RiftLibParticleHelper {

    public static boolean isRiftLibParticle(String name) {
        return getParticleIdentifiers().contains(name);
    }

    //-----with particle json files-----
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

    @Nullable
    public static ParticleInformation getParticleConstructor(@NotNull String name) {
        Collection<ParticleInformation> particleInformations = RiftLibCacheClient.getInstance().getParticleBuilders().values();

        for (ParticleInformation particleInformation : particleInformations) {
            if (particleInformation.identifier != null && particleInformation.identifier.equals(name))
                return particleInformation;
        }
        return null;
    }

    //-----with particle builders-----
    public static void createParticle(
            @NotNull ParticleBuilder builder, double x, double y, double z,
            @NotNull String... variables
    ) {
        createParticle(builder, x, y, z, 0D, 0D, variables);
    }

    public static void createParticle(
            @NotNull ParticleBuilder builder, double x, double y, double z,
            double rotationX, double rotationY, @NotNull String... variables
    ) {
        validateVariableArray(variables);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            RiftLibMod.PROXY.spawnParticle(builder, x, y, z, rotationX, rotationY, variables);
        }
        else {
            ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibCreateBuiltParticle(
                    builder, null,
                    x, y, z, rotationX, rotationY, variables
            ));
        }
    }

    public static void createParticle(
            @NotNull World world, @NotNull ParticleBuilder builder,
            double x, double y, double z, @NotNull String... variables
    ) {
        createParticle(world, builder, x, y, z, 0D, 0D, variables);
    }

    public static void createParticle(
            @NotNull World world, @NotNull ParticleBuilder builder,
            double x, double y, double z, double rotationX, double rotationY,
            @NotNull String... variables
    ) {
        validateVariableArray(variables);
        if (world.isRemote) RiftLibMod.PROXY.spawnParticle(builder, x, y, z, rotationX, rotationY, variables);
        else {
            ServerProxy.MESSAGE_WRAPPER.sendToDimension(
                    new RiftLibCreateBuiltParticle(
                            builder, world.provider.getDimension(),
                            x, y, z, rotationX, rotationY, variables
                    ),
                    world.provider.getDimension()
            );
        }
    }

    //-----particles attached to animated entities-----
    /**
     * Attaches a JSON particle to an entity's animated locator. Server calls notify tracking clients.
     */
    public static <T extends Entity & IAnimatable<?>> void createParticle(
            @NotNull String name, @NotNull String locatorName, @NotNull T entity,
            @NotNull String... variables
    ) {
        validateVariableArray(variables);
        if (entity.world.isRemote) RiftLibMod.PROXY.spawnParticle(name, locatorName, entity, variables);
        else ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibAttachedParticle(entity, name, locatorName, variables), entity);
    }

    /**
     * Attaches a builder particle to an entity's animated locator, following its position and rotation.
     * Server calls transfer the definition to tracking clients; client calls affect only that client.
     */
    public static <T extends Entity & IAnimatable<?>> void createParticle(
            @NotNull ParticleBuilder builder, @NotNull String locatorName, @NotNull T entity,
            @NotNull String... variables
    ) {
        validateVariableArray(variables);
        if (entity.world.isRemote) RiftLibMod.PROXY.spawnParticle(builder, locatorName, entity, variables);
        else ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(
                new RiftLibAttachedParticle(entity, builder, locatorName, variables), entity);
    }

    /**
     * Stops attached emitters with this identifier and locator, allowing existing particles to finish.
     * Also cancels matching requests waiting for the entity's model to become available.
     */
    public static <T extends Entity & IAnimatable<?>> void killParticle(
            @NotNull T entity, @NotNull String name, @NotNull String locatorName
    ) {
        if (entity.world.isRemote) RiftLibMod.PROXY.killParticle(entity, name, locatorName);
        else ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(
                RiftLibAttachedParticle.forRemoval(entity, name, locatorName), entity);
    }

    //-----misc-----
    private static void validateVariableArray(@NotNull String[] variables) {
        if (variables.length % 2 == 0) return;
        throw new IllegalArgumentException("Emitter variables must be supplied as name-expression pairs");
    }
}