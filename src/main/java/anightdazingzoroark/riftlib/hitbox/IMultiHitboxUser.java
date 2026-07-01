package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import net.minecraft.entity.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface IMultiHitboxUser<T extends EntityLivingBase & IAnimatable<AnimationDataEntity> & IMultiHitboxUser<?>> extends IEntityMultiPart {
    /**
     * Get the parent. Must always return the entity its being implemented in.
     * */
    @NotNull
    T getMultiHitboxUser();

    /**
     * Get the multihitbox list, which is where hitboxes are stored and managed
     * */
    @NotNull
    MultiHitboxList<T> getMultiHitboxList();

    /**
     * Can be overriden to manage the damage multiplier of specific parts
     * */
    default float hitboxDamageMultiplier(RiftLibCollisionHitbox<T> collisionHitbox, DamageSource source) {
        return 1f;
    }

    /**
     * Get the model scale of the user.
     * */
    default float multiHitboxUserScale() {
        return 1f;
    }

    /**
     * This is for dealing with damage multipliers from attacking at different parts
     * */
    @SuppressWarnings({"unchecked"})
    @Override
    default boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) part;
        if (!hitbox.isDisabled()) {
            damage *= this.hitboxDamageMultiplier((RiftLibCollisionHitbox<T>) part, source);

            //as long as there is damage dealt, it will be applied
            if (damage > 0f) return this.getMultiHitboxUser().attackEntityFrom(source, damage);
        }
        return false;
    }

    /**
    * This makes it so that when HWYLA is installed, the info box directly shows info about the parent.
    * Set this to false if you already have a different factory in mind for hitboxes.
     */
    default boolean hitboxUseHWYLA() {
        return true;
    }

    /**
     * This makes it so that if true, this entity's hitboxes can collide with other entities
     * */
    default boolean hitboxCanCollideWithEntities() {
        return false;
    }

    /**
     * This is here so that there will be no more need to overwrite this
     * method that came from IEntityMultiPart on ur entities
     * */
    @Override
    default World getWorld() {
        return this.getMultiHitboxUser().world;
    }
}
