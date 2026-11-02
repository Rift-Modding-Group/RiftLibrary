package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.RiftLibMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class RiftLibUpdateLoopingSound extends RiftLibMessage<RiftLibUpdateLoopingSound> {
    private int dimension;
    private int entityId;
    @NotNull
    private ResourceLocation sound = new ResourceLocation("minecraft", "empty");
    @NotNull
    private SoundCategory category = SoundCategory.NEUTRAL;
    private boolean playing;

    public RiftLibUpdateLoopingSound() {}

    public RiftLibUpdateLoopingSound(@NotNull Entity source, @NotNull SoundEvent sound, @NotNull SoundCategory category, boolean playing) {
        this.dimension = source.world.provider.getDimension();
        this.entityId = source.getEntityId();
        this.sound = sound.getSoundName();
        this.category = category;
        this.playing = playing;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buffer) {
        this.dimension = buffer.readInt();
        this.entityId = buffer.readInt();
        this.sound = new ResourceLocation(ByteBufUtils.readUTF8String(buffer));
        this.category = SoundCategory.values()[buffer.readInt()];
        this.playing = buffer.readBoolean();
    }

    @Override
    public void toBytes(@NotNull ByteBuf buffer) {
        buffer.writeInt(this.dimension);
        buffer.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buffer, this.sound.toString());
        buffer.writeInt(this.category.ordinal());
        buffer.writeBoolean(this.playing);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateLoopingSound message, EntityPlayer player, MessageContext context) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateLoopingSound message, EntityPlayer player, MessageContext context) {
        if (client.world == null || client.world.provider.getDimension() != message.dimension) return;
        Entity source = client.world.getEntityByID(message.entityId);
        SoundEvent soundEvent = SoundEvent.REGISTRY.getObject(message.sound);
        if (source == null || soundEvent == null) return;
        RiftLibMod.PROXY.updateLoopingSound(source, soundEvent, message.category, message.playing);
    }
}
