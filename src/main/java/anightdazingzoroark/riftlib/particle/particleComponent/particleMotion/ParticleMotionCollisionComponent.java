package anightdazingzoroark.riftlib.particle.particleComponent.particleMotion;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticle;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleMotionCollisionComponent extends RiftLibParticleComponent {
    private IValue enabled = MolangParser.ONE;
    private float collisionDrag;
    private float coeffOfRestitution;
    private Float collisionRadius;
    private boolean expireOnContact;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("enabled")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("enabled");
            this.enabled = this.parseBooleanExpression(parser, componentValue);
        }
        if (rawComponent.getValue().componentValues.containsKey("collision_drag")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("collision_drag");
            this.collisionDrag = componentValue.number;
        }
        if (rawComponent.getValue().componentValues.containsKey("coefficient_of_restitution")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("coefficient_of_restitution");
            this.coeffOfRestitution = componentValue.number;
        }
        if (rawComponent.getValue().componentValues.containsKey("collision_radius")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("collision_radius");
            this.collisionRadius = componentValue.number;
        }
        if (rawComponent.getValue().componentValues.containsKey("expire_on_contact")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expire_on_contact");
            this.expireOnContact = componentValue.bool;
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {
        particle.collisionEnabled = this.enabled;
        particle.collisionDrag = this.collisionDrag;
        particle.coeffOfRestitution = this.coeffOfRestitution;
        particle.collisionRadius = this.collisionRadius;
        particle.expireOnContact = this.expireOnContact;
    }
}
