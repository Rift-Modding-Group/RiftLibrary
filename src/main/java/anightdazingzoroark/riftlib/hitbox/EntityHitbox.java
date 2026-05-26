package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.HitboxUtils;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.List;

public class EntityHitbox<T extends IMultiHitboxUser<?>> extends MultiPartEntityPart {
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

    public EntityHitbox(T parent, AnimatedLocator hitboxLocator, float damageMultiplier, float width, float height, boolean affectedByAnim) {
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

        EntityHitbox<?> entityAlreadyUsingID = this.world.getEntityByID(entityId) instanceof EntityHitbox<?> entityHitbox ? entityHitbox : null;
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
        float locatorWidthDelta = Math.max(
                this.hitboxLocator.getParentBone().getScaleX(),
                this.hitboxLocator.getParentBone().getScaleZ()
        );
        float locatorHeightDelta = this.hitboxLocator.getParentBone().getScaleY();
        float finalWidth = this.fixedWidth * this.getParent().multiHitboxUserScale() * locatorWidthDelta;
        float finalHeight = this.fixedHeight * this.getParent().multiHitboxUserScale() * locatorHeightDelta;
        this.setSize(finalWidth, finalHeight);

        //-----set position-----
        //correct the model space positions first
        Vec3d modelSpacePos = this.hitboxLocator.getModelSpacePosition();
        float newHitboxX = -(float) modelSpacePos.x / 16f;
        float newHitboxY = (float) modelSpacePos.y / 16f - (this.fixedHeight / 2f) - (this.hitboxLocator.getParentBone().getScaleY() - 1) / 3;
        float newHitboxZ = -(float) modelSpacePos.z / 16f;

        //set initial entity offset from center
        Vec3d posVec = new Vec3d(
                newHitboxX * this.getParent().multiHitboxUserScale(),
                newHitboxY * this.getParent().multiHitboxUserScale(),
                newHitboxZ * this.getParent().multiHitboxUserScale()
        );

        //determine yaw
        double normalYawRadians = -Math.toRadians(parentEntityLiving.rotationYawHead);
        double riddenYawRadians = -Math.toRadians(parentEntityLiving.rotationYaw);
        double finalYawRadians = parentEntityLiving.isBeingRidden() ? riddenYawRadians : normalYawRadians;

        //rotate vector around yaw
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0, finalYawRadians, 0);
        posVec = VectorUtils.rotateVectorWithQuaternion(posVec, quaternion);

        //put in world
        this.setPositionAndUpdate(
                this.getParentAsEntityLiving().posX + posVec.x,
                this.getParentAsEntityLiving().posY + posVec.y,
                this.getParentAsEntityLiving().posZ + posVec.z
        );

        //-----remove if entity is dead or no longer exists-----
        if (this.getParentAsEntityLiving() == null || !this.getParentAsEntityLiving().isEntityAlive()) {
            this.world.removeEntityDangerously(this);
        }
        super.onUpdate();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return this.getParentAsEntityLiving().processInitialInteract(player, hand);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.damageSourceIsRider(source)) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    private boolean damageSourceIsRider(DamageSource source) {
        if (this.getParentAsEntityLiving() == null || source == null) return false;

        if (this.getParentAsEntityLiving().isBeingRidden()) {
            if (source.getTrueSource() != null && this.getParentAsEntityLiving().isPassenger(source.getTrueSource())) {
                return true;
            }
            if (source.getImmediateSource() != null && this.getParentAsEntityLiving().isPassenger(source.getImmediateSource())) {
                return true;
            }
        }
        return false;
    }


    @Deprecated
    public Vec3d getDisplacementVec() {
        return new Vec3d(0, 0, 0);
    }

    //recommended instead of using the parent variable
    public T getParent() {
        return (T) this.parent;
    }

    public EntityLiving getParentAsEntityLiving() {
        return (EntityLiving) this.parent;
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
