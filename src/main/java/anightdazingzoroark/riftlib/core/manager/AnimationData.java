/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package anightdazingzoroark.riftlib.core.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangQueryValue;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import org.apache.commons.lang3.tuple.Pair;

import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;

public class AnimationData {
	private HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection;
	private final HashMap<String, AnimationController> animationControllers = new HashMap<>();
    private final List<AnimatedLocator> animatedLocators = new ArrayList<>();
	private final MolangParser parser = RiftLibCache.getInstance().parser;
    private final IAnimatable iAnimatable;
    public final MolangScope dataScope = new MolangScope();
    private GeoModel currentModel;
	public double tick;
	public boolean isFirstTick = true;
	private double resetTickLength = 1;
	public Double startTick;
	public Object ticker;
	public boolean shouldPlayWhilePaused = false;

	/**
	 * Instantiates a new Animation controller collection.
	 */
	public AnimationData(IAnimatable iAnimatable) {
        this.iAnimatable = iAnimatable;
		this.boneSnapshotCollection = new HashMap<>();
		this.initAnimationVariables();
	}

	/**
	 * This method is how you register animation controllers, without this, your
	 * AnimationPredicate method will never be called
	 *
	 * @param value The value
	 * @return the animation controller
	 */
	public AnimationController addAnimationController(AnimationController value) {
		return this.animationControllers.put(value.getName(), value);
	}

	public HashMap<String, Pair<IBone, BoneSnapshot>> getBoneSnapshotCollection() {
		return this.boneSnapshotCollection;
	}

	public void setBoneSnapshotCollection(HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection) {
		this.boneSnapshotCollection = boneSnapshotCollection;
	}

	public void clearSnapshotCache() {
		this.boneSnapshotCollection = new HashMap<>();
	}

	public double getResetSpeed() {
		return this.resetTickLength;
	}

	/**
	 * This is how long it takes for any bones that don't have an animation to
	 * revert back to their original position
	 *
	 * @param resetTickLength The amount of ticks it takes to reset. Cannot be
	 *                        negative.
	 */
	public void setResetSpeedInTicks(double resetTickLength) {
		this.resetTickLength = resetTickLength < 0 ? 0 : resetTickLength;
	}

	public HashMap<String, AnimationController> getAnimationControllers() {
		return this.animationControllers;
	}

    public void createAnimatedLocators(GeoModel model) {
        if (this.currentModel != model) {
            this.animatedLocators.clear();

            List<GeoLocator> locatorList = model.getAllLocators();
            for (GeoLocator locator : locatorList) {
                if (locator == null) continue;
                this.animatedLocators.add(new AnimatedLocator(locator, this.iAnimatable));
            }

            this.currentModel = model;
        }
    }

    public AnimatedLocator getAnimatedLocator(String name) {
        for (AnimatedLocator animatedLocator : this.animatedLocators) {
            if (animatedLocator == null) continue;
            if (animatedLocator.getName() != null && animatedLocator.getName().equals(name)) return animatedLocator;
        }
        return null;
    }

    public List<AnimatedLocator> getAnimatedLocators() {
        return this.animatedLocators;
    }

	public void initAnimationVariables() {
		List<AnimatableValue> initAnimatableValues = this.iAnimatable.createAnimationVariables();
		this.parser.withScope(this.dataScope, () -> {
			for (AnimatableValue animatableValue : initAnimatableValues) {
				if (animatableValue.isExpression()) {
					try {
						this.parser.parseExpression(animatableValue.getExpressionValue()).get();
					}
					catch (Exception e) {}
				}
				else {
					this.parser.setValue(animatableValue.getConstantValue().left, animatableValue.getConstantValue().right);
				}
			}
		});
	}

	public void updateAnimationVariables() {
		List<AnimatableValue> updateAnimatableValues = this.iAnimatable.tickAnimationVariables();
		this.parser.withScope(this.dataScope, () -> {
			for (AnimatableValue animatableValue : updateAnimatableValues) {
				if (animatableValue.isExpression()) {
					try {
						this.parser.parseExpression(animatableValue.getExpressionValue()).get();
					}
					catch (Exception e) {}
				}
				else {
					this.parser.setValue(animatableValue.getConstantValue().left, animatableValue.getConstantValue().right);
				}
			}
		});
	}

	public void updateMolangQueries() {
		this.parser.withScope(this.dataScope, () -> MolangQueryValue.updateMolangQueryValues(this.parser, this.iAnimatable));
	}
}
