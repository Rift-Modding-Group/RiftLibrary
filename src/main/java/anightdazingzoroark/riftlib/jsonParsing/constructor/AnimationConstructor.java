package anightdazingzoroark.riftlib.jsonParsing.constructor;

import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.keyframe.*;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationChannel;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimationConstructor {
    public static Animation getAnimationFromRawAnimationEntry(Map.Entry<String, RawAnimationFile.RawAnimation> rawAnimation) {
        Animation toReturn = new Animation();

        //set anim info
        toReturn.animationName = rawAnimation.getKey();
        toReturn.loop = rawAnimation.getValue().loop == Boolean.TRUE ? LoopType.LOOP : LoopType.PLAY_ONCE;
        toReturn.animationLength = rawAnimation.getValue().animationLength != null ? rawAnimation.getValue().animationLength * 20 : null; //if length is null, it will be calculated later based on the provided info
        toReturn.animTimeUpdateExpression = rawAnimation.getValue().animTimeUpdate; //if null, it just use default update method

        //create particle animations
        Map<String, RawAnimationFile.RawParticleEffectAnimations> rawParticleEffectAnimations = rawAnimation.getValue().particleEffects;
        if (rawParticleEffectAnimations != null) {
            for (Map.Entry<String, RawAnimationFile.RawParticleEffectAnimations> rawParticleEffectAnim : rawParticleEffectAnimations.entrySet()) {
                EventKeyFrame.ParticleEventKeyFrame particleEventKeyFrame = new EventKeyFrame.ParticleEventKeyFrame(
                        Double.parseDouble(rawParticleEffectAnim.getKey()) * 20,
                        rawParticleEffectAnim.getValue().effect,
                        rawParticleEffectAnim.getValue().locator,
                        rawParticleEffectAnim.getValue().preEffectScript
                );
                toReturn.particleKeyFrames.add(particleEventKeyFrame);
            }
        }

        //create sound animations
        Map<String, RawAnimationFile.RawSoundEffectAnimations> rawSoundEffectAnimations = rawAnimation.getValue().soundEffects;
        if (rawSoundEffectAnimations != null) {
            for (Map.Entry<String, RawAnimationFile.RawSoundEffectAnimations> rawSoundEffectAnim : rawSoundEffectAnimations.entrySet()) {
                EventKeyFrame.SoundEventKeyFrame soundEventKeyFrame = new EventKeyFrame.SoundEventKeyFrame(
                        Double.parseDouble(rawSoundEffectAnim.getKey()) * 20,
                        rawSoundEffectAnim.getValue().effect,
                        rawSoundEffectAnim.getValue().locator
                );
                toReturn.soundKeyFrames.add(soundEventKeyFrame);
            }
        }

        //create bone animations
        Map<String, RawAnimationFile.RawBoneAnimations> rawBoneAnimations = rawAnimation.getValue().bones;
        if (rawBoneAnimations != null) {
            for (Map.Entry<String, RawAnimationFile.RawBoneAnimations> rawBoneAnimation : rawBoneAnimations.entrySet()) {
                BoneAnimation boneAnimation = new BoneAnimation();
                boneAnimation.boneName = rawBoneAnimation.getKey();

                //positions
                if (rawBoneAnimation.getValue().position != null) {
                    try {
                        RawAnimationChannel rawPositionChannel = rawBoneAnimation.getValue().position;
                        boneAnimation.positionKeyFrames = convertRawChannelToFrameList(rawPositionChannel, false);
                    }
                    catch (Exception e) {}
                }

                //rotations
                if (rawBoneAnimation.getValue().rotation != null) {
                    try {
                        RawAnimationChannel rawRotationChannel = rawBoneAnimation.getValue().rotation;
                        boneAnimation.rotationKeyFrames = convertRawChannelToFrameList(rawRotationChannel, true);
                    }
                    catch (Exception e) {}
                }

                //scaling
                if (rawBoneAnimation.getValue().scale != null) {
                    try {
                        RawAnimationChannel rawPositionChannel = rawBoneAnimation.getValue().scale;
                        boneAnimation.scaleKeyFrames = convertRawChannelToFrameList(rawPositionChannel, false);
                    }
                    catch (Exception e) {}
                }

                toReturn.boneAnimations.add(boneAnimation);
            }
        }

        //manually compute anim length based on above info
        if (toReturn.animationLength == null) toReturn.animationLength = calculateLength(toReturn.boneAnimations);

        return toReturn;
    }

    private static double calculateLength(List<BoneAnimation> boneAnimations) {
        double longestLength = 0;
        for (BoneAnimation animation : boneAnimations) {
            double rotationKeyframeTime = animation.rotationKeyFrames.getLastKeyframeTime();
            double positionKeyframeTime = animation.positionKeyFrames.getLastKeyframeTime();
            double scaleKeyframeTime = animation.scaleKeyFrames.getLastKeyframeTime();
            longestLength = maxAll(longestLength, rotationKeyframeTime, positionKeyframeTime, scaleKeyframeTime);
        }
        return longestLength == 0 ? Double.MAX_VALUE : longestLength;
    }

    private static double maxAll(double... values) {
        double max = 0;
        for (double value : values) max = Math.max(value, max);
        return max;
    }

    private static VectorKeyFrameList convertRawChannelToFrameList(RawAnimationChannel rawAnimationChannel, boolean isRotation) throws NumberFormatException {
        VectorKeyFrameList toReturn = new VectorKeyFrameList(isRotation);
        KeyFrame.KeyFrameAxisValue previousXValue = null;
        KeyFrame.KeyFrameAxisValue previousYValue = null;
        KeyFrame.KeyFrameAxisValue previousZValue = null;

        //vector mode for raw anim channels only has 1 keyframe so there's that
        int channelSize = rawAnimationChannel.isKeyframed() ? rawAnimationChannel.keyframes.size() : 1;
        for (int i = 0; i < channelSize; i++) {
            RawAnimationChannel.RawKeyframe rawKeyframe = rawAnimationChannel.keyframes.get(i);
            RawAnimationChannel.RawKeyframe previousRawKeyframe = i == 0 ? null : rawAnimationChannel.keyframes.get(i - 1);

            Double previousKeyFrameLocation = previousRawKeyframe == null ? 0 : previousRawKeyframe.time;
            Double currentKeyFrameLocation = rawKeyframe.time;
            double animationTimeDifference = currentKeyFrameLocation - previousKeyFrameLocation;

            KeyFrame.KeyFrameAxisValue xValue = parseExpression(rawKeyframe.vector[0]);
            KeyFrame.KeyFrameAxisValue yValue = parseExpression(rawKeyframe.vector[1]);
            KeyFrame.KeyFrameAxisValue zValue = parseExpression(rawKeyframe.vector[2]);

            KeyFrame.KeyFrameAxisValue currentXValue = isRotation && !xValue.isExpression()
                    ? new KeyFrame.KeyFrameAxisValue(Math.toRadians(-xValue.getConstValue()))
                    : xValue;
            KeyFrame.KeyFrameAxisValue currentYValue = isRotation && !yValue.isExpression()
                    ? new KeyFrame.KeyFrameAxisValue(Math.toRadians(-yValue.getConstValue()))
                    : yValue;
            KeyFrame.KeyFrameAxisValue currentZValue = isRotation && !zValue.isExpression()
                    ? new KeyFrame.KeyFrameAxisValue(Math.toRadians(zValue.getConstValue()))
                    : zValue;

            KeyFrame.KeyFrameVectorValue currentVector = new KeyFrame.KeyFrameVectorValue(
                    currentXValue,
                    currentYValue,
                    currentZValue
            );
            KeyFrame.KeyFrameVectorValue previousVector = new KeyFrame.KeyFrameVectorValue(
                    previousXValue,
                    previousYValue,
                    previousZValue
            );

            KeyFrame keyFrameToAdd;

            if (rawKeyframe.easingType != null) {
                EasingType easingType = EasingType.getEasingTypeFromString(rawKeyframe.easingType);
                if (rawKeyframe.easingArgs != null) {
                    List<Double> easingArgs = convertEasingArgsToList(rawKeyframe.easingArgs);

                    keyFrameToAdd = new KeyFrame(
                            animationTimeDifference * 20,
                            i == 0 ? currentVector : previousVector,
                            currentVector,
                            easingType, easingArgs
                    );
                }
                else {
                    keyFrameToAdd = new KeyFrame(
                            animationTimeDifference * 20,
                            i == 0 ? currentVector : previousVector,
                            currentVector,
                            easingType
                    );
                }
            }
            else {
                keyFrameToAdd = new KeyFrame(
                        animationTimeDifference * 20,
                        i == 0 ? currentVector : previousVector,
                        currentVector
                );
            }

            previousXValue = currentXValue;
            previousYValue = currentYValue;
            previousZValue = currentZValue;

            toReturn.addKeyFrame(keyFrameToAdd);
        }

        return toReturn;
    }

    private static KeyFrame.KeyFrameAxisValue parseExpression(RawAnimationChannel.RawVectorValue element) {
        //presumes that the vector value was a string
        if (element.stringValue != null) return new KeyFrame.KeyFrameAxisValue(element.stringValue);
            //presumes that the vector value was a double
        else return new KeyFrame.KeyFrameAxisValue(element.numericalValue);
    }

    private static List<Double> convertEasingArgsToList(double[] easingArgsArray) {
        List<Double> toReturn = new ArrayList<>();
        for (double v : easingArgsArray) toReturn.add(v);
        return toReturn;
    }
}
