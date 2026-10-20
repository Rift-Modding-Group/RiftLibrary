package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Animation data for non-living entities, using the entity's own yaw.
 * */
public class AnimationDataEntityNonLiving extends AbstractAnimationDataAdvancedEntity<Entity, AnimationDataEntityNonLiving> {
    public AnimationDataEntityNonLiving(@NotNull Entity holder) {
        super(holder, getAnimatable(holder));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static IAnimatable<AnimationDataEntityNonLiving> getAnimatable(@NotNull Entity holder) {
        if (holder instanceof IAnimatable<?> animatable) return (IAnimatable<AnimationDataEntityNonLiving>) animatable;
        throw new IllegalArgumentException("AnimationDataEntityNonLiving holder must implement IAnimatable");
    }
}
