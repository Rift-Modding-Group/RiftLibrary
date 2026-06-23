package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This hitbox deals with entity collisions and where players can attack on
 * IMultiHitboxUser instances
 * */
public class RiftLibCollisionHitbox<T extends IMultiHitboxUser<?>> extends MultiPartEntityPart implements IHitbox<T> {
    private final float damageMultiplier;
    //these are the final definitive scales of the hitbox and will be used for such
    public final float fixedWidth;
    public final float fixedHeight;
    //the animated locator this hitbox is to be pegged to
    private final AnimatedLocator hitboxLocator;
    //others
    public final boolean affectedByAnim;
    private boolean isDisabled;
    public final List<EntityHitboxDamageDefinition> damageDefinitions = new ArrayList<>();

    public RiftLibCollisionHitbox(T parent, AnimatedLocator hitboxLocator, float damageMultiplier, float width, float height, boolean affectedByAnim) {
        super(parent, HitboxUtils.locatorHitboxToHitbox(hitboxLocator.getName()), width, height);
        this.hitboxLocator = hitboxLocator;
        this.damageMultiplier = damageMultiplier;
        this.fixedWidth = width;
        this.fixedHeight = height;
        this.affectedByAnim = affectedByAnim;
        this.onAddedToWorld();
    }

    /**
     * In order to forcibly sync the entityIds from server to client, this has to be done.
     * I fucking hate how hitboxes are dealt with in this version anyway.
     * */
    public void syncEntityIdFromServer(int entityId) {
        if (this.getEntityId() == entityId) return;

        if (this.world.getEntityByID(this.getEntityId()) == this) {
            this.world.entitiesById.removeObject(this.getEntityId());
        }

        RiftLibCollisionHitbox<?> entityAlreadyUsingID = this.world.getEntityByID(entityId) instanceof RiftLibCollisionHitbox<?> entityHitbox ? entityHitbox : null;
        if (entityAlreadyUsingID != null && entityAlreadyUsingID.getParent() == this.getParent()) {
            this.world.entitiesById.removeObject(entityId);
        }

        this.setEntityId(entityId);
        this.world.entitiesById.addKey(entityId, this);
    }

