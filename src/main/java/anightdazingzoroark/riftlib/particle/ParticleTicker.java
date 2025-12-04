package anightdazingzoroark.riftlib.particle;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;

import static anightdazingzoroark.riftlib.ClientProxy.EMITTER_LIST;

public class ParticleTicker {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Iterator<RiftLibParticleEmitter> it = EMITTER_LIST.iterator();
        while (it.hasNext()) {
            RiftLibParticleEmitter emitter = it.next();
            emitter.update();
            if (emitter.isDead()) it.remove();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();

        for (RiftLibParticleEmitter emitter : EMITTER_LIST) {
            emitter.render(partialTicks);
        }
    }
}
