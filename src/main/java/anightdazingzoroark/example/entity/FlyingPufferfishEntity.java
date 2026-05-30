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
import anightdazingzoroark.riftlib.ray.RiftLibRayHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;
import java.util.Map;

public class FlyingPufferfishEntity extends EntityFlying implements IAnimatable<FlyingPufferfishEntity, AnimationDataEntity>, IMultiHitboxUser<FlyingPufferfishEntity>, IRayCreator<FlyingPufferfishEntity> {
    private final AnimationDataEntity animationData = new AnimationDataEntity(this);
    private final Map<String, RiftLibRay.Builder> rayMap;
    private HitboxDefinitionList hitboxDefinitionList;
    private Entity[] hitboxes = {};

    public FlyingPufferfishEntity(World worldIn) {
        super(worldIn);
        this.setSize(1f, 1f);
        this.rayMap = Map.of(
                "puffUp", new RiftLibRay.Builder(this, "rayCenter")
                        .setShapeImpact(0, 8)
                        .setRaySpeed(1)
                        .setBreakBlockCondition(blockPos -> {
                            IBlockState blockState = this.world.getBlockState(blockPos);
                            float hardness = blockState.getBlockHardness(this.world, blockPos);
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
    public Map<String, RiftLibRay.Builder> getRayBuilders() {
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
    public List<AnimationController<FlyingPufferfishEntity, AnimationDataEntity>> createAnimationControllers() {
        return List.of(
                new AnimationController<FlyingPufferfishEntity, AnimationDataEntity>(
                        this, "puff", "default",
                        new AnimationControllerState<AnimationDataEntity>("default")
                                .addAnimation("animation.flying_pufferfish.inflate_loop")
                )
        );
    }

    @Override
    public AnimationDataEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public Map<String, AnimatableRunValue> createAnimationMessageEffects() {
        return Map.of(
                "puffUpRay", new AnimatableRunValue(
                        () -> RiftLibRayHelper.createRay(this, "puffUp"), Side.SERVER
                )
        );
    }
}
