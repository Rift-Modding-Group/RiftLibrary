/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.riftlib.core.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.easing.EasingManager;
import anightdazingzoroark.riftlib.core.keyframe.*;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.apache.commons.lang3.tuple.Pair;

import anightdazingzoroark.riftlib.molang.MolangParser;

import anightdazingzoroark.riftlib.core.AnimationState;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.event.AnimationEvent;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.util.Axis;

/**
 * The type Animation controller.
 *
 * @param <T> the type parameter
 */
public class AnimationController<T extends IAnimatable> {
	static List<ModelFetcher<?>> modelFetchers = new ArrayList<>();
	/**
	 * The Entity.
	 */
	protected T animatable;
	/**
	 * The animation predicate, is tested in every process call (i.e. every frame)
	 */
	protected IAnimationPredicate<T> animationPredicate;

	/**
	 * The name of the animation controller
	 */
	private final String name;

	protected AnimationState animationState = AnimationState.Stopped;

	/**
	 * How long it takes to transition between animations
	 */
	public double transitionLengthTicks;

	public boolean isJustStarting = false;

	public static void addModelFetcher(ModelFetcher<?> fetcher) {
		modelFetchers.add(fetcher);
	}
	
	public static void removeModelFetcher(ModelFetcher<?> fetcher) {
		Objects.requireNonNull(fetcher);
		modelFetchers.remove(fetcher);
	}

	/**
	 * An AnimationPredicate is run every render frame for ever AnimationController.
	 * The "test" method is where you should change animations, stop animations,
	 * restart, etc.
	 */
	@FunctionalInterface
	public interface IAnimationPredicate<P extends IAnimatable> {
		/**
		 * An AnimationPredicate is run every render frame for ever AnimationController.
		 * The "test" method is where you should change animations, stop animations,
		 * restart, etc.
		 *
		 * @return CONTINUE if the animation should continue, STOP if it should stop.
		 */
		PlayState test(AnimationEvent<P> event);
	}

	private final HashMap<String, BoneAnimationQueue> boneAnimationQueues = new HashMap<>();
	public double tickOffset;
	protected Queue<Animation> animationQueue = new LinkedList<>();
	protected Animation currentAnimation;
	protected AnimationBuilder currentAnimationBuilder = new AnimationBuilder();
	protected boolean shouldResetTick = false;
	private final HashMap<String, BoneSnapshot> boneSnapshots = new HashMap<>();
	private boolean justStopped = false;
	protected boolean justStartedTransition = false;
	public Function<Double, Double> customEasingMethod;
	protected boolean needsAnimationReload = false;
	public double animationSpeed = 1D;
	private final Set<EventKeyFrame> executedKeyFrames = new HashSet<>();
    private EventKeyFrame.ParticleEventKeyFrame lastParticleEvent;
    private EventKeyFrame.SoundEventKeyFrame lastSoundEvent;

	/**
	 * This method sets the current animation with an animation builder. You can run
	 * this method every frame, if you pass in the same animation builder every
	 * time, it won't restart. Additionally, it smoothly transitions between
	 * animation states.
	 */
	public void setAnimation(AnimationBuilder builder) {
		IAnimatableModel<T> model = getModel(this.animatable);
		if (model != null) {
			if (builder == null || builder.getRawAnimationList().isEmpty()) {
				this.animationState = AnimationState.Stopped;
			}
            else if (!builder.getRawAnimationList().equals(currentAnimationBuilder.getRawAnimationList())
					|| this.needsAnimationReload) {
				AtomicBoolean encounteredError = new AtomicBoolean(false);
				// Convert the list of animation names to the actual list, keeping track of the
				// loop boolean along the way
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
                else this.animationQueue = animations;
                this.currentAnimationBuilder = builder;

				// Reset the adjusted tick to 0 on next animation process call
                this.shouldResetTick = true;
				this.animationState = AnimationState.Transitioning;
                this.justStartedTransition = true;
                this.needsAnimationReload = false;
			}
		}
	}

