package anightdazingzoroark.riftlib.internalMessage;

import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * carry json particle json data from server to client
 */
public class RiftLibCreateBuiltParticle extends RiftLibMessage<RiftLibCreateBuiltParticle> {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RawMolangValue.class, new RawMolangValue.Deserializer())
            .registerTypeAdapter(RawParticleComponent.class, new RawParticleComponent.Deserializer())
            .create();

    private String definition;
    @Nullable
    private Integer dimension;
    private double x, y, z;
    private double rotationX, rotationY;
    private String[] variables = new String[0];

    public RiftLibCreateBuiltParticle() {}

    public RiftLibCreateBuiltParticle(
            @NotNull ParticleBuilder builder, @Nullable Integer dimension,
            double x, double y, double z, double rotationX, double rotationY,
            @NotNull String... variables
    ) {
        if (variables.length % 2 != 0) {
            throw new IllegalArgumentException("Emitter variables must be supplied as name-expression pairs");
        }
        this.definition = encodeDefinition(builder);
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.variables = variables.clone();
        for (String variable : this.variables) Objects.requireNonNull(variable, "Emitter variables cannot be null");
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buffer) {
        int size = buffer.readInt();
        if (size < 0 || size > 1048576 || size > buffer.readableBytes()) {
            throw new IllegalArgumentException("Invalid particle definition length");
        }
        this.definition = buffer.readCharSequence(size, StandardCharsets.UTF_8).toString();
        if (buffer.readBoolean()) this.dimension = buffer.readInt();
        else this.dimension = null;
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.rotationX = buffer.readDouble();
        this.rotationY = buffer.readDouble();
        int count = buffer.readInt();
        if (count < 0 || count % 2 != 0 || count > buffer.readableBytes()) {
            throw new IllegalArgumentException("Invalid emitter variable count");
        }
        this.variables = new String[count];
        for (int i = 0; i < count; i++) this.variables[i] = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    public void toBytes(@NotNull ByteBuf buffer) {
        byte[] bytes = this.definition.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 1048576) throw new IllegalArgumentException("Particle definition exceeds 1 MiB");
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
        buffer.writeBoolean(this.dimension != null);
        if (this.dimension != null) buffer.writeInt(this.dimension);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeDouble(this.rotationX);
        buffer.writeDouble(this.rotationY);
        buffer.writeInt(this.variables.length);
        for (String variable : this.variables) ByteBufUtils.writeUTF8String(buffer, variable);
    }

    @Override
    public void executeOnServer(MinecraftServer server, RiftLibCreateBuiltParticle message, EntityPlayer player, MessageContext context) {}

    @SideOnly(Side.CLIENT)
    @Override
    public void executeOnClient(Minecraft client, RiftLibCreateBuiltParticle message, EntityPlayer player, MessageContext context) {
        if (client.world == null || message.dimension != null && client.world.provider.getDimension() != message.dimension) {
            return;
        }
        RiftLibMod.PROXY.spawnParticle(
                decodeDefinition(message.definition), message.x, message.y, message.z,
                message.rotationX, message.rotationY, message.variables
        );
    }

    @NotNull
    public static String encodeDefinition(@NotNull ParticleBuilder builder) {
        RawParticle raw = ParticleBuilder.toRawParticle(builder);
        RawParticle.RawParticleDescription metadata = raw.rawParticleEffect.description;
        Map<String, RawParticleComponent> components = raw.rawParticleEffect.components;
        JsonObject root = new JsonObject();
        JsonObject effect = new JsonObject();
        JsonObject description = new JsonObject();
        description.addProperty("identifier", metadata.identifier);
        JsonObject rendering = new JsonObject();
        rendering.addProperty("texture", metadata.basicRenderParameters.texture);
        rendering.addProperty("material", metadata.basicRenderParameters.material);
        description.add("basic_render_parameters", rendering);
        effect.add("description", description);
        JsonObject componentJson = new JsonObject();
        for (Map.Entry<String, RawParticleComponent> entry : components.entrySet()) {
            Map<String, RawParticleComponent.ComponentValue> values = entry.getValue().componentValues;
            if (values.size() == 1 && values.containsKey("$value")) {
                componentJson.add(entry.getKey(), values.get("$value").toJson());
            }
            else componentJson.add(entry.getKey(), new RawParticleComponent.ObjectValue(values).toJson());
        }
        effect.add("components", componentJson);
        root.add("particle_effect", effect);
        return gson.toJson(root);
    }

    @NotNull
    public static ParticleBuilder decodeDefinition(@NotNull String definition) {
        return ParticleBuilder.fromRawParticle(gson.fromJson(definition, RawParticle.class));
    }
}