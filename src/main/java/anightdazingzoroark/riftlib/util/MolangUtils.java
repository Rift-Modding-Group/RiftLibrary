package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.internalMessage.RiftLibRunAnimationMessageEffect;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraftforge.fml.relauncher.Side;

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

	public static void parseValue(MolangParser parser, AbstractAnimationData<?> animationData, String value) {
		parseValue(parser, animationData, new AnimatableValue(value));
	}

	/**
	 * For use in animation controllers, its for either assigning variables from a string or
	 * sending messages to the server. Meant for use on client only.
	 * */
	public static void parseValue(MolangParser parser, AbstractAnimationData<?> animationData, AnimatableValue animatableValue) {
		//as this is animatable value we're dealing with, first we check if its a message
		if (animatableValue.isExpression()
				&& animatableValue.getExpressionValue().startsWith("'")
				&& animatableValue.getExpressionValue().endsWith("'")
		) {
			String valueToSend = animatableValue.getExpressionValue().substring(1, animatableValue.getExpressionValue().length() - 1);
			for (AnimatableRunValue runValue : animationData.getAnimatable().animationMessageEffects().values()) {
                Side[] sideOrder = runValue.sideOrder();
				if (sideOrder == null || sideOrder.length == 0) sideOrder = new Side[]{Side.SERVER};

				for (Side side : sideOrder) {
					if (side == Side.SERVER) {
						ServerProxy.MESSAGE_WRAPPER.sendToServer(new RiftLibRunAnimationMessageEffect(
								valueToSend, animationData.asNBT()
						));
					}
					else if (side == Side.CLIENT) {
						ServerProxy.MESSAGE_WRAPPER.sendToAll(new RiftLibRunAnimationMessageEffect(
								valueToSend, animationData.asNBT()
						));
					}
				}
			}
			return;
		}

		parser.withScope(animationData.dataScope, () -> {
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
}
