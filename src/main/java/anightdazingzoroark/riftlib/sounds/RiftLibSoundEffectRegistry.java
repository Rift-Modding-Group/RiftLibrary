package anightdazingzoroark.riftlib.sounds;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RiftLibSoundEffectRegistry {
    public static final Map<String, ImmutablePair<RiftLibSoundEffect, SoundEvent>> soundEffectMap = new HashMap<>();

    /**
     * Register sound effects meant for use in animations
     *
     * @param modID The id of your mod
     * @param soundIdentifier The identifier of your sound as defined in sounds.json
     * @param soundEffect The sound effect associated with the sound to register
     */
    public static void registerSoundEffect(String modID, String soundIdentifier, RiftLibSoundEffect soundEffect) {
        //register to vanilla game
        ResourceLocation soundID = new ResourceLocation(modID, soundIdentifier);
        SoundEvent soundEvent = new SoundEvent(soundID).setRegistryName(soundID);

        //add to map
        soundEffectMap.put(soundEffect.soundEffectName, new ImmutablePair<>(soundEffect, soundEvent));
    }

    @SubscribeEvent
    public void registerSoundEvents(RegistryEvent.Register<SoundEvent> event) {
        Set<Map.Entry<String, ImmutablePair<RiftLibSoundEffect, SoundEvent>>> soundEffectMapEntries =  soundEffectMap.entrySet();
        for (Map.Entry<String, ImmutablePair<RiftLibSoundEffect, SoundEvent>> soundEffectEntry : soundEffectMapEntries) {
            event.getRegistry().register(soundEffectEntry.getValue().right);
        }
    }
}
