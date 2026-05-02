package anightdazingzoroark.riftlib.core.controller;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.AnimationState;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.keyframe.AnimationPoint;
import anightdazingzoroark.riftlib.core.keyframe.BoneAnimation;
import anightdazingzoroark.riftlib.core.keyframe.BoneAnimationQueue;
import anightdazingzoroark.riftlib.core.keyframe.EventKeyFrame;
import anightdazingzoroark.riftlib.core.keyframe.VectorKeyFrameList;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.core.processor.BoneAnimationValuesList;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.util.Axis;
import anightdazingzoroark.riftlib.core.util.MathUtil;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An animation controller contains the states between which an animatable object
 * will switch between and manages the playing of animations and controls them.
 * */
public class AnimationController<A extends IAnimatable<D>, D extends AbstractAnimationData<?>> {
    static List<ModelFetcher<?>> modelFetchers = new ArrayList<>();

    protected final A animatable;
    @NonNull
    private final String name;
    private final Map<String, AnimationControllerState<D>> animControllerStates = new LinkedHashMap<>();
    private final String initialState;
    private final HashMap<String, BoneAnimationQueue> boneAnimationQueues = new HashMap<>();
    private final LinkedHashMap<String, SingleAnimationRuntime> activeAnimationRuntimes = new LinkedHashMap<>();
    private final List<EventKeyFrame.ParticleEventKeyFrame> particleEvents = new ArrayList<>();
    private final List<EventKeyFrame.SoundEventKeyFrame> soundEvents = new ArrayList<>();
    private final List<EventKeyFrame.CustomInstructionKeyFrame> customInstructionEvents = new ArrayList<>();

    @NonNull
    public final EasingType easingType;
    @Nullable
    public final Function<Double, Double> customEasingMethod;

    public boolean isJustStarting = false;
    public double animationSpeed = 1D;

    @NotNull
    private String currentState;
    private AnimationState animationState = AnimationState.Stopped;
    private boolean initialized;
    private boolean needsAnimationReload;
    private D processingData;
    private MolangParser processingParser;
    private MolangScope processingScope;

    public static void addModelFetcher(ModelFetcher<?> fetcher) {
        modelFetchers.add(fetcher);
    }

    public static void removeModelFetcher(ModelFetcher<?> fetcher) {
        Objects.requireNonNull(fetcher);
        modelFetchers.remove(fetcher);
    }

