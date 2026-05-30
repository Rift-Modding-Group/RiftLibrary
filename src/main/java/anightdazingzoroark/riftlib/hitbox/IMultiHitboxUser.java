package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.model.ServerModelRegistry;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.DamageSource;

import java.util.ArrayList;
import java.util.List;

public interface IMultiHitboxUser<T extends EntityLivingBase & IAnimatable<T, AnimationDataEntity>> extends IEntityMultiPart {
    //get the parent
    //must always return the entity its being implemented in
    //so its return statement in the entity implementing this should be "return this;"
    T getMultiHitboxUser();

    default float multiHitboxUserScale() {
        return 1f;
    }

    default void setHitboxes() {
        T entity = this.getMultiHitboxUser();
        if (entity == null) return;

        //set linker
        EntityHitboxLinker hitboxLinker = RiftLibLinkerRegistry.INSTANCE.hitboxLinkerMap.get(entity.getClass());
        if (hitboxLinker == null) return;

        //search definition list
        HitboxDefinitionList definitionList = entity.world.isRemote ? hitboxLinker.getClientHitboxDefinitionList(entity) : hitboxLinker.getServerHitboxDefinitionList(entity);
        if (definitionList == null || definitionList == this.getHitboxDefinitionList()) return;
        if (!entity.world.isRemote) ServerModelRegistry.requireServerModel(entity, "server hitboxes");

        AnimationDataEntity animData = entity.getAnimationData();
        if (animData.getAnimatedLocators().isEmpty()) return;

        List<EntityHitbox<?>> hitboxesToAdd = new ArrayList<>();
        for (HitboxDefinitionList.HitboxDefinition hitboxDefinition : definitionList.list) {
            //find the locator to peg to first
            String locatorName = "hitbox_"+hitboxDefinition.locator();

            AnimatedLocator locatorToSet = animData.getAnimatedLocator(locatorName);
            if (locatorToSet == null || !HitboxUtils.locatorCanBeHitbox(locatorToSet)) return;

            //create the hitbox
            EntityHitbox<?> hitbox = new EntityHitbox<>(
                    this,
                    locatorToSet,
                    hitboxDefinition.damageMultiplier(),
                    hitboxDefinition.width(),
                    hitboxDefinition.height(),
                    hitboxDefinition.affectedByAnim()
            );

            //add the damage definitions
            for (HitboxDefinitionList.HitboxDamageDefinition damageDefinition : hitboxDefinition.damageDefinitionList()) {
                hitbox.damageDefinitions.add(new EntityHitbox.EntityHitboxDamageDefinition(
                        damageDefinition.damageSource(),
                        damageDefinition.damageType(),
                        damageDefinition.damageMultiplier()
                ));
            }

            hitboxesToAdd.add(hitbox);
        }

        for (EntityHitbox<?> hitbox : hitboxesToAdd) this.addPart(hitbox);
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
    default void addPart(EntityHitbox hitbox) {
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

    default EntityHitbox getHitboxByName(String name) {
        if (this.getMultiHitboxUser() == null) return null;
        if (this.getMultiHitboxUser().getParts() == null) return null;
        for (int x = 0; x < this.getMultiHitboxUser().getParts().length; x++) {
            EntityHitbox hitbox = (EntityHitbox) this.getMultiHitboxUser().getParts()[x];
            if (hitbox.partName.equals(name)) return hitbox;
        }
        return null;
    }

    //this is for dealing with damage multipliers from attacking at different parts
    default boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        EntityHitbox hitbox = (EntityHitbox) part;
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

    //this makes it so that when HWYLA is installed
    //the info box directly shows info about the parent
    //this can be left false if you want to replace it with something else you want
    default boolean hitboxUseHWYLA() {
        return true;
    }
}
