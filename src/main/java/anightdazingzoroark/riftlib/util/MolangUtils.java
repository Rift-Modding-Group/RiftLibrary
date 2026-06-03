package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.internalMessage.RiftLibApplyMessageEffect;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
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

	public static void parseValue(AbstractAnimationData<?, ?> animationData, String value) {
		parseValue(animationData, new AnimatableValue(value));
	}

	/**
	 * For use in animation controllers, its for either assigning variables from a string or
	 * sending messages to the server. Meant for use on client only.
	 * */
	public static void parseValue(AbstractAnimationData<?, ?> animationData, AnimatableValue animatableValue) {
		//as this is animatable value we're dealing with, first we check if its a message
		if (animatableValue.isExpression()
				&& animatableValue.getExpressionValue().startsWith("'")
				&& animatableValue.getExpressionValue().endsWith("'")
		) {
			String valueToSend = animatableValue.getExpressionValue().substring(1, animatableValue.getExpressionValue().length() - 1);
			for (Map.Entry<String, AnimatableRunValue> effectMapEntry : animationData.getAnimationMessageEffects().entrySet()) {
				if (!valueToSend.equals(effectMapEntry.getKey())) continue;

				//anim data world is nullable so if theres no world, skip
				if (animationData.getWorld() == null) continue;

				//if no side order, just assume its for server only then
                Side[] sideOrder = effectMapEntry.getValue().sideOrder();
				if (sideOrder == null || sideOrder.length == 0) sideOrder = new Side[]{Side.SERVER};

				RiftLibApplyMessageEffect applyMessageEffect = new RiftLibApplyMessageEffect(animationData, valueToSend);
				for (Side side : sideOrder) {
					if (side == Side.SERVER) ServerProxy.MESSAGE_WRAPPER.sendToServer(applyMessageEffect);
					if (side == Side.CLIENT) ServerProxy.MESSAGE_WRAPPER.sendToAll(applyMessageEffect);
				}

				boolean forServer = Arrays.stream(sideOrder).anyMatch(i -> i == Side.SERVER);
				boolean forClient = Arrays.stream(sideOrder).anyMatch(i -> i == Side.CLIENT);

				//test on client first
				if (forClient && animationData.getWorld().isRemote) {
					effectMapEntry.getValue().runValue().run();
				}
				//test on server next
				else if (forServer && !animationData.getWorld().isRemote) {
					effectMapEntry.getValue().runValue().run();
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
