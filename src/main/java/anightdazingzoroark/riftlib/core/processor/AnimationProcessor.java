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

			//-----running custom instructions-----
			boolean isServerSynced = animationData.isServerSynced() || ServerModelRegistry.hasServerModel(entity);
			boolean runCustomInstructions = !runClientEffects || !isServerSynced;
			for (EventKeyFrame.CustomInstructionKeyFrame customInstructionEvent : controller.drainCustomInstructionEvents()) {
				if (runCustomInstructions) MolangUtils.parseValue(entity, customInstructionEvent.instruction);
			}

			//-----client only stuff down here-----
			if (runClientEffects) {
				//animation effects
				for (EventKeyFrame.ParticleEventKeyFrame particleEvent : controller.drainParticleEvents()) {
					AnimatedLocator locator = animationData.getAnimatedLocator(particleEvent.locator);
					if (locator != null) {
						ParticleBuilder particleBuilder = RiftLibParticleHelper.getParticleBuilder(particleEvent.effect);
						if (particleBuilder != null) locator.createParticleEmitter(particleBuilder);
					}
				}

				//sound effects
				for (EventKeyFrame.SoundEventKeyFrame soundEvent : controller.drainSoundEvents()) {
					AnimatedLocator locator = animationData.getAnimatedLocator(soundEvent.locator);
					if (locator != null) RiftLibSoundHelper.playSound(entity, locator, soundEvent.effect);
				}
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

			float[] rot = boneAnimationValues.getRotations(bone.getName());
			float[] pos = boneAnimationValues.getPositions(bone.getName());
			float[] scale = boneAnimationValues.getScales(bone.getName());

			bone.setRotationX(rot[0] + initialSnapshot.rotationValueX);
			bone.setRotationY(rot[1] + initialSnapshot.rotationValueY);
			bone.setRotationZ(rot[2] + initialSnapshot.rotationValueZ);

			bone.setPositionX(pos[0] + initialSnapshot.positionOffsetX);
			bone.setPositionY(pos[1] + initialSnapshot.positionOffsetY);
			bone.setPositionZ(pos[2] + initialSnapshot.positionOffsetZ);

			bone.setScaleX(scale[0] * initialSnapshot.scaleValueX);
			bone.setScaleY(scale[1] * initialSnapshot.scaleValueY);
			bone.setScaleZ(scale[2] * initialSnapshot.scaleValueZ);

			snapshot.rotationValueX = bone.getRotationX();
			snapshot.rotationValueY = bone.getRotationY();
			snapshot.rotationValueZ = bone.getRotationZ();
			snapshot.positionOffsetX = bone.getPositionX();
			snapshot.positionOffsetY = bone.getPositionY();
			snapshot.positionOffsetZ = bone.getPositionZ();
			snapshot.scaleValueX = bone.getScaleX();
			snapshot.scaleValueY = bone.getScaleY();
			snapshot.scaleValueZ = bone.getScaleZ();

			snapshot.isCurrentlyRunningRotationAnimation = true;
			snapshot.isCurrentlyRunningPositionAnimation = true;
			snapshot.isCurrentlyRunningScaleAnimation = true;

			dirtyTracker.hasRotationChanged = true;
			dirtyTracker.hasPositionChanged = true;
			dirtyTracker.hasScaleChanged = true;
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
						MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueX, initialSnapshot.rotationValueX),
						MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueY, initialSnapshot.rotationValueY),
						MathUtil.lerpValues(percentageReset, saveSnapshot.rotationValueZ, initialSnapshot.rotationValueZ)
				);
				model.setRotationX(dBoneAnimationValues.getRotations(model.getName())[0]);
				model.setRotationY(dBoneAnimationValues.getRotations(model.getName())[1]);
				model.setRotationZ(dBoneAnimationValues.getRotations(model.getName())[2]);
				saveSnapshot.rotationValueX = model.getRotationX();
				saveSnapshot.rotationValueY = model.getRotationY();
				saveSnapshot.rotationValueZ = model.getRotationZ();
			}
			if (!tracker.getValue().hasPositionChanged) {
				if (saveSnapshot.isCurrentlyRunningPositionAnimation) {
					saveSnapshot.mostRecentResetPositionTick = (float) seekTime;
					saveSnapshot.isCurrentlyRunningPositionAnimation = false;
				}

				double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetPositionTick + 1D), 1);

				dBoneAnimationValues.addPositions(
						model.getName(),
						MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetX, initialSnapshot.positionOffsetX),
						MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetY, initialSnapshot.positionOffsetY),
						MathUtil.lerpValues(percentageReset, saveSnapshot.positionOffsetZ, initialSnapshot.positionOffsetZ)
				);
				model.setPositionX(dBoneAnimationValues.getPositions(model.getName())[0]);
				model.setPositionY(dBoneAnimationValues.getPositions(model.getName())[1]);
				model.setPositionZ(dBoneAnimationValues.getPositions(model.getName())[2]);
				saveSnapshot.positionOffsetX = model.getPositionX();
				saveSnapshot.positionOffsetY = model.getPositionY();
				saveSnapshot.positionOffsetZ = model.getPositionZ();
			}
			if (!tracker.getValue().hasScaleChanged) {
				if (saveSnapshot.isCurrentlyRunningScaleAnimation) {
					saveSnapshot.mostRecentResetScaleTick = (float) seekTime;
					saveSnapshot.isCurrentlyRunningScaleAnimation = false;
				}

				double percentageReset = Math.min((seekTime - saveSnapshot.mostRecentResetScaleTick + 1D), 1);

				model.setScaleX(MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueX, initialSnapshot.scaleValueX));
				model.setScaleY(MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueY, initialSnapshot.scaleValueY));
				model.setScaleZ(MathUtil.lerpValues(percentageReset, saveSnapshot.scaleValueZ, initialSnapshot.scaleValueZ));
				saveSnapshot.scaleValueX = model.getScaleX();
				saveSnapshot.scaleValueY = model.getScaleY();
				saveSnapshot.scaleValueZ = model.getScaleZ();
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
