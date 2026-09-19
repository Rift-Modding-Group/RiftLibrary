package anightdazingzoroark.riftlib.util;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    R apply(T first, U second, V third);
}
