package anightdazingzoroark.riftlib.core.processor;

import java.util.*;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.keyframe.*;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundHelper;
import anightdazingzoroark.riftlib.util.MolangUtils;
import org.apache.commons.lang3.tuple.Pair;

import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.core.snapshot.DirtyTracker;
import anightdazingzoroark.riftlib.core.util.MathUtil;

public class AnimationProcessor {
	public void tickAnimation(
			IAnimatable<?> entity,
			List<IBone> modelRendererList,
			double seekTime,
			boolean crashWhenCantFindBone,
			boolean runClientEffects
	) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AbstractAnimationData<?, ?> animationData = entity.getAnimationData();
		// Keeps track of which bones have had animations applied to them, and
		// eventually sets the ones that don't have an animation to their default values
		Map<String, DirtyTracker> modelTracker = this.createNewDirtyTracker(modelRendererList);

		// Store the current value of each bone rotation/position/scale
		this.updateBoneSnapshots(modelRendererList, animationData.getBoneSnapshotCollection());

		Map<String, Pair<IBone, BoneSnapshot>> boneSnapshots = animationData.getBoneSnapshotCollection();

		//create anim values list to store the changes in
		BoneAnimationValuesList boneAnimationValues = new BoneAnimationValuesList();
		List<EventKeyFrame.CustomInstructionKeyFrame> customInstructionEvents = new ArrayList<>();
		List<EventKeyFrame.ParticleEventKeyFrame> particleEvents = new ArrayList<>();
		List<EventKeyFrame.SoundEventKeyFrame> soundEvents = new ArrayList<>();
		boolean isServerSynced = animationData.isServerSynced() || ServerModelRegistry.hasServerModel(entity);
		boolean runCustomInstructions = !runClientEffects || !isServerSynced;

