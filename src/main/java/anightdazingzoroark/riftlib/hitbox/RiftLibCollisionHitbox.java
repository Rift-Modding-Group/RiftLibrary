package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * This hitbox deals with entity collisions and where players can attack on
 * IMultiHitboxUser instances
 * */
public class RiftLibCollisionHitbox<T extends IMultiHitboxUser<?>> extends MultiPartEntityPart implements IHitbox<T> {
    @NotNull
    private final AnimatedBoundingBox boundingBox;
    private boolean isDisabled;

    public RiftLibCollisionHitbox(T parent, AnimatedBoundingBox boundingBox) {
        super(
                parent, boundingBox.getName(),
                boundingBox.getModelSpaceSize()[0] / 16f, boundingBox.getModelSpaceSize()[1] / 16f
        );
        this.boundingBox = boundingBox;
        this.onAddedToWorld();
        this.registerEntityIdForServer();
    }

    /**
     * In order to forcibly sync the entityIds from server to client, this has to be done.
     * I fucking hate how hitboxes are dealt with in this version anyway.
     * */
    private void registerEntityIdForServer() {
        if (this.world.isRemote) return;
        if (this.world.getEntityByID(this.getEntityId()) == this) return;

        this.world.entitiesById.addKey(this.getEntityId(), this);
    }

    /**
     * Same reason as above
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

    public boolean hasHitboxTag(String name) {
        return Arrays.asList(this.boundingBox.getTags()).contains(name);
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
    public AnimatedBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public void setDisabled(boolean value) {
        this.isDisabled = value;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }
}
