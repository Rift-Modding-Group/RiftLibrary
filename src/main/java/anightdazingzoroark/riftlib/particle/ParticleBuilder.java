package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticle;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the information used to create particle emitters.
 */
public class ParticleBuilder {
    @Nullable
    private String identifier;
    @Nullable
    private ResourceLocation texture;
    @Nullable
    private ParticleMaterial material;
    @NotNull
    private final List<RiftLibEmitterComponent.Builder<?>> emitterComponents = new ArrayList<>();
    @NotNull
    private final List<RiftLibParticleComponent.Builder<?>> particleComponents = new ArrayList<>();
    @NotNull
    private final Map<String, RawParticleComponent> rawComponents = new LinkedHashMap<>();

    @NotNull
    public ParticleBuilder setIdentifier(@NotNull String identifier) {
        this.identifier = Objects.requireNonNull(identifier);
        return this;
    }

    @NotNull
    public ParticleBuilder setTexture(@NotNull ResourceLocation texture) {
        this.texture = Objects.requireNonNull(texture);
        return this;
    }

    @NotNull
    public ParticleBuilder setMaterial(@NotNull ParticleMaterial material) {
        this.material = Objects.requireNonNull(material);
        return this;
    }

    @NotNull
    public ParticleBuilder addEmitterComponent(@NotNull RiftLibEmitterComponent.Builder<?> component) {
        this.emitterComponents.add(Objects.requireNonNull(component));
        return this;
    }

    @NotNull
    public ParticleBuilder addParticleComponent(@NotNull RiftLibParticleComponent.Builder<?> component) {
        this.particleComponents.add(Objects.requireNonNull(component));
        return this;
    }

    /**
     * Builds a constructor with fresh component instances.
     */
    @SideOnly(Side.CLIENT)
    @NotNull
    public ParticleInformation build() {
        ParticleInformation result = new ParticleInformation();
        result.identifier = Objects.requireNonNull(this.identifier, "Particle identifier must be set");
        result.texture = Objects.requireNonNull(this.texture, "Particle texture must be set");
        result.material = Objects.requireNonNull(this.material, "Particle material must be set");
        result.molangParser = new MolangParser();
        for (Map.Entry<String, RawParticleComponent> entry : this.rawComponents.entrySet()) {
            if (RiftLibParticleComponentRegistry.isEmitterComponent(entry.getKey())) {
                RiftLibEmitterComponent component = RiftLibParticleComponentRegistry.createEmitterComponent(entry.getKey());
                component.parseRawComponent(entry, result.molangParser);
                result.emitterComponents.add(component);
            }
            if (RiftLibParticleComponentRegistry.isParticleComponent(entry.getKey())) {
                RawParticleComponent raw = new RawParticleComponent();
                raw.componentValues = RawParticleComponent.fromValue(new RawParticleComponent.ObjectValue(entry.getValue().componentValues)).asObject();
                result.rawParticleComponents.add(new ImmutablePair<>(entry.getKey(), raw));
            }
        }
        for (RiftLibEmitterComponent.Builder<?> component : this.emitterComponents) {
            result.emitterComponents.add(component.build(result.molangParser));
        }
        for (RiftLibParticleComponent.Builder<?> component : this.particleComponents) {
            result.particleComponents.add(component.build(result.molangParser));
        }
        return result;
    }

    /**
     * Captures settings for transfer without constructing runtime components.
     */
    @NotNull
    public static RawParticle toRawParticle(@NotNull ParticleBuilder builder) {
        RawParticle raw = new RawParticle();
        raw.rawParticleEffect = new RawParticle.RawParticleEffect();
        raw.rawParticleEffect.description = new RawParticle.RawParticleDescription();
        raw.rawParticleEffect.description.identifier = Objects.requireNonNull(builder.identifier, "Particle identifier must be set");
        RawParticle.RawBasicRenderParameters rendering = new RawParticle.RawBasicRenderParameters();
        rendering.texture = Objects.requireNonNull(builder.texture, "Particle texture must be set").toString();
        rendering.material = Objects.requireNonNull(builder.material, "Particle material must be set").materialName;
        raw.rawParticleEffect.description.basicRenderParameters = rendering;
        Map<String, RawParticleComponent> components = new LinkedHashMap<>();
        for (Map.Entry<String, RawParticleComponent> entry : builder.rawComponents.entrySet()) {
            RawParticleComponent component = new RawParticleComponent();
            component.componentValues = RawParticleComponent.fromValue(new RawParticleComponent.ObjectValue(entry.getValue().componentValues)).asObject();
            components.put(entry.getKey(), component);
        }
        for (RiftLibEmitterComponent.Builder<?> componentBuilder : builder.emitterComponents) {
            RiftLibEmitterComponent.RawComponent definition = componentBuilder.toRawComponent();
            RawParticleComponent component = new RawParticleComponent();
            component.componentValues = RawParticleComponent.fromValue(definition.fields()).asObject();
            components.put(definition.identifier(), component);
        }
        for (RiftLibParticleComponent.Builder<?> componentBuilder : builder.particleComponents) {
            RiftLibParticleComponent.RawComponent definition = componentBuilder.toRawComponent();
            RawParticleComponent component = new RawParticleComponent();
            component.componentValues = RawParticleComponent.fromValue(definition.fields()).asObject();
            components.put(definition.identifier(), component);
        }
        raw.rawParticleEffect.components = components;
        return raw;
    }

