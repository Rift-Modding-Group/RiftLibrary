package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.exceptions.MolangException;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * This class is to hold information pertaining to animation data for an animated object
 * */
public abstract class AbstractAnimationData<T> {
    @NotNull
    private final T holder;
    @NotNull
    private final IAnimatable<?> animatable;
    private HashMap<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection = new HashMap<>();
    private final HashMap<String, AnimationController<?, ?>> animationControllers = new HashMap<>();
    private final List<AnimatedLocator> animatedLocators = new ArrayList<>();
    private int animatedLocatorTicker;
    private final MolangParser parser = RiftLibCache.getInstance().parser;
    public final MolangScope dataScope = new MolangScope();
    private GeoModel currentModel;
    public double tick;
    public boolean isFirstTick = true;
    private double resetTickLength = 1;
    public Object ticker;
    public boolean shouldPlayWhilePaused = false;

    //time values meant for molang queries
    public double animTime;
    public double deltaTime;
    public double lifeTime;

    public AbstractAnimationData(@NotNull T holder, @NotNull IAnimatable<?> animatable) {
        this.holder = holder;
        this.animatable = animatable;
        this.initAnimationControllers();
        this.initAnimationVariables();
    }

    @NotNull
    public T getHolder() {
        return this.holder;
    }

    @NotNull
    public IAnimatable<?> getAnimatable() {
        return this.animatable;
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

    public HashMap<String, AnimationController<?, ?>> getAnimationControllers() {
        return this.animationControllers;
    }

    public void createAnimatedLocators(GeoModel model) {
        //update animated locators based on the model
        if (this.currentModel != model) {
            this.animatedLocators.clear();

            List<GeoLocator> locatorList = model.getAllLocators();
            for (GeoLocator locator : locatorList) {
                if (locator == null) continue;
                this.animatedLocators.add(new AnimatedLocator(locator, this));
            }

            this.currentModel = model;
        }
    }

    public void updateAnimatedLocators() {
        this.animatedLocatorTicker = 0;
        for (AnimatedLocator locator : this.animatedLocators) locator.setUpdated(true);
    }

    public void tickAnimatedLocators() {
        if (this.animatedLocators.isEmpty()) return;
        if (this.animatedLocatorTicker < 5) {
            this.animatedLocatorTicker++;
        }
        else {
            for (AnimatedLocator locator : this.animatedLocators) locator.setUpdated(false);
            this.animatedLocatorTicker = 0;
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

    public void initAnimationControllers() {
        IAnimatable<? extends AbstractAnimationData<?>> animatable = (IAnimatable<? extends AbstractAnimationData<?>>) this.animatable;

        List<? extends AnimationController<?, ?>> controllers = animatable.createAnimationControllers();
        for (AnimationController<?, ?> controller : controllers) {
            this.animationControllers.put(controller.getName(), controller);
        }
    }

    public void initAnimationVariables() {
        List<AnimatableValue> initAnimatableValues = this.animatable.createAnimationVariables();
        for (AnimatableValue animatableValue : initAnimatableValues) {
            MolangUtils.parseValue(this.parser, this.dataScope, animatableValue);
        }
    }

    public void updateAnimationVariables() {
        List<AnimatableValue> updateAnimatableValues = this.animatable.tickAnimationVariables();
        for (AnimatableValue animatableValue : updateAnimatableValues) {
            MolangUtils.parseValue(this.parser, this.dataScope, animatableValue);
        }
    }

    public void updateMolangQueries() {
        this.parser.withScope(this.dataScope, () -> {
            for (Map.Entry<String, BiFunction<AbstractAnimationData<T>, MolangParser, Double>> entry : this.getMolangQueries().entrySet()) {
                this.parser.setValue(entry.getKey(), entry.getValue().apply(this, this.parser));
            }
        });
    }

    /**
     * This is for specifically updating the time values to molang
     * */
    public void syncTimeQueries() {
        this.parser.withScope(this.dataScope, () -> {
            parser.setValue("query.anim_time", this.animTime);
            parser.setValue("query.delta_time", this.deltaTime);
            parser.setValue("query.life_time", this.lifeTime);
        });
    }

    /**
     * This is for sending animation data to the server
     * */
    @NotNull
    public abstract NBTTagCompound asNBT();

    /**
     * Each AnimationData class is to have their own molang queries in addition to the ones here shared amongst all
     * TODO: make molang queries true functions and not molang variables
     * */
    protected HashMap<String, BiFunction<AbstractAnimationData<T>, MolangParser, Double>> getMolangQueries() {
        HashMap<String, BiFunction<AbstractAnimationData<T>, MolangParser, Double>> toReturn = new HashMap<>();
        //-----for data specifically-----
        toReturn.put("query.anim_time", (animData, parser) -> {
            return animData.animTime;
        });
        toReturn.put("query.delta_time", (animData, parser) -> {
            return animData.deltaTime;
        });
        toReturn.put("query.life_time", (animData, parser) -> {
            return animData.lifeTime;
        });
        //for world for some reason
        toReturn.put("query.actor_count", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getLoadedEntityList().size();
        });
        toReturn.put("query.time_of_day", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return world.getTotalWorldTime() / 24000D;
        });
        toReturn.put("query.moon_phase", (animData, parser) -> {
            World world = Minecraft.getMinecraft().world;
            if (world == null) return 0D;
            return (double) world.getMoonPhase();
        });
        return toReturn;
    }
}
