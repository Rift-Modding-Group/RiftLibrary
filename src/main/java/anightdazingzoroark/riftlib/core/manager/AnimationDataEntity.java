package anightdazingzoroark.riftlib.core.manager;

import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link AnimationDataEntityLiving} for living entities.
 */
@Deprecated(since = "2.1.0")
public class AnimationDataEntity extends AnimationDataEntityLiving {
    public AnimationDataEntity(@NotNull EntityLivingBase holder) {
        super(holder);
    }
}
