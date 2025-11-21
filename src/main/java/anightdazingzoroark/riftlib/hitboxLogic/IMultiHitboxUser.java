package anightdazingzoroark.riftlib.hitboxLogic;

import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.file.HitboxDefinitionList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.util.DamageSource;

public interface IMultiHitboxUser extends IEntityMultiPart {
    //get the parent
    //must always return the entity its being implemented in
    //so its return statement in the entity implementing this should be "return this;"
    Entity getMultiHitboxUser();

    //this must be placed in the constructor of the entity
    //and must be the entity itself being entered
    default <T extends Entity & IAnimatable & IMultiHitboxUser> void initializeHitboxes(T entity) {
        EntityHitboxLinker hitboxLinker = RiftLibLinkerRegistry.INSTANCE.hitboxLinkerMap.get(entity.getClass());
        if (hitboxLinker == null) return;

        for (HitboxDefinitionList.HitboxDefinition hitboxDefinition : hitboxLinker.getHitboxDefinitionList(entity).list) {
            //create the hitbox
            EntityHitbox hitbox = new EntityHitbox(
                    entity,
                    hitboxDefinition.locator,
                    hitboxDefinition.damageMultiplier,
                    hitboxDefinition.width,
                    hitboxDefinition.height,
                    (float) hitboxDefinition.position.x,
                    (float) hitboxDefinition.position.y,
                    (float) hitboxDefinition.position.z,
                    hitboxDefinition.affectedByAnim
            );
            //add the damage definitions
            for (HitboxDefinitionList.HitboxDamageDefinition damageDefinition : hitboxDefinition.damageDefinitionList) {
                hitbox.damageDefinitions.add(new EntityHitbox.EntityHitboxDamageDefinition(
                        damageDefinition.damageSource,
                        damageDefinition.damageType,
                        damageDefinition.damageMultiplier
                ));
            }

            this.addPart(hitbox);
        }
    }

    void setParts(Entity[] hitboxes);

    default void addPart(EntityHitbox hitbox) {
        if (this.getMultiHitboxUser() == null) return;
        if (this.getMultiHitboxUser().getParts() == null) return;
        Entity[] newHitboxArray = new Entity[this.getMultiHitboxUser().getParts().length + 1];
        for (int x = 0; x < newHitboxArray.length; x++) {
            if (x < newHitboxArray.length - 1) newHitboxArray[x] = this.getMultiHitboxUser().getParts()[x];
            else newHitboxArray[x] = hitbox;
        }
        this.setParts(newHitboxArray);
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

    //this is to be placed in a method like onUpdate() or onLivingUpdate()
    //to update all hitboxes every tick
    default void updateParts() {
        if (this.getMultiHitboxUser() == null) return;
        if (this.getMultiHitboxUser().getParts() == null) return;
        for (Entity entity : this.getMultiHitboxUser().getParts()) {
            if (entity instanceof EntityHitbox) {
                entity.onUpdate();
                ((EntityHitbox) entity).resize(((IAnimatable) this.getMultiHitboxUser()).scale());
            }
        }
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

    default void updateHitboxPos(String hitboxName, float x, float y, float z) {
        for (int i = 0; i < this.getMultiHitboxUser().getParts().length; i++) {
            if (((EntityHitbox) this.getMultiHitboxUser().getParts()[i]).partName.equals(hitboxName)) {
                EntityHitbox hitbox = (EntityHitbox) this.getMultiHitboxUser().getParts()[i];
                hitbox.changeOffset(x, y, z);
            }
        }
    }

    default void updateHitboxScaleFromAnim(String hitboxName, float width, float height) {
        for (int i = 0; i < this.getMultiHitboxUser().getParts().length; i++) {
            if (((EntityHitbox) this.getMultiHitboxUser().getParts()[i]).partName.equals(hitboxName)) {
                EntityHitbox hitbox = (EntityHitbox) this.getMultiHitboxUser().getParts()[i];
                hitbox.resizeByAnim(width, height);
            }
        }
    }

    //this makes it so that when HWYLA is installed
    //the info box directly shows info about the parent
    //this can be left false if you want to replace it with something else you want
    default boolean hitboxUseHWYLA() {
        return true;
    }
}
