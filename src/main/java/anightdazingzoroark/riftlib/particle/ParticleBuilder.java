package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.Constant;
import anightdazingzoroark.riftlib.molang.math.IValue;
import net.minecraft.util.ResourceLocation;

public class ParticleBuilder {
    //-----particle information-----
    public String identifier;
    public ResourceLocation texture;
    public ParticleMaterial material;

    //-----particle appearance billboard information-----
    public IValue[] size;
    public int textureWidth, textureHeight;
    public IValue[] uv;
    public IValue[] uvSize;

    //-----emitter information-----
    //condition in which emitter activates
    public IValue emitterActivationValue;
    //condition in which emitter expires
    public IValue emitterExpirationValue;

    //-----emitted particle information-----
    //condition in which a particle expires
    public IValue particleExpirationValue;
    //time until particle expires
    public IValue particleLifetimeValue;

    //-----emitter shape information-----
    public EmitterShape emitterShape;

    //-----emitter rate information-----
    public EmitterRate emitterRate;

    //-----emitter shape classes-----
    public abstract static class EmitterShape {
        public IValue[] offset = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
        public String particleDirection;
        public IValue[] customParticleDirection;
    }

    public static class DiscEmitterShape extends EmitterShape {
        public String discPlaneDirection;
        public IValue[] customDiscPlaneDirection;
        public IValue radius = MolangParser.ONE;
        public boolean surfaceOnly;
    }

    public static class BoxEmitterShape extends EmitterShape {
        public IValue halfDimensions;
        public boolean surfaceOnly;
    }

    public static class CustomEmitterShape extends EmitterShape {}

    public static class EntityAABBEmitterShape extends EmitterShape {
        public boolean surfaceOnly;
    }

    public static class PointEmitterShape extends EmitterShape {}

    public static class SphereEmitterShape extends EmitterShape {
        public IValue radius = MolangParser.ONE;
        public boolean surfaceOnly;
    }

    //-----emitter rate classes-----
    public abstract static class EmitterRate {}

    public static class InstantEmitterRate extends EmitterRate {
        public IValue particleCount = new Constant(10f);
    }

    public static class ManualEmitterRate extends EmitterRate {
        public IValue maxParticleCount = new Constant(50f);
    }

    public static class SteadyEmitterRate extends EmitterRate {
        public IValue particleSpawnRate = MolangParser.ONE;
        public IValue maxParticleCount = new Constant(50f);
    }
}
