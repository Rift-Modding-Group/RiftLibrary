package anightdazingzoroark.riftlib.core.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.animatedLocator.EntityAnimatedLocator;
import anightdazingzoroark.riftlib.model.animatedLocator.IAnimatedLocator;
import anightdazingzoroark.riftlib.model.animatedLocator.ItemAnimatedLocator;
import anightdazingzoroark.riftlib.model.animatedLocator.TileEntityAnimatedLocator;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

public class AnimationFactory {
	private final IAnimatable animatable;
	private final HashMap<Integer, AnimationData> animationDataMap = new HashMap<>();
    private final List<IAnimatedLocator> animatedLocators = new ArrayList<>();
    private GeoModel currentModel;

	public AnimationFactory(IAnimatable animatable) {
		this.animatable = animatable;
	}

	/**
	 * This creates or gets the cached animation manager for any unique ID. For
	 * itemstacks, this is typically a hashcode of their nbt. For entities it should
	 * be their unique uuid. For tile entities you can use nbt or just one constant
	 * value since they are not singletons.
	 *
	 * @param uniqueID A unique integer ID. For every ID the same animation manager
	 *                 will be returned.
	 * @return the animatable manager
	 */
	public AnimationData getOrCreateAnimationData(Integer uniqueID) {
		if (!this.animationDataMap.containsKey(uniqueID)) {
			AnimationData data = new AnimationData();
            this.animatable.registerControllers(data);
            this.animationDataMap.put(uniqueID, data);
		}
		return this.animationDataMap.get(uniqueID);
	}

    public void createAnimatedLocators(GeoModel model) {
        if (model != this.currentModel) {
            this.animatedLocators.clear();
            List<GeoLocator> locators = model.getAllLocators();
            for (GeoLocator locator : locators) {
                if (this.animatable instanceof Entity) {
                    this.animatedLocators.add(new EntityAnimatedLocator((Entity) this.animatable, locator));
                }
                else if (this.animatable instanceof TileEntity) {
                    this.animatedLocators.add(new TileEntityAnimatedLocator((TileEntity) this.animatable, locator));
                }
                else if (this.animatable instanceof Item) {
                    this.animatedLocators.add(new ItemAnimatedLocator((Item) this.animatable, locator));
                }
            }
            this.currentModel = model;
        }
    }

    public IAnimatedLocator getAnimatedLocator(String name) {
        for (IAnimatedLocator locator : this.animatedLocators) {
            if (locator.getLocatorName().equals(name)) return locator;
        }
        return null;
    }

    public List<IAnimatedLocator> getAnimatedLocators() {
        return this.animatedLocators;
    }
}
