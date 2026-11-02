package anightdazingzoroark.riftlib.sounds;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.internalMessage.RiftLibUpdateLoopingSound;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import anightdazingzoroark.riftlib.internalMessage.RiftLibPlaySoundForPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class RiftLibSoundHelper {
    @NotNull
    private static final Map<Entity, Map<ResourceLocation, LoopDefinition>> activeServerLoops = new WeakHashMap<>();

    private record LoopDefinition(@NotNull SoundEvent sound, @NotNull SoundCategory category) {
    }

    public static class Events {
        @SubscribeEvent
        public void onStartTracking(@NotNull PlayerEvent.StartTracking event) {
            if (!(event.getEntityPlayer() instanceof EntityPlayerMP player)) return;
            Map<ResourceLocation, LoopDefinition> loops = activeServerLoops.get(event.getTarget());
            if (loops == null) return;
            for (LoopDefinition loop : loops.values()) {
                ServerProxy.MESSAGE_WRAPPER.sendTo(
                        new RiftLibUpdateLoopingSound(event.getTarget(), loop.sound(), loop.category(), true), player);
            }
        }

        @SubscribeEvent
        public void onWorldUnload(@NotNull WorldEvent.Unload event) {
            if (event.getWorld().isRemote) return;
            activeServerLoops.keySet().removeIf(source -> source.world == event.getWorld());
        }
    }

    /**
     * This function is used in AnimationProcessor to play a sound event registered in RiftLibSoundEffectRegistry.
     */
    public static void playSound(IAnimatable<?> source, AnimatedLocator locator, String soundName) {
        if (!RiftLibSoundEffectRegistry.soundEffectMap.containsKey(soundName)) return;

        SoundEvent soundEvent = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).right;
        RiftLibSoundEffect soundEffect = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).left;

        Vec3d locatorPos = locator.getWorldSpacePosition();

        Minecraft.getMinecraft().world.playSound(
            Minecraft.getMinecraft().player,
            locatorPos.x, locatorPos.y, locatorPos.z,
            soundEvent,
            getSoundCategoryFromAnimatable(source),
            soundEffect.getVolume(),
            soundEffect.getPitch()
        );
    }

    /**
     * If you ever want to play a sound event from RiftLibSoundEffectRegistry to a player
     * for whatever reason, you may use this.
     *
     * @param targetPlayer The player you want to play the sound effect to
     * @param soundName The name of the registered sound effect
     * @param soundCategory The sound category the sound effect is to utilize when played
     */
    public static void playSound(EntityPlayer targetPlayer, String soundName, SoundCategory soundCategory) {
        if (!(targetPlayer instanceof EntityPlayerMP)) return;
        ServerProxy.MESSAGE_WRAPPER.sendTo(new RiftLibPlaySoundForPlayer(targetPlayer, soundName, soundCategory), (EntityPlayerMP) targetPlayer);
    }

    /** Starts one persistent positional loop, ending when stopLoopingSound is called. */
    public static void playLoopingSound(@NotNull Entity source, @Nullable SoundEvent sound, @NotNull SoundCategory category) {
        updateLoopingSound(source, sound, category, true);
    }

    public static void stopLoopingSound(@NotNull Entity source, @Nullable SoundEvent sound, @NotNull SoundCategory category) {
        updateLoopingSound(source, sound, category, false);
    }

    private static void updateLoopingSound(@NotNull Entity source, @Nullable SoundEvent sound,
                                          @NotNull SoundCategory category, boolean playing) {
        if (sound == null) return;
        if (source.world.isRemote) {
            RiftLibMod.PROXY.updateLoopingSound(source, sound, category, playing);
        }
        else {
            ResourceLocation soundName = sound.getSoundName();
            if (playing) {
                Map<ResourceLocation, LoopDefinition> loops = activeServerLoops.computeIfAbsent(source, key -> new HashMap<>());
                LoopDefinition previous = loops.putIfAbsent(soundName, new LoopDefinition(sound, category));
                if (previous != null) return;
            }
            else {
                Map<ResourceLocation, LoopDefinition> loops = activeServerLoops.get(source);
                if (loops != null) {
                    loops.remove(soundName);
                    if (loops.isEmpty()) activeServerLoops.remove(source);
                }
            }
            RiftLibUpdateLoopingSound message = new RiftLibUpdateLoopingSound(source, sound, category, playing);
            ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(message, source);
            if (source instanceof EntityPlayerMP player) ServerProxy.MESSAGE_WRAPPER.sendTo(message, player);
        }
    }

    private static SoundCategory getSoundCategoryFromAnimatable(IAnimatable<?> source) {
        if (source.getAnimationData() instanceof AbstractAnimationDataEntity<?, ?> entitySrc) return entitySrc.getHolder().getSoundCategory();
        //will add back soon
        //else if (source instanceof GeoArmorItem) return SoundCategory.PLAYERS;
        return SoundCategory.MASTER;
    }
}
