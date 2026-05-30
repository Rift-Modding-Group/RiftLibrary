package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class BombProjectile extends RiftLibProjectile {
    public BombProjectile(World worldIn) {
        super(worldIn);
    }

    public BombProjectile(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public BombProjectile(World worldIn, EntityLivingBase shooter) {
        super(worldIn, shooter);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void projectileEntityEffects(EntityLivingBase entityLivingBase) {
        this.world.createExplosion(this, this.posX, this.posY, this.posZ, 4f, true);
    }

    @Override
    public double getDamage() {
        return 0f;
    }

    @Override
    public boolean canRotateVertically() {
        return false;
    }

    @Override
    public void initializeAnimationData(AnimationDataProjectile animationData) {
        animationData.addAnimationController(new AnimationController<BombProjectile, AnimationDataProjectile>(
                this, "bomb", "default",
                new AnimationControllerState<AnimationDataProjectile>("default")
                        .addAnimation("animation.bomb.flame_particles")
                        .addAnimation("animation.bomb.sounds")
        ));
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }
}
