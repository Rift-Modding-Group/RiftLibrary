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

public class RiftLibApplyMessageEffect extends RiftLibMessage<RiftLibApplyMessageEffect> {
    private NBTTagCompound animDataNBT;
    private String messageName;

    public RiftLibApplyMessageEffect() {}

    public RiftLibApplyMessageEffect(AbstractAnimationData<?, ?> animData, String messageName) {
        this.animDataNBT = animData.getNBT();
        this.messageName = messageName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.animDataNBT = ByteBufUtils.readTag(buf);
        this.messageName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, this.animDataNBT);
        ByteBufUtils.writeUTF8String(buf, this.messageName);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibApplyMessageEffect message, EntityPlayer player, MessageContext messageContext) {
        AbstractAnimationData<?, ?> resolvedAnimData = AnimationDataResolver.resolveNBTAsData(server.getEntityWorld(), message.animDataNBT);
        if (resolvedAnimData == null) return;

        resolvedAnimData.getAnimationMessageEffects().get(message.messageName).runValue().run();
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibApplyMessageEffect message, EntityPlayer player, MessageContext messageContext) {
        AbstractAnimationData<?, ?> resolvedAnimData = AnimationDataResolver.resolveNBTAsData(client.world, message.animDataNBT);
        if (resolvedAnimData == null) return;

        resolvedAnimData.getAnimationMessageEffects().get(message.messageName).runValue().run();
    }
}
