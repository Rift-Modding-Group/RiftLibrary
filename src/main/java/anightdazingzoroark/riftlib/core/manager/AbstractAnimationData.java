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
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * This class is to hold information pertaining to animation data for an animated object
 * */
public abstract class AbstractAnimationData<T> {
    @NotNull
    private final T holder;
    @NotNull
    private final IAnimatable<?, ? extends AbstractAnimationData<?>> animatable;
    private final Map<String, Pair<IBone, BoneSnapshot>> boneSnapshotCollection = new HashMap<>();
    private final Map<String, AnimationController<?, ?>> animationControllers = new HashMap<>();
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

    public AbstractAnimationData(
            @NotNull T holder,
            @NotNull IAnimatable<?, ? extends AbstractAnimationData<?>> animatable
    ) {
        this.holder = holder;
        this.animatable = animatable;
        //looks unintuitive i know, but its to prevent NPEs from armor data
        this.parser = FMLCommonHandler.instance().getSide().isClient() ? RiftLibCacheClient.getInstance().parser : RiftLibCacheServer.getInstance().parser;
        this.createMolangQueries();

        this.initAnimationControllers();
        this.initAnimationVariables();
        this.initAnimationMessageEffects();
    }

    @NotNull
    public T getHolder() {
        return this.holder;
    }

    public Map<String, Pair<IBone, BoneSnapshot>> getBoneSnapshotCollection() {
        return this.boneSnapshotCollection;
    }

    public Map<String, AnimationController<?, ?>> getAnimationControllers() {
        return this.animationControllers;
    }

    public Map<String, AnimatableRunValue> getAnimationMessageEffects() {
        return this.animationMessageEffects;
    }

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

    //check if all animations in the current state in the given controller have reached the end of their play cycle
    public boolean allAnimationsFinished(String controllerName) {
        for (Map.Entry<String, AnimationController<?, ?>> animationControllerEntry : this.animationControllers.entrySet()) {
            if (!(animationControllerEntry.getKey().equals(controllerName))) continue;
            return animationControllerEntry.getValue().allAnimationsFinished();
        }
        return false;
    }

    private void initAnimationControllers() {
        IAnimatable<? extends IAnimatable<?, AbstractAnimationData<T>>, AbstractAnimationData<T>> animatable = (IAnimatable<? extends IAnimatable<?, AbstractAnimationData<T>>, AbstractAnimationData<T>>) this.animatable;

        List<? extends AnimationController<?, ?>> controllers = animatable.createAnimationControllers();
        for (AnimationController<?, ?> controller : controllers) {
            this.animationControllers.put(controller.getName(), controller);
        }
    }

    private void initAnimationVariables() {
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

    private void initAnimationMessageEffects() {
        IAnimatable<? extends IAnimatable<?, AbstractAnimationData<T>>, AbstractAnimationData<T>> animatable = (IAnimatable<? extends IAnimatable<?, AbstractAnimationData<T>>, AbstractAnimationData<T>>) this.animatable;
        Map<String, AnimatableRunValue> messageEffects = animatable.createAnimationMessageEffects();
        this.animationMessageEffects.putAll(messageEffects);
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

    public abstract World getWorld();

    public Map<String, Supplier<Double>> getMolangQueries() {
        return Map.copyOf(this.molangQueries);
    }
}
