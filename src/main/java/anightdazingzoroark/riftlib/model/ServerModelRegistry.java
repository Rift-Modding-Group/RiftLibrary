package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class ServerModelRegistry {
    private static final Map<Class<?>, Supplier<? extends AnimatedGeoModel<?>>> SERVER_MODEL_FACTORIES = new HashMap<>();
    private static final Map<IAnimatable<?, ?>, AnimatedGeoModel<?>> SERVER_MODELS = new WeakHashMap<>();
    private static boolean modelFetcherRegistered;

    public static <T extends IAnimatable<?, ?>> void registerServerModel(Class<T> animatableClass, Supplier<? extends AnimatedGeoModel<T>> modelFactory) {
        ensureModelFetcherRegistered();
        SERVER_MODEL_FACTORIES.put(animatableClass, modelFactory);
    }

    public static void clearServerModels() {
        SERVER_MODELS.clear();
    }

    private static boolean hasServerModel(IAnimatable<?, ?> animatable) {
        return findFactory(animatable.getClass()) != null;
    }

    public static void requireServerModel(IAnimatable<?, ?> animatable, String featureName) {
        if (hasServerModel(animatable)) return;

        Class<?> animatableClass = animatable.getClass();
        throw new IllegalStateException(
                "Cannot create " + featureName + " for " + animatableClass.getName() +
                        " because no AnimatedGeoModel has been registered on the server!"
        );
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAnimatable<?, ?>> AnimatedGeoModel<T> getServerModel(T animatable) {
        AnimatedGeoModel<?> cachedModel = SERVER_MODELS.get(animatable);
        if (cachedModel != null) return (AnimatedGeoModel<T>) cachedModel;

        Supplier<? extends AnimatedGeoModel<?>> factory = findFactory(animatable.getClass());
        if (factory == null) return null;

        AnimatedGeoModel<?> model = factory.get();
        SERVER_MODELS.put(animatable, model);

        return (AnimatedGeoModel<T>) model;
    }

    private static Supplier<? extends AnimatedGeoModel<?>> findFactory(Class<?> animatableClass) {
        Supplier<? extends AnimatedGeoModel<?>> exactFactory = SERVER_MODEL_FACTORIES.get(animatableClass);

        if (exactFactory != null) return exactFactory;

        for (Map.Entry<Class<?>, Supplier<? extends AnimatedGeoModel<?>>> entry : SERVER_MODEL_FACTORIES.entrySet()) {
            if (entry.getKey().isAssignableFrom(animatableClass)) return entry.getValue();
        }

        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void ensureModelFetcherRegistered() {
        if (modelFetcherRegistered) return;

        AnimationController.addModelFetcher(animatable -> (IAnimatableModel) getServerModel(animatable));
        modelFetcherRegistered = true;
    }
}
