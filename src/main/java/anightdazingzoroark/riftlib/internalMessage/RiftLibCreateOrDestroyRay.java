package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RayTicker;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class RiftLibCreateOrDestroyRay extends RiftLibMessage<RiftLibCreateOrDestroyRay> {
    private boolean create;
    private int entityId;
    private String rayName;

    public RiftLibCreateOrDestroyRay() {}

    public RiftLibCreateOrDestroyRay(boolean create, IRayCreator<?> rayCreator, String rayName) {
        this.create = create;
        this.entityId = rayCreator.getRayCreator().getEntityId();
        this.rayName = rayName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.create = buf.readBoolean();
        this.entityId = buf.readInt();
        this.rayName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.create);
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.rayName);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {}

    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateOrDestroyRay message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IRayCreator<?> rayCreator)) return;

        if (message.create) {
            RiftLibRay ray = rayCreator.getRays().get(rayName);
            RayTicker.RAY_PAIR_LIST.add(new ImmutablePair<>(rayCreator, ray));
        }
        else {
            for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.RAY_PAIR_LIST) {
                RiftLibRay ray = rayCreator.getRays().get(rayName);
                if (rayCreator == rayPair.getLeft() && ray == rayPair.getRight()) {
                    ray.endRay();
                    break;
                }
            }
        }
    }
}
