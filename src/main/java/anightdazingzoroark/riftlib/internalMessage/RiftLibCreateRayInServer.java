package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class RiftLibCreateRayInServer extends RiftLibMessage<RiftLibCreateRayInServer> {
    private int entityId;
    private String rayName;
    private List<AxisAlignedBB> aabbList;

    public RiftLibCreateRayInServer() {}

    public RiftLibCreateRayInServer(IRayCreator<?> rayCreator, String rayName, List<AxisAlignedBB> aabbList) {
        this.entityId = rayCreator.getRayCreator().getEntityId();
        this.rayName = rayName;
        this.aabbList = aabbList;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.rayName = ByteBufUtils.readUTF8String(buf);

        int aabbCount = buf.readInt();
        this.aabbList = new ArrayList<>(aabbCount);

        for (int i = 0; i < aabbCount; i++) {
            this.aabbList.add(new AxisAlignedBB(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
            ));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.rayName);

        buf.writeInt(this.aabbList.size());
        for (AxisAlignedBB aabb : this.aabbList) {
            buf.writeDouble(aabb.minX);
            buf.writeDouble(aabb.minY);
            buf.writeDouble(aabb.minZ);
            buf.writeDouble(aabb.maxX);
            buf.writeDouble(aabb.maxY);
            buf.writeDouble(aabb.maxZ);
        }
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateRayInServer message, EntityPlayer player, MessageContext messageContext) {
        Entity entity = server.getEntityWorld().getEntityByID(message.entityId);
        if (!(entity instanceof IRayCreator<?> rayCreator)) return;
        rayCreator.applyRayVectorResult(message.rayName, message.aabbList);
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateRayInServer message, EntityPlayer player, MessageContext messageContext) {}
}
