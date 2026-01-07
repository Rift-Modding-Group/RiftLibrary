package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.molang.MolangException;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

public class ParticleTicker {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) throws MolangException {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<RiftLibParticleEmitter> it = ClientProxy.EMITTER_LIST.iterator();
        while (it.hasNext()) {
            RiftLibParticleEmitter emitter = it.next();
            emitter.update();
            if (emitter.isDead()) {
                //remove from the animatedlocator
                emitter.getLocator().removeParticleEmitter(emitter);

                //remove from list of emitters
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();

        for (RiftLibParticleEmitter emitter : ClientProxy.EMITTER_LIST) {
            if (emitter == null) continue;
            emitter.render(partialTicks);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            ClientProxy.EMITTER_LIST.clear();
        }
    }
}
