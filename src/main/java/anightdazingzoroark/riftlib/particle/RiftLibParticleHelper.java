package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.ServerProxy;
import anightdazingzoroark.riftlib.internalMessage.RiftLibCreateParticle;

public class RiftLibParticleHelper {
    public static void createParticle(String name, double x, double y, double z) {
        ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibCreateParticle(name, x, y, z));
    }
}
