package anightdazingzoroark.riftlib.core;

import anightdazingzoroark.riftlib.core.manager.AbstractAnimationData;
import anightdazingzoroark.riftlib.molang.MolangObject;
import anightdazingzoroark.riftlib.molang.MolangScope;
import org.jetbrains.annotations.NotNull;

/**
 * This interface must be applied to any object that wants to be animated
 */
public interface IAnimatable<D extends AbstractAnimationData<?, D>> extends MolangObject {
    /**
     * The animation data for the object that will be animated.
     * */
    @NotNull
    D getAnimationData();

    /**
     * Operations relevant to the initialization of animation data
     * are to be run here, such as animation controllers, molang
     * variable initialization, etc.
     * */
    void initializeAnimationData(@NotNull D animationData);

    @Override
    @NotNull
    default MolangScope getMolangScope() {
        return this.getAnimationData().getDataScope();
    }

    @Override
    @NotNull
    default AbstractAnimationData<?, ?> getMolangAnimationData() {
        return this.getAnimationData();
    }

}
