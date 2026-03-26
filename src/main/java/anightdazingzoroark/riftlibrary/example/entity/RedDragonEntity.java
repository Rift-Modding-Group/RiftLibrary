package anightdazingzoroark.riftlibrary.example.entity;

import anightdazingzoroark.riftlibrary.main.animator.IAnimated;
import net.minecraft.entity.EntityCreature;
import net.minecraft.world.World;

public class RedDragonEntity extends EntityCreature implements IAnimated {
    public RedDragonEntity(World worldIn) {
        super(worldIn);
        this.setSize(1, 1);
    }
}
