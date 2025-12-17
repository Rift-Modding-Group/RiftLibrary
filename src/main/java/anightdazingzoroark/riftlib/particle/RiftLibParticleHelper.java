package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.ServerProxy;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateParticle;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import net.minecraft.entity.Entity;

import java.util.Collection;

public class RiftLibParticleHelper {
    public static boolean isRiftLibParticle(String name) {
        Collection<ParticleBuilder> particleBuilders = RiftLibCache.getInstance().getParticleBuilders().values();
        for (ParticleBuilder particleBuilder : particleBuilders) {
            if (particleBuilder.identifier != null && particleBuilder.identifier.equals(name)) return true;
        }
        return false;
    }

    public static void createParticle(String name, double x, double y, double z) {
        ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibCreateParticle(name, x, y, z));
    }
}
