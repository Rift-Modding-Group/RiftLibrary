package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticleBuilder {
    public String identifier;
    public ResourceLocation texture;
    public ParticleMaterial material;
    public MolangParser molangParser;
    public final List<RiftLibEmitterComponent> emitterComponents = new ArrayList<>();
    public final List<ImmutablePair<String, RawParticleComponent>> rawParticleComponents = new ArrayList<>(); //note that they're not yet parsed here
}
