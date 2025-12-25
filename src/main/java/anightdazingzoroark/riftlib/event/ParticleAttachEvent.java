package anightdazingzoroark.riftlib.event;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.animatedLocator.IAnimatedLocator;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ParticleAttachEvent extends Event {
    public final IAnimatable animatable;
    public final IAnimatedLocator animatedLocator;

    public ParticleAttachEvent(IAnimatable animatable, IAnimatedLocator animatedLocator) {
        this.animatable = animatable;
        this.animatedLocator = animatedLocator;
    }
}
