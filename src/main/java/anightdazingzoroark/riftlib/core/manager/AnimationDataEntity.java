package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.geo.GeoLocator;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.MolangUtils;
import anightdazingzoroark.riftlib.util.QuaternionUtils;
import anightdazingzoroark.riftlib.util.VectorUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjglx.util.vector.Quaternion;

import java.util.*;
import java.util.function.Function;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase, AnimationDataEntity> {
    @NotNull
    private final Function<EntityLivingBase, Float> holderScale;
    private final Map<String, AnimatedBoundingBox> animatedBoundingBoxes = new HashMap<>();
    private final Map<String, List<AnimatedBoundingBox>> animatedBoundingBoxesByTag = new HashMap<>();
    //client only, meant for debugging
    private final Map<String, AxisAlignedBB> debugBoundingBoxes = new HashMap<>();
    private boolean boundingBoxesRecentlyUpdated;

    public AnimationDataEntity(EntityLivingBase holder) {
        this(holder, 1f);
    }

    public AnimationDataEntity(EntityLivingBase holder, float holderScale) {
        this(holder, entity -> holderScale);
    }

    public AnimationDataEntity(EntityLivingBase holder, @NotNull Function<EntityLivingBase, Float> holderScale) {
        super(holder, getAnimatable(holder));
        this.holderScale = holderScale;
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
    @NonNull
    public NBTTagCompound getNBT() {
        NBTTagCompound toReturn = super.getNBT();
        toReturn.setString("AnimationTargetType", "Entity");
        toReturn.setInteger("EntityID", this.getHolder().getEntityId());
        return toReturn;
    }

    @Override
    protected void createMolangQueries() {
        super.createMolangQueries();
        //---normal stuff---
        this.registerMolangQuery("query.health", (values, animData) -> {
            return (double) this.getHolder().getHealth();
        });
        this.registerMolangQuery("query.max_health", (values, animData) -> {
            return (double) this.getHolder().getMaxHealth();
        });
        this.registerMolangQuery("query.is_riding", (values, animData) -> {
            return MolangUtils.booleanToDouble(this.getHolder().isRiding());
        });
    }

    @SuppressWarnings("unchecked")
    private static IAnimatable<AnimationDataEntity> getAnimatable(EntityLivingBase holder) {
        if (holder instanceof IAnimatable<?>) return (IAnimatable<AnimationDataEntity>) holder;
        throw new IllegalArgumentException("AnimationDataEntity holder must implement IAnimatable");
    }

    //this override updates locators and bounding boxes
    @Override
    public void createAnimatedObjects(GeoModel model) {
        if (this.currentModel != model) {
            this.animatedLocators.clear();
            this.animatedBoundingBoxes.clear();
            this.animatedBoundingBoxesByTag.clear();

            //locators
            for (GeoLocator locator : model.allLocators) {
                if (locator == null) continue;
                this.animatedLocators.put(locator.getName(), new AnimatedLocator(locator, this));
            }

            //bounding boxes
            if (this.getHolder() instanceof IMultiHitboxUser<?>) {
                for (GeoBoundingBox boundingBox : model.allBoundingBoxes) {
                    if (boundingBox == null) continue;
                    AnimatedBoundingBox toAdd = new AnimatedBoundingBox(boundingBox);
                    this.animatedBoundingBoxes.put(toAdd.getName(), toAdd);
                    for (String tag : toAdd.getTags()) {
                        this.animatedBoundingBoxesByTag.computeIfAbsent(tag, key -> new ArrayList<>()).add(toAdd);
                    }
                }
                this.boundingBoxesRecentlyUpdated = true;
            }

            this.currentModel = model;
        }
    }

    /**
     * The basis of model scaling of the entity.
     * */
    public float getScale() {
        return this.holderScale.apply(this.getHolder());
    }

    //-----animated bounding box definitions from here on out (only entities use hitboxes hence this lol)-----
    public Map<String, AnimatedBoundingBox> getAnimatedBoundingBoxes() {
        return this.animatedBoundingBoxes;
    }

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
        float[] unscaledModelSpaceSize = animatedBoundingBox.getUnscaledModelSpaceSize();
        float scale = this.holderScale.apply(this.getHolder());

        double yaw = -Math.toRadians(this.getHolder().isBeingRidden() ? this.getHolder().rotationYaw : this.getHolder().rotationYawHead);
        Quaternion quaternion = QuaternionUtils.createXYZQuaternion(0D, yaw, 0D);
        Vec3d hitboxPos = VectorUtils.rotateVectorWithQuaternion(new Vec3d(
                (-modelSpacePos.x + unscaledModelSpaceSize[0] / 2D) * scale / 16D,
                (modelSpacePos.y + unscaledModelSpaceSize[1] / 2D - modelSpaceSize[1] / 2D) * scale / 16D,
                -(modelSpacePos.z + unscaledModelSpaceSize[0] / 2D) * scale / 16D
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
    public void createDebugAABB(String name) {
        AxisAlignedBB aabb = this.getWorldSpaceAABB(name);
        if (aabb != null) this.debugBoundingBoxes.put(name, aabb);
    }

    @SideOnly(Side.CLIENT)
    public void removeDebugAABB(String name) {
        this.debugBoundingBoxes.remove(name);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public AxisAlignedBB getDebugAABB(@NotNull String name) {
        return this.debugBoundingBoxes.get(name);
    }

    @SideOnly(Side.CLIENT)
    public Map<String, AxisAlignedBB> getDebugBoundingBoxes() {
        return this.debugBoundingBoxes;
    }
}
