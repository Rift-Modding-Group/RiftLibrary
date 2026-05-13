package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.MiscUtils;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.function.BiFunction;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase> {
    public AnimationDataEntity(EntityLivingBase holder) {
        super(holder, getAnimatable(holder));
    }

    @Override
    @NonNull
    public NBTTagCompound asNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();
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

    private static IAnimatable<?> getAnimatable(EntityLivingBase holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<?>) holder;
        throw new IllegalArgumentException("AnimationDataEntity holder must implement IAnimatableNew");
    }
}
