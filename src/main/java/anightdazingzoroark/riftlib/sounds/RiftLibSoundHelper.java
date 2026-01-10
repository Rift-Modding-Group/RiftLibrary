package anightdazingzoroark.riftlib.sounds;

import anightdazingzoroark.riftlib.ServerProxy;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.internalMessage.RiftLibPlaySoundForPlayer;
import anightdazingzoroark.riftlib.item.GeoArmorItem;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

public class RiftLibSoundHelper {
    /**
     * This function is used in AnimationProcessor to play a sound event registered in RiftLibSoundEffectRegistry.
     */
    public static void playSound(IAnimatable source, AnimatedLocator locator, String soundName) {
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

    private static SoundCategory getSoundCategoryFromAnimatable(IAnimatable source) {
        if (source instanceof Entity) {
            Entity entity = (Entity) source;
            return entity.getSoundCategory();
        }
        else if (source instanceof TileEntity) return SoundCategory.BLOCKS;
        else if (source instanceof GeoArmorItem) return SoundCategory.PLAYERS;
        return SoundCategory.MASTER;
    }
}
