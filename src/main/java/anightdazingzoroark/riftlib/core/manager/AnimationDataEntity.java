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

import java.util.ArrayList;
import java.util.List;

public class AnimationDataEntity extends AbstractAnimationDataEntity<EntityLivingBase, AnimationDataEntity> {
    private final List<AnimatedBoundingBox> animatedBoundingBoxes = new ArrayList<>();
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

            //locators
            for (GeoLocator locator : model.allLocators) {
                if (locator == null) continue;
                this.animatedLocators.add(new AnimatedLocator(locator, this));
            }

            //bounding boxes
            if (this.getHolder() instanceof IMultiHitboxUser<?>) {
                for (GeoBoundingBox boundingBox : model.allBoundingBoxes) {
                    if (boundingBox == null) continue;
                    this.animatedBoundingBoxes.add(new AnimatedBoundingBox(boundingBox));
                }
                this.boundingBoxesRecentlyUpdated = true;
            }

            this.currentModel = model;
        }
    }

    //-----animated bounding box definitions from here on out (only entities use hitboxes hence this lol)-----
    public List<AnimatedBoundingBox> getAnimatedBoundingBoxes() {
        return this.animatedBoundingBoxes;
    }

    public boolean getBoundingBoxesRecentlyUpdated() {
        boolean toReturn = this.boundingBoxesRecentlyUpdated;
        this.boundingBoxesRecentlyUpdated = false;
        return toReturn;
    }
}
