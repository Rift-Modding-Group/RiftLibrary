package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;

public class GoKart extends EntityCreature implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public GoKart(World worldIn) {
        super(worldIn);
    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
