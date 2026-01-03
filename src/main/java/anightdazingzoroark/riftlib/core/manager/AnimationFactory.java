package anightdazingzoroark.riftlib.core.manager;

import java.util.HashMap;
import java.util.List;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;

public class AnimationFactory {
	private final IAnimatable animatable;
	private final HashMap<Integer, AnimationData> animationDataMap = new HashMap<>();
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

    public void setCurrentModel(GeoModel model) {
        if (this.currentModel != model) this.currentModel = model;
    }

    public GeoLocator getLocator(String name) {
        if (this.currentModel == null) return null;
        List<GeoLocator> locatorList = this.currentModel.getAllLocators();
        for (GeoLocator locator : locatorList) {
            if (locator == null) continue;
            if (locator.name != null && locator.name.equals(name)) return locator;
        }
        return null;
    }

    public List<GeoLocator> getGeoLocators() {
        if (this.currentModel == null) return null;
        return this.currentModel.getAllLocators();
    }
}
