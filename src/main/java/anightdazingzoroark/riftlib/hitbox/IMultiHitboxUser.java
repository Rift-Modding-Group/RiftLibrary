package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import net.minecraft.entity.*;
import net.minecraft.util.DamageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IMultiHitboxUser<T extends EntityLivingBase & IAnimatable<AnimationDataEntity>> extends IEntityMultiPart {
    /**
     * Get the parent. Must always return the entity its being implemented in.
     * */
    T getMultiHitboxUser();

    /**
     * Get the model scale of the user.
     * */
    default float multiHitboxUserScale() {
        return 1f;
    }

    default void setHitboxes() {
        T entity = this.getMultiHitboxUser();
        if (entity == null) return;

        //find linker class
        Class<?> entityClass = entity.getClass();
        EntityHitboxLinker hitboxLinker = null;
        for (Map.Entry<Class<? extends EntityLiving>, EntityHitboxLinker<?>> hitboxLinkEntry : RiftLibLinkerRegistry.INSTANCE.hitboxLinkerMap.entrySet()) {
            if (!hitboxLinkEntry.getKey().isAssignableFrom(entityClass)) continue;
            hitboxLinker = hitboxLinkEntry.getValue();
        }
        if (hitboxLinker == null) return;

        //search definition list
        HitboxDefinitionList definitionList = entity.world.isRemote ? hitboxLinker.getClientHitboxDefinitionList(entity) : hitboxLinker.getServerHitboxDefinitionList(entity);
        if (definitionList == null || definitionList == this.getHitboxDefinitionList()) return;
        if (!entity.world.isRemote) ServerModelRegistry.requireServerModel(entity, "server hitboxes");

        AnimationDataEntity animData = entity.getAnimationData();
        if (animData.getAnimatedLocators().isEmpty()) return;

        List<RiftLibCollisionHitbox<?>> hitboxesToAdd = new ArrayList<>();
        for (HitboxDefinitionList.HitboxDefinition hitboxDefinition : definitionList.list) {
            //find the locator to peg to first
            String locatorName = "hitbox_"+hitboxDefinition.locator();

            AnimatedLocator locatorToSet = animData.getAnimatedLocator(locatorName);
            if (locatorToSet == null || !HitboxUtils.locatorCanBeHitbox(locatorToSet)) return;

            //create the hitbox
            RiftLibCollisionHitbox<?> hitbox = new RiftLibCollisionHitbox<>(
                    this,
                    locatorToSet,
                    hitboxDefinition.damageMultiplier(),
                    hitboxDefinition.width(),
                    hitboxDefinition.height(),
                    hitboxDefinition.affectedByAnim()
            );

            //add the damage definitions
            for (HitboxDefinitionList.HitboxDamageDefinition damageDefinition : hitboxDefinition.damageDefinitionList()) {
                hitbox.damageDefinitions.add(new RiftLibCollisionHitbox.EntityHitboxDamageDefinition(
                        damageDefinition.damageSource(),
                        damageDefinition.damageType(),
                        damageDefinition.damageMultiplier()
                ));
            }

            hitboxesToAdd.add(hitbox);
        }

        for (RiftLibCollisionHitbox<?> hitbox : hitboxesToAdd) this.addPart(hitbox);
        this.setHitboxDefinitionList(definitionList);
    }

    /**
     * This is only here because while entities do have a getter for hitboxes, they dont have
     * a setter for them too. Strangest part of all is how for some reason is that it isnt
     * an array for hitboxes strictly its just an array for any entity lol.
     * */
    void setParts(Entity[] hitboxes);

    //-----required to ensure no duplicate definition lists are given when settig hitboxes-----
    HitboxDefinitionList getHitboxDefinitionList();

    void setHitboxDefinitionList(HitboxDefinitionList hitboxDefinitionList);

    //-----helper functions for hitboxes from here on out-----
    default void addPart(RiftLibCollisionHitbox<?> hitbox) {
        if (this.getMultiHitboxUser() == null) return;
        if (this.getMultiHitboxUser().getParts() == null) return;
        Entity[] newHitboxArray = new Entity[this.getMultiHitboxUser().getParts().length + 1];
        for (int x = 0; x < newHitboxArray.length; x++) {
            if (x < newHitboxArray.length - 1) newHitboxArray[x] = this.getMultiHitboxUser().getParts()[x];
            else newHitboxArray[x] = hitbox;
        }
        this.setParts(newHitboxArray);
        this.getMultiHitboxUser().world.entitiesById.addKey(hitbox.getEntityId(), hitbox);
    }

    default RiftLibCollisionHitbox<?> getHitboxByName(String name) {
        if (this.getMultiHitboxUser() == null) return null;
        if (this.getMultiHitboxUser().getParts() == null) return null;
        for (int x = 0; x < this.getMultiHitboxUser().getParts().length; x++) {
            RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) this.getMultiHitboxUser().getParts()[x];
            if (hitbox.partName.equals(name)) return hitbox;
        }
        return null;
    }

    //this is for dealing with damage multipliers from attacking at different parts
    default boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        RiftLibCollisionHitbox<?> hitbox = (RiftLibCollisionHitbox<?>) part;
        if (!hitbox.isDisabled()) {
            //get the individual damage definitions of the hitbox and multiply em with newDamage
            //if those dont exist, use the default damageMultiplier
            if (hitbox.damageSourceWithinDamageDefinitions(source)) damage *= hitbox.getDamageMultiplierForSource(source);
            else damage *= hitbox.getDamageMultiplier();

            //as long as there is damage dealt, it will be applied
            if (damage > 0f) return this.getMultiHitboxUser().attackEntityFrom(source, damage);
        }
        return false;
    }

    /**
    * This makes it so that when HWYLA is installed, the info box directly shows info about the parent.
    * Set this to false if you already have a different factory for hitboxes.
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
}
