package anightdazingzoroark.riftlib.core.manager;

import anightdazingzoroark.riftlib.core.AnimatableRunValue;
import anightdazingzoroark.riftlib.core.AnimatableValue;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.processor.IBone;
import anightdazingzoroark.riftlib.core.snapshot.BoneSnapshot;
import anightdazingzoroark.riftlib.geo.render.GeoLocator;
import anightdazingzoroark.riftlib.geo.render.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedLocator;
import anightdazingzoroark.riftlib.molang.MolangParser;
import anightdazingzoroark.riftlib.molang.MolangScope;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import anightdazingzoroark.riftlib.resource.server.RiftLibCacheServer;
import anightdazingzoroark.riftlib.util.MolangUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is to hold information pertaining to animation data for an animated object
 * */
public abstract class AbstractAnimationData<T, D extends AbstractAnimationData<T, D>> {
    @NotNull
    private final T holder;
    private final Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection = new HashMap<>();
    private final Map<String, AnimationController<? extends IAnimatable<D>, D>> animationControllers = new HashMap<>();
    private final List<AnimatableValue> initAnimationValues = new ArrayList<>();
    private final List<AnimatableValue> onUpdateAnimationValues = new ArrayList<>();
    private final Map<String, AnimatableRunValue> animationMessageEffects = new HashMap<>();
    protected final Map<String, Supplier<Double>> molangQueries = new HashMap<>();
    private final List<AnimatedLocator> animatedLocators = new ArrayList<>();
    private int animatedLocatorTicker;
    @NotNull
    private final MolangParser parser;
    @NotNull
    private final MolangScope dataScope = new MolangScope();
    private GeoModel currentModel;
    public double tick;
    public boolean isFirstTick = true;
    public Object ticker;
    public boolean shouldPlayWhilePaused = false;

    //time values meant for molang queries
    public double animTime;
    public double deltaTime;
    public double lifeTime;

    public AbstractAnimationData(@NotNull T holder, @NotNull IAnimatable<D> animatable) {
        this.holder = holder;
        //looks unintuitive i know, but its to prevent NPEs from armor data
        this.parser = FMLCommonHandler.instance().getSide().isClient() ?
                RiftLibCacheClient.getInstance().parser : RiftLibCacheServer.getInstance().parser;
        this.createMolangQueries();
        animatable.initializeAnimationData((D) this);
        this.initAnimationVariables();
    }

    @NotNull
    public T getHolder() {
        return this.holder;
    }

    //-----initialization methods start here-----
    /**
     * This registers animation controllers
     * */
    public void addAnimationController(AnimationController<? extends IAnimatable<D>, D> animationController) {
        this.animationControllers.put(animationController.getName(), animationController);
    }

    /**
     * This only runs once and will run when this object just started rendering
     * This is meant for updating molang variables once.
     */
    public void addInitAnimationValue(AnimatableValue animatableValue) {
        this.initAnimationValues.add(animatableValue);
    }

    /**
     * This runs as long as the holder gets rendered on client or ticked on server
     * This is meant for updating molang variables repeatedly
     */
    public void addOnUpdateAnimationValue(AnimatableValue animatableValue) {
        this.onUpdateAnimationValues.add(animatableValue);
    }

    /**
     * This allows for running custom code from animations on the server or client
     */
    public void addAnimationMessageEffect(String name, AnimatableRunValue animMessageEffect) {
        this.animationMessageEffects.put(name, animMessageEffect);
    }
    //-----initialization methods end here-----

    //-----animated locator stuff starts here-----
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
    //-----animated locator stuff ends here-----

    public Map<String, Pair<IBone, BoneSnapshot>> getBoneSnapshotCollection() {
        return this.boneSnapshotCollection;
    }

    public Map<String, AnimationController<? extends IAnimatable<D>, D>> getAnimationControllers() {
        return this.animationControllers;
    }

    public Map<String, AnimatableRunValue> getAnimationMessageEffects() {
        return Map.copyOf(this.animationMessageEffects);
    }

    //check if all animations in the current state in the given controller have reached the end of their play cycle
    public boolean allAnimationsFinished(String controllerName) {
        for (Map.Entry<String, AnimationController<? extends IAnimatable<D>, D>> animationControllerEntry : this.animationControllers.entrySet()) {
            if (!(animationControllerEntry.getKey().equals(controllerName))) continue;
            return animationControllerEntry.getValue().allAnimationsFinished();
        }
        return false;
    }

    private void initAnimationVariables() {
        for (AnimatableValue animatableValue : this.initAnimationValues) {
            MolangUtils.parseValue(this.parser, this.dataScope, animatableValue);
        }
    }

    public void updateAnimationVariables() {
        for (AnimatableValue animatableValue : this.onUpdateAnimationValues) {
            MolangUtils.parseValue(this.parser, this.dataScope, animatableValue);
        }
    }

    public double getVariable(String name) {
        return MolangUtils.getVariable(this.parser, this.dataScope, name);
    }

    @NotNull
    public MolangParser getParser() {
        return this.parser;
    }

    @NotNull
    public MolangScope getDataScope() {
        return this.dataScope;
    }

    /**
     * This is for updating animation data while its animating
     * */
    public abstract void updateOnDataTick();

    public abstract boolean isValid();

    /**
     * This is for sending animation data to the server
     * */
    @NotNull
    public abstract NBTTagCompound asNBT();

    /**
     * Each AnimationData class is to have their own molang queries in addition to the ones here shared amongst all
     * */
    protected void createMolangQueries() {
        //-----for data specifically-----
        this.molangQueries.put("query.anim_time", () -> this.animTime);
        this.molangQueries.put("query.delta_time", () -> this.deltaTime);
        this.molangQueries.put("query.life_time", () -> this.lifeTime);
        //-----for world-----
        this.molangQueries.put("query.actor_count", () -> {
            World world = this.getWorld();
            if (world == null) return 0D;
            return (double) world.getLoadedEntityList().size();
        });
        this.molangQueries.put("query.time_of_day", () -> {
            World world = this.getWorld();
            if (world == null) return 0D;
            return world.getTotalWorldTime() / 24000D;
        });
        this.molangQueries.put("query.moon_phase", () -> {
            World world = this.getWorld();
            if (world == null) return 0D;
            return (double) world.getMoonPhase();
        });
    }

    @Nullable
    public abstract World getWorld();

    public Map<String, Supplier<Double>> getMolangQueries() {
        return Map.copyOf(this.molangQueries);
    }
}
