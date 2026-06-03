package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RiftLibTickClientFromServer extends RiftLibMessage<RiftLibTickClientFromServer> {
    private NBTTagCompound targetDataNBT;

    public RiftLibTickClientFromServer() {}

    public RiftLibTickClientFromServer(AbstractAnimationData<?, ?> targetData) {
        this.targetDataNBT = targetData.asNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.targetDataNBT = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.targetDataNBT);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibTickClientFromServer message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibTickClientFromServer message, EntityPlayer player, MessageContext messageContext) {
        //test if nbt has anim time
        if (!message.targetDataNBT.hasKey("Tick")) return;
        double tick = message.targetDataNBT.getDouble("Tick");

        //test for matching valid anim data on client end
        AbstractAnimationData<?, ?> animData = AnimationDataResolver.resolveNBTAsData(client.world, message.targetDataNBT);
        if (animData == null) return;

        //set tick
        animData.tick = tick;
    }
}
