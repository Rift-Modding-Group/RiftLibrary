package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.internalMessage.RiftLibRunAnimationMessageEffect;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.proxy.ServerProxy;

public class MolangUtils {
	public static float normalizeTime(long timestamp) {
		return ((float) timestamp / 24000);
	}

	public static float booleanToFloat(boolean input) {
		return input ? 1.0F : 0.0F;
	}

	public static double booleanToDouble(boolean input) {
		return input ? 1D : 0D;
	}

	/**
	 * This is for use in AnimationData related stuff, its for
	 * assigning variables from AnimatableValues to a Molang Scope.
	 * */
	public static void parseValue(MolangParser parser, MolangScope dataScope, AnimatableValue animatableValue) {
		parser.withScope(dataScope, () -> {
			if (animatableValue.isExpression()) {
				try {
					parser.parseExpression(animatableValue.getExpressionValue()).get();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			else {
				String name = animatableValue.getConstantValue().left;
				if (parser.isFunction(name)) {
					throw new RuntimeException(new MolangException("Cannot assign value to function '"+name+"'!"));
				}
				parser.setValue(name, animatableValue.getConstantValue().right);
			}
		});
	}

	/**
	 * For use in animation controllers, its for either assigning variables from a string or
	 * sending messages to the server. Meant for use on client only.
	 * */
	public static void parseValue(MolangParser parser, AbstractAnimationData<?> animationData, String value) {
		if (value.startsWith("'") && value.endsWith("'")) {
			String valueToSend = value.substring(1, value.length() - 1);
			ServerProxy.MESSAGE_WRAPPER.sendToServer(new RiftLibRunAnimationMessageEffect(
					valueToSend, animationData.asNBT()
			));
		}
		else {
			parser.withScope(animationData.dataScope, () -> {
				try {
					parser.parseExpression(value).get();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}
}
