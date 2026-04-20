package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocatorNew;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangQueryParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractAnimationData<T> {
    private final T holder;
    private final IAnimatable<?> animatable;
    private HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection = new HashMap<>();
    private final HashMap<String, AnimationController<?>> animationControllers = new HashMap<>();
    private final List<AnimatedLocatorNew> animatedLocators = new ArrayList<>();
    private int animatedLocatorTicker;
    private final MolangParser parser = RiftLibCache.getInstance().parser;
    private final MolangQueryParser queryParser = RiftLibCache.getInstance().queryParser;
    public final MolangScope dataScope = new MolangScope();
    private GeoModel currentModel;
    public double tick;
    public boolean isFirstTick = true;
    private double resetTickLength = 1;
    public Object ticker;
    public boolean shouldPlayWhilePaused = false;
    private boolean initialized;

    //values meant for molang queries
    public double animTime;
    public double deltaTime;
    public double lifeTime;

    public AbstractAnimationData(T holder, IAnimatable<?> animatable) {
        this.holder = holder;
        this.animatable = animatable;
    }

    public T getHolder() {
        return this.holder;
    }

    public void initialize() {
        if (this.initialized) return;
        this.initialized = true;
        ((IAnimatable) this.animatable).registerControllers(this);
        this.initAnimationVariables();
    }

    /**
     * This method is how you register animation controllers, without this, your
     * AnimationPredicate method will never be called
     *
     * @param value The value
     * @return the animation controller
     */
    public AnimationController<?> addAnimationController(AnimationController<?> value) {
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

    public HashMap<String, AnimationController<?>> getAnimationControllers() {
        return this.animationControllers;
    }

    public void createAnimatedLocators(GeoModel model) {
        //update animated locators based on the model
        if (this.currentModel != model) {
            this.animatedLocators.clear();

            List<GeoLocator> locatorList = model.getAllLocators();
            for (GeoLocator locator : locatorList) {
                if (locator == null) continue;
                this.animatedLocators.add(new AnimatedLocatorNew(locator, this));
            }

            this.currentModel = model;
        }
    }

    public void updateAnimatedLocators() {
        this.animatedLocatorTicker = 0;
        for (AnimatedLocatorNew locator : this.animatedLocators) locator.setUpdated(true);
    }

    public void tickAnimatedLocators() {
        if (this.animatedLocators.isEmpty()) return;
        if (this.animatedLocatorTicker < 5) {
            this.animatedLocatorTicker++;
        }
        else {
            for (AnimatedLocatorNew locator : this.animatedLocators) locator.setUpdated(false);
            this.animatedLocatorTicker = 0;
        }
    }

    public AnimatedLocatorNew getAnimatedLocator(String name) {
        for (AnimatedLocatorNew animatedLocator : this.animatedLocators) {
            if (animatedLocator == null) continue;
            if (animatedLocator.getName() != null && animatedLocator.getName().equals(name)) return animatedLocator;
        }
        return null;
    }

    public List<AnimatedLocatorNew> getAnimatedLocators() {
        return this.animatedLocators;
    }

    public void initAnimationVariables() {
        List<AnimatableValue> initAnimatableValues = this.animatable.createAnimationVariables();
        this.parser.withScope(this.dataScope, () -> {
            for (AnimatableValue animatableValue : initAnimatableValues) {
                if (animatableValue.isExpression()) {
                    try {
                        this.parser.parseExpression(animatableValue.getExpressionValue()).get();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    String name = animatableValue.getConstantValue().left;
                    if (this.parser.isQuery(name)) {
                        throw new RuntimeException(new MolangException("Cannot assign value to query '"+name+"'!"));
                    }
                    this.parser.setValue(name, animatableValue.getConstantValue().right);
                }
            }
        });
    }

    public void updateAnimationVariables() {
        List<AnimatableValue> updateAnimatableValues = this.animatable.tickAnimationVariables();
        this.parser.withScope(this.dataScope, () -> {
            for (AnimatableValue animatableValue : updateAnimatableValues) {
                if (animatableValue.isExpression()) {
                    try {
                        this.parser.parseExpression(animatableValue.getExpressionValue()).get();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    String name = animatableValue.getConstantValue().left;
                    if (this.parser.isQuery(name)) {
                        throw new RuntimeException(new MolangException("Cannot assign value to query '"+name+"'!"));
                    }
                    this.parser.setValue(name, animatableValue.getConstantValue().right);
                }
            }
        });
    }

    public void updateMolangQueries() {
        this.queryParser.updateQueries(this, this.parser, this.dataScope);
    }

    /**
     * This is for sending animation data to the server
     * */
    @NotNull
    public abstract NBTTagCompound asNBT();
}
