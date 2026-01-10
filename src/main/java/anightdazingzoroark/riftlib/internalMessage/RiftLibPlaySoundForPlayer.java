package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffect;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffectRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibPlaySoundForPlayer extends RiftLibMessage<RiftLibPlaySoundForPlayer> {
    private int playerId;
    private String soundName;
    private SoundCategory soundCategory;

    public RiftLibPlaySoundForPlayer() {}

    public RiftLibPlaySoundForPlayer(EntityPlayer player, String soundName, SoundCategory soundCategory) {
        this.playerId = player.getEntityId();
        this.soundName = soundName;
        this.soundCategory = soundCategory;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.soundName = ByteBufUtils.readUTF8String(buf);
        this.soundCategory = SoundCategory.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        ByteBufUtils.writeUTF8String(buf, this.soundName);
        buf.writeInt(this.soundCategory.ordinal());
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibPlaySoundForPlayer message, EntityPlayer messagePlayer, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibPlaySoundForPlayer message, EntityPlayer messagePlayer, MessageContext messageContext) {
        EntityPlayer player = (EntityPlayer) client.world.getEntityByID(message.playerId);
        if (player == null) return;

        SoundEvent soundEvent = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).right;
        RiftLibSoundEffect soundEffect = RiftLibSoundEffectRegistry.soundEffectMap.get(soundName).left;

        Minecraft.getMinecraft().world.playSound(
                player,
                player.posX, player.posY, player.posZ,
                soundEvent, message.soundCategory,
                soundEffect.getVolume(),
                soundEffect.getPitch()
        );
    }
}
