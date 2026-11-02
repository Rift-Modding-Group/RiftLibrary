package anightdazingzoroark.riftlib.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.sound.SoundEvent.SoundSourceEvent;
import paulscode.sound.SoundSystem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A positional loop that persists until stopped or its source becomes invalid.
 * */
@SideOnly(Side.CLIENT)
public class RiftLibLoopingSound extends MovingSound {
    @NotNull
    private static final Map<LoopKey, RiftLibLoopingSound> activeLoops = new HashMap<>();
    private int playbackGraceTicks = 20;

    private record LoopKey(@NotNull Entity source, @NotNull ResourceLocation sound) {}

    /**
     * Starts or stops a source loop. Repeated starts reuse the existing audio buffer.
     * */
    public static void updateSound(
            @NotNull Entity source, @NotNull SoundEvent sound,
            @NotNull SoundCategory category, boolean playing
    ) {
        Minecraft client = Minecraft.getMinecraft();
        if (source.world != client.world || playing && !source.isEntityAlive()) return;
        LoopKey key = new LoopKey(source, sound.getSoundName());
        RiftLibLoopingSound existing = activeLoops.get(key);
        if (!playing) {
            if (existing != null) {
                activeLoops.remove(key);
                existing.stop();
                client.getSoundHandler().stopSound(existing);
            }
            return;
        }
        if (existing != null) {
            if (!existing.isDonePlaying()
                    && (existing.playbackGraceTicks > 0 || client.getSoundHandler().isSoundPlaying(existing))) {
                return;
            }
            existing.stop();
            client.getSoundHandler().stopSound(existing);
        }
        RiftLibLoopingSound loop = new RiftLibLoopingSound(sound, category, source::getPositionVector,
                () -> source.world == client.world && source.isEntityAlive()
                        && client.world.getEntityByID(source.getEntityId()) == source);
        activeLoops.put(key, loop);
        client.getSoundHandler().playSound(loop);
    }

    public static class Events {
        @SubscribeEvent
        public void onSoundSource(@NotNull SoundSourceEvent event) {
            if (event.getSound() instanceof RiftLibLoopingSound) {
                SoundSystem soundSystem = event.getManager().sndSystem;
                soundSystem.setPriority(event.getUuid(), true);
            }
        }

        @SubscribeEvent
        public void onClientTick(@NotNull TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft client = Minecraft.getMinecraft();
            activeLoops.entrySet().removeIf(entry -> {
                Entity source = entry.getKey().source();
                RiftLibLoopingSound loop = entry.getValue();
                if (!client.isGamePaused()) {
                    loop.playbackGraceTicks = Math.max(0, loop.playbackGraceTicks - 1);
                }
                if (source.world != client.world || !source.isEntityAlive() || loop.isDonePlaying()
                        || client.world.getEntityByID(source.getEntityId()) != source
                ) {
                    entry.getValue().stop();
                    client.getSoundHandler().stopSound(entry.getValue());
                    return true;
                }
                if (!client.isGamePaused() && loop.playbackGraceTicks == 0
                        && !client.getSoundHandler().isSoundPlaying(loop)) {
                    SoundEvent sound = SoundEvent.REGISTRY.getObject(entry.getKey().sound());
                    if (sound != null) updateSound(source, sound, loop.getCategory(), true);
                }
                return false;
            });
        }

        @SubscribeEvent
        public void onWorldUnload(@NotNull WorldEvent.Unload event) {
            if (!event.getWorld().isRemote) return;
            activeLoops.entrySet().removeIf(entry -> {
                if (entry.getKey().source().world != event.getWorld()) return false;
                entry.getValue().stop();
                Minecraft.getMinecraft().getSoundHandler().stopSound(entry.getValue());
                return true;
            });
        }
    }

    @NotNull
    private final Supplier<Vec3d> position;
    @NotNull
    private final BooleanSupplier keepPlaying;

    public RiftLibLoopingSound(
            @NotNull SoundEvent sound, @NotNull SoundCategory category,
            @NotNull Supplier<Vec3d> position, @NotNull BooleanSupplier keepPlaying
    ) {
        super(Objects.requireNonNull(sound), Objects.requireNonNull(category));
        this.position = Objects.requireNonNull(position);
        this.keepPlaying = Objects.requireNonNull(keepPlaying);
        this.repeat = true;
        this.repeatDelay = 0;
        this.update();
    }

    public void stop() {
        this.donePlaying = true;
        this.repeat = false;
    }

    @Override
    public void update() {
        if (this.donePlaying) return;
        if (!this.keepPlaying.getAsBoolean()) {
            this.stop();
            return;
        }
        Vec3d currentPosition = this.position.get();
        this.xPosF = (float) currentPosition.x;
        this.yPosF = (float) currentPosition.y;
        this.zPosF = (float) currentPosition.z;
    }
}
