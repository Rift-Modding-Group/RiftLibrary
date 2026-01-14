/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.riftlib.core.builder;

import java.util.ArrayList;
import java.util.List;

import anightdazingzoroark.riftlib.core.keyframe.EventKeyFrame;
import anightdazingzoroark.riftlib.core.keyframe.BoneAnimation;

/**
 * A specific animation instance
 */
public class Animation {
	public String animationName;
	public Double animationLength;
	public LoopType loop = LoopType.LOOP;
	public String animTimeUpdateExpression;
	public List<BoneAnimation> boneAnimations = new ArrayList<>();
	public List<EventKeyFrame.SoundEventKeyFrame> soundKeyFrames = new ArrayList<>();
	public List<EventKeyFrame.ParticleEventKeyFrame> particleKeyFrames = new ArrayList<>();
}
