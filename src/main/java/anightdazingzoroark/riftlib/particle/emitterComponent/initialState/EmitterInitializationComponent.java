package anightdazingzoroark.riftlib.particle.emitterComponent.initialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.expressions.MolangAssignment;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangMultiStatement;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmitterInitializationComponent extends RiftLibEmitterComponent {
    private List<MolangExpression> initialOperations = new ArrayList<>();
    private List<MolangExpression> repeatingOperations = new ArrayList<>();

    @Override
    public void parseRawComponent(Map.Entry<String, RawParticleComponent> rawComponent, MolangParser parser) throws MolangException {
        if (rawComponent.getValue().componentValues.containsKey("creation_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("creation_expression");

            //parse the expression to find initial operations
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                MolangMultiStatement molangMultiStatement = (MolangMultiStatement) parser.parseExpression(componentValue.string);

                if (molangMultiStatement != null) this.initialOperations = new ArrayList<>(molangMultiStatement.expressions);
            }
        }
        if (rawComponent.getValue().componentValues.containsKey("per_update_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("per_update_expression");

            //parse the expression to find repeating operations
            if (componentValue.valueType == RawParticleComponent.ComponentValueType.STRING) {
                MolangMultiStatement molangMultiStatement = (MolangMultiStatement) parser.parseExpression(componentValue.string);

                if (molangMultiStatement != null) this.repeatingOperations = new ArrayList<>(molangMultiStatement.expressions);
            }
        }
    }

    @Override
    public void applyComponent(RiftLibParticleEmitter emitter) {
        emitter.initialOperations = this.initialOperations;
        emitter.repeatingOperations = this.repeatingOperations;
    }
}
