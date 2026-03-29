package anightdazingzoroark.riftlib.particle.emitterComponent.emitterShape;

import anightdazingzoroark.riftlib.exceptions.InvalidValueException;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class EmitterShapeDiscComponent extends RiftLibEmitterShapeComponent {
    private IValue[] planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
    private IValue radius = MolangParser.ZERO;
    private boolean surfaceOnly;
    private String particleDirection;
    private IValue[] customParticleDirection;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("plane_normal")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("plane_normal");

            //assuming its a string, we have to choose between x, y, and z directions
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                switch (componentValue.string) {
                    case "x":
                        this.planeNormal = new IValue[]{MolangParser.ONE, MolangParser.ZERO, MolangParser.ZERO};
                        break;
                    case "y":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ONE, MolangParser.ZERO};
                        break;
                    case "z":
                        this.planeNormal = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ONE};
                        break;
                    default:
                        throw new MolangException("Invalid value for plane direction!");
                }
            }
            else if (componentValue.valueType == RawParticleComponent.ComponentValueType.ARRAY) {
                this.planeNormal = this.parseExpressionArray(parser, 3, componentValue);
            }
        }
        if (rawComponent.getValue().componentValues.containsKey("offset")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("offset");
            this.offset = parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("radius")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("radius");
            this.radius = parseExpression(parser, componentValue);
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
        //disc formula is x^2 + y^2 = r^2
        Vec3d vecNormal = new Vec3d(
                this.planeNormal[0].get(),
                this.planeNormal[1].get(),
                this.planeNormal[2].get()
        ).normalize();

        //orthonormal basis spanning the disc plane
        Vec3d helper = (Math.abs(vecNormal.y) < 1) ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
        Vec3d vecX = vecNormal.crossProduct(helper).normalize();
        Vec3d vecY = vecNormal.crossProduct(vecX).normalize();

        double radius = this.surfaceOnly ? this.radius.get() : emitter.random.nextDouble() * this.radius.get();
        double theta = 2 * Math.PI * emitter.random.nextDouble();

        Vec3d inPlane = vecX.scale(radius * Math.cos(theta)).add(vecY.scale(radius * Math.sin(theta)));

        return new Vec3d(
                inPlane.x + this.offset[0].get(),
                inPlane.y + this.offset[1].get(),
                inPlane.z + this.offset[2].get()
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