    @Override
    public void onUpdate() {
        EntityLivingBase parentEntityLiving = this.getParent().getMultiHitboxUser();

        //-----set scale-----
        float[] finalSize = this.getHitboxSize();
        this.setSize(finalSize[0], finalSize[1]);

        //-----set position-----
        Vec3d finalPosition = this.getHitboxPosition();
        this.setPositionAndUpdate(finalPosition.x, finalPosition.y, finalPosition.z);

        //-----handle collisions-----
        if (this.getParent().hitboxCanCollideWithEntities() && !this.world.isRemote) {
            List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.2D, 0D, 0.2D));
            entities.stream().filter(entity -> entity != this.parent && !(entity instanceof MultiPartEntityPart) && entity.canBePushed())
                    .forEach(this::collideEntityWithHitbox);
        }

        //-----remove if entity is dead or no longer exists-----
        if (!parentEntityLiving.isEntityAlive()) {
            this.world.removeEntityDangerously(this);
        }
        super.onUpdate();
    }

    /**
     * This special collision method is to make it so hitboxes perform entity collision
     * without having to override Entity.applyEntityCollision
     * (note: is really bad xd)
     * */
    private void collideEntityWithHitbox(Entity entityIn) {
        EntityLivingBase parent = this.getParent().getMultiHitboxUser();
        if (entityIn == null || entityIn.equals(parent) || parent.isRidingSameEntity(entityIn) || entityIn.noClip) return;

        double dispX = entityIn.posX - parent.posX;
        double dispZ = entityIn.posZ - parent.posZ;
        double maxDisp = MathHelper.absMax(dispX, dispZ);

        maxDisp = MathHelper.sqrt(maxDisp);
        dispX /= maxDisp;
        dispZ /= maxDisp;
        double d3 = Math.min(1D / maxDisp, 1D);

        dispX *= d3;
        dispZ *= d3;
        dispX *= 0.05f;
        dispZ *= 0.05f;
        dispX *= 1f - parent.entityCollisionReduction;
        dispZ *= 1f - parent.entityCollisionReduction;

        entityIn.addVelocity(dispX, 0D, dispZ);

        //mark dirty to force push on players
        if (entityIn instanceof EntityPlayer) entityIn.velocityChanged = true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return this.getParent().getMultiHitboxUser().processInitialInteract(player, hand);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.damageSourceIsRider(source)) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    private boolean damageSourceIsRider(DamageSource source) {
        if (source == null) return false;
        EntityLivingBase parentEntityLiving = this.getParent().getMultiHitboxUser();

        if (parentEntityLiving.isBeingRidden()) {
            if (source.getTrueSource() != null && parentEntityLiving.isPassenger(source.getTrueSource())) {
                return true;
            }
            if (source.getImmediateSource() != null && parentEntityLiving.isPassenger(source.getImmediateSource())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recommended instead of using the parent variable
     * */
    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public T getParent() {
        return (T) this.parent;
    }

    @Override
    @NotNull
    public AnimatedLocator getHitboxLocator() {
        return this.hitboxLocator;
    }

    @Override
    public float[] getFixedSize() {
        return new float[]{this.fixedWidth, this.fixedHeight};
    }

    public void setDisabled(boolean value) {
        this.isDisabled = value;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public boolean damageSourceWithinDamageDefinitions(DamageSource damageSource) {
        for (EntityHitboxDamageDefinition damageDefinition : this.damageDefinitions) {
            if (damageDefinition.damageSource != null) {
                if (damageDefinition.damageSource.equals(damageSource.damageType)) return true;
            }
            else if (damageDefinition.damageType != null) {
                switch (damageDefinition.damageType) {
                    case "projectile":
                        if (damageSource.isProjectile()) return true;
                    case "magic":
                        if (damageSource.isMagicDamage()) return true;
                    case "fire":
                        if (damageSource.isFireDamage()) return true;
                    case "explosion":
                        if (damageSource.isExplosion()) return true;
                }
            }
        }
        return false;
    }

    public float getDamageMultiplierForSource(DamageSource damageSource) {
        float toReturn = 1f;
        for (EntityHitboxDamageDefinition damageDefinition : this.damageDefinitions) {
            if (damageDefinition.damageSource != null) {
                if (damageDefinition.damageSource.equals(damageSource.damageType)) {
                    toReturn *= damageDefinition.damageMultiplier;
                }
            }
            else if (damageDefinition.damageType != null) {
                switch (damageDefinition.damageType) {
                    case "projectile":
                        if (damageSource.isProjectile()) {
                            toReturn *= damageDefinition.damageMultiplier;
                            break;
                        }
                    case "magic":
                        if (damageSource.isMagicDamage()) {
                            toReturn *= damageDefinition.damageMultiplier;
                            break;
                        }
                    case "fire":
                        if (damageSource.isFireDamage()) {
                            toReturn *= damageDefinition.damageMultiplier;
                            break;
                        }
                    case "explosion":
                        if (damageSource.isExplosion()) {
                            toReturn *= damageDefinition.damageMultiplier;
                            break;
                        }
                }
            }
        }
        return toReturn;
    }

    //either one of damageSource or damageType must be null
    //damageSource is an instance of the DamageSource object (arrow, cactus, etc)
    //damageType is one of the booleans associated with a DamageSource object (projectile, magic, etc)
    //if damageSource or damageType both not null, damageSource will be prioritized
    public record EntityHitboxDamageDefinition(String damageSource, String damageType, float damageMultiplier) {
        @Override
        @NotNull
        public String toString() {
            return "[source=" + this.damageSource + ", type=" + this.damageType + ", multiplier=" + this.damageMultiplier + "]";
        }
    }
}
