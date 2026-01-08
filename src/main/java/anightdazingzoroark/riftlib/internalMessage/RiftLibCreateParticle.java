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
    private double rotationX, rotationY;

    public RiftLibCreateParticle() {}

    public RiftLibCreateParticle(String name, double x, double y, double z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RiftLibCreateParticle(String name, double x, double y, double z, double rotationX, double rotationY) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.name = ByteBufUtils.readUTF8String(buf);
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.rotationX = buf.readDouble();
        this.rotationY = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.name);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeDouble(this.rotationX);
        buf.writeDouble(this.rotationY);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateParticle message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateParticle message, EntityPlayer player, MessageContext messageContext) {
        if (message.rotationX != 0 || message.rotationY != 0) {
            RiftLibMod.PROXY.spawnParticle(message.name, message.x, message.y, message.z, message.rotationX, message.rotationY);
        }
        else RiftLibMod.PROXY.spawnParticle(message.name, message.x, message.y, message.z);
    }
}
