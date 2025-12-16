package anightdazingzoroark.riftlib.particle.particleComponent.particleMotion;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleMotionDynamicComponent extends RiftLibParticleComponent {
    private IValue[] linearAcceleration = new IValue[]{MolangParser.ZERO, MolangParser.ZERO, MolangParser.ZERO};
    private IValue linearDragCoefficient = MolangParser.ZERO;
    private IValue rotationAcceleration = MolangParser.ZERO;
    private IValue rotationDragCoefficient = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("linear_acceleration")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("linear_acceleration");
            this.linearAcceleration = this.parseExpressionArray(parser, 3, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("linear_drag_coefficient")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("linear_drag_coefficient");
            this.linearDragCoefficient = this.parseExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_acceleration")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_acceleration");
            this.rotationAcceleration = this.parseExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("rotation_drag_coefficient")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("rotation_drag_coefficient");
            this.rotationDragCoefficient = this.parseExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.linearAcceleration = this.linearAcceleration;
        particle.linearDragCoefficient = this.linearDragCoefficient;
        particle.rotationAcceleration = this.rotationAcceleration;
        particle.rotationDragCoefficient = this.rotationDragCoefficient;
    }
}
