package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Transfers locator attachment requests without changing the existing free particle messages.
 */
public class RiftLibAttachedParticle extends RiftLibMessage<RiftLibAttachedParticle> {
    private boolean create = true;
    private int dimension;
    private int entityId;
    @NotNull
    private String name = "";
    @NotNull
    private String locatorName = "";
    @Nullable
    private String definition;
    @NotNull
    private String[] variables = new String[0];

    public RiftLibAttachedParticle() {
    }

    public <T extends Entity & IAnimatable<?>> RiftLibAttachedParticle(
            @NotNull T entity, @NotNull String name, @NotNull String locatorName, @NotNull String... variables
    ) {
        this(entity, name, locatorName, null, variables);
    }

    public <T extends Entity & IAnimatable<?>> RiftLibAttachedParticle(
            @NotNull T entity, @NotNull ParticleBuilder builder, @NotNull String locatorName, @NotNull String... variables
    ) {
        this(entity, "", locatorName, RiftLibCreateBuiltParticle.encodeDefinition(builder), variables);
    }

    private <T extends Entity & IAnimatable<?>> RiftLibAttachedParticle(
            @NotNull T entity, @NotNull String name, @NotNull String locatorName,
            @Nullable String definition, @NotNull String[] variables
    ) {
        if (variables.length % 2 != 0) {
            throw new IllegalArgumentException("Emitter variables must be supplied as name-expression pairs");
        }
        this.dimension = entity.world.provider.getDimension();
        this.entityId = entity.getEntityId();
        this.name = Objects.requireNonNull(name);
        this.locatorName = Objects.requireNonNull(locatorName);
        this.definition = definition;
        this.variables = variables.clone();
        for (String variable : this.variables) Objects.requireNonNull(variable, "Emitter variables cannot be null");
    }

    @NotNull
    public static <T extends Entity & IAnimatable<?>> RiftLibAttachedParticle forRemoval(
            @NotNull T entity, @NotNull String name, @NotNull String locatorName
    ) {
        RiftLibAttachedParticle message = new RiftLibAttachedParticle(entity, name, locatorName);
        message.create = false;
        return message;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buffer) {
        this.create = buffer.readBoolean();
        this.dimension = buffer.readInt();
        this.entityId = buffer.readInt();
        this.name = ByteBufUtils.readUTF8String(buffer);
        this.locatorName = ByteBufUtils.readUTF8String(buffer);
        this.definition = null;
        this.variables = new String[0];
        if (!this.create) return;
        if (buffer.readBoolean()) {
            int size = buffer.readInt();
            if (size < 0 || size > 1048576 || size > buffer.readableBytes()) {
                throw new IllegalArgumentException("Invalid particle definition length");
            }
            this.definition = buffer.readCharSequence(size, StandardCharsets.UTF_8).toString();
        }
        int count = buffer.readInt();
        if (count < 0 || count % 2 != 0 || count > buffer.readableBytes()) {
            throw new IllegalArgumentException("Invalid emitter variable count");
        }
        this.variables = new String[count];
        for (int index = 0; index < count; index++) this.variables[index] = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void toBytes(@NotNull ByteBuf buffer) {
        buffer.writeBoolean(this.create);
        buffer.writeInt(this.dimension);
        buffer.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buffer, this.name);
        ByteBufUtils.writeUTF8String(buffer, this.locatorName);
        if (!this.create) return;
        buffer.writeBoolean(this.definition != null);
        if (this.definition != null) {
            byte[] bytes = this.definition.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 1048576) throw new IllegalArgumentException("Particle definition exceeds 1 MiB");
            buffer.writeInt(bytes.length);
            buffer.writeBytes(bytes);
        }
        buffer.writeInt(this.variables.length);
        for (String variable : this.variables) ByteBufUtils.writeUTF8String(buffer, variable);
    }

    @Override
    public void executeOnServer(
            MinecraftServer server, RiftLibAttachedParticle message, EntityPlayer player, MessageContext context
    ) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(
            Minecraft client, RiftLibAttachedParticle message, EntityPlayer player, MessageContext context
    ) {
        if (client.world == null || client.world.provider.getDimension() != message.dimension) return;
        Entity entity = client.world.getEntityByID(message.entityId);
        if (!(entity instanceof IAnimatable<?>)) return;
        if (!message.create) {
            RiftLibParticleHelper.killParticle((Entity & IAnimatable<?>) entity, message.name, message.locatorName);
        }
        else if (message.definition != null) {
            RiftLibParticleHelper.createParticle(RiftLibCreateBuiltParticle.decodeDefinition(message.definition),
                    message.locatorName, (Entity & IAnimatable<?>) entity, message.variables);
        }
        else {
            RiftLibParticleHelper.createParticle(message.name, message.locatorName,
                    (Entity & IAnimatable<?>) entity, message.variables);
        }
    }
}
