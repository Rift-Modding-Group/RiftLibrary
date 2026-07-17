package anightdazingzoroark.riftlib.core.controller;

import anightdazingzoroark.riftlib.core.AnimationState;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.easing.EasingType;
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
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An animation controller contains the states between which an animatable object
 * will switch between and manages the playing of animations and controls them.
 * */
public class AnimationController<A extends IAnimatable<D>, D extends AbstractAnimationData<?, D>> {
    static List<ModelFetcher<?>> modelFetchers = new ArrayList<>();
    private static final AtomicInteger STATE_PARTICLE_CONTROLLER_ID = new AtomicInteger();

    private final A animatable;
    @NonNull
    private final String name;
    private final int stateParticleControllerId = STATE_PARTICLE_CONTROLLER_ID.getAndIncrement();
    private final Map<String, AnimationControllerState<D>> animControllerStates = new LinkedHashMap<>();
    private final String initialState;
    private final Map<String, BoneAnimationQueue> boneAnimationQueues = new HashMap<>();
    private final LinkedHashMap<String, SingleAnimationRuntime<?, ?>> activeAnimationRuntimes = new LinkedHashMap<>();
    private final List<EventKeyFrame.ParticleEventKeyFrame> particleEvents = new ArrayList<>();
    private final List<EventKeyFrame.SoundEventKeyFrame> soundEvents = new ArrayList<>();
    private final List<EventKeyFrame.CustomInstructionKeyFrame> customInstructionEvents = new ArrayList<>();
    private final List<StateParticleEvent> stateParticleEvents = new ArrayList<>();

    @NonNull
    public final EasingType easingType;
    @Nullable
    public final Function<Double, Double> customEasingMethod;

    public boolean isJustStarting = false;
    public double animationSpeed = 1D;

    @NotNull
    private String currentState;
    private boolean initialized;
    private boolean needsAnimationReload;

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

