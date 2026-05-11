package anightdazingzoroark.example.entity.ai;

import anightdazingzoroark.example.entity.DragonEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;

public class DragonAttackAI extends EntityAIBase {
    protected DragonEntity dragon;
    double speedTowardsTarget;
    protected Path path;
    protected int delayCounter;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected int attackCooldown;

    public DragonAttackAI(DragonEntity creature, double speedIn) {
        this.dragon = creature;
        this.speedTowardsTarget = speedIn;
        this.setMutexBits(3);
    }

    public boolean shouldExecute() {
        EntityLivingBase entitylivingbase = this.dragon.getAttackTarget();

        if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else {
            this.path = this.dragon.getNavigator().getPathToEntityLiving(entitylivingbase);
            double d0 = this.dragon.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);

            if (this.path != null) return !this.dragon.isBeingRidden();
            else return this.getAttackReachSqr(entitylivingbase) >= d0 && !this.dragon.isBeingRidden();
        }
    }

    public boolean shouldContinueExecuting() {
        EntityLivingBase entitylivingbase = this.dragon.getAttackTarget();

        if (this.dragon.isBeingRidden()) return false;
        else if (entitylivingbase == null) return false;
        else if (!entitylivingbase.isEntityAlive()) return false;
        else return !(entitylivingbase instanceof EntityPlayer) || !(((EntityPlayer)entitylivingbase).isSpectator() && ((EntityPlayer)entitylivingbase).isCreative());
    }

    @Override
    public void startExecuting() {
        this.dragon.getNavigator().setPath(this.path, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    @Override
    public void resetTask() {
        EntityLivingBase entitylivingbase = this.dragon.getAttackTarget();

        if (entitylivingbase instanceof EntityPlayer && (((EntityPlayer)entitylivingbase).isSpectator() || ((EntityPlayer)entitylivingbase).isCreative())) {
            this.dragon.setAttackTarget(null);
        }

        this.dragon.getNavigator().clearPath();
        this.attackCooldown = 0;
    }

    public void updateTask() {
        EntityLivingBase entitylivingbase = this.dragon.getAttackTarget();
        if (entitylivingbase == null) return;
        this.dragon.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        this.targetX = entitylivingbase.posX;
        this.targetY = entitylivingbase.getEntityBoundingBox().minY;
        this.targetZ = entitylivingbase.posZ;
        double d0 = this.dragon.getDistanceSq(this.targetX, this.targetY, this.targetZ);
        --this.delayCounter;

        if (this.dragon.getEntitySenses().canSee(entitylivingbase) && this.delayCounter <= 0) {
            this.delayCounter = 4 + this.dragon.getRNG().nextInt(7);

            if (d0 > 1024.0D) this.delayCounter += 10;
            else if (d0 > 256.0D) this.delayCounter += 5;

            if (d0 >= this.getAttackReachSqr(entitylivingbase)) {
                if (!this.dragon.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget)) this.delayCounter += 15;
            }
            else {
                this.path = null;
                this.dragon.getNavigator().clearPath();
            }
        }
        this.checkAndPerformAttack(entitylivingbase, d0);
    }

    protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
        double d0 = this.getAttackReachSqr(enemy);

        if (--this.attackCooldown <= 0 && distToEnemySqr <= d0) {
            this.dragon.setAttacking(true);
            this.attackCooldown = 20;
        }
    }

    protected double getAttackReachSqr(EntityLivingBase attackTarget) {
        return (this.dragon.attackWidth() * this.dragon.attackWidth() + attackTarget.width);
    }
}
