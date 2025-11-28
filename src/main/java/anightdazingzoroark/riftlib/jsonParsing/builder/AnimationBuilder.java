package anightdazingzoroark.riftlib.jsonParsing.builder;

import anightdazingzoroark.riftlib.core.ConstantValue;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.easing.EasingType;
import anightdazingzoroark.riftlib.core.keyframe.BoneAnimation;
import anightdazingzoroark.riftlib.core.keyframe.KeyFrame;
import anightdazingzoroark.riftlib.core.keyframe.VectorKeyFrameList;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationChannel;
import anightdazingzoroark.riftlib.jsonParsing.raw.animation.RawAnimationFile;
import anightdazingzoroark.riftlib.molang.MolangException;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.math.IValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnimationBuilder {
    public static Animation getAnimationFromRawAnimationEntry(Map.Entry<String, RawAnimationFile.RawAnimation> rawAnimation, MolangParser parser) {
        Animation toReturn = new Animation();

        //set anim info
        toReturn.animationName = rawAnimation.getKey();
        toReturn.loop = rawAnimation.getValue().loop == Boolean.TRUE ? LoopType.LOOP : LoopType.PLAY_ONCE;
        toReturn.animationLength = rawAnimation.getValue().animationLength != null ? rawAnimation.getValue().animationLength * 20 : null; //if length is null, it will be calculated later based on the provided info

        //until i figure out how to implement this later on, this will be left as a comment
        //for reference when i add this back in somehow
        /*
        // Handle parsing sound effect keyframes
		ArrayList<Map.Entry<String, JsonElement>> soundEffectFrames = getSoundEffectFrames(animationJsonObject);
		if (soundEffectFrames != null) {
			for (Map.Entry<String, JsonElement> keyFrame : soundEffectFrames) {
				animation.soundKeyFrames.add(new EventKeyFrame(Double.parseDouble(keyFrame.getKey()) * 20,
						keyFrame.getValue().getAsJsonObject().get("effect").getAsString()));
			}
		}

		// Handle parsing particle effect keyframes
		ArrayList<Map.Entry<String, JsonElement>> particleKeyFrames = getParticleEffectFrames(animationJsonObject);
		if (particleKeyFrames != null) {
			for (Map.Entry<String, JsonElement> keyFrame : particleKeyFrames) {
				JsonObject object = keyFrame.getValue().getAsJsonObject();
				JsonElement effect = object.get("effect");
				JsonElement locator = object.get("locator");
				JsonElement pre_effect_script = object.get("pre_effect_script");
				animation.particleKeyFrames.add(new ParticleEventKeyFrame(Double.parseDouble(keyFrame.getKey()) * 20,
						effect == null ? "" : effect.getAsString(), locator == null ? "" : locator.getAsString(),
						pre_effect_script == null ? "" : pre_effect_script.getAsString()));
			}
		}

		// Handle parsing custom instruction keyframes
		ArrayList<Map.Entry<String, JsonElement>> customInstructionKeyFrames = getCustomInstructionKeyFrames(
				animationJsonObject);
		if (customInstructionKeyFrames != null) {
			for (Map.Entry<String, JsonElement> keyFrame : customInstructionKeyFrames) {
				animation.customInstructionKeyframes.add(new EventKeyFrame(Double.parseDouble(keyFrame.getKey()) * 20,
						keyFrame.getValue() instanceof JsonArray
								? convertJsonArrayToList(keyFrame.getValue().getAsJsonArray()).toString()
								: keyFrame.getValue().getAsString()));
			}
		}
         */

        //create bone animations
        Map<String, RawAnimationFile.RawBoneAnimations> rawBoneAnimations = rawAnimation.getValue().bones;
        List<BoneAnimation> boneAnimationList = new ArrayList<>();
        for (Map.Entry<String, RawAnimationFile.RawBoneAnimations> rawBoneAnimation : rawBoneAnimations.entrySet()) {
            BoneAnimation boneAnimation = new BoneAnimation();
            boneAnimation.boneName = rawBoneAnimation.getKey();

            //anti NPE protection
            boneAnimation.positionKeyFrames = new VectorKeyFrameList<>();
            boneAnimation.rotationKeyFrames = new VectorKeyFrameList<>();
            boneAnimation.scaleKeyFrames = new VectorKeyFrameList<>();

            //positions
            if (rawBoneAnimation.getValue().position != null) {
                try {
                    RawAnimationChannel rawPositionChannel = rawBoneAnimation.getValue().position;
                    boneAnimation.positionKeyFrames = convertRawChannelToFrameList(rawPositionChannel, false, parser);
                }
                catch (Exception e) {}
            }

            //rotations
            if (rawBoneAnimation.getValue().rotation != null) {
                try {
                    RawAnimationChannel rawRotationChannel = rawBoneAnimation.getValue().rotation;
                    boneAnimation.rotationKeyFrames = convertRawChannelToFrameList(rawRotationChannel, true, parser);
                }
                catch (Exception e) {}
            }

            //scaling
            if (rawBoneAnimation.getValue().scale != null) {
                try {
                    RawAnimationChannel rawPositionChannel = rawBoneAnimation.getValue().scale;
                    boneAnimation.scaleKeyFrames = convertRawChannelToFrameList(rawPositionChannel, false, parser);
                }
                catch (Exception e) {}
            }

            boneAnimationList.add(boneAnimation);
        }
        toReturn.boneAnimations = boneAnimationList;

        //manually compute anim length based on above info
        if (toReturn.animationLength == null) toReturn.animationLength = calculateLength(toReturn.boneAnimations);

        return toReturn;
    }

    private static double calculateLength(List<BoneAnimation> boneAnimations) {
        double longestLength = 0;
        for (BoneAnimation animation : boneAnimations) {
            double xKeyframeTime = animation.rotationKeyFrames.getLastKeyframeTime();
            double yKeyframeTime = animation.positionKeyFrames.getLastKeyframeTime();
            double zKeyframeTime = animation.scaleKeyFrames.getLastKeyframeTime();
            longestLength = maxAll(longestLength, xKeyframeTime, yKeyframeTime, zKeyframeTime);
        }
        return longestLength == 0 ? Double.MAX_VALUE : longestLength;
    }

    private static double maxAll(double... values) {
        double max = 0;
        for (double value : values) max = Math.max(value, max);
        return max;
    }

    private static VectorKeyFrameList<KeyFrame<IValue>> convertRawChannelToFrameList(RawAnimationChannel rawAnimationChannel, boolean isRotation, MolangParser parser) throws NumberFormatException, MolangException {
        IValue previousXValue = null;
        IValue previousYValue = null;
        IValue previousZValue = null;

        List<KeyFrame<IValue>> xKeyFrames = new ArrayList<>();
        List<KeyFrame<IValue>> yKeyFrames = new ArrayList<>();
        List<KeyFrame<IValue>> zKeyFrames = new ArrayList<>();

        //vector mode for raw anim channels only has 1 keyframe so there's that
        int channelSize = rawAnimationChannel.isKeyframed() ? rawAnimationChannel.keyframes.size() : 1;
        for (int i = 0; i < channelSize; i++) {
            RawAnimationChannel.RawKeyframe rawKeyframe = rawAnimationChannel.keyframes.get(i);
            RawAnimationChannel.RawKeyframe previousRawKeyframe = i == 0 ? null : rawAnimationChannel.keyframes.get(i - 1);

            Double previousKeyFrameLocation = previousRawKeyframe == null ? 0 : previousRawKeyframe.time;
            Double currentKeyFrameLocation = rawKeyframe.time;
            double animationTimeDifference = currentKeyFrameLocation - previousKeyFrameLocation;

            IValue xValue = parseExpression(parser, rawKeyframe.vector[0]);
            IValue yValue = parseExpression(parser, rawKeyframe.vector[1]);
            IValue zValue = parseExpression(parser, rawKeyframe.vector[2]);

            IValue currentXValue = isRotation && xValue instanceof ConstantValue
                    ? ConstantValue.fromDouble(Math.toRadians(-xValue.get()))
                    : xValue;
            IValue currentYValue = isRotation && yValue instanceof ConstantValue
                    ? ConstantValue.fromDouble(Math.toRadians(-yValue.get()))
                    : yValue;
            IValue currentZValue = isRotation && zValue instanceof ConstantValue
                    ? ConstantValue.fromDouble(Math.toRadians(zValue.get()))
                    : zValue;
            KeyFrame<IValue> xKeyFrame;
            KeyFrame<IValue> yKeyFrame;
            KeyFrame<IValue> zKeyFrame;

            if (rawKeyframe.easingType != null) {
                EasingType easingType = EasingType.getEasingTypeFromString(rawKeyframe.easingType);
                if (rawKeyframe.easingArgs != null) {
                    List<Double> easingArgs = convertEasingArgsToList(rawKeyframe.easingArgs);
                    xKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentXValue : previousXValue, currentXValue, easingType, easingArgs);
                    yKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentYValue : previousYValue, currentYValue, easingType, easingArgs);
                    zKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentZValue : previousZValue, currentZValue, easingType, easingArgs);
                }
                else {
                    xKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentXValue : previousXValue, currentXValue, easingType);
                    yKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentYValue : previousYValue, currentYValue, easingType);
                    zKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                            i == 0 ? currentZValue : previousZValue, currentZValue, easingType);

                }
            }
            else {
                xKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                        i == 0 ? currentXValue : previousXValue, currentXValue);
                yKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                        i == 0 ? currentYValue : previousYValue, currentYValue);
                zKeyFrame = new KeyFrame<>(animationTimeDifference * 20,
                        i == 0 ? currentZValue : previousZValue, currentZValue);
            }

            previousXValue = currentXValue;
            previousYValue = currentYValue;
            previousZValue = currentZValue;

            xKeyFrames.add(xKeyFrame);
            yKeyFrames.add(yKeyFrame);
            zKeyFrames.add(zKeyFrame);
        }

        return new VectorKeyFrameList<>(xKeyFrames, yKeyFrames, zKeyFrames);
    }

    private static IValue parseExpression(MolangParser parser, RawAnimationChannel.RawVectorValue element) throws MolangException {
        //presumes that the vector value was a string
        if (element.stringValue != null) return parser.parseExpression(element.stringValue);
        //presumes that the vector value was a double
        else return ConstantValue.fromDouble(element.numericalValue);
    }

    private static List<Double> convertEasingArgsToList(double[] easingArgsArray) {
        List<Double> toReturn = new ArrayList<>();
        for (double v : easingArgsArray) toReturn.add(v);
        return toReturn;
    }
}