	/**
	 * By default Geckolib uses the easing types of every keyframe. If you want to
	 * override that for an entire AnimationController, change this value.
	 */
	public EasingType easingType = EasingType.NONE;

	/**
	 * Instantiates a new Animation controller. Each animation controller can run
	 * one animation at a time. You can have several animation controllers for each
	 * entity, i.e. one animation to control the entity's size, one to control
	 * movement, attacks, etc.
	 *
	 * @param animatable            The entity
	 * @param name                  Name of the animation controller
	 *                              (move_controller, size_controller,
	 *                              attack_controller, etc.)
	 * @param transitionLengthTicks How long it takes to transition between
	 *                              animations (IN TICKS!!)
	 */
	public AnimationController(T animatable, String name, float transitionLengthTicks, IAnimationPredicate<T> animationPredicate) {
		this.animatable = animatable;
		this.name = name;
		this.transitionLengthTicks = transitionLengthTicks;
		this.animationPredicate = animationPredicate;
        this.tickOffset = 0.0d;
	}

	/**
	 * Instantiates a new Animation controller. Each animation controller can run
	 * one animation at a time. You can have several animation controllers for each
	 * entity, i.e. one animation to control the entity's size, one to control
	 * movement, attacks, etc.
	 *
	 * @param animatable            The entity
	 * @param name                  Name of the animation controller
	 *                              (move_controller, size_controller,
	 *                              attack_controller, etc.)
	 * @param transitionLengthTicks How long it takes to transition between
	 *                              animations (IN TICKS!!)
	 * @param easingtype            The method of easing to use. The other
	 *                              constructor defaults to no easing.
	 */
	public AnimationController(T animatable, String name, float transitionLengthTicks, EasingType easingtype, IAnimationPredicate<T> animationPredicate) {
		this.animatable = animatable;
		this.name = name;
		this.transitionLengthTicks = transitionLengthTicks;
		this.easingType = easingtype;
		this.animationPredicate = animationPredicate;
        this.tickOffset = 0.0d;
	}

	/**
	 * Instantiates a new Animation controller. Each animation controller can run
	 * one animation at a time. You can have several animation controllers for each
	 * entity, i.e. one animation to control the entity's size, one to control
	 * movement, attacks, etc.
	 *
	 * @param animatable            The entity
	 * @param name                  Name of the animation controller
	 *                              (move_controller, size_controller,
	 *                              attack_controller, etc.)
	 * @param transitionLengthTicks How long it takes to transition between
	 *                              animations (IN TICKS!!)
	 * @param customEasingMethod    If you want to use an easing method that's not
	 *                              included in the EasingType enum, pass your
	 *                              method into here. The parameter that's passed in
	 *                              will be a number between 0 and 1. Return a
	 *                              number also within 0 and 1. Take a look at
	 *                              {@link EasingManager}
	 */
	public AnimationController(T animatable, String name, float transitionLengthTicks, Function<Double, Double> customEasingMethod, IAnimationPredicate<T> animationPredicate) {
		this.animatable = animatable;
		this.name = name;
		this.transitionLengthTicks = transitionLengthTicks;
		this.customEasingMethod = customEasingMethod;
		this.easingType = EasingType.CUSTOM;
		this.animationPredicate = animationPredicate;
        this.tickOffset = 0.0d;
	}

	/**
	 * Gets the controller's name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the current animation. Can be null
	 *
	 * @return the current animation
	 */

	public Animation getCurrentAnimation() {
		return this.currentAnimation;
	}

	/**
	 * Returns the current state of this animation controller.
	 */
	public AnimationState getAnimationState() {
		return this.animationState;
	}

	/**
	 * Gets the current animation's bone animation queues.
	 *
	 * @return the bone animation queues
	 */
	public HashMap<String, BoneAnimationQueue> getBoneAnimationQueues() {
		return this.boneAnimationQueues;
	}

