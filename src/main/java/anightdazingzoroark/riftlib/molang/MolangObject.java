package anightdazingzoroark.riftlib.molang;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Molang Objects are game objects representable by query.actor_owner
 * */
public interface MolangObject {
    @NotNull
    MolangScope getMolangScope();

    @Nullable
    default Object getMolangActorOwner() {
        return null;
    }

    @Nullable
    default AbstractAnimationData<?, ?> getMolangAnimationData() {
        return null;
    }
}
