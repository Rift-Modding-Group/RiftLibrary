package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangException;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleTicker {
    public static final List<RiftLibParticleEmitter> EMITTER_LIST = new ArrayList<>();
    public static int EMITTER_ID;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) throws MolangException {
        if (event.phase != TickEvent.Phase.END) return;

        //do not tick if the game is paused
        if (Minecraft.getMinecraft().isGamePaused()) return;

        Iterator<RiftLibParticleEmitter> it = EMITTER_LIST.iterator();
        while (it.hasNext()) {
            RiftLibParticleEmitter emitter = it.next();
            emitter.update();
            if (emitter.isDead()) {
                //remove from the animatedlocator
                if (emitter.getLocator() != null) emitter.getLocator().removeParticleEmitter(emitter);

                //remove from list of emitters
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        float partialTicks = event.getPartialTicks();

        for (RiftLibParticleEmitter emitter : EMITTER_LIST) {
            if (emitter == null) continue;
            emitter.render(partialTicks);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) EMITTER_LIST.clear();
    }
}
