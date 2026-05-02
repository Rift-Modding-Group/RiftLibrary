package anightdazingzoroark.riftlib.core.controller;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A state contains all the animations that will play on an animatable
 * and must have transition statements to other states. Should be
 * better than what we currently got for animations. And ye its based
 * on Bedrock's animation controller system but adapted to java.
 * */
public class AnimationControllerState<D extends AbstractAnimationData<?>> {
    public final String name;
    public final double transitionLength;
    private final LinkedHashMap<String, StateAnimation<D>> animations = new LinkedHashMap<>();
    private final List<ImmutablePair<String, Function<D, Boolean>>> stateTransitions = new ArrayList<>();
    private final List<AnimatableValue> onEntryAnimatableValues = new ArrayList<>();
    private final List<AnimatableValue> onExitAnimatableValues = new ArrayList<>();

    public AnimationControllerState(String name) {
        this(name, 0D);
    }

    /**
     * @param name The name of the state
     * @param transitionLength The time in seconds to cross-fade when transitioning away from this state
     * */
    public AnimationControllerState(String name, double transitionLength) {
        this.name = name;
        this.transitionLength = transitionLength;
    }

    public AnimationControllerState<D> addAnimation(String animationName) {
        return this.addAnimation(animationName, null, (data) -> true);
    }

    public AnimationControllerState<D> addAnimation(String animationName, LoopType loopType) {
        return this.addAnimation(animationName, loopType, (data) -> true);
    }

    /**
     * @param animationName The name of the animation
     * @param animationPredicate The predicate that ensures that the animation should play when in this state
     * */
    public AnimationControllerState<D> addAnimation(String animationName, Function<D, Boolean> animationPredicate) {
        return this.addAnimation(animationName, null, animationPredicate);
    }

    public AnimationControllerState<D> addAnimation(String animationName, LoopType loopType, Function<D, Boolean> animationPredicate) {
        this.animations.put(animationName, new StateAnimation<>(animationName, loopType, animationPredicate));
        return this;
    }

    public Map<String, StateAnimation<D>> getAnimations() {
        return this.animations;
    }

    /**
     * @param stateName The name of the state to transition to
     * @param transition The condition in which to transition to that state
     * */
    public AnimationControllerState<D> addStateTransition(String stateName, Function<D, Boolean> transition) {
        this.stateTransitions.add(new ImmutablePair<>(stateName, transition));
        return this;
    }

    public List<ImmutablePair<String, Function<D, Boolean>>> getStateTransitions() {
        return this.stateTransitions;
    }

    /**
     * This is basically a MoLang expression or a message that will take effect when entering this state
     * It also takes effect when initializing in this state
     * */
    public AnimationControllerState<D> addEntryEffect(AnimatableValue animatableValue) {
        this.onEntryAnimatableValues.add(animatableValue);
        return this;
    }

    public List<AnimatableValue> getEntryEffects() {
        return this.onEntryAnimatableValues;
    }

    /**
     * Same as addEntryEffect but for when exiting the state
     * */
    public AnimationControllerState<D> addExitEffect(AnimatableValue animatableValue) {
        this.onExitAnimatableValues.add(animatableValue);
        return this;
    }

    public List<AnimatableValue> getExitEffects() {
        return this.onExitAnimatableValues;
    }

    public static final class StateAnimation<D extends AbstractAnimationData<?>> {
        private final String name;
        private final LoopType loopType;
        private final Function<D, Boolean> predicate;

        private StateAnimation(String name, LoopType loopType, Function<D, Boolean> predicate) {
            this.name = name;
            this.loopType = loopType;
            this.predicate = predicate;
        }

        public String getName() {
            return this.name;
        }

        public LoopType getLoopType() {
            return this.loopType;
        }

        public Function<D, Boolean> getPredicate() {
            return this.predicate;
        }
    }
}
