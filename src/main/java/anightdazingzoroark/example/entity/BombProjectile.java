package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.PlayState;
import anightdazingzoroark.riftlib.core.builder.AnimationBuilder;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.event.predicate.AnimationEvent;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
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
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "bomb_flames", 0, new AnimationController.IAnimationPredicate() {
            @Override
            public PlayState test(AnimationEvent event) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bomb.flame_particles", LoopType.PLAY_ONCE));
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }
}
