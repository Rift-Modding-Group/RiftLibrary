package anightdazingzoroark.riftlib.message;

import anightdazingzoroark.RiftLibMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class RiftLibMessage<T extends RiftLibMessage<T>> implements IMessage, IMessageHandler<T, IMessage> {
    public abstract void fromBytes(ByteBuf buf);

    public abstract void toBytes(ByteBuf buf);

    public abstract void executeOnServer(MinecraftServer server, T message, EntityPlayer player, MessageContext messageContext);

    @SideOnly(Side.CLIENT)
    public abstract void executeOnClient(Minecraft client, T message, EntityPlayer player, MessageContext messageContext);

    @Override
    public IMessage onMessage(T message, MessageContext messageContext) {
        RiftLibMod.PROXY.handleMessage(message, messageContext);
        return null;
    }
}
