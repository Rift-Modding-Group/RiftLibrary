package anightdazingzoroark.riftlib.core.controller;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A state contains all the animations that will play on an animatable
 * and must have transition statements to other states. Should be
 * better than what we currently got for animations. And ye its based
 * on Bedrock's animation controller system but adapted to java.
 * */
public class AnimationControllerState<D extends AbstractAnimationData<?, ?>> {
    @NotNull
    public final String name;
    public final double transitionLength;
    @NotNull
    public EasingType transitionEasingType;
    //we want to preserve order in which anims r added, hence this
    @NotNull
    private final LinkedHashMap<String, StateAnimation<D>> animations = new LinkedHashMap<>();
    //same here but for states
    @NotNull
    private final LinkedHashMap<String, Function<D, Boolean>> stateTransitions = new LinkedHashMap<>();
    @NotNull
    private final List<Consumer<D>> onEntryEffects = new ArrayList<>();
    @NotNull
    private final List<Consumer<D>> onExitEffects = new ArrayList<>();
    @NotNull
    private final List<StateParticle> particleEffects = new ArrayList<>();

    public AnimationControllerState(@NotNull String name) {
        this(name, 0D);
    }

    /**
     * @param name The name of the state
     * @param transitionLength The time in seconds to cross-fade when transitioning away from this state
     * */
    public AnimationControllerState(@NotNull String name, double transitionLength) {
        this(name, transitionLength, EasingType.NONE);
    }

    /**
     * @param name The name of the state
     * @param transitionLength The time in seconds to cross-fade when transitioning away from this state
     * @param transitionEasingType Easing for the cross-fade only; NONE preserves the controller's easing
     * */
    public AnimationControllerState(@NotNull String name, double transitionLength, @NotNull EasingType transitionEasingType) {
        this.name = name;
        this.transitionLength = transitionLength * 20D;
        this.transitionEasingType = transitionEasingType;
    }

    /**
     * @param animationName The name of the animation
     * */
    @NotNull
    public AnimationControllerState<D> addAnimation(@NotNull String animationName) {
        this.animations.put(animationName, new StateAnimation<>(animationName, (data) -> true));
        return this;
    }

    /**
     * @param animationName The name of the animation
     * @param animationPredicate The predicate that ensures that the animation should play when in this state
     * */
    @NotNull
    public AnimationControllerState<D> addAnimation(@NotNull String animationName, @NotNull Function<D, Boolean> animationPredicate) {
        this.animations.put(animationName, new StateAnimation<>(animationName, animationPredicate));
        return this;
    }

    @NotNull
    public Map<String, StateAnimation<D>> getAnimations() {
        return this.animations;
    }

    /**
     * @param stateName The name of the state to transition to
     * @param transition The condition in which to transition to that state
     * */
    @NotNull
    public AnimationControllerState<D> addStateTransition(@NotNull String stateName, Function<D, Boolean> transition) {
        this.stateTransitions.put(stateName, transition);
        return this;
    }

    @NotNull
    public LinkedHashMap<String, Function<D, Boolean>> getStateTransitions() {
        return this.stateTransitions;
    }

    /**
     * This is basically a MoLang expression or a message that will take effect when entering this state
     * It also takes effect when initializing in this state
     * */
    @NotNull
    public AnimationControllerState<D> addEntryEffect(@NotNull Consumer<D> entryEffect) {
        this.onEntryEffects.add(entryEffect);
        return this;
    }

    @NotNull
    public List<Consumer<D>> getEntryEffects() {
        return this.onEntryEffects;
    }

    /**
     * Same as addEntryEffect but for when exiting the state
     * */
    @NotNull
    public AnimationControllerState<D> addExitEffect(@NotNull Consumer<D> entryEffect) {
        this.onExitEffects.add(entryEffect);
        return this;
    }

    @NotNull
    public List<Consumer<D>> getExitEffects() {
        return this.onExitEffects;
    }

    /**
     * Defines the particle to create when entering this state. This particle will
     * be deleted when transitioning away from this state
     * */
    @NotNull
    public AnimationControllerState<D> addParticleEffect(@NotNull String particleName, @NotNull String particleLocator) {
        this.particleEffects.add(new StateParticle(particleName, particleLocator));
        return this;
    }

    @NotNull
    public List<StateParticle> getParticleEffects() {
        return this.particleEffects;
    }

    public record StateAnimation<D extends AbstractAnimationData<?, ?>> (@NotNull String name, @NotNull Function<D, Boolean> predicate) {}

    public record StateParticle(@NotNull String particleName, @NotNull String particleLocator) {}
}
