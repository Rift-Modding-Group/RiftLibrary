package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.molang.utils.Interpolations;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Shared advanced data for living and non-living entity data.
 * */
public abstract class AbstractAnimationDataAdvancedEntity<T extends Entity, D extends AbstractAnimationDataAdvancedEntity<T, D>>
        extends AbstractAnimationDataEntity<T, D> {
    private Function<T, Float> holderScale;
    private final Map<String, AnimatedBoundingBox> animatedBoundingBoxes = new HashMap<>();
    private final Map<String, List<AnimatedBoundingBox>> animatedBoundingBoxesByTag = new HashMap<>();
    private final Map<String, AxisAlignedBB> debugBoundingBoxes = new HashMap<>();
    private boolean boundingBoxesRecentlyUpdated;

    public AbstractAnimationDataAdvancedEntity(@NonNull T holder, @NotNull IAnimatable<D> animatable) {
        super(holder, animatable);
    }

    @Override
    public void updateOnDataTick() {
        super.updateOnDataTick();

        //update all debugged world space bounding boxes
        if (this.getHolder().world.isRemote) {
            Set<String> debugAABBSet = new HashSet<>(this.debugBoundingBoxes.keySet());
            for (String debugAABB : debugAABBSet) {
                AxisAlignedBB newAABB = this.getWorldSpaceAABB(debugAABB);
                if (newAABB != null) this.debugBoundingBoxes.put(debugAABB, newAABB);
            }
        }
    }

    @Override
    public void createAnimatedObjects(@NotNull GeoModel model) {
        if (this.currentModel == model) return;
        super.createAnimatedObjects(model);
        this.animatedBoundingBoxes.clear();
        this.animatedBoundingBoxesByTag.clear();

        if (this.getHolder() instanceof IMultiHitboxUser<?>) {
            for (GeoBoundingBox boundingBox : model.getAllBoundingBoxes()) {
                if (boundingBox == null) continue;
                AnimatedBoundingBox toAdd = new AnimatedBoundingBox(boundingBox);
                this.animatedBoundingBoxes.put(toAdd.getName(), toAdd);
                for (String tag : toAdd.getTags()) {
                    this.animatedBoundingBoxesByTag.computeIfAbsent(tag, key -> new ArrayList<>()).add(toAdd);
                }
            }
            this.boundingBoxesRecentlyUpdated = true;
        }
    }

    @Override
    @NonNull
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = super.getNBT();
        toReturn.setString("AnimationTargetType", "Entity");
        toReturn.setInteger("EntityID", this.getHolder().getEntityId());
        return toReturn;
    }

    /**
     * The yaw used by model-space locators, hitboxes, and rays.
     * */
    public float getModelYaw() {
        return this.getHolder().rotationYaw;
    }

    public float getModelYaw(float partialTicks) {
        return Interpolations.lerpYaw(this.getHolder().prevRotationYaw, this.getHolder().rotationYaw, partialTicks);
    }

    public void setScale(float value) {
        this.setScale(entity -> value);
    }

    public void setScale(@NotNull Function<T, Float> holderScale) {
        this.holderScale = holderScale;
    }

    /**
     * The basis of model scaling of the entity.
     * */
    public float getScale() {
        return this.holderScale == null ? 1f : this.holderScale.apply(this.getHolder());
    }

    //-----animated bounding box definitions from here on out (only entities use hitboxes hence this lol)-----
    @NotNull
    public Map<String, AnimatedBoundingBox> getAnimatedBoundingBoxes() {
        return this.animatedBoundingBoxes;
    }

    @NotNull
    public Map<String, List<AnimatedBoundingBox>> getAnimatedBoundingBoxesByTag() {
        return this.animatedBoundingBoxesByTag;
    }

    public boolean getBoundingBoxesRecentlyUpdated() {
        boolean toReturn = this.boundingBoxesRecentlyUpdated;
        this.boundingBoxesRecentlyUpdated = false;
        return toReturn;
    }

    //-----world space bounding box stuff starts here-----
    /**
     * Simple helper to create a world-space AABB
     * */
    @Nullable
    public AxisAlignedBB getWorldSpaceAABB(@NotNull String boundingBoxName) {
        AnimatedBoundingBox animatedBoundingBox = this.animatedBoundingBoxes.get(boundingBoxName);
        if (animatedBoundingBox == null) return null;

        Vec3d modelSpacePos = animatedBoundingBox.getModelSpacePosition();
        float[] modelSpaceSize = animatedBoundingBox.getModelSpaceSize();
        float scale = this.getScale();

        double yaw = -Math.toRadians(this.getModelYaw());
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0D, yaw, 0D);
        Vec3d hitboxPos = VectorUtils.rotateVectorWithQuaternion(new Vec3d(
                -modelSpacePos.x * scale / 16D,
                (modelSpacePos.y - modelSpaceSize[1] / 2D) * scale / 16D,
                -modelSpacePos.z * scale / 16D
        ), quaternion);

        double halfWidth = modelSpaceSize[0] * scale / 32D;
        double height = modelSpaceSize[1] * scale / 16D;
        return new AxisAlignedBB(
                this.getHolder().posX + hitboxPos.x - halfWidth,
                this.getHolder().posY + hitboxPos.y,
                this.getHolder().posZ + hitboxPos.z - halfWidth,
                this.getHolder().posX + hitboxPos.x + halfWidth,
                this.getHolder().posY + hitboxPos.y + height,
                this.getHolder().posZ + hitboxPos.z + halfWidth
        );
    }

    @SideOnly(Side.CLIENT)
    public void createDebugAABB(@NotNull String name) {
        AxisAlignedBB aabb = this.getWorldSpaceAABB(name);
        if (aabb != null) this.debugBoundingBoxes.put(name, aabb);
    }

    @SideOnly(Side.CLIENT)
    public void removeDebugAABB(@NotNull String name) {
        this.debugBoundingBoxes.remove(name);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public AxisAlignedBB getDebugAABB(@NotNull String name) {
        return this.debugBoundingBoxes.get(name);
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    public Map<String, AxisAlignedBB> getDebugBoundingBoxes() {
        return this.debugBoundingBoxes;
    }
}