	/**
	 * This method is called every frame in order to populate the animation point
	 * queues, and process animation state logic.
	 *
	 * @param tick                   The current tick + partial tick
	 * @param event                  The animation test event
	 * @param modelRendererList      The list of all AnimatedModelRender's
	 * @param boneSnapshotCollection The bone snapshot collection
	 */
	public void process(double tick, AnimationEvent<T> event, List<IBone> modelRendererList,
			HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection, MolangParser parser, MolangScope scope,
			boolean crashWhenCantFindBone) {
        double tickWithinScope = tick;
        parser.withScope(scope, () -> {
            parser.setValue("query.life_time", tickWithinScope / 20);
        });
		if (this.currentAnimation != null) {
			IAnimatableModel<T> model = getModel(this.animatable);
			if (model != null) {
				Animation animation = model.getAnimation(this.currentAnimation.animationName, this.animatable);
				if (animation != null) {
					LoopType loop = currentAnimation.loop;
                    this.currentAnimation = animation;
                    this.currentAnimation.loop = loop;
				}
			}
		}

		createInitialQueues(modelRendererList);

		double actualTick = tick;
		tick = adjustTick(tick);

		// Transition period has ended, reset the tick and set the animation to running
		if (this.animationState == AnimationState.Transitioning && tick >= this.transitionLengthTicks) {
			this.shouldResetTick = true;
            this.animationState = AnimationState.Running;
			tick = adjustTick(actualTick);
		}

		assert tick >= 0 : "GeckoLib: Tick was less than zero";

		// This tests the animation predicate
		PlayState playState = this.testAnimationPredicate(event);
		if (playState == PlayState.STOP || (currentAnimation == null && animationQueue.isEmpty())) {
			// The animation should transition to the model's initial state
            this.animationState = AnimationState.Stopped;
            this.justStopped = true;
			return;
		}
		if (this.justStartedTransition && (this.shouldResetTick || this.justStopped)) {
            this.justStopped = false;
			tick = adjustTick(actualTick);
		}
        else if (currentAnimation == null && !this.animationQueue.isEmpty()) {
			this.shouldResetTick = true;
			this.animationState = AnimationState.Transitioning;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
			tick = adjustTick(actualTick);
		}
        else {
			if (this.animationState != AnimationState.Transitioning) {
                this.animationState = AnimationState.Running;
			}
		}

		// Handle transitioning to a different animation (or just starting one)
		if (this.animationState == AnimationState.Transitioning) {
			// Just started transitioning, so set the current animation to the first one
			if (tick == 0 || this.isJustStarting) {
                this.justStartedTransition = false;
				this.currentAnimation = animationQueue.poll();
				this.resetEventKeyFrames();
				saveSnapshotsForAnimation(this.currentAnimation, boneSnapshotCollection);
			}
			if (this.currentAnimation != null) {
				this.setAnimTime(parser, 0);
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

					// Adding the initial positions of the upcoming animation, so the model
					// transitions to the initial state of the new animation
					if (!rotationKeyFrames.isEmpty()) {
                        AnimationPoint xPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                        AnimationPoint yPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                        AnimationPoint zPoint = rotationKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

						boneAnimationQueue.rotationXQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.rotationValueX - initialSnapshot.rotationValueX,
								xPoint.animationStartValue));
						boneAnimationQueue.rotationYQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.rotationValueY - initialSnapshot.rotationValueY,
								yPoint.animationStartValue));
						boneAnimationQueue.rotationZQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.rotationValueZ - initialSnapshot.rotationValueZ,
								zPoint.animationStartValue));
					}

					if (!positionKeyFrames.isEmpty()) {
                        AnimationPoint xPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                        AnimationPoint yPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                        AnimationPoint zPoint = positionKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

						boneAnimationQueue.positionXQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.positionOffsetX, xPoint.animationStartValue));
						boneAnimationQueue.positionYQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.positionOffsetY, yPoint.animationStartValue));
						boneAnimationQueue.positionZQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.positionOffsetZ, zPoint.animationStartValue));
					}

					if (!scaleKeyFrames.isEmpty()) {
                        AnimationPoint xPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.X);
                        AnimationPoint yPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Y);
                        AnimationPoint zPoint = scaleKeyFrames.getAnimationPointAtTick(parser, scope, 0, Axis.Z);

						boneAnimationQueue.scaleXQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.scaleValueX, xPoint.animationStartValue));
						boneAnimationQueue.scaleYQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.scaleValueY, yPoint.animationStartValue));
						boneAnimationQueue.scaleZQueue.add(new AnimationPoint(null, tick, transitionLengthTicks,
								boneSnapshot.scaleValueZ, zPoint.animationStartValue));
					}
				}
			}
		}
        else if (getAnimationState() == AnimationState.Running) {
			// Actually run the animation
			this.processCurrentAnimation(tick, actualTick, parser, scope, crashWhenCantFindBone);
		}
	}

	private void setAnimTime(MolangParser parser, double tick) {
		parser.setValue("query.anim_time", tick / 20);
	}

	private IAnimatableModel<T> getModel(T animatable) {
		for (ModelFetcher<?> modelFetcher : modelFetchers) {
			IAnimatableModel<T> model = (IAnimatableModel<T>) modelFetcher.apply(animatable);
			if (model != null) return model;
		}
		System.out.printf(
				"Could not find suitable model for animatable of type %s. Did you register a Model Fetcher?%n",
				animatable.getClass());
		return null;
	}

	protected PlayState testAnimationPredicate(AnimationEvent<T> event) {
		return this.animationPredicate.test(event);
	}

	// At the beginning of a new transition, save a snapshot of the model's
	// rotation, position, and scale values as the initial value to lerp from
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

	private void processCurrentAnimation(double tick, double actualTick, MolangParser parser, MolangScope scope, boolean crashWhenCantFindBone) {
		assert currentAnimation != null;
		//Animation has ended
		if (tick >= currentAnimation.animationLength) {
			if (this.currentAnimation.loop == LoopType.PLAY_ONCE) {
                this.resetEventKeyFrames();

				//pull the next animation from the queue
				Animation peek = this.animationQueue.peek();
				if (peek == null) {
					//No more animations left, stop the animation controller
					this.animationState = AnimationState.Stopped;
					return;
				}
                else {
					//Otherwise, set the state to transitioning and start transitioning to the next
					//animation next frame
					this.animationState = AnimationState.Transitioning;
                    this.shouldResetTick = true;
                    this.currentAnimation = this.animationQueue.peek();
				}
			}
            //unlike other loop types, hold on last frame doesn't reset key frames
            //until a new animation in the queue shows up
            else if (this.currentAnimation.loop == LoopType.HOLD_ON_LAST_FRAME) {
                Animation peek = this.animationQueue.peek();
                if (peek != null) {
                    this.resetEventKeyFrames();
                    this.animationState = AnimationState.Transitioning;
                    this.shouldResetTick = true;
                    this.currentAnimation = this.animationQueue.peek();
                }
            }
            //if the current animation is set to loop, keep it as the current animation and
            //just start over
            else if (this.currentAnimation.loop == LoopType.LOOP) {
                this.resetEventKeyFrames();

				//Reset the adjusted tick so the next animation starts at tick 0
				this.shouldResetTick = true;
				tick = this.adjustTick(actualTick);
			}
		}
		this.setAnimTime(parser, tick);

		// Loop through every boneanimation in the current animation and process the
		// values
		List<BoneAnimation> boneAnimations = currentAnimation.boneAnimations;
		for (BoneAnimation boneAnimation : boneAnimations) {
			BoneAnimationQueue boneAnimationQueue = boneAnimationQueues.get(boneAnimation.boneName);
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
				boneAnimationQueue.rotationXQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.X));
				boneAnimationQueue.rotationYQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Y));
				boneAnimationQueue.rotationZQueue.add(rotationKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Z));
			}

			if (!positionKeyFrames.isEmpty()) {
				boneAnimationQueue.positionXQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.X));
				boneAnimationQueue.positionYQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Y));
				boneAnimationQueue.positionZQueue.add(positionKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Z));
			}

			if (!scaleKeyFrames.isEmpty()) {
				boneAnimationQueue.scaleXQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.X));
				boneAnimationQueue.scaleYQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Y));
				boneAnimationQueue.scaleZQueue.add(scaleKeyFrames.getAnimationPointAtTick(parser, scope, tick, Axis.Z));
			}
		}

        //create a riftlibrary particle emitter that's attached to a locator
        for (EventKeyFrame.ParticleEventKeyFrame particleEventKeyFrame : this.currentAnimation.particleKeyFrames) {
            if (!this.executedKeyFrames.contains(particleEventKeyFrame) && tick >= particleEventKeyFrame.getStartTick()) {
                this.lastParticleEvent = particleEventKeyFrame;
                this.executedKeyFrames.add(particleEventKeyFrame);
            }
        }

        //create a riftlibrary sound effect that's attached to a locator
        for (EventKeyFrame.SoundEventKeyFrame soundEventKeyFrame : this.currentAnimation.soundKeyFrames) {
            if (!this.executedKeyFrames.contains(soundEventKeyFrame) && tick >= soundEventKeyFrame.getStartTick()) {
                this.lastSoundEvent = soundEventKeyFrame;
                this.executedKeyFrames.add(soundEventKeyFrame);
            }
        }

		if (this.transitionLengthTicks == 0 && shouldResetTick && this.animationState == AnimationState.Transitioning) {
			this.currentAnimation = animationQueue.poll();
		}
	}

	// Helper method to populate all the initial animation point queues
	private void createInitialQueues(List<IBone> modelRendererList) {
        this.boneAnimationQueues.clear();
		for (IBone modelRenderer : modelRendererList) {
            this.boneAnimationQueues.put(modelRenderer.getName(), new BoneAnimationQueue(modelRenderer));
		}
	}

	// Used to reset the "tick" everytime a new animation starts, a transition
	// starts, or something else of importance happens
	protected double adjustTick(double tick) {
		if (this.shouldResetTick) {
			if (getAnimationState() == AnimationState.Transitioning) {
				this.tickOffset = tick;
			}
            else if (getAnimationState() == AnimationState.Running) {
				this.tickOffset = tick;
			}
            this.shouldResetTick = false;
			return 0;
		}
        // assert tick - this.tickOffset >= 0;
        else return this.animationSpeed * Math.max(tick - this.tickOffset, 0.0D);
	}

    public EventKeyFrame.ParticleEventKeyFrame getLastParticleEvent() {
        EventKeyFrame.ParticleEventKeyFrame toReturn = this.lastParticleEvent;
        this.lastParticleEvent = null;
        return toReturn;
    }

    public EventKeyFrame.SoundEventKeyFrame getLastSoundEvent() {
        EventKeyFrame.SoundEventKeyFrame toReturn = this.lastSoundEvent;
        this.lastSoundEvent = null;
        return toReturn;
    }

	private void resetEventKeyFrames() {
		this.executedKeyFrames.clear();
	}

	public void markNeedsReload() {
		this.needsAnimationReload = true;
	}

	public void clearAnimationCache() {
		this.currentAnimationBuilder = new AnimationBuilder();
        this.resetEventKeyFrames();
	}

	public double getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	@FunctionalInterface
	public interface ModelFetcher<T> extends Function<IAnimatable, IAnimatableModel<T>> {}
}