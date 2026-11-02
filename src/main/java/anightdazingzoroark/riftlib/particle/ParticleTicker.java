package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ParticleTicker {
    public static final List<RiftLibParticleEmitter> EMITTER_LIST = new ArrayList<>();
    public static int EMITTER_ID;
    @NotNull
    private static List<PendingAttachment<?>> pendingAttachments = new ArrayList<>();
    @NotNull
    private static Map<RiftLibParticleEmitter, PendingAttachment<?>> attachedEmitters = new HashMap<>();

    private record PendingAttachment<T extends Entity & IAnimatable<?>>(
            @NotNull ParticleInformation particle, @NotNull String locatorName, @NotNull T entity,
            @NotNull String[] variables, int expiresAtTick
    ) {
    }

    public static <T extends Entity & IAnimatable<?>> void attachParticle(
            @NotNull ParticleInformation particle, @NotNull String locatorName, @NotNull T entity,
            @NotNull String... variables
    ) {
        String[] inputs = variables.clone();
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (entity.world != Minecraft.getMinecraft().world || entity.isDead) return;
            pendingAttachments.add(new PendingAttachment<>(particle, locatorName, entity, inputs, entity.ticksExisted + 100));
        });
    }

    public static <T extends Entity & IAnimatable<?>> void killAttachedParticle(
            @NotNull T entity, @NotNull String name, @NotNull String locatorName
    ) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (entity.world != Minecraft.getMinecraft().world) return;
            pendingAttachments.removeIf(request -> request.entity() == entity
                    && name.equals(request.particle().identifier) && locatorName.equals(request.locatorName()));
            attachedEmitters.forEach((emitter, request) -> {
                if (request.entity() == entity && name.equals(request.particle().identifier)
                        && locatorName.equals(request.locatorName())) {
                    emitter.killEmitter();
                }
            });
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        //do not tick if the game is paused
        if (Minecraft.getMinecraft().isGamePaused()) return;

        Iterator<PendingAttachment<?>> pending = pendingAttachments.iterator();
        while (pending.hasNext()) {
            PendingAttachment<?> request = pending.next();
            Entity entity = request.entity();
            if (entity.world != Minecraft.getMinecraft().world || entity.isDead) {
                pending.remove();
                continue;
            }
            AnimatedLocator locator = request.entity().getAnimationData().getAnimatedLocator(request.locatorName());
            if (locator != null) {
                RiftLibParticleEmitter emitter = new RiftLibParticleEmitter(request.particle(), entity.world, locator, request.variables());
                EMITTER_LIST.add(emitter);
                attachedEmitters.put(emitter, request);
                pending.remove();
            }
            else if (entity.ticksExisted >= request.expiresAtTick()) {
                RiftLib.LOGGER.warn("Cannot attach particle {}: locator {} is unavailable on entity {}",
                        request.particle().identifier, request.locatorName(), entity.getEntityId());
                pending.remove();
            }
        }

        Iterator<RiftLibParticleEmitter> it = EMITTER_LIST.iterator();
        while (it.hasNext()) {
            RiftLibParticleEmitter emitter = it.next();
            emitter.update();
            //remove from list of emitters
            if (emitter.isDead()) it.remove();
        }
        attachedEmitters.keySet().removeIf(RiftLibParticleEmitter::isDead);
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
        if (event.getWorld().isRemote) {
            EMITTER_LIST.clear();
            pendingAttachments.clear();
            attachedEmitters.clear();
        }
    }
}
