package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Information passed to a particle emitter to create it
 * */
public class ParticleInformation {
    public String identifier;
    public ResourceLocation texture;
    public ParticleMaterial material;
    public MolangParser molangParser;
    public final List<RiftLibEmitterComponent> emitterComponents = new ArrayList<>();
    public final List<ImmutablePair<String, RawParticleComponent>> rawParticleComponents = new ArrayList<>();
    public final List<RiftLibParticleComponent> particleComponents = new ArrayList<>();
}