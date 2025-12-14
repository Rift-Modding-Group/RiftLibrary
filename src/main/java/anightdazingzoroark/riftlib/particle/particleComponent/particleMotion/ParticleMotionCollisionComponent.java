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

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("enabled")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("enabled");
            this.enabled = this.parseBooleanExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticle particle) {

    }
}
