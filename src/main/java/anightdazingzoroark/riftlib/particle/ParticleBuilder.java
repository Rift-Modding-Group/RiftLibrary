package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ParticleBuilder {
    public String identifier;
    public ResourceLocation texture;
    public ParticleMaterial material;
    public MolangParser molangParser;
    public final List<RiftLibParticleComponent> particleComponents = new ArrayList<>();
}
