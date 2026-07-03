package anightdazingzoroark.riftlib.util;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.molang.MolangParser;

import java.util.concurrent.atomic.AtomicReference;

public class MolangUtils {
	public static double booleanToDouble(boolean input) {
		return input ? 1D : 0D;
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
}
