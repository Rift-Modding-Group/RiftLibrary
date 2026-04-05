package anightdazingzoroark.riftlib.hitboxLogic;

import anightdazingzoroark.riftlib.core.IAnimatable;
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

public class EntityHitbox extends MultiPartEntityPart {
    private final float damageMultiplier;
    //these are the final definitive scales of the hitbox and will be used for such
    public final float fixedWidth;
    public final float fixedHeight;
    //these are the anim dependent scales
    private float widthScaleDisplacement = 1f;
    private float heightScaleDisplacement = 1f;
    //these are the final definitive displacements of the hitbox from the center of the entity
    private final float xOffset;
    private final float yOffset;
    private final float zOffset;
    //these are anim dependent displacments due to animations
    private float xDisplacement;
    private float yDisplacement;
    private float zDisplacement;
    //others
    public final boolean affectedByAnim;
    private boolean isDisabled;
    public final List<EntityHitboxDamageDefinition> damageDefinitions = new ArrayList<>();

    public EntityHitbox(IMultiHitboxUser parent, String partName, float damageMultiplier, float width, float height, float xOffset, float yOffset, float zOffset, boolean affectedByAnim) {
        super(parent, partName, width, height);
        this.damageMultiplier = damageMultiplier;
        this.fixedWidth = width;
        this.fixedHeight = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.affectedByAnim = affectedByAnim;
    }

    @Override
    public void onUpdate() {
        EntityLivingBase parentEntityLiving = this.getParentAsEntityLiving();

        //set scale
        float parentScale = parentEntityLiving instanceof IAnimatable animatable ? animatable.scale() : 1f;
        float finalWidth = this.fixedWidth * parentScale * this.widthScaleDisplacement;
        float finalHeight = this.fixedHeight * parentScale * this.heightScaleDisplacement;
        this.setSize(finalWidth, finalHeight);

        //set initial entity offset from center
        Vec3d posVec = new Vec3d(
                (this.xOffset + this.xDisplacement) * parentScale,
                (this.yOffset + this.yDisplacement) * parentScale,
                (this.zOffset + this.zDisplacement) * parentScale
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

        //remove if entity is dead or no longer exists
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

    public void resizeByAnim(float widthScaleDisplacement, float heightScaleDisplacement) {
        this.widthScaleDisplacement = widthScaleDisplacement;
        this.heightScaleDisplacement = heightScaleDisplacement;
    }

    public void displaceByAnim(float xDisplacement, float yDisplacement, float zDisplacement) {
        this.xDisplacement = xDisplacement;
        this.yDisplacement = yDisplacement;
        this.zDisplacement = zDisplacement;
    }

    public float getWidthScaleDisplacement() {
        return this.widthScaleDisplacement;
    }

    public float getHeightScaleDisplacement() {
        return this.heightScaleDisplacement;
    }

    public Vec3d getDisplacementVec() {
        return new Vec3d(this.xDisplacement, this.yDisplacement, this.zDisplacement);
    }

    //recommended instead of using the parent variable
    public IMultiHitboxUser getParent() {
        return (IMultiHitboxUser) this.parent;
    }

    public EntityLiving getParentAsEntityLiving() {
        return (EntityLiving) this.parent;
    }

    //get the scale of the parent as an IAnimatable
    private float getParentScale() {
        if (this.parent instanceof IAnimatable) {
            return ((IAnimatable) this.parent).scale();
        }
        return 1f;
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
