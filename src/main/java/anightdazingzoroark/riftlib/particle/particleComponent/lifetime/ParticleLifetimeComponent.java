package anightdazingzoroark.riftlib.particle.particleComponent.lifetime;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.particleComponent.RiftLibParticleComponent;

import java.util.Map;

public class ParticleLifetimeComponent extends RiftLibParticleComponent {
    //condition in which a particle expires
    public IValue particleExpirationValue = MolangParser.ZERO;
    //time until particle expires
    public IValue particleLifetimeValue;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        //this expression makes the particle expire when true (non-zero)
        //evaluated once per particle
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
            this.particleExpirationValue = parseExpression(parser, componentValue);
        }
        //particle will expire after this much time, evaluated once
        if (rawComponent.getValue().componentValues.containsKey("max_lifetime")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("max_lifetime");
            this.particleLifetimeValue = parseExpression(parser, componentValue);
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.particleExpiration = this.particleExpirationValue;
        emitter.particleMaxLifetime = this.particleLifetimeValue;
    }
}
