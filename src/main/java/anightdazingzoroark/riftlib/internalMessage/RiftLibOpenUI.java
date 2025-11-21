package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RiftLibOpenUI extends RiftLibMessage<RiftLibOpenUI> {
    private String uiID;
    private NBTTagCompound nbtTagCompound;
    private int x, y, z;

    public RiftLibOpenUI() {}

    public RiftLibOpenUI(String uiID, NBTTagCompound nbtTagCompound, int x, int y, int z) {
        this.uiID = uiID;
        this.nbtTagCompound = nbtTagCompound;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uiID = ByteBufUtils.readUTF8String(buf);
        this.nbtTagCompound = ByteBufUtils.readTag(buf);
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.uiID);
        ByteBufUtils.writeTag(buf, this.nbtTagCompound);
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibOpenUI message, EntityPlayer player, MessageContext messageContext) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft client, RiftLibOpenUI message, EntityPlayer player, MessageContext messageContext) {
        ClientProxy.showUI(message.uiID, message.nbtTagCompound, message.x, message.y, message.z);
    }
}
