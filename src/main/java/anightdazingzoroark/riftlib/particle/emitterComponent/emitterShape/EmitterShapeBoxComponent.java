package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.shape.threeDimShape.RiftLibBoxShape;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class EmitterShapeBoxComponent extends RiftLibEmitterShapeComponent {
    public boolean surfaceOnly;
    public IValue[] halfDimensions;
    public String particleDirection;
    public IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("half_dimensions")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("half_dimensions");
            this.halfDimensions = this.parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("surface_only")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("surface_only");
            this.surfaceOnly = componentValue.bool == Boolean.TRUE;
        }
        if (rawComponent.getValue().componentValues.containsKey("direction")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("direction");
            //direction as string
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                if (componentValue.string.equals("inwards") || componentValue.string.equals("outwards")) {
                    this.particleDirection = componentValue.string;
                }
                else throw new InvalidValueException("Invalid value "+componentValue.string+" for direction!");
            }
            //custom direction
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                this.customParticleDirection = parseExpressionArray(parser, 3, componentValue);
            }
        }
    }

    @Override
    public Vec3d defineParticleOffset(RiftLibParticleEmitter emitter) {
        return new RiftLibBoxShape(
                this.getOffsetVector(),
                this.halfDimensions[0].get() * 2.0,
                this.halfDimensions[1].get() * 2.0,
                this.halfDimensions[2].get() * 2.0
        ).randomPoint(this.surfaceOnly);
    }

    @Override
    public Vec3d defineDirection(RiftLibParticleEmitter emitter, double emissionX, double emissionY, double emissionZ) {
        //if it has custom particle direction, just return it instead
        if (this.customParticleDirection != null) {
            return new Vec3d(
                    this.customParticleDirection[0].get(),
                    this.customParticleDirection[1].get(),
                    this.customParticleDirection[2].get()
            ).normalize();
        }
        else {
            //generally the direction to go towards should be the same as its xyz offset from the center of sphere emitter
            int pointer = this.particleDirection.equals("outwards") ? 1 : this.particleDirection.equals("inwards") ? -1 : 0;
            return new Vec3d(
                    pointer * (emissionX - emitter.posX),
                    pointer * (emissionY - emitter.posY),
                    pointer * (emissionZ - emitter.posZ)
            ).normalize();
        }
    }
}