		//get changes from all anim controllers
		for (AnimationController<?, ?> controller : animationData.getAnimationControllers().values()) {
			controller.isJustStarting = animationData.isFirstTick;
			controller.process(seekTime, modelRendererList, boneSnapshots, crashWhenCantFindBone);

			for (BoneAnimationQueue boneAnimation : controller.getBoneAnimationQueues().values()) {
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
					boneAnimationValues.addRotations(
							bone.getName(),
							MathUtil.lerpValues(rXPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(rYPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(rZPoint, controller.easingType, controller.customEasingMethod)
					);
				}

				if (pXPoint != null && pYPoint != null && pZPoint != null) {
					boneAnimationValues.addPositions(
							bone.getName(),
							MathUtil.lerpValues(pXPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(pYPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(pZPoint, controller.easingType, controller.customEasingMethod)
					);
				}

				if (sXPoint != null && sYPoint != null && sZPoint != null) {
					boneAnimationValues.addScales(
							bone.getName(),
							MathUtil.lerpValues(sXPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(sYPoint, controller.easingType, controller.customEasingMethod),
							MathUtil.lerpValues(sZPoint, controller.easingType, controller.customEasingMethod)
					);
				}
			}

			customInstructionEvents.addAll(controller.drainCustomInstructionEvents());

			//-----client only stuff down here-----
			if (runClientEffects) {
				particleEvents.addAll(controller.drainParticleEvents());
				soundEvents.addAll(controller.drainSoundEvents());
			}
			else {
				controller.drainParticleEvents();
				controller.drainSoundEvents();
			}
		}

		//apply changes from anims to bones and locators
		for (IBone bone : modelRendererList) {
			BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
			BoneSnapshot snapshot = boneSnapshots.get(bone.getName()).getRight();

			DirtyTracker dirtyTracker = modelTracker.get(bone.getName());
			if (dirtyTracker == null) continue;

			if (boneAnimationValues.hasRotations(bone.getName())) {
				float[] rot = boneAnimationValues.getRotations(bone.getName());
				bone.getRotation().set(
						rot[0] + initialSnapshot.getRotation().x,
						rot[1] + initialSnapshot.getRotation().y,
						rot[2] + initialSnapshot.getRotation().z
				);
				snapshot.getRotation().set(bone.getRotation());
				snapshot.isCurrentlyRunningRotationAnimation = true;
				dirtyTracker.hasRotationChanged = true;
			}

			if (boneAnimationValues.hasPositions(bone.getName())) {
				float[] pos = boneAnimationValues.getPositions(bone.getName());
				bone.getPosition().set(
						pos[0] + initialSnapshot.getPosition().x,
						pos[1] + initialSnapshot.getPosition().y,
						pos[2] + initialSnapshot.getPosition().z
				);
				snapshot.getPosition().set(bone.getPosition());
				snapshot.isCurrentlyRunningPositionAnimation = true;
				dirtyTracker.hasPositionChanged = true;
			}

			if (boneAnimationValues.hasScales(bone.getName())) {
				float[] scale = boneAnimationValues.getScales(bone.getName());
				bone.getScale().set(
						scale[0] * initialSnapshot.getScale().x,
						scale[1] * initialSnapshot.getScale().y,
						scale[2] * initialSnapshot.getScale().z
				);
				snapshot.getScale().set(bone.getScale());
				snapshot.isCurrentlyRunningScaleAnimation = true;
				dirtyTracker.hasScaleChanged = true;
			}
		}

		BoneAnimationValuesList dBoneAnimationValues = new BoneAnimationValuesList();
		for (Map.Entry<String, DirtyTracker> tracker : modelTracker.entrySet()) {
			IBone model = tracker.getValue().model;
			BoneSnapshot initialSnapshot = model.getInitialSnapshot();
			BoneSnapshot saveSnapshot = boneSnapshots.get(tracker.getKey()).getRight();
			if (saveSnapshot == null) {
				if (crashWhenCantFindBone) {
					throw new RuntimeException(
							"Could not find save snapshot for bone: " + tracker.getValue().model.getName()
									+ ". Please don't add bones that are used in an animation at runtime.");
				}
				else continue;
			}

			if (!tracker.getValue().hasRotationChanged) {
				if (saveSnapshot.isCurrentlyRunningRotationAnimation) {
					saveSnapshot.mostRecentResetRotationTick = (float) seekTime;
					saveSnapshot.isCurrentlyRunningRotationAnimation = false;
				}

				double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetRotationTick + 1D), 1);

				dBoneAnimationValues.addRotations(
						model.getName(),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getRotation().x, initialSnapshot.getRotation().x),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getRotation().y, initialSnapshot.getRotation().y),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getRotation().z, initialSnapshot.getRotation().z)
				);
				model.getRotation().set(
						dBoneAnimationValues.getRotations(model.getName())[0],
						dBoneAnimationValues.getRotations(model.getName())[1],
						dBoneAnimationValues.getRotations(model.getName())[2]
				);
				saveSnapshot.getRotation().set(model.getRotation());
			}
			if (!tracker.getValue().hasPositionChanged) {
				if (saveSnapshot.isCurrentlyRunningPositionAnimation) {
					saveSnapshot.mostRecentResetPositionTick = (float) seekTime;
					saveSnapshot.isCurrentlyRunningPositionAnimation = false;
				}

				double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetPositionTick + 1D), 1);

				dBoneAnimationValues.addPositions(
						model.getName(),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getPosition().x, initialSnapshot.getPosition().x),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getPosition().y, initialSnapshot.getPosition().y),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getPosition().z, initialSnapshot.getPosition().z)
				);
				model.getPosition().set(
						dBoneAnimationValues.getPositions(model.getName())[0],
						dBoneAnimationValues.getPositions(model.getName())[1],
						dBoneAnimationValues.getPositions(model.getName())[2]
				);
				saveSnapshot.getPosition().set(model.getPosition());
			}
			if (!tracker.getValue().hasScaleChanged) {
				if (saveSnapshot.isCurrentlyRunningScaleAnimation) {
					saveSnapshot.mostRecentResetScaleTick = (float) seekTime;
					saveSnapshot.isCurrentlyRunningScaleAnimation = false;
				}

				double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetScaleTick + 1D), 1);

				model.getScale().set(
						MathUtil.lerpValues(percentageReset, saveSnapshot.getScale().x, initialSnapshot.getScale().x),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getScale().y, initialSnapshot.getScale().y),
						MathUtil.lerpValues(percentageReset, saveSnapshot.getScale().z, initialSnapshot.getScale().z)
				);
				saveSnapshot.getScale().set(model.getScale());
			}
		}

		//-----running custom instructions-----
		for (EventKeyFrame.CustomInstructionKeyFrame customInstructionEvent : customInstructionEvents) {
			if (runCustomInstructions) entity.getAnimationData().parseExpression(customInstructionEvent.instruction);
		}

		//-----client only stuff down here-----
		if (runClientEffects) {
			//animation effects
			for (EventKeyFrame.ParticleEventKeyFrame particleEvent : particleEvents) {
				AnimatedLocator locator = animationData.getAnimatedLocator(particleEvent.locator);
				if (locator != null) {
					ParticleBuilder particleBuilder = RiftLibParticleHelper.getParticleBuilder(particleEvent.effect);
					if (particleBuilder != null) locator.createParticleEmitter(particleBuilder);
				}
			}

			//sound effects
			for (EventKeyFrame.SoundEventKeyFrame soundEvent : soundEvents) {
				AnimatedLocator locator = animationData.getAnimatedLocator(soundEvent.locator);
				if (locator != null) RiftLibSoundHelper.playSound(entity, locator, soundEvent.effect);
			}
		}
		animationData.isFirstTick = false;
	}

	private Map<String, DirtyTracker> createNewDirtyTracker(List<IBone> modelRendererList) {
		Map<String, DirtyTracker> tracker = new HashMap<>();
		for (IBone bone : modelRendererList) {
			tracker.put(bone.getName(), new DirtyTracker(false, false, false, bone));
		}
		return tracker;
	}

	private void updateBoneSnapshots(List<IBone> modelRendererList, Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection) {
		for (IBone bone : modelRendererList) {
			if (!boneSnapshotCollection.containsKey(bone.getName())) {
				boneSnapshotCollection.put(bone.getName(), Pair.of(bone, new BoneSnapshot(bone.getInitialSnapshot())));
			}
		}
	}
}