    public int getStateParticleControllerId() {
        return this.stateParticleControllerId;
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

    public boolean allAnimationsFinished() {
        return !this.activeAnimationRuntimes.isEmpty() && this.activeAnimationRuntimes.values().stream().allMatch(SingleAnimationRuntime::isFinished);
    }

    public Map<String, BoneAnimationQueue> getBoneAnimationQueues() {
        return this.boneAnimationQueues;
    }

    public void process(
            double tick,
            List<IBone> modelRendererList,
            Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
            boolean crashWhenCantFindBone
    ) {
        D processingData = this.animatable.getAnimationData();
        this.particleEvents.clear();
        this.soundEvents.clear();
        this.customInstructionEvents.clear();
        this.stateParticleEvents.clear();
        this.createInitialQueues(modelRendererList);

        try {
            if (!this.initialized) {
                this.currentState = this.initialState;
                AnimationControllerState<D> initialControllerState = this.getCurrentControllerState();
                this.applyEffects(initialControllerState.getEntryEffects());
                this.queueStateParticleEvents(initialControllerState, true);
                this.initialized = true;
            }

            AnimationControllerState<D> currentControllerState = this.getCurrentControllerState();
            double transitionLength = this.applyTransitions(processingData, currentControllerState);
            currentControllerState = this.getCurrentControllerState();

            LinkedHashMap<String, AnimationControllerState.StateAnimation<D>> desiredAnimations = new LinkedHashMap<>();
            for (AnimationControllerState.StateAnimation<D> animationEntry : currentControllerState.getAnimations().values()) {
                if (animationEntry.predicate().apply(processingData)) {
                    desiredAnimations.put(animationEntry.name(), animationEntry);
                }
            }

            this.syncActiveAnimationRuntimes(desiredAnimations, transitionLength);

            if (this.needsAnimationReload) {
                for (SingleAnimationRuntime<?, ?> runtime : this.activeAnimationRuntimes.values()) {
                    runtime.markNeedsReload();
                }
                this.needsAnimationReload = false;
            }

            if (this.activeAnimationRuntimes.isEmpty()) return;

            BoneAnimationValuesList animationValues = new BoneAnimationValuesList();
            Set<String> rotationBones = new HashSet<>();
            Set<String> positionBones = new HashSet<>();
            Set<String> scaleBones = new HashSet<>();

            List<String> runtimesToRemove = new ArrayList<>();
            for (Map.Entry<String, SingleAnimationRuntime<?, ?>> runtimeEntry : this.activeAnimationRuntimes.entrySet()) {
                SingleAnimationRuntime<?, ?> runtime = runtimeEntry.getValue();
                runtime.isJustStarting = this.isJustStarting;
                runtime.animationSpeed = this.animationSpeed;
                runtime.process(tick, modelRendererList, boneSnapshotCollection, crashWhenCantFindBone);

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

                if (runtime.shouldRemove()) {
                    runtimesToRemove.add(runtimeEntry.getKey());
                }
            }

            for (String runtimeName : runtimesToRemove) {
                this.activeAnimationRuntimes.remove(runtimeName);
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

    public List<StateParticleEvent> drainStateParticleEvents() {
        List<StateParticleEvent> toReturn = new ArrayList<>(this.stateParticleEvents);
        this.stateParticleEvents.clear();
        return toReturn;
    }

    public void applyStateParticleEvent(AbstractAnimationData<?, ?> animationData, StateParticleEvent stateParticleEvent) {
        if (stateParticleEvent.createsParticle()) {
            AnimatedLocator locator = animationData.getAnimatedLocator(stateParticleEvent.particleLocator());
            if (locator == null) return;

            ParticleBuilder particleBuilder = RiftLibParticleHelper.getParticleBuilder(stateParticleEvent.particleName());
            if (particleBuilder == null) return;

            this.removeStateParticleEmitter(stateParticleEvent);
            RiftLibParticleEmitter emitter = locator.createParticleEmitter(particleBuilder);
            emitter.setStateParticleOwner(
                    stateParticleEvent.controllerId(),
                    stateParticleEvent.stateName(),
                    stateParticleEvent.particleIndex()
            );
        }
        else this.removeStateParticleEmitter(stateParticleEvent);
    }

    public void removeStateParticleEmitter(StateParticleEvent stateParticleEvent) {
        for (RiftLibParticleEmitter emitter : ParticleTicker.EMITTER_LIST) {
            if (emitter == null) continue;
            if (emitter.isStateParticleEmitter(
                    stateParticleEvent.controllerId(),
                    stateParticleEvent.stateName(),
                    stateParticleEvent.particleIndex()
            )) {
                emitter.killEmitter();
            }
        }
    }

    private void syncActiveAnimationRuntimes(
            LinkedHashMap<String, AnimationControllerState.StateAnimation<D>> desiredAnimations,
            double transitionLength
    ) {
        for (Map.Entry<String, SingleAnimationRuntime<?, ?>> runtimeEntry : this.activeAnimationRuntimes.entrySet()) {
            if (!desiredAnimations.containsKey(runtimeEntry.getKey())) {
                runtimeEntry.getValue().beginFadeOut(transitionLength);
            }
        }

        for (AnimationControllerState.StateAnimation<D> desiredAnimation : desiredAnimations.values()) {
            SingleAnimationRuntime<?, ?> runtime = this.activeAnimationRuntimes.computeIfAbsent(
                    desiredAnimation.name(),
                    key -> new SingleAnimationRuntime<>(this.animatable, desiredAnimation.name(), transitionLength)
            );
            runtime.cancelFadeOut();
            runtime.setAnimation();
        }
    }

    private double applyTransitions(D processingData, AnimationControllerState<D> currentControllerState) {
        double transitionLength = currentControllerState.transitionLength;
        for (Map.Entry<String, Function<D, Boolean>> transitionEntry : currentControllerState.getStateTransitions().entrySet()) {
            if (!transitionEntry.getValue().apply(processingData)) continue;

            String nextStateName = transitionEntry.getKey();
            if (this.currentState.equals(nextStateName)) return transitionLength;

            AnimationControllerState<D> nextControllerState = this.getState(nextStateName);
            this.applyEffects(currentControllerState.getExitEffects());
            this.queueStateParticleEvents(currentControllerState, false);
            this.stateParticleEvents.add(new StateParticleEvent(this.stateParticleControllerId, this.name, currentControllerState.name, -1, "", "", false));
            this.currentState = nextControllerState.name;
            this.applyEffects(nextControllerState.getEntryEffects());
            this.queueStateParticleEvents(nextControllerState, true);
            return transitionLength;
        }
        return transitionLength;
    }

    private void applyEffects(Collection<Consumer<D>> effects) {
        for (Consumer<D> effect : effects) effect.accept(this.animatable.getAnimationData());
    }

    private void queueStateParticleEvents(AnimationControllerState<D> state, boolean createsParticle) {
        List<AnimationControllerState.StateParticle> particleEffects = state.getParticleEffects();
        for (int i = 0; i < particleEffects.size(); i++) {
            AnimationControllerState.StateParticle particle = particleEffects.get(i);
            this.stateParticleEvents.add(new StateParticleEvent(
                    this.stateParticleControllerId,
                    this.name,
                    state.name,
                    i,
                    particle.particleName(),
                    particle.particleLocator(),
                    createsParticle
            ));
        }
    }

    private void createInitialQueues(List<IBone> modelRendererList) {
        this.boneAnimationQueues.clear();
        for (IBone modelRenderer : modelRendererList) {
            this.boneAnimationQueues.put(modelRenderer.getName(), new BoneAnimationQueue(modelRenderer));
        }
    }

    @FunctionalInterface
    public interface ModelFetcher<T> extends Function<IAnimatable<?>, IAnimatableModel<T>> {}

    public record StateParticleEvent(
            int controllerId,
            @NotNull String controllerName,
            @NotNull String stateName,
            int particleIndex,
            @NotNull String particleName,
            @NotNull String particleLocator,
            boolean createsParticle
    ) {}

    private static class SingleAnimationRuntime<A extends IAnimatable<D>, D extends AbstractAnimationData<?, D>> {
        private final Map<String, BoneAnimationQueue> boneAnimationQueues = new HashMap<>();
        private final Map<String, BoneSnapshot> boneSnapshots = new HashMap<>();
        private final Set<EventKeyFrame> executedKeyFrames = new HashSet<>();
        private final List<EventKeyFrame.ParticleEventKeyFrame> pendingParticleEvents = new ArrayList<>();
        private final List<EventKeyFrame.SoundEventKeyFrame> pendingSoundEvents = new ArrayList<>();
        private final List<EventKeyFrame.CustomInstructionKeyFrame> pendingCustomInstructionEvents = new ArrayList<>();
        private final Map<String, BoneSnapshot> fadeOutSnapshots = new HashMap<>();

        private final A animatable;
        private final String animationName;
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
        private boolean isJustStarting;
        private boolean fadingOut;
        private boolean fadeOutInitialized;
        private boolean markedForRemoval;
        private boolean needsAnimationRestart;

        private SingleAnimationRuntime(A animatable, String animationName, double transitionLengthTicks) {
            this.animatable = animatable;
            this.animationName = animationName;
            this.transitionLengthTicks = transitionLengthTicks;
        }

        private AnimationState getAnimationState() {
            return this.animationState;
        }

        private boolean isFinished() {
            return this.animationState == AnimationState.Stopped && this.currentAnimation != null && this.animationQueue.peek() == null;
        }

        private Map<String, BoneAnimationQueue> getBoneAnimationQueues() {
            return this.boneAnimationQueues;
        }

        private void markNeedsReload() {
            this.needsAnimationReload = true;
        }

        private boolean shouldRemove() {
            return this.markedForRemoval;
        }

        private void beginFadeOut(double transitionLengthTicks) {
            if (this.fadingOut || this.markedForRemoval) return;
            this.fadingOut = true;
            this.fadeOutInitialized = false;
            this.transitionLengthTicks = transitionLengthTicks;
            this.shouldResetTick = true;
            this.animationState = AnimationState.Transitioning;
        }

        private void cancelFadeOut() {
            this.needsAnimationRestart |= this.fadingOut || this.markedForRemoval;
            this.fadingOut = false;
            this.fadeOutInitialized = false;
            this.fadeOutSnapshots.clear();
            this.markedForRemoval = false;
        }

        private void setAnimation() {
            IAnimatableModel<A> model = this.getModel();
            if (model == null) return;

            AnimationBuilder builder = new AnimationBuilder();
            builder.addAnimation(this.animationName);

            if (builder.getRawAnimationList().isEmpty()) {
                this.animationState = AnimationState.Stopped;
                return;
            }
            if (builder.getRawAnimationList().equals(this.currentAnimationBuilder.getRawAnimationList())
                    && !this.needsAnimationReload
                    && !this.needsAnimationRestart) {
                return;
            }

            AtomicBoolean encounteredError = new AtomicBoolean(false);
            LinkedList<Animation> animations = builder.getRawAnimationList().stream().map((rawAnimation) -> {
                Animation animation = model.getAnimations(rawAnimation.animationName, this.animatable);
                if (animation == null) {
                    System.out.printf("Could not load animation: %s. Is it missing?", rawAnimation.animationName);
                    encounteredError.set(true);
                }
                return animation;
            }).collect(Collectors.toCollection(LinkedList::new));

            if (encounteredError.get()) return;

            this.animationQueue = animations;
            this.currentAnimationBuilder = builder;
            this.currentAnimation = null;
            this.lastResolvedAnimTick = 0D;
            this.lastFrameTick = -1D;
            this.tickOffset = 0D;
            this.boneSnapshots.clear();
            this.shouldResetTick = true;
            this.animationState = AnimationState.Transitioning;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
            this.needsAnimationRestart = false;
        }

        private void process(
                double tick, List<IBone> modelRendererList,
                Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
                boolean crashWhenCantFindBone
        ) {
            double deltaTime = this.lastFrameTick >= 0 ? tick - this.lastFrameTick : 0;
            this.lastFrameTick = tick;

            D data = this.animatable.getAnimationData();
            data.lifeTime = tick / 20D;
            data.deltaTime = deltaTime / 20D;

            if (this.currentAnimation != null) {
                IAnimatableModel<A> model = this.getModel();
                if (model != null) {
                    Animation animation = model.getAnimations(this.currentAnimation.animationName, this.animatable);
                    if (animation != null) {
                        LoopType loop = this.currentAnimation.loop;
                        this.currentAnimation = animation;
                        this.currentAnimation.loop = loop;
                    }
                }
            }

            this.createInitialQueues(modelRendererList);
            this.markedForRemoval = false;

            double actualTick = tick;
            tick = this.adjustTick(tick);

            if (!this.fadingOut
                    && this.animationState == AnimationState.Transitioning
                    && tick >= this.transitionLengthTicks
                    && !this.justStartedTransition
                    && this.currentAnimation != null) {
                this.shouldResetTick = true;
                this.animationState = AnimationState.Running;
                tick = this.adjustTick(actualTick);
            }

            assert tick >= 0 : "RiftLib: Tick was less than zero";

            if (this.currentAnimation == null && this.animationQueue.isEmpty()) {
                this.animationState = AnimationState.Stopped;
                this.justStopped = true;
            }
            else if (this.fadingOut) {
                this.processFadeOut(tick, boneSnapshotCollection, modelRendererList, crashWhenCantFindBone);
            }
            else {
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
                        this.saveSnapshotsForAnimation(this.currentAnimation, boneSnapshotCollection, this.boneSnapshots);
                    }
                    if (this.currentAnimation != null) {
                        double transitionResolvedTick = this.resolveAnimTick(tick);
                        this.setAnimTime(transitionResolvedTick);

                        for (BoneAnimation boneAnimation : this.currentAnimation.boneAnimations) {
                            BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName);
                            BoneSnapshot boneSnapshot = this.boneSnapshots.get(boneAnimation.boneName);
                            Optional<IBone> first = modelRendererList.stream()
                                    .filter(x -> x.getName().equals(boneAnimation.boneName)).findFirst();
                            if (first.isEmpty()) {
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
                                AnimationPoint xPoint = rotationKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.X);
                                AnimationPoint yPoint = rotationKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Y);
                                AnimationPoint zPoint = rotationKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Z);

                                boneAnimationQueue.rotationXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getRotation().x - initialSnapshot.getRotation().x,
                                        xPoint.animationStartValue));
                                boneAnimationQueue.rotationYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getRotation().y - initialSnapshot.getRotation().y,
                                        yPoint.animationStartValue));
                                boneAnimationQueue.rotationZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getRotation().z - initialSnapshot.getRotation().z,
                                        zPoint.animationStartValue));
                            }

                            if (!positionKeyFrames.isEmpty()) {
                                AnimationPoint xPoint = positionKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.X);
                                AnimationPoint yPoint = positionKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Y);
                                AnimationPoint zPoint = positionKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Z);

                                boneAnimationQueue.positionXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getPosition().x, xPoint.animationStartValue));
                                boneAnimationQueue.positionYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getPosition().y, yPoint.animationStartValue));
                                boneAnimationQueue.positionZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getPosition().z, zPoint.animationStartValue));
                            }

                            if (!scaleKeyFrames.isEmpty()) {
                                AnimationPoint xPoint = scaleKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.X);
                                AnimationPoint yPoint = scaleKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Y);
                                AnimationPoint zPoint = scaleKeyFrames.getAnimationPointAtTick(data, transitionResolvedTick, Axis.Z);

                                boneAnimationQueue.scaleXQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getScale().x, xPoint.animationStartValue));
                                boneAnimationQueue.scaleYQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getScale().y, yPoint.animationStartValue));
                                boneAnimationQueue.scaleZQueue.add(new AnimationPoint(null, tick, this.transitionLengthTicks,
                                        boneSnapshot.getScale().z, zPoint.animationStartValue));
                            }
                        }
                    }
                }
                else if (this.animationState == AnimationState.Running) {
                    this.processCurrentAnimation(tick, actualTick, crashWhenCantFindBone);
                }

                this.isJustStarting = false;
            }
        }

        private void processFadeOut(
                double tick, Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
                List<IBone> modelRendererList, boolean crashWhenCantFindBone
        ) {
            if (!this.fadeOutInitialized) {
                this.fadeOutSnapshots.clear();
                this.saveSnapshotsForAnimation(this.currentAnimation, boneSnapshotCollection, this.fadeOutSnapshots);
                this.fadeOutInitialized = true;
            }

            double transitionTick = this.transitionLengthTicks <= 0 ? 0 : Math.min(tick, this.transitionLengthTicks);
            if (this.currentAnimation != null) {
                for (BoneAnimation boneAnimation : this.currentAnimation.boneAnimations) {
                    BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName);
                    BoneSnapshot boneSnapshot = this.fadeOutSnapshots.get(boneAnimation.boneName);
                    Optional<IBone> first = modelRendererList.stream()
                            .filter(x -> x.getName().equals(boneAnimation.boneName)).findFirst();
                    if (first.isEmpty()) {
                        if (crashWhenCantFindBone) {
                            throw new RuntimeException("Could not find bone: " + boneAnimation.boneName);
                        }
                        else continue;
                    }
                    if (boneSnapshot == null) continue;

                    BoneSnapshot initialSnapshot = first.get().getInitialSnapshot();

                    boneAnimationQueue.rotationXQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getRotation().x - initialSnapshot.getRotation().x, 0));
                    boneAnimationQueue.rotationYQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getRotation().y - initialSnapshot.getRotation().y, 0));
                    boneAnimationQueue.rotationZQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getRotation().z - initialSnapshot.getRotation().z, 0));

                    boneAnimationQueue.positionXQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getPosition().x, 0));
                    boneAnimationQueue.positionYQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getPosition().y, 0));
                    boneAnimationQueue.positionZQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getPosition().z, 0));

                    boneAnimationQueue.scaleXQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getScale().x, 1));
                    boneAnimationQueue.scaleYQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getScale().y, 1));
                    boneAnimationQueue.scaleZQueue.add(new AnimationPoint(null, transitionTick, this.transitionLengthTicks,
                            boneSnapshot.getScale().z, 1));
                }
            }

            if (this.transitionLengthTicks <= 0 || transitionTick >= this.transitionLengthTicks) {
                this.animationState = AnimationState.Stopped;
                this.markedForRemoval = true;
                this.fadingOut = false;
                this.clearStoppedState();
            }
        }

        private void setAnimTime(double tick) {
            this.animatable.getAnimationData().animTime = tick / 20D;
        }

        private void saveSnapshotsForAnimation(
                Animation animation,
                Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection,
                Map<String, BoneSnapshot> targetSnapshots
        ) {
            for (Pair<IBone, BoneSnapshot> snapshot : boneSnapshotCollection.values()) {
                if (animation != null && animation.boneAnimations != null) {
                    if (animation.boneAnimations.stream().anyMatch(x -> x.boneName.equals(snapshot.getLeft().getName()))) {
                        targetSnapshots.put(snapshot.getLeft().getName(), new BoneSnapshot(snapshot.getLeft()));
                    }
                }
            }
        }

        private void processCurrentAnimation(double tick, double actualTick, boolean crashWhenCantFindBone) {
            assert this.currentAnimation != null;
            double resolvedTick = this.resolveAnimTick(tick);

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
            this.setAnimTime(resolvedTick);

            D data = this.animatable.getAnimationData();
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
                    boneAnimationQueue.rotationXQueue.add(rotationKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.X));
                    boneAnimationQueue.rotationYQueue.add(rotationKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Y));
                    boneAnimationQueue.rotationZQueue.add(rotationKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Z));
                }

                if (!positionKeyFrames.isEmpty()) {
                    boneAnimationQueue.positionXQueue.add(positionKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.X));
                    boneAnimationQueue.positionYQueue.add(positionKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Y));
                    boneAnimationQueue.positionZQueue.add(positionKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Z));
                }

                if (!scaleKeyFrames.isEmpty()) {
                    boneAnimationQueue.scaleXQueue.add(scaleKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.X));
                    boneAnimationQueue.scaleYQueue.add(scaleKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Y));
                    boneAnimationQueue.scaleZQueue.add(scaleKeyFrames.getAnimationPointAtTick(data, resolvedTick, Axis.Z));
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
                if (this.animatable.getAnimationData().isServerSynced()
                        && this.currentAnimation != null
                        && this.currentAnimation.loop == LoopType.LOOP
                ) {
                    this.tickOffset = 0D;
                    this.shouldResetTick = false;
                    return this.animationSpeed * Math.max(tick, 0.0D);
                }

                this.tickOffset = tick;
                this.shouldResetTick = false;
                return 0;
            }
            else return this.animationSpeed * Math.max(tick - this.tickOffset, 0.0D);
        }

        private double resolveAnimTick(double fallbackTick) {
            if (this.currentAnimation == null) return 0D;

            double resolved = fallbackTick;

            if (this.hasAnimTimeExpression()) {
                resolved = MolangUtils.parseValueAndGet(this.animatable.getAnimationData(), this.currentAnimation.animTimeUpdateExpression) * this.animationSpeed * 20D;
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

        private void clearStoppedState() {
            this.boneAnimationQueues.clear();
            this.boneSnapshots.clear();
            this.fadeOutSnapshots.clear();
            this.animationQueue.clear();
            this.currentAnimation = null;
            this.currentAnimationBuilder = new AnimationBuilder();
            this.lastResolvedAnimTick = 0D;
            this.lastFrameTick = -1D;
            this.tickOffset = 0D;
            this.shouldResetTick = false;
            this.justStopped = false;
            this.justStartedTransition = false;
            this.fadeOutInitialized = false;
            this.needsAnimationReload = false;
            this.needsAnimationRestart = false;
            this.resetEventKeyFrames();
        }

        private IAnimatableModel<A> getModel() {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER
                    && ServerModelRegistry.hasServerModel(this.animatable)) {
                return (IAnimatableModel<A>) ServerModelRegistry.getServerModel(this.animatable);
            }

            for (ModelFetcher<?> modelFetcher : modelFetchers) {
                IAnimatableModel<A> model = (IAnimatableModel<A>) modelFetcher.apply(this.animatable);
                if (model != null) return model;
            }
            System.out.printf(
                    "Could not find suitable model for animatable of type %s. Did you registerVariable a Model Fetcher?%n",
                    this.animatable.getClass());
            return null;
        }
    }
}
