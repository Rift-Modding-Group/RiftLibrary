package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.geo.GeoLocator;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.internalMessage.RiftLibClearBoundingBoxes;
import anightdazingzoroark.riftlib.internalMessage.RiftLibShowBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase, AnimationDataEntity> {
    @NotNull
    private final Function<EntityLivingBase, Float> holderScale;
    private final Map<String, AnimatedBoundingBox> animatedBoundingBoxes = new HashMap<>();
    private final Map<String, List<AnimatedBoundingBox>> animatedBoundingBoxesByTag = new HashMap<>();
    private final Map<String, AxisAlignedBB> worldSpaceBoundingBoxes = new HashMap<>();
    private final List<String> displayedWorldSpaceBoundingBoxes = new ArrayList<>(); //client only, meant for debugging
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

        //update all world space bounding boxes
        Set<String> aabbNames = new HashSet<>(this.worldSpaceBoundingBoxes.keySet());
        aabbNames.addAll(this.displayedWorldSpaceBoundingBoxes);
        for (String aabbName : aabbNames) {
            AxisAlignedBB newAABB = this.createWorldSpaceAABB(aabbName);
            if (newAABB != null) this.worldSpaceBoundingBoxes.put(aabbName, newAABB);
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
     * Simple helper to create or get a world-space AABB
     * */
    @Nullable
    public AxisAlignedBB getOrCreateWorldSpaceAABB(@NotNull String aabbName) {
        //find first
        AxisAlignedBB toReturn = this.worldSpaceBoundingBoxes.get(aabbName);
        if (toReturn != null) return toReturn;

        //if it was null, create and try to return end result
        this.defineWorldSpaceAABB(aabbName);
        return this.worldSpaceBoundingBoxes.get(aabbName);
    }

    /**
     * Turn an animated bounding box into a world space AABB.
     * */
    public void defineWorldSpaceAABB(@NotNull String aabbName) {
        this.defineWorldSpaceAABB(aabbName, false);
    }

    /**
     * Turn an animated bounding box into a world space AABB, with debugging too.
     * */
    public void defineWorldSpaceAABB(@NotNull String aabbName, boolean displayDebug) {
        AxisAlignedBB axisAlignedBB = this.createWorldSpaceAABB(aabbName);
        if (axisAlignedBB == null) return;
        this.worldSpaceBoundingBoxes.put(aabbName, axisAlignedBB);
        if (displayDebug && !this.getHolder().world.isRemote) {
            ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(new RiftLibShowBoundingBox(this.getHolder(), aabbName, true), this.getHolder());
        }
    }

    /**
     * Client-only method whose only purpose is to display a bounding box
     * */
    @SideOnly(Side.CLIENT)
    public void displayWordSpaceBoundingBox(@NotNull String aabbName) {
        if (!this.displayedWorldSpaceBoundingBoxes.contains(aabbName) && this.worldSpaceBoundingBoxes.containsKey(aabbName)) {
            this.displayedWorldSpaceBoundingBoxes.add(aabbName);
        }
    }

    public void removeWorldSpaceAABB(@NotNull String aabbName) {
        this.worldSpaceBoundingBoxes.remove(aabbName);
        if (!this.getHolder().world.isRemote) {
            ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(new RiftLibShowBoundingBox(this.getHolder(), aabbName, false), this.getHolder());
        }
    }

    @SideOnly(Side.CLIENT)
    public void hideWordSpaceBoundingBox(@NotNull String aabbName) {
        this.displayedWorldSpaceBoundingBoxes.remove(aabbName);
    }

    @Nullable
    public AxisAlignedBB getWorldSpaceAABB(@NotNull String aabbName) {
        return this.worldSpaceBoundingBoxes.get(aabbName);
    }

    public boolean hasDisplayedWorldSpaceAABBs() {
        return !this.displayedWorldSpaceBoundingBoxes.isEmpty();
    }

    @NotNull
    public Map<String, AxisAlignedBB> getDisplayedWorldSpaceAABBs() {
        Map<String, AxisAlignedBB> toReturn = new LinkedHashMap<>();
        for (String aabbName : this.displayedWorldSpaceBoundingBoxes) {
            AxisAlignedBB aabb = this.worldSpaceBoundingBoxes.get(aabbName);
            if (aabb != null) toReturn.put(aabbName, aabb);
        }
        return toReturn;
    }

    @Nullable
    private AxisAlignedBB createWorldSpaceAABB(@NotNull String boundingBoxName) {
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

    /**
     * For clearing all defined world space AABBs
     * */
    public void clearAllWorldSpaceAABBs() {
        this.worldSpaceBoundingBoxes.clear();
        if (!this.getHolder().world.isRemote) {
            ServerProxy.MESSAGE_WRAPPER.sendToAllTracking(new RiftLibClearBoundingBoxes(this.getHolder()), this.getHolder());
        }
    }

    @SideOnly(Side.CLIENT)
    public void clearAllDisplayedAABBs() {
        this.displayedWorldSpaceBoundingBoxes.clear();
    }
}
