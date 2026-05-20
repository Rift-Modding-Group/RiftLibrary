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
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjglx.util.vector.Quaternion;

public class RiftLibUpdateRayPos extends RiftLibMessage<RiftLibUpdateRayPos> {
    private int entityId;
    private String rayName;
    private Vec3d rayPosVec;
    private Quaternion rayQuat;

    public RiftLibUpdateRayPos() {}

    public RiftLibUpdateRayPos(IRayCreator<?> rayCreator, String rayName, Vec3d rayPosVec, Quaternion rayQuat) {
        this.entityId = rayCreator.getRayCreator().getEntityId();
        this.rayName = rayName;
        this.rayPosVec = rayPosVec;
        this.rayQuat = rayQuat;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.rayName = ByteBufUtils.readUTF8String(buf);

        double rayPosX = buf.readDouble();
        double rayPosY = buf.readDouble();
        double rayPosZ = buf.readDouble();
        this.rayPosVec = new Vec3d(rayPosX, rayPosY, rayPosZ);

        float rayQuatX = buf.readFloat();
        float rayQuatY = buf.readFloat();
        float rayQuatZ = buf.readFloat();
        float rayQuatW = buf.readFloat();
        this.rayQuat = new Quaternion(rayQuatX, rayQuatY, rayQuatZ, rayQuatW);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.rayName);

        buf.writeDouble(this.rayPosVec.x);
        buf.writeDouble(this.rayPosVec.y);
        buf.writeDouble(this.rayPosVec.z);

        buf.writeFloat(this.rayQuat.x);
        buf.writeFloat(this.rayQuat.y);
        buf.writeFloat(this.rayQuat.z);
        buf.writeFloat(this.rayQuat.w);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibUpdateRayPos message, EntityPlayer player, MessageContext messageContext) {
        if (message.rayName == null || this.rayPosVec == null || this.rayQuat == null) return;

        Entity entity = server.getEntityWorld().getEntityByID(message.entityId);
        if (!(entity instanceof IRayCreator<?> rayCreator)) return;

        for (ImmutablePair<IRayCreator<?>, RiftLibRay> rayPair : RayTicker.Server.RAY_PAIR_LIST) {
            if (rayCreator != rayPair.getLeft() || !message.rayName.equals(rayPair.getRight().rayName)) continue;
            rayPair.getRight().displaceByAnim(message.rayPosVec);
            rayPair.getRight().displaceQuatByAnim(message.rayQuat);
        }
    }

    @Override
    public void executeOnClient(Minecraft client, RiftLibUpdateRayPos message, EntityPlayer player, MessageContext messageContext) {}
}
