package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibCreateParticle extends RiftLibMessage<RiftLibCreateParticle> {
    private String name;
    private double x, y, z;

    public RiftLibCreateParticle() {}

    public RiftLibCreateParticle(String name, double x, double y, double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.name = ByteBufUtils.readUTF8String(buf);
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.name);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateParticle message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateParticle message, EntityPlayer player, MessageContext messageContext) {
        RiftLibMod.PROXY.spawnParticle(message.name, message.x, message.y, message.z);
    }
}
