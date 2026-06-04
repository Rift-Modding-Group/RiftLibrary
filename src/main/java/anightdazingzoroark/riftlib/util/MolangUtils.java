package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

	public static void parseValue(IAnimatable<?> animatable, String value) {
		parseValue(animatable, new AnimatableValue(value));
	}

	/**
	 * For use in animation controllers, its for either assigning variables from a string or
	 * sending messages to the server. Meant for use on client only.
	 * */
	public static void parseValue(IAnimatable<?> animatable, AnimatableValue animatableValue) {
		AbstractAnimationData<?, ?> animationData = animatable.getAnimationData();

		//as this is animatable value we're dealing with, first we check if its a message
		//note that these messages only work if on the server
		if (animatableValue.isExpression()
				&& animatableValue.getExpressionValue().startsWith("'")
				&& animatableValue.getExpressionValue().endsWith("'")
		) {
			//if world doesnt exist, skip. this is mostly here cos
			//AbstractAnimationData.getWorld() is nullable and i need
			//a way for intellij to stfu about it
			if (animationData.getWorld() == null) return;

			String valueToSend = animatableValue.getExpressionValue().substring(1, animatableValue.getExpressionValue().length() - 1);
			for (Map.Entry<String, AnimatableRunValue> effectMapEntry : animationData.getAnimationMessageEffects().entrySet()) {
				if (!valueToSend.equals(effectMapEntry.getKey())) continue;

				//if no side order, just assume its for client only then
                Side[] sideOrder = effectMapEntry.getValue().sideOrder();
				if (sideOrder == null || sideOrder.length == 0) sideOrder = new Side[]{Side.CLIENT};

				//require the presence of a server model if it specifies server side
				if (Arrays.asList(sideOrder).contains(Side.SERVER)) {
					ServerModelRegistry.requireServerModel(animatable, "server animation message effects");
				}

				//now apply message effect based on side order
				for (Side side : sideOrder) {
					if (side == Side.SERVER && !animationData.getWorld().isRemote) effectMapEntry.getValue().runValue().run();
					if (side == Side.CLIENT && animationData.getWorld().isRemote) effectMapEntry.getValue().runValue().run();
				}
			}
		}
		else {
			MolangParser parser = animationData.getParser();
			parser.withScope(animationData.getDataScope(), () -> {
				if (animatableValue.isExpression()) {
					try {
						parser.parseExpression(animatableValue.getExpressionValue(), animationData).get();
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

	/**
	 * Parse a molang expression and get its return value
	 * */
	public static double parseValueAndGet(AbstractAnimationData<?, ?> animationData, String expression) {
		AtomicReference<Double> toReturn = new AtomicReference<>(0D);
		MolangParser parser = animationData.getParser();

		parser.withScope(animationData.getDataScope(), () -> {
			try {
				double parsedValue = parser.parseExpression(expression, animationData).get();
				toReturn.set(parsedValue);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		return toReturn.get();
	}

	public static double getVariable(MolangParser parser, MolangScope dataScope, String name) {
		AtomicReference<Double> toReturn = new AtomicReference<>(0D);
		parser.withScope(dataScope, () -> {
			toReturn.set(parser.getVariable(name).get());
		});
		return toReturn.get();
	}
}
