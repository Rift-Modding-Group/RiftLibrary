package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public abstract class RiftLibEmitterShapeComponent extends RiftLibEmitterComponent {
    protected IValue[] offset = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};

    public void applyComponent(@NotNull RiftLibParticleEmitter emitter) {
        emitter.setEmitterShape(this);
    }

    public abstract Vec3d defineParticleOffset(RiftLibParticleEmitter emitter);

    public abstract Vec3d defineDirection(RiftLibParticleEmitter emitter, double emissionX, double emissionY, double emissionZ);
}
