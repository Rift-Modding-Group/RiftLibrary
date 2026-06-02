package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jspecify.annotations.NonNull;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase, AnimationDataEntity> {
    public AnimationDataEntity(EntityLivingBase holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = super.asNBT();
        toReturn.setString("AnimationTargetType", "Entity");
        toReturn.setInteger("EntityID", this.getHolder().getEntityId());
        return toReturn;
    }

    @Override
    protected void createMolangQueries() {
        super.createMolangQueries();
        this.molangQueries.put("query.health", () -> {
            return (double) this.getHolder().getHealth();
        });
        this.molangQueries.put("query.max_health", () -> {
            return (double) this.getHolder().getMaxHealth();
        });
        this.molangQueries.put("query.is_riding", () -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
    }

    private static IAnimatable<AnimationDataEntity> getAnimatable(EntityLivingBase holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<AnimationDataEntity>) holder;
        throw new IllegalArgumentException("AnimationDataEntity holder must implement IAnimatable");
    }
}
