package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntityLiving;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.hitbox.MultiHitboxList;
import anightdazingzoroark.riftlib.ray.rayShape.impact.RiftLibRaySphereImpactShape;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class FlyingPufferfishEntity extends EntityFlying implements IAnimatable<AnimationDataEntityLiving>, IMultiHitboxUser<FlyingPufferfishEntity> {
    @NotNull
    private final MultiHitboxList<FlyingPufferfishEntity> multiHitboxList;
    @NotNull
    private final AnimationDataEntityLiving animationData = new AnimationDataEntityLiving(this);
    @NotNull
    private static RiftLibRayBuilder PUFF_UP_RAY = new RiftLibRayBuilder("riftlib:puff_up")
            .setImpactOnly()
            .setImpactShape(RiftLibRaySphereImpactShape::new)
            .setMotionSpeed(1D)
            .setMaxMotionDistance(8)
            .setBlockBreakCheck((rayCreator, blockPos) -> {
                World world = rayCreator.world;
                IBlockState blockState = world.getBlockState(blockPos);
                float hardness = blockState.getBlockHardness(world, blockPos);
                return hardness <= 1f && hardness >= 0f;
            })
            .setImpactCreationInterval(1)
            .setHitHandler((rayCreator, originPos, rayHitResult) -> {
                if (!(rayCreator instanceof FlyingPufferfishEntity pufferfish)) return;
                for (Entity entity : rayHitResult.hitEntities()) {
                    if (!(entity instanceof EntityLivingBase entityLivingBase)) continue;
                    entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(pufferfish), 1f);
                }
            });

    public FlyingPufferfishEntity(World worldIn) {
        super(worldIn);
        this.setSize(1f, 1f);
        this.multiHitboxList = new MultiHitboxList<>(this, this.animationData);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.setLastAttackedEntity(entityIn);
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 5f);
    }

    //hitbox stuff starts here
    @Override
    @NotNull
    public FlyingPufferfishEntity getMultiHitboxUser() {
        return this;
    }

    @Override
    @NotNull
    public MultiHitboxList<FlyingPufferfishEntity> getMultiHitboxList() {
        return this.multiHitboxList;
    }

    @Override
    public Entity[] getParts() {
        return this.multiHitboxList.getHitboxesAsArray();
    }
    //hitbox stuff ends here

    @Override
    public void initializeAnimationData(@NonNull AnimationDataEntityLiving animationData) {
        //---scaling---
        animationData.setScale(2f);

        //---animations---
        animationData.addAnimation("animation.flying_pufferfish.inflate_loop");

        animationData.addAnimationMessageEffect("startPuffUpRay", new AnimatableRunValue(
                () -> RiftLibRayHelper.createRay(PUFF_UP_RAY, "rayCenter", this), Side.SERVER
        ));
        animationData.addAnimationMessageEffect("endPuffUpRay", new AnimatableRunValue(
                () -> RiftLibRayHelper.killRay(this, PUFF_UP_RAY.getName()), Side.SERVER
        ));
    }

    @Override
    @NotNull
    public AnimationDataEntityLiving getAnimationData() {
        return this.animationData;
    }
}
