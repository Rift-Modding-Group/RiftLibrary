/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.riftlib.core.keyframe;

import anightdazingzoroark.riftlib.molang.math.IValue;

public class BoneAnimation {
	public String boneName;
	public VectorKeyFrameList<KeyFrame<IValue>> rotationKeyFrames;
	public VectorKeyFrameList<KeyFrame<IValue>> positionKeyFrames;
	public VectorKeyFrameList<KeyFrame<IValue>> scaleKeyFrames;
}
