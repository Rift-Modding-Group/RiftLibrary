package anightdazingzoroark.riftlib.core.manager;

import java.util.HashMap;
import java.util.List;

import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.model.AnimatedLocator;

public class AnimationFactory {
	private final IAnimatable animatable;
	private final HashMap<Integer, AnimationData> animationDataMap = new HashMap<>();

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
			AnimationData data = new AnimationData(this.animatable);
            this.animatable.registerControllers(data);
            this.animationDataMap.put(uniqueID, data);
		}
		return this.animationDataMap.get(uniqueID);
	}

    public void removeAnimationData(Integer uniqueID) {
        //first of all, kill all the animation locators
        //in the extracted animationdata
        //then remove the animationdata
        if (this.animationDataMap.containsKey(uniqueID)) {
            AnimationData data = this.animationDataMap.get(uniqueID);
            List<AnimatedLocator> animatedLocators = data.getAnimatedLocators();
            for (AnimatedLocator animatedLocator : animatedLocators) {
                animatedLocator.killLocator();
            }
            this.animationDataMap.remove(uniqueID);
        }
    }
}
