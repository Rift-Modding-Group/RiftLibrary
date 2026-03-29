package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
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
        //in cubes, |x|, |y|, and |z| are less than or equal to associated dimension length
        double randomX = emitter.random.nextDouble() * this.halfDimensions[0].get() * 2 - this.halfDimensions[0].get();
        double randomY = emitter.random.nextDouble() * this.halfDimensions[1].get() * 2 - this.halfDimensions[1].get();
        double randomZ = emitter.random.nextDouble() * this.halfDimensions[2].get() * 2 - this.halfDimensions[2].get();

        if (this.surfaceOnly) {
            int face = emitter.random.nextInt(6);

            return switch (face) {
                //positive x
                case 0 -> new Vec3d(
                        this.halfDimensions[0].get() + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //negative x
                case 1 -> new Vec3d(
                        -this.halfDimensions[0].get() + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //positive y
                case 2 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        this.halfDimensions[1].get() + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //negative y
                case 3 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        -this.halfDimensions[1].get() + this.offset[1].get(),
                        randomZ + this.offset[2].get()
                );
                //positive z
                case 4 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        this.halfDimensions[2].get() + this.offset[2].get()
                );
                //negative z
                case 5 -> new Vec3d(
                        randomX + this.offset[0].get(),
                        randomY + this.offset[1].get(),
                        -this.halfDimensions[2].get() + this.offset[2].get()
                );
                default -> Vec3d.ZERO;
            };
        }
        else return new Vec3d(
                randomX + this.offset[0].get(),
                randomY + this.offset[1].get(),
                randomZ + this.offset[2].get()
        );
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
