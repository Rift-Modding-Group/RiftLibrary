package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.GeoBoundingBox;
import anightdazingzoroark.riftlib.geo.GeoLocator;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.hitbox.IMultiHitboxUser;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase, AnimationDataEntity> {
    private final Map<String, AnimatedBoundingBox> animatedBoundingBoxes = new HashMap<>();
    private final Map<String, List<AnimatedBoundingBox>> animatedBoundingBoxesByTag = new HashMap<>();
    private boolean boundingBoxesRecentlyUpdated;

    public AnimationDataEntity(EntityLivingBase holder) {
        super(holder, getAnimatable(holder));
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
}