    /**
     * Restores transferable settings; runtime construction is deferred to build().
     */
    @NotNull
    public static ParticleBuilder fromRawParticle(@NotNull RawParticle raw) {
        RawParticle.RawParticleDescription description = raw.rawParticleEffect.description;
        ParticleBuilder builder = new ParticleBuilder().setIdentifier(description.identifier)
                .setTexture(new ResourceLocation(description.basicRenderParameters.texture))
                .setMaterial(ParticleMaterial.getMaterialFromString(description.basicRenderParameters.material));
        for (Map.Entry<String, RawParticleComponent> entry : raw.rawParticleEffect.components.entrySet()) {
            RawParticleComponent component = new RawParticleComponent();
            component.componentValues = RawParticleComponent.fromValue(new RawParticleComponent.ObjectValue(entry.getValue().componentValues)).asObject();
            builder.rawComponents.put(entry.getKey(), component);
        }
        return builder;
    }

    /**
     * Stores metadata and component definitions without building runtime components.
     */
    @NotNull
    public static NBTTagCompound toNBT(@NotNull ParticleBuilder builder) {
        RawParticle raw = toRawParticle(builder);
        RawParticle.RawParticleDescription description = raw.rawParticleEffect.description;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("identifier", description.identifier);
        tag.setString("texture", description.basicRenderParameters.texture);
        tag.setString("material", description.basicRenderParameters.material);
        NBTTagCompound components = new NBTTagCompound();
        for (Map.Entry<String, RawParticleComponent> entry : raw.rawParticleEffect.components.entrySet()) {
            components.setTag(entry.getKey(), writeComponentValue(new RawParticleComponent.ObjectValue(entry.getValue().componentValues)));
        }
        tag.setTag("components", components);
        return tag;
    }

    /**
     * Restores a builder from toNBT() output; runtime construction remains deferred.
     */
    @NotNull
    public static ParticleBuilder fromNBT(@NotNull NBTTagCompound tag) {
        if (!tag.hasKey("identifier", 8) || !tag.hasKey("texture", 8)
                || !tag.hasKey("material", 8) || !tag.hasKey("components", 10)) {
            throw new IllegalArgumentException("Particle NBT requires identifier, texture, material, and components");
        }
        ParticleBuilder builder = new ParticleBuilder().setIdentifier(tag.getString("identifier"))
                .setTexture(new ResourceLocation(tag.getString("texture")))
                .setMaterial(ParticleMaterial.getMaterialFromString(tag.getString("material")));
        NBTTagCompound components = tag.getCompoundTag("components");
        for (String identifier : components.getKeySet()) {
            if (!components.hasKey(identifier, 10)) {
                throw new IllegalArgumentException("Particle component must be an NBT compound: " + identifier);
            }
            RawParticleComponent component = new RawParticleComponent();
            component.componentValues = readComponentValue(components.getCompoundTag(identifier)).asObject();
            builder.rawComponents.put(identifier, component);
        }
        return builder;
    }

    //helper for writing component to nbt
    @NotNull
    private static NBTBase writeComponentValue(@NotNull RawParticleComponent.ComponentValue value) {
        return switch (value) {
            case RawParticleComponent.MolangValue(RawMolangValue raw) -> {
                if (raw.stringValue != null) yield new NBTTagString(raw.stringValue);
                yield new NBTTagDouble(Objects.requireNonNull(raw.numericalValue, "Empty Molang value"));
            }
            case RawParticleComponent.BooleanValue(boolean bool) -> new NBTTagByte((byte) (bool ? 1 : 0));
            case RawParticleComponent.ArrayValue(List<RawParticleComponent.ComponentValue> values) -> {
                NBTTagList array = new NBTTagList();
                // Wrapping entries preserves mixed numbers, expressions, and nested values.
                for (RawParticleComponent.ComponentValue element : values) {
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setTag("value", writeComponentValue(element));
                    array.appendTag(entry);
                }
                yield array;
            }
            case RawParticleComponent.ObjectValue(Map<String, RawParticleComponent.ComponentValue> values) -> {
                NBTTagCompound object = new NBTTagCompound();
                for (Map.Entry<String, RawParticleComponent.ComponentValue> entry : values.entrySet()) {
                    object.setTag(entry.getKey(), writeComponentValue(entry.getValue()));
                }
                yield object;
            }
        };
    }

    //helper for writing component from nbt
    @NotNull
    private static RawParticleComponent.ComponentValue readComponentValue(@NotNull NBTBase tag) {
        return switch (tag) {
            case NBTTagByte bool -> new RawParticleComponent.BooleanValue(bool.getByte() != 0);
            case NBTPrimitive number -> RawParticleComponent.fromValue(number.getDouble());
            case NBTTagString string -> RawParticleComponent.fromValue(string.getString());
            case NBTTagList array -> {
                if (array.tagCount() > 0 && array.getTagType() != 10) {
                    throw new IllegalArgumentException("Particle NBT arrays require compound entries");
                }
                List<RawParticleComponent.ComponentValue> values = new ArrayList<>(array.tagCount());
                for (int index = 0; index < array.tagCount(); index++) {
                    NBTTagCompound entry = array.getCompoundTagAt(index);
                    if (!entry.hasKey("value")) {
                        throw new IllegalArgumentException("Particle NBT array entry is missing its value");
                    }
                    values.add(readComponentValue(Objects.requireNonNull(entry.getTag("value"))));
                }
                yield new RawParticleComponent.ArrayValue(values);
            }
            case NBTTagCompound object -> {
                Map<String, RawParticleComponent.ComponentValue> values = new LinkedHashMap<>();
                for (String key : object.getKeySet()) {
                    values.put(key, readComponentValue(Objects.requireNonNull(object.getTag(key))));
                }
                yield new RawParticleComponent.ObjectValue(values);
            }
            default -> throw new IllegalArgumentException("Unsupported particle NBT tag: " + tag.getId());
        };
    }
}
