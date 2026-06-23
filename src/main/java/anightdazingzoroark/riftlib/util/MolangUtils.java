package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.internalMessage.RiftLibApplyMessageEffect;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
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
	public static void parseValue(AbstractAnimationData<?, ?> animationData, AnimatableValue animatableValue) {
		MolangParser parser = animationData.getParser();
		MolangScope dataScope = animationData.getDataScope();

		parser.withScope(dataScope, () -> {
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
				parser.setVariable(name, animatableValue.getConstantValue().right);
			}
		});
	}

	public static void parseValue(IAnimatable<?> animatable, String value) {
		parseValue(animatable, new AnimatableValue(value));
	}

	/**
	 * For use in animation controllers and custom animation instructions.
	 * */
	public static void parseValue(IAnimatable<?> animatable, AnimatableValue animatableValue) {
		AbstractAnimationData<?, ?> animationData = animatable.getAnimationData();
		parseValue(animationData, animatableValue);
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
