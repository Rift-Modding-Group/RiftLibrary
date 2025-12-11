package anightdazingzoroark.riftlib.particle.particleComponent.lifetime.emiter;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.Map;

public class EmitterLifetimeExpressionComponent extends RiftLibEmitterLifetimeComponent {
    //condition in which emitter activates
    public IValue emitterActivationValue = MolangParser.ONE;
    //condition in which emitter expires
    public IValue emitterExpirationValue = MolangParser.ZERO;

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        //when the expression is non-zero, the emitter will emit particles.
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("activation_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("activation_expression");
            this.emitterActivationValue = parseExpression(parser, componentValue);
        }
        //will expire if the expression is non-zero.
        //evaluated every frame
        if (rawComponent.getValue().componentValues.containsKey("expiration_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("expiration_expression");
            this.emitterExpirationValue = parseExpression(parser, componentValue);
        }
    }
}
