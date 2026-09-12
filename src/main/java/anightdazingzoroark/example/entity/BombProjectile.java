package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataProjectile;
import anightdazingzoroark.riftlib.projectile.RiftLibProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BombProjectile extends RiftLibProjectile {
    @NotNull
    private final AnimationDataProjectile data = new AnimationDataProjectile(this);

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
    public void projectileImpactEffects(@Nullable EntityLivingBase hitEntity, @NotNull Vec3d hitPos) {
        this.world.createExplosion(this, hitPos.x, hitPos.y, hitPos.z, 4f, true);
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
    public SoundEvent getOnProjectileHitSound() {
        return null;
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataProjectile animationData) {
        animationData.addAnimationController(new AnimationController<BombProjectile, AnimationDataProjectile>(
                this, "bomb", "default",
                new AnimationControllerState<AnimationDataProjectile>("default")
                        .addAnimation("animation.bomb.flame_particles")
                        .addAnimation("animation.bomb.sounds")
        ));
    }

    @Override
    @NotNull
    public AnimationDataProjectile getAnimationData() {
        return this.data;
    }
}
