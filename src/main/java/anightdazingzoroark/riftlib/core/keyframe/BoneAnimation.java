package anightdazingzoroark.riftlib.core.keyframe;

/**
 * This new and improved :tm: BoneAnimation class will hold all information
 * about bone positions, rotations, and scales for animations
 */
public class BoneAnimation {
    public String boneName;
    public VectorKeyFrameList rotationKeyFrames = new VectorKeyFrameList(true);
    public VectorKeyFrameList positionKeyFrames = new VectorKeyFrameList();
    public VectorKeyFrameList scaleKeyFrames = new VectorKeyFrameList();


}
