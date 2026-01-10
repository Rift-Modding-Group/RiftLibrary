package anightdazingzoroark.riftlib.sounds;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.item.GeoArmorItem;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class RiftLibSoundHelper {
    public static void playSound(IAnimatable source, AnimatedLocator locator, String soundName) {
        if (!RiftLibSoundEffectRegistry.soundEffectMap.containsKey(soundName)) return;

        SoundEvent soundEvent = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).right;
        RiftLibSoundEffect soundEffect = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).left;

        BlockPos sourcePos = getPosFromAnimatable(source);

        Minecraft.getMinecraft().world.playSound(
            Minecraft.getMinecraft().player,
            sourcePos.getX(), sourcePos.getY(), sourcePos.getZ(),
            soundEvent,
            getSoundCategoryFromAnimatable(source),
            soundEffect.getVolume(),
            soundEffect.getPitch()
        );
    }

    public static void playSound(String soundName) {
        playSound(Minecraft.getMinecraft().player, soundName);
    }

    public static void playSound(EntityPlayer targetPlayer, String soundName) {
        if (targetPlayer == null) return;

        //todo: use packets do deal with this
    }

    private static BlockPos getPosFromAnimatable(IAnimatable source) {
        if (source instanceof Entity) {
            Entity entity = (Entity) source;
            return entity.getPosition();
        }
        return new BlockPos(0, 0, 0);
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
