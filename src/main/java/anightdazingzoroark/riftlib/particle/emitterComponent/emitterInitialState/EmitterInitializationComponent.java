package anightdazingzoroark.riftlib.particle.emitterComponent.emitterInitialState;

import anightdazingzoroark.riftlib.jsonParsing.raw.RawMolangValue;
import anightdazingzoroark.riftlib.jsonParsing.raw.particle.RawParticleComponent;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.expressions.MolangExpression;
import anightdazingzoroark.riftlib.molang.expressions.MolangMultiStatement;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.emitterComponent.RiftLibEmitterComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EmitterInitializationComponent extends RiftLibEmitterComponent {
    private List<MolangExpression> initialOperations = new ArrayList<>();
    private List<MolangExpression> repeatingOperations = new ArrayList<>();

    @Override
    public void parseRawComponent(@NotNull Map.Entry<String, RawParticleComponent> rawComponent, @NotNull MolangParser parser) {
        if (rawComponent.getValue().componentValues.containsKey("creation_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("creation_expression");

            //parse the expression to find initial operations
            if (componentValue.isString()) {
                MolangMultiStatement molangMultiStatement = (MolangMultiStatement) componentValue.asMolang().compile(parser);
                this.initialOperations = new ArrayList<>(molangMultiStatement.getExpressions());
            } else
                throw new IllegalArgumentException("'creation_expression' in 'minecraft:emitter_initialization' must be an expression!");
        }
        if (rawComponent.getValue().componentValues.containsKey("per_update_expression")) {
            RawParticleComponent.ComponentValue componentValue = rawComponent.getValue().componentValues.get("per_update_expression");

            //parse the expression to find repeating operations
            if (componentValue.isString()) {
                MolangMultiStatement molangMultiStatement = (MolangMultiStatement) componentValue.asMolang().compile(parser);
                this.repeatingOperations = new ArrayList<>(molangMultiStatement.getExpressions());
            } else
                throw new IllegalArgumentException("'per_update_expression' in 'minecraft:emitter_initialization' must be an expression!");
        }
    }

    @Override
    public void applyComponent(@NotNull RiftLibParticleEmitter emitter) {
        emitter.setInitializationOperations(this.initialOperations, this.repeatingOperations);
    }

    public static class Builder extends RiftLibEmitterComponent.Builder<EmitterInitializationComponent> {
        private final EmitterInitializationComponent toReturn = new EmitterInitializationComponent();

        @NotNull
        public Builder setCreationExpression(@NotNull String expression) {
            this.toReturn.initialOperations = List.of(RawMolangValue.compileExpression(expression));
            return this;
        }

        @NotNull
        public Builder setCreationExpression(@NotNull MolangExpression... expressions) {
            this.toReturn.initialOperations = List.of(expressions);
            return this;
        }

        @NotNull
        public Builder setPerUpdateExpression(@NotNull String expression) {
            this.toReturn.repeatingOperations = List.of(RawMolangValue.compileExpression(expression));
            return this;
        }

        @NotNull
        public Builder setPerUpdateExpression(@NotNull MolangExpression... expressions) {
            this.toReturn.repeatingOperations = List.of(expressions);
            return this;
        }

        @Override
        @NotNull
        public RawComponent toRawComponent() {
            return new RawComponent() {
                @Override
                @NotNull
                public String identifier() {
                    return "minecraft:emitter_initialization";
                }

                @Override
                @NotNull
                public Map<String, Object> fields() {
                    String creation = null;
                    String update = null;
                    if (!Builder.this.toReturn.initialOperations.isEmpty()) {
                        creation = new MolangMultiStatement(Builder.this.toReturn.initialOperations).toString();
                    }
                    if (!Builder.this.toReturn.repeatingOperations.isEmpty()) {
                        update = new MolangMultiStatement(Builder.this.toReturn.repeatingOperations).toString();
                    }
                    Map<String, Object> fields = new HashMap<>();
                    if (creation != null) fields.put("creation_expression", creation);
                    if (update != null) fields.put("per_update_expression", update);
                    return fields;
                }
            };
        }

        @Override
        @NotNull
        public Builder copy() {
            Builder result = new Builder();
            result.toReturn.initialOperations = List.copyOf(this.toReturn.initialOperations);
            result.toReturn.repeatingOperations = List.copyOf(this.toReturn.repeatingOperations);
            return result;
        }

        @Override
        @NotNull
        public EmitterInitializationComponent build(@NotNull MolangParser parser) {
            EmitterInitializationComponent result = new EmitterInitializationComponent();
            result.initialOperations = this.toReturn.initialOperations.stream().map(expression -> (MolangExpression) RawMolangValue.copy(expression, parser)).toList();
            result.repeatingOperations = this.toReturn.repeatingOperations.stream().map(expression -> (MolangExpression) RawMolangValue.copy(expression, parser)).toList();
            return result;
        }
    }
}
