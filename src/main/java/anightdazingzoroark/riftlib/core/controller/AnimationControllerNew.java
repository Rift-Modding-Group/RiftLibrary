package anightdazingzoroark.riftlib.core.controller;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.AnimationState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;

public class AnimationControllerNew<A extends IAnimatable<D>, D extends AbstractAnimationData<?>> extends AnimationController<A> {
    private final Map<String, AnimationControllerState<D>> animControllerStates = new LinkedHashMap<>();
    private final String initialState;

    @NotNull
    private String currentState;

    private D processingData;
    private MolangParser processingParser;
    private MolangScope processingScope;
    private boolean initialized;

    @SafeVarargs
    public AnimationControllerNew(A animatable, String name, @NonNull String initialState, AnimationControllerState<D>... controllerStates) {
        super(animatable, name, 0, event -> PlayState.STOP);
        this.initialState = initialState;
        this.currentState = initialState;

        for (AnimationControllerState<D> controllerState : controllerStates) {
            AnimationControllerState<D> previousState = this.animControllerStates.put(controllerState.name, controllerState);
            if (previousState != null) {
                throw new IllegalArgumentException("Duplicate animation controller state: " + controllerState.name);
            }
        }

        if (!this.animControllerStates.containsKey(initialState)) {
            throw new IllegalArgumentException("Missing initial animation controller state: " + initialState);
        }
    }

    @NotNull
    public String getCurrentState() {
        return this.currentState;
    }

    public AnimationControllerState<D> getCurrentControllerState() {
        return this.getState(this.currentState);
    }

    public AnimationControllerState<D> getState(String stateName) {
        AnimationControllerState<D> controllerState = this.animControllerStates.get(stateName);
        if (controllerState == null) {
            throw new IllegalArgumentException("Unknown animation controller state: " + stateName);
        }
        return controllerState;
    }

    public Collection<AnimationControllerState<D>> getStates() {
        return this.animControllerStates.values();
    }

    public boolean isCurrentState(String stateName) {
        return this.currentState.equals(stateName);
    }

    public boolean allAnimationsFinished() {
        return this.getAnimationState() == AnimationState.Stopped && this.currentAnimation != null && this.animationQueue.peek() == null;
    }

    @Override
    public void process(AbstractAnimationData<?> data, double tick, AnimationEvent event,
                        List<IBone> modelRendererList,
                        HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
                        MolangParser parser, MolangScope scope, boolean crashWhenCantFindBone) {
        this.processingData = (D) data;
        this.processingParser = parser;
        this.processingScope = scope;
        try {
            super.process(data, tick, event, modelRendererList, boneSnapshotCollection, parser, scope, crashWhenCantFindBone);
        }
        finally {
            this.processingData = null;
            this.processingParser = null;
            this.processingScope = null;
        }
    }

    @Override
    protected PlayState testAnimationPredicate(AnimationEvent event) {
        if (this.processingData == null || this.processingParser == null || this.processingScope == null) {
            return PlayState.STOP;
        }

        if (!this.initialized) {
            this.currentState = this.initialState;
            this.applyEffects(this.getCurrentControllerState().getEntryEffects());
            this.initialized = true;
        }

        AnimationControllerState<D> currentControllerState = this.getCurrentControllerState();
        this.applyTransitions(currentControllerState);
        currentControllerState = this.getCurrentControllerState();

        AnimationBuilder animationBuilder = new AnimationBuilder();
        for (Map.Entry<String, Function<D, Boolean>> animationEntry : currentControllerState.getAnimations().entrySet()) {
            if (Boolean.TRUE.equals(animationEntry.getValue().apply(this.processingData))) {
                animationBuilder.addAnimation(animationEntry.getKey());
            }
        }

        if (animationBuilder.getRawAnimationList().isEmpty()) {
            this.clearAnimationCache();
            return PlayState.STOP;
        }

        this.setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }

    private void applyTransitions(AnimationControllerState<D> currentControllerState) {
        for (ImmutablePair<String, Function<D, Boolean>> transitionEntry : currentControllerState.getStateTransitions()) {
            if (!transitionEntry.right.apply(this.processingData)) continue;

            String nextStateName = transitionEntry.left;
            if (this.currentState.equals(nextStateName)) return;

            this.applyEffects(currentControllerState.getExitEffects());
            this.transitionLengthTicks = currentControllerState.transitionLength;
            this.currentState = this.getState(nextStateName).name;
            this.applyEffects(this.getCurrentControllerState().getEntryEffects());
            return;
        }
    }

    private void applyEffects(Collection<AnimatableValue> effects) {
        this.processingParser.withScope(this.processingScope, () -> {
            for (AnimatableValue effect : effects) {
                if (effect.isExpression()) {
                    try {
                        this.processingParser.parseExpression(effect.getExpressionValue()).get();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }

                ImmutablePair<String, Double> constantValue = effect.getConstantValue();
                if (this.processingParser.isQuery(constantValue.left)) {
                    throw new RuntimeException(new MolangException("Cannot assign value to query '" + constantValue.left + "'!"));
                }
                this.processingParser.setValue(constantValue.left, constantValue.right);
            }
        });
    }
}
