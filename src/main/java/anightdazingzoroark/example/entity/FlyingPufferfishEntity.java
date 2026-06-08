package anightdazingzoroark.example.entity;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.hitbox.HitboxDefinitionList;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.ray.IRayCreator;
import anightdazingzoroark.riftlib.ray.RiftLibRay;
import anightdazingzoroark.riftlib.ray.RiftLibRayBuilder;
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;

public class FlyingPufferfishEntity extends EntityFlying implements IAnimatable<AnimationDataEntity>, IMultiHitboxUser<FlyingPufferfishEntity>, IRayCreator<FlyingPufferfishEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);
    private final Map<String, RiftLibRayBuilder> rayMap;
    private HitboxDefinitionList hitboxDefinitionList;
    private Entity[] hitboxes = {};

    public FlyingPufferfishEntity(World worldIn) {
        super(worldIn);
        this.setSize(1f, 1f);
        this.rayMap = Map.of(
                "puffUp", new RiftLibRayBuilder()
                        .setShapeImpactSphere(0, 8)
                        .setRaySpeed(1)
                        .setBreakBlockCondition((rayCreator, blockPos) -> {
                            IBlockState blockState = rayCreator.getRayCreator().world.getBlockState(blockPos);
                            float hardness = blockState.getBlockHardness(rayCreator.getRayCreator().world, blockPos);
                            return hardness <= 1f && hardness >= 0f;
                        })
                        .setOnlyOneSegment()
        );
    }

    //hitbox stuff starts here
    @Override
    public FlyingPufferfishEntity getMultiHitboxUser() {
        return this;
    }

    @Override
    public float multiHitboxUserScale() {
        return 2f;
    }

    @Override
    public Entity[] getParts() {
        return this.hitboxes;
    }

    @Override
    public void setParts(Entity[] hitboxes) {
        this.hitboxes = hitboxes;
    }

    @Override
    public HitboxDefinitionList getHitboxDefinitionList() {
        return this.hitboxDefinitionList;
    }

    @Override
    public void setHitboxDefinitionList(HitboxDefinitionList hitboxDefinitionList) {
        this.hitboxDefinitionList = hitboxDefinitionList;
    }

    @Override
    public World getWorld() {
        return this.world;
    }
    //hitbox stuff ends here

    //ray stuff starts here
    @Override
    public float rayCreatorScale() {
        return 2f;
    }

    @Override
    public FlyingPufferfishEntity getRayCreator() {
        return this;
    }

    @Override
    public Map<String, RiftLibRayBuilder> getRayBuilders() {
        return this.rayMap;
    }

    @Override
    public void applyRaySegments(String rayName, BlockPos originPos, RiftLibRay.RayHitResult rayHitResult) {
        for (Entity entity : rayHitResult.hitEntities()) {
            if (!(entity instanceof EntityLivingBase entityLivingBase)) continue;
            entityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this), 1f);
        }
    }
    //ray stuff ends here

    @Override
    public void initializeAnimationData(AnimationDataEntity animationData) {
        animationData.addAnimationController(new AnimationController<FlyingPufferfishEntity, AnimationDataEntity>(
                this, "puff", "default",
                new AnimationControllerState<AnimationDataEntity>("default")
                        .addAnimation("animation.flying_pufferfish.inflate_loop")
        ));

        animationData.addAnimationMessageEffect("puffUpRay", new AnimatableRunValue(
                () -> RiftLibRayHelper.createRay(this, "puffUp", "rayCenter"), Side.SERVER
        ));
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }
}