    /**
     * Good ol initializer for animation controllers
     *
     * @param animatable The IAnimatable instance to animate.
     * @param name The name of this controller.
     * @param initialState The name of the initial controller state to use. This means that when this controller starts animating, it starts out as this.
     * @param controllerStates All the controller states that will be used.
     * */
    @SafeVarargs
    public AnimationController(A animatable, @NonNull String name, @NonNull String initialState, AnimationControllerState<D>... controllerStates) {
        this.animatable = animatable;
        this.name = name;
        this.initialState = initialState;
        this.currentState = initialState;
        this.easingType = EasingType.NONE;
        this.customEasingMethod = null;

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

    /**
     * Initializer with an easing type
     *
     * @param animatable The IAnimatable instance to animate.
     * @param name The name of this controller.
     * @param initialState The name of the initial controller state to use. This means that when this controller starts animating, it starts out as this.
     * @param easingType The easing type to use in the animation controller.
     * @param controllerStates All the controller states that will be used.
     * */
    @SafeVarargs
    public AnimationController(A animatable, @NonNull String name, @NonNull String initialState, @NonNull EasingType easingType, AnimationControllerState<D>... controllerStates) {
        this.animatable = animatable;
        this.name = name;
        this.initialState = initialState;
        this.currentState = initialState;
        this.easingType = easingType;
        this.customEasingMethod = null;

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

    /**
     * Initializer with a custom easing type
     *
     * @param animatable The IAnimatable instance to animate.
     * @param name The name of this controller.
     * @param initialState The name of the initial controller state to use. This means that when this controller starts animating, it starts out as this.
     * @param customEasingMethod A function that represents the custom easing method that will be used.
     * @param controllerStates All the controller states that will be used.
     * */
    @SafeVarargs
    public AnimationController(A animatable, @NonNull String name, @NonNull String initialState, @Nullable Function<Double, Double> customEasingMethod, AnimationControllerState<D>... controllerStates) {
        this.animatable = animatable;
        this.name = name;
        this.initialState = initialState;
        this.currentState = initialState;
        this.easingType = EasingType.CUSTOM;
        this.customEasingMethod = customEasingMethod;

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

    public @NonNull String getName() {
        return this.name;
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
        return !this.activeAnimationRuntimes.isEmpty() && this.activeAnimationRuntimes.values().stream().allMatch(SingleAnimationRuntime::isFinished);
    }

    public AnimationState getAnimationState() {
        return this.animationState;
    }

    public HashMap<String, BoneAnimationQueue> getBoneAnimationQueues() {
        return this.boneAnimationQueues;
    }

    public void process(AbstractAnimationData<?> data, double tick, AnimationEvent event,
                        List<IBone> modelRendererList,
                        HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
                        MolangParser parser, MolangScope scope, boolean crashWhenCantFindBone) {
        this.processingData = (D) data;
        this.processingParser = parser;
        this.processingScope = scope;
        this.particleEvents.clear();
        this.soundEvents.clear();
        this.customInstructionEvents.clear();
        this.createInitialQueues(modelRendererList);

        try {
            if (!this.initialized) {
                this.currentState = this.initialState;
                this.applyEffects(this.getCurrentControllerState().getEntryEffects());
                this.initialized = true;
            }

            AnimationControllerState<D> currentControllerState = this.getCurrentControllerState();
            this.applyTransitions(currentControllerState);
            currentControllerState = this.getCurrentControllerState();

            LinkedHashMap<String, AnimationControllerState.StateAnimation<D>> desiredAnimations = new LinkedHashMap<>();
            for (AnimationControllerState.StateAnimation<D> animationEntry : currentControllerState.getAnimations().values()) {
                if (animationEntry.getPredicate().apply(this.processingData)) {
                    desiredAnimations.put(animationEntry.getName(), animationEntry);
                }
            }

            this.syncActiveAnimationRuntimes(desiredAnimations, currentControllerState.transitionLength);

            if (this.needsAnimationReload) {
                for (SingleAnimationRuntime runtime : this.activeAnimationRuntimes.values()) {
                    runtime.markNeedsReload();
                }
                this.needsAnimationReload = false;
            }

            this.animationState = this.activeAnimationRuntimes.isEmpty() ? AnimationState.Stopped : AnimationState.Running;
            if (this.activeAnimationRuntimes.isEmpty()) {
                return;
            }

            BoneAnimationValuesList animationValues = new BoneAnimationValuesList();
            Set<String> rotationBones = new HashSet<>();
            Set<String> positionBones = new HashSet<>();
            Set<String> scaleBones = new HashSet<>();

            for (SingleAnimationRuntime runtime : this.activeAnimationRuntimes.values()) {
                runtime.isJustStarting = this.isJustStarting;
                runtime.easingType = this.easingType;
                runtime.customEasingMethod = this.customEasingMethod;
                runtime.animationSpeed = this.animationSpeed;
                runtime.process(this.processingData, tick, event, modelRendererList, boneSnapshotCollection, parser, scope, crashWhenCantFindBone);
                this.animationState = mergeAnimationStates(this.animationState, runtime.getAnimationState());

                for (BoneAnimationQueue boneAnimation : runtime.getBoneAnimationQueues().values()) {
                    IBone bone = boneAnimation.bone;

                    AnimationPoint rXPoint = boneAnimation.rotationXQueue.poll();
                    AnimationPoint rYPoint = boneAnimation.rotationYQueue.poll();
                    AnimationPoint rZPoint = boneAnimation.rotationZQueue.poll();

                    AnimationPoint pXPoint = boneAnimation.positionXQueue.poll();
                    AnimationPoint pYPoint = boneAnimation.positionYQueue.poll();
                    AnimationPoint pZPoint = boneAnimation.positionZQueue.poll();

                    AnimationPoint sXPoint = boneAnimation.scaleXQueue.poll();
                    AnimationPoint sYPoint = boneAnimation.scaleYQueue.poll();
                    AnimationPoint sZPoint = boneAnimation.scaleZQueue.poll();

                    if (rXPoint != null && rYPoint != null && rZPoint != null) {
                        rotationBones.add(bone.getName());
                        animationValues.addRotations(
                                bone.getName(),
                                MathUtil.lerpValues(rXPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(rYPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(rZPoint, this.easingType, this.customEasingMethod)
                        );
                    }

                    if (pXPoint != null && pYPoint != null && pZPoint != null) {
                        positionBones.add(bone.getName());
                        animationValues.addPositions(
                                bone.getName(),
                                MathUtil.lerpValues(pXPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(pYPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(pZPoint, this.easingType, this.customEasingMethod)
                        );
                    }

                    if (sXPoint != null && sYPoint != null && sZPoint != null) {
                        scaleBones.add(bone.getName());
                        animationValues.addScales(
                                bone.getName(),
                                MathUtil.lerpValues(sXPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(sYPoint, this.easingType, this.customEasingMethod),
                                MathUtil.lerpValues(sZPoint, this.easingType, this.customEasingMethod)
                        );
                    }
                }

                this.particleEvents.addAll(runtime.drainParticleEvents());
                this.soundEvents.addAll(runtime.drainSoundEvents());
                this.customInstructionEvents.addAll(runtime.drainCustomInstructionEvents());
            }

            for (String boneName : rotationBones) {
                BoneAnimationQueue queue = this.boneAnimationQueues.get(boneName);
                float[] rotation = animationValues.getRotations(boneName);
                queue.rotationXQueue.add(new AnimationPoint(null, 0, 0, 0, rotation[0]));
                queue.rotationYQueue.add(new AnimationPoint(null, 0, 0, 0, rotation[1]));
                queue.rotationZQueue.add(new AnimationPoint(null, 0, 0, 0, rotation[2]));
            }

            for (String boneName : positionBones) {
                BoneAnimationQueue queue = this.boneAnimationQueues.get(boneName);
                float[] position = animationValues.getPositions(boneName);
                queue.positionXQueue.add(new AnimationPoint(null, 0, 0, 0, position[0]));
                queue.positionYQueue.add(new AnimationPoint(null, 0, 0, 0, position[1]));
                queue.positionZQueue.add(new AnimationPoint(null, 0, 0, 0, position[2]));
            }

            for (String boneName : scaleBones) {
                BoneAnimationQueue queue = this.boneAnimationQueues.get(boneName);
                float[] scale = animationValues.getScales(boneName);
                queue.scaleXQueue.add(new AnimationPoint(null, 0, 0, 1, scale[0]));
                queue.scaleYQueue.add(new AnimationPoint(null, 0, 0, 1, scale[1]));
                queue.scaleZQueue.add(new AnimationPoint(null, 0, 0, 1, scale[2]));
            }
        }
        finally {
            this.processingData = null;
            this.processingParser = null;
            this.processingScope = null;
            this.isJustStarting = false;
        }
    }

    public List<EventKeyFrame.ParticleEventKeyFrame> drainParticleEvents() {
        List<EventKeyFrame.ParticleEventKeyFrame> toReturn = new ArrayList<>(this.particleEvents);
        this.particleEvents.clear();
        return toReturn;
    }

    public List<EventKeyFrame.SoundEventKeyFrame> drainSoundEvents() {
        List<EventKeyFrame.SoundEventKeyFrame> toReturn = new ArrayList<>(this.soundEvents);
        this.soundEvents.clear();
        return toReturn;
    }

    public List<EventKeyFrame.CustomInstructionKeyFrame> drainCustomInstructionEvents() {
        List<EventKeyFrame.CustomInstructionKeyFrame> toReturn = new ArrayList<>(this.customInstructionEvents);
        this.customInstructionEvents.clear();
        return toReturn;
    }

    public void markNeedsReload() {
        this.needsAnimationReload = true;
    }

    public void clearAnimationCache() {
        this.activeAnimationRuntimes.clear();
    }

    public double getAnimationSpeed() {
        return this.animationSpeed;
    }

    public void setAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    private void syncActiveAnimationRuntimes(LinkedHashMap<String, AnimationControllerState.StateAnimation<D>> desiredAnimations,
                                             double transitionLength) {
        this.activeAnimationRuntimes.entrySet().removeIf(entry -> !desiredAnimations.containsKey(entry.getKey()));

        for (AnimationControllerState.StateAnimation<D> desiredAnimation : desiredAnimations.values()) {
            SingleAnimationRuntime runtime = this.activeAnimationRuntimes.computeIfAbsent(
                    desiredAnimation.getName(),
                    key -> new SingleAnimationRuntime(desiredAnimation.getName(), desiredAnimation.getLoopType(), transitionLength)
            );
            runtime.transitionLengthTicks = transitionLength;
            runtime.setAnimation(desiredAnimation.getName(), desiredAnimation.getLoopType());
        }
    }

    private void applyTransitions(AnimationControllerState<D> currentControllerState) {
        for (ImmutablePair<String, Function<D, Boolean>> transitionEntry : currentControllerState.getStateTransitions()) {
            if (!transitionEntry.right.apply(this.processingData)) {
                continue;
            }

            String nextStateName = transitionEntry.left;
            if (this.currentState.equals(nextStateName)) {
                return;
            }

            this.applyEffects(currentControllerState.getExitEffects());
            this.currentState = this.getState(nextStateName).name;
            this.applyEffects(this.getCurrentControllerState().getEntryEffects());
            this.activeAnimationRuntimes.clear();
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

    private void createInitialQueues(List<IBone> modelRendererList) {
        this.boneAnimationQueues.clear();
        for (IBone modelRenderer : modelRendererList) {
            this.boneAnimationQueues.put(modelRenderer.getName(), new BoneAnimationQueue(modelRenderer));
        }
    }

    private IAnimatableModel<A> getModel(A animatable) {
        for (ModelFetcher<?> modelFetcher : modelFetchers) {
            IAnimatableModel<A> model = (IAnimatableModel<A>) modelFetcher.apply(animatable);
            if (model != null) return model;
        }
        System.out.printf(
                "Could not find suitable model for animatable of type %s. Did you register a Model Fetcher?%n",
                animatable.getClass());
        return null;
    }

    private static AnimationState mergeAnimationStates(AnimationState left, AnimationState right) {
        if (left == AnimationState.Transitioning || right == AnimationState.Transitioning) {
            return AnimationState.Transitioning;
        }
        if (left == AnimationState.Running || right == AnimationState.Running) {
            return AnimationState.Running;
        }
        return AnimationState.Stopped;
    }

    @FunctionalInterface
    public interface ModelFetcher<T> extends Function<IAnimatable<?>, IAnimatableModel<T>> {}

    private final class SingleAnimationRuntime {
        private final HashMap<String, BoneAnimationQueue> boneAnimationQueues = new HashMap<>();
        private final HashMap<String, BoneSnapshot> boneSnapshots = new HashMap<>();
        private final Set<EventKeyFrame> executedKeyFrames = new HashSet<>();
        private final List<EventKeyFrame.ParticleEventKeyFrame> pendingParticleEvents = new ArrayList<>();
        private final List<EventKeyFrame.SoundEventKeyFrame> pendingSoundEvents = new ArrayList<>();
        private final List<EventKeyFrame.CustomInstructionKeyFrame> pendingCustomInstructionEvents = new ArrayList<>();

        private final String animationName;
        private LoopType loopType;
        private AnimationState animationState = AnimationState.Stopped;
        private Queue<Animation> animationQueue = new LinkedList<>();
        private Animation currentAnimation;
        private AnimationBuilder currentAnimationBuilder = new AnimationBuilder();
        private boolean shouldResetTick;
        private boolean justStopped;
        private boolean justStartedTransition;
        private boolean needsAnimationReload;
        private double tickOffset;
        private double lastResolvedAnimTick;
        private double lastFrameTick = -1D;
        private double transitionLengthTicks;
        private double animationSpeed = 1D;
        private EasingType easingType = EasingType.NONE;
        private Function<Double, Double> customEasingMethod;
        private boolean isJustStarting;

        private SingleAnimationRuntime(String animationName, LoopType loopType, double transitionLengthTicks) {
            this.animationName = animationName;
            this.loopType = loopType;
            this.transitionLengthTicks = transitionLengthTicks;
        }

        private AnimationState getAnimationState() {
            return this.animationState;
        }

        private boolean isFinished() {
            return this.animationState == AnimationState.Stopped && this.currentAnimation != null && this.animationQueue.peek() == null;
        }

        private HashMap<String, BoneAnimationQueue> getBoneAnimationQueues() {
            return this.boneAnimationQueues;
        }

        private void markNeedsReload() {
            this.needsAnimationReload = true;
        }

        private void setAnimation(String animationName, LoopType loopType) {
            IAnimatableModel<A> model = getModel(animatable);
            if (model == null) return;

            AnimationBuilder builder = new AnimationBuilder();
            if (loopType != null) {
                builder.addAnimation(animationName, loopType);
            }
            else {
                builder.addAnimation(animationName);
            }

            if (builder.getRawAnimationList().isEmpty()) {
                this.animationState = AnimationState.Stopped;
                return;
            }
            if (builder.getRawAnimationList().equals(this.currentAnimationBuilder.getRawAnimationList()) && !this.needsAnimationReload) {
                return;
            }

            AtomicBoolean encounteredError = new AtomicBoolean(false);
            LinkedList<Animation> animations = builder.getRawAnimationList().stream().map((rawAnimation) -> {
                Animation animation = model.getAnimation(rawAnimation.animationName, animatable);
                if (animation == null) {
                    System.out.printf("Could not load animation: %s. Is it missing?", rawAnimation.animationName);
                    encounteredError.set(true);
                }
                if (animation != null && rawAnimation.loopType != null) {
                    animation.loop = rawAnimation.loopType;
                }
                return animation;
            }).collect(Collectors.toCollection(LinkedList::new));

            if (encounteredError.get()) return;

            this.animationQueue = animations;
            this.currentAnimationBuilder = builder;
            this.loopType = loopType;
            this.shouldResetTick = true;
            this.animationState = AnimationState.Transitioning;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
        }

        private void process(AbstractAnimationData<?> data, double tick, AnimationEvent event, List<IBone> modelRendererList,
                             HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection, MolangParser parser,
                             MolangScope scope, boolean crashWhenCantFindBone) {
            double deltaTime = this.lastFrameTick >= 0 ? tick - this.lastFrameTick : 0;
            this.lastFrameTick = tick;

            data.lifeTime = tick / 20D;
            data.deltaTime = deltaTime / 20D;
            if (this.currentAnimation != null) {
                IAnimatableModel<A> model = getModel(animatable);
                if (model != null) {
                    Animation animation = model.getAnimation(this.currentAnimation.animationName, animatable);
                    if (animation != null) {
                        LoopType loop = this.currentAnimation.loop;
                        this.currentAnimation = animation;
                        this.currentAnimation.loop = loop;
                    }
                }
            }

            this.createInitialQueues(modelRendererList);

            double actualTick = tick;
            tick = this.adjustTick(tick);

            if (this.animationState == AnimationState.Transitioning && tick >= this.transitionLengthTicks) {
                this.shouldResetTick = true;
                this.animationState = AnimationState.Running;
                tick = this.adjustTick(actualTick);
            }

            assert tick >= 0 : "RiftLib: Tick was less than zero";

            if (this.currentAnimation == null && this.animationQueue.isEmpty()) {
                this.animationState = AnimationState.Stopped;
                this.justStopped = true;
                return;
            }
            if (this.justStartedTransition && (this.shouldResetTick || this.justStopped)) {
                this.justStopped = false;
                tick = this.adjustTick(actualTick);
            }
            else if (this.currentAnimation == null && !this.animationQueue.isEmpty()) {
                this.shouldResetTick = true;
                this.animationState = AnimationState.Transitioning;
                this.justStartedTransition = true;
                this.needsAnimationReload = false;
                tick = this.adjustTick(actualTick);
            }
            else if (this.animationState != AnimationState.Transitioning) {
                this.animationState = AnimationState.Running;
            }

            if (this.animationState == AnimationState.Transitioning) {
                if (tick == 0 || this.isJustStarting) {
                    this.justStartedTransition = false;
                    this.currentAnimation = this.animationQueue.poll();
                    this.resetEventKeyFrames();
                    this.saveSnapshotsForAnimation(this.currentAnimation, boneSnapshotCollection);
                }
                if (this.currentAnimation != null) {
                    this.setAnimTime(data, 0);
                    for (BoneAnimation boneAnimation : this.currentAnimation.boneAnimations) {
                        BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName);
                        BoneSnapshot boneSnapshot = this.boneSnapshots.get(boneAnimation.boneName);
                        Optional<IBone> first = modelRendererList.stream()
                                .filter(x -> x.getName().equals(boneAnimation.boneName)).findFirst();
                        if (!first.isPresent()) {
                            if (crashWhenCantFindBone) {
                                throw new RuntimeException("Could not find bone: " + boneAnimation.boneName);
                            }
                            else continue;
                        }
                        BoneSnapshot initialSnapshot = first.get().getInitialSnapshot();
                        assert boneSnapshot != null : "Bone snapshot was null";

                        VectorKeyFrameList rotationKeyFrames = boneAnimation.rotationKeyFrames;
                        VectorKeyFrameList positionKeyFrames = boneAnimation.positionKeyFrames;
                        VectorKeyFrameList scaleKeyFrames = boneAnimation.scaleKeyFrames;

                        if (!rotationKeyFrames.isEmpty()) {
                            AnimationPoint xPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                            AnimationPoint yPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                            AnimationPoint zPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

                            boneAnimationQueue.rotationXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.rotationValueX - initialSnapshot.rotationValueX,
                                    xPoint.animationStartValue));
                            boneAnimationQueue.rotationYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.rotationValueY - initialSnapshot.rotationValueY,
                                    yPoint.animationStartValue));
                            boneAnimationQueue.rotationZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.rotationValueZ - initialSnapshot.rotationValueZ,
                                    zPoint.animationStartValue));
                        }

                        if (!positionKeyFrames.isEmpty()) {
                            AnimationPoint xPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                            AnimationPoint yPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                            AnimationPoint zPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

                            boneAnimationQueue.positionXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.positionOffsetX, xPoint.animationStartValue));
                            boneAnimationQueue.positionYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.positionOffsetY, yPoint.animationStartValue));
                            boneAnimationQueue.positionZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.positionOffsetZ, zPoint.animationStartValue));
                        }

                        if (!scaleKeyFrames.isEmpty()) {
                            AnimationPoint xPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                            AnimationPoint yPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                            AnimationPoint zPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

                            boneAnimationQueue.scaleXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.scaleValueX, xPoint.animationStartValue));
                            boneAnimationQueue.scaleYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.scaleValueY, yPoint.animationStartValue));
                            boneAnimationQueue.scaleZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                    boneSnapshot.scaleValueZ, zPoint.animationStartValue));
                        }
                    }
                }
            }
            else if (this.animationState == AnimationState.Running) {
                this.processCurrentAnimation(data, tick, actualTick, parser, scope, crashWhenCantFindBone);
            }

            this.isJustStarting = false;
        }

        private void setAnimTime(AbstractAnimationData<?> data, double tick) {
            data.animTime = tick / 20D;
        }

        private void saveSnapshotsForAnimation(Animation animation,
                                               HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection) {
            for (Pair<IBone, BoneSnapshot> snapshot : boneSnapshotCollection.values()) {
                if (animation != null && animation.boneAnimations != null) {
                    if (animation.boneAnimations.stream().anyMatch(x -> x.boneName.equals(snapshot.getLeft().getName()))) {
                        this.boneSnapshots.put(snapshot.getLeft().getName(), new BoneSnapshot(snapshot.getRight()));
                    }
                }
            }
        }

        private void processCurrentAnimation(AbstractAnimationData<?> data, double tick, double actualTick, MolangParser parser,
                                             MolangScope scope, boolean crashWhenCantFindBone) {
            assert this.currentAnimation != null;
            double resolvedTick = this.resolveAnimTick(tick, parser, scope);

            if (resolvedTick < this.lastResolvedAnimTick) {
                this.resetEventKeyFrames();
            }
            this.lastResolvedAnimTick = resolvedTick;

            if (resolvedTick >= this.currentAnimation.animationLength) {
                if (this.currentAnimation.loop == LoopType.PLAY_ONCE) {
                    resolvedTick = Math.min(resolvedTick, this.currentAnimation.animationLength);
                    this.collectEventKeyFrames(resolvedTick);
                    if (this.animationQueue.peek() == null) {
                        this.animationState = AnimationState.Stopped;
                    }
                    else {
                        this.resetEventKeyFrames();
                        this.animationState = AnimationState.Transitioning;
                        this.shouldResetTick = true;
                        this.currentAnimation = this.animationQueue.peek();
                    }
                    return;
                }
                else if (this.currentAnimation.loop == LoopType.HOLD_ON_LAST_FRAME) {
                    resolvedTick = Math.min(resolvedTick, this.currentAnimation.animationLength);
                    this.collectEventKeyFrames(resolvedTick);

                    Animation peek = this.animationQueue.peek();
                    if (peek != null) {
                        this.resetEventKeyFrames();
                        this.animationState = AnimationState.Transitioning;
                        this.shouldResetTick = true;
                        this.currentAnimation = peek;
                    }
                }
                else if (this.currentAnimation.loop == LoopType.LOOP) {
                    this.resetEventKeyFrames();
                    this.shouldResetTick = true;

                    if (!this.hasAnimTimeExpression()) {
                        tick = this.adjustTick(actualTick);
                        resolvedTick = tick;
                    }
                }
            }
            this.setAnimTime(data, resolvedTick);

            for (BoneAnimation boneAnimation : this.currentAnimation.boneAnimations) {
                BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName);
                if (boneAnimationQueue == null) {
                    if (crashWhenCantFindBone) {
                        throw new RuntimeException("Could not find bone: " + boneAnimation.boneName);
                    }
                    else continue;
                }

                VectorKeyFrameList rotationKeyFrames = boneAnimation.rotationKeyFrames;
                VectorKeyFrameList positionKeyFrames = boneAnimation.positionKeyFrames;
                VectorKeyFrameList scaleKeyFrames = boneAnimation.scaleKeyFrames;

                if (!rotationKeyFrames.isEmpty()) {
                    boneAnimationQueue.rotationXQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.X));
                    boneAnimationQueue.rotationYQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Y));
                    boneAnimationQueue.rotationZQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Z));
                }

                if (!positionKeyFrames.isEmpty()) {
                    boneAnimationQueue.positionXQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.X));
                    boneAnimationQueue.positionYQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Y));
                    boneAnimationQueue.positionZQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Z));
                }

                if (!scaleKeyFrames.isEmpty()) {
                    boneAnimationQueue.scaleXQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.X));
                    boneAnimationQueue.scaleYQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Y));
                    boneAnimationQueue.scaleZQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, resolvedTick, Axis.Z));
                }
            }

            this.collectEventKeyFrames(resolvedTick);

            if (this.transitionLengthTicks == 0 && this.shouldResetTick && this.animationState == AnimationState.Transitioning) {
                this.currentAnimation = this.animationQueue.poll();
            }
        }

        private void collectEventKeyFrames(double resolvedTick) {
            for (EventKeyFrame.ParticleEventKeyFrame particleEventKeyFrame : this.currentAnimation.particleKeyFrames) {
                if (!this.executedKeyFrames.contains(particleEventKeyFrame) && resolvedTick >= particleEventKeyFrame.getStartTick()) {
                    this.pendingParticleEvents.add(particleEventKeyFrame);
                    this.executedKeyFrames.add(particleEventKeyFrame);
                }
            }

            for (EventKeyFrame.SoundEventKeyFrame soundEventKeyFrame : this.currentAnimation.soundKeyFrames) {
                if (!this.executedKeyFrames.contains(soundEventKeyFrame) && resolvedTick >= soundEventKeyFrame.getStartTick()) {
                    this.pendingSoundEvents.add(soundEventKeyFrame);
                    this.executedKeyFrames.add(soundEventKeyFrame);
                }
            }

            for (EventKeyFrame.CustomInstructionKeyFrame customInstructionKeyFrame : this.currentAnimation.customInstructionKeyFrames) {
                if (!this.executedKeyFrames.contains(customInstructionKeyFrame) && resolvedTick >= customInstructionKeyFrame.getStartTick()) {
                    this.pendingCustomInstructionEvents.add(customInstructionKeyFrame);
                    this.executedKeyFrames.add(customInstructionKeyFrame);
                }
            }
        }

        private List<EventKeyFrame.ParticleEventKeyFrame> drainParticleEvents() {
            List<EventKeyFrame.ParticleEventKeyFrame> toReturn = new ArrayList<>(this.pendingParticleEvents);
            this.pendingParticleEvents.clear();
            return toReturn;
        }

        private List<EventKeyFrame.SoundEventKeyFrame> drainSoundEvents() {
            List<EventKeyFrame.SoundEventKeyFrame> toReturn = new ArrayList<>(this.pendingSoundEvents);
            this.pendingSoundEvents.clear();
            return toReturn;
        }

        private List<EventKeyFrame.CustomInstructionKeyFrame> drainCustomInstructionEvents() {
            List<EventKeyFrame.CustomInstructionKeyFrame> toReturn = new ArrayList<>(this.pendingCustomInstructionEvents);
            this.pendingCustomInstructionEvents.clear();
            return toReturn;
        }

        private void createInitialQueues(List<IBone> modelRendererList) {
            this.boneAnimationQueues.clear();
            for (IBone modelRenderer : modelRendererList) {
                this.boneAnimationQueues.put(modelRenderer.getName(), new BoneAnimationQueue(modelRenderer));
            }
        }

        private double adjustTick(double tick) {
            if (this.shouldResetTick) {
                this.tickOffset = tick;
                this.shouldResetTick = false;
                return 0;
            }
            else return this.animationSpeed * Math.max(tick - this.tickOffset, 0.0D);
        }

        private double resolveAnimTick(double fallbackTick, MolangParser parser, MolangScope scope) {
            if (this.currentAnimation == null) return 0D;

            double resolved = fallbackTick;

            if (this.hasAnimTimeExpression()) {
                AtomicReference<Double> atomicResolved = new AtomicReference<>(0D);
                parser.withScope(scope, () -> {
                    try {
                        double value = parser.parseExpression(this.currentAnimation.animTimeUpdateExpression).get();
                        atomicResolved.set(value);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                resolved = atomicResolved.get() * this.animationSpeed * 20D;
            }

            if (this.currentAnimation.loop == LoopType.LOOP) {
                double animLength = this.currentAnimation.animationLength;
                if (animLength > 0D) resolved = resolved % animLength;
            }

            return Math.max(resolved, 0D);
        }

        private boolean hasAnimTimeExpression() {
            return this.currentAnimation != null && this.currentAnimation.animTimeUpdateExpression != null;
        }

        private void resetEventKeyFrames() {
            this.executedKeyFrames.clear();
            this.pendingParticleEvents.clear();
            this.pendingSoundEvents.clear();
            this.pendingCustomInstructionEvents.clear();
        }
    }
}
