package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.core.manager.AnimationDataEntity;
import anightdazingzoroark.riftlib.internalMessage.RiftLibSyncHitboxEntityId;
import anightdazingzoroark.riftlib.model.AnimatedBoundingBox;
import anightdazingzoroark.riftlib.proxy.ServerProxy;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Utility class to manage collisionHitboxes on an IMultiHitboxUser instance
 * */
public class MultiHitboxList<T extends IMultiHitboxUser<?>> {
    @NotNull
    private final T multiHitboxUser;
    @NotNull
    private final AnimationDataEntity animData;
    private final Map<String, RiftLibCollisionHitbox<T>> collisionHitboxes = new HashMap<>();
    private final Map<String, List<RiftLibCollisionHitbox<T>>> collisionHitboxesByTag = new HashMap<>();
    private final Map<String, RiftLibOffenseHitbox<T>> offenseHitboxes = new HashMap<>();
    private final Map<String, List<RiftLibOffenseHitbox<T>>> offenseHitboxesByTag = new HashMap<>();

    public MultiHitboxList(@NotNull T multiHitboxUser, @NotNull AnimationDataEntity animData) {
        this.multiHitboxUser = multiHitboxUser;
        this.animData = animData;
    }

    /**
     * Ticked on server (and client hopefully) to update collisionHitboxes
     * */
    public void updateHitboxes() {
        //-----reset if bounding box list on anim data got recently updated-----
        if (this.animData.getBoundingBoxesRecentlyUpdated()) {
            for (RiftLibCollisionHitbox<T> collisionHitbox : this.collisionHitboxes.values()) {
                this.multiHitboxUser.getWorld().removeEntityDangerously(collisionHitbox);
            }
            this.collisionHitboxes.clear();
            this.collisionHitboxesByTag.clear();
        }

        //-----create collision hitboxes from bounding boxes-----
        for (AnimatedBoundingBox animatedBoundingBox : this.animData.getAnimatedBoundingBoxes()) {
            if (!animatedBoundingBox.canDoCollisions()) continue;
            if (!this.collisionHitboxes.containsKey(animatedBoundingBox.getName())) {
                RiftLibCollisionHitbox<T> toCreate = new RiftLibCollisionHitbox<>(this.multiHitboxUser, animatedBoundingBox);

                this.collisionHitboxes.put(animatedBoundingBox.getName(), toCreate);
                for (String tag : animatedBoundingBox.getTags()) {
                    this.collisionHitboxesByTag.computeIfAbsent(tag, key -> new ArrayList<>()).add(toCreate);
                }
            }
        }

        //-----tick all already existing collision hitboxes-----
        for (RiftLibCollisionHitbox<T> collisionHitbox : this.collisionHitboxes.values()) {
            collisionHitbox.onUpdate();

            //(server side only) sync serverside collision hitboxes to client
            if (!this.multiHitboxUser.getWorld().isRemote) {
                ServerProxy.HITBOX_MESSAGE_WRAPPER.sendToAllTracking(
                        new RiftLibSyncHitboxEntityId(this.multiHitboxUser.getMultiHitboxUser(), collisionHitbox),
                        this.multiHitboxUser.getMultiHitboxUser()
                );
            }
        }

        //-----tick all already existing offense hitboxes-----
        if (!this.multiHitboxUser.getWorld().isRemote) {
            for (RiftLibOffenseHitbox<T> offenseHitbox : this.offenseHitboxes.values()) {
                offenseHitbox.onUpdate();
            }
        }
    }

    /**
     * Required because this is how proxy entities as collisionHitboxes are dealt with in vanilla
     * */
    public Entity[] getHitboxesAsArray() {
        return this.collisionHitboxes.values().toArray(Entity[]::new);
    }

    @NotNull
    public RiftLibCollisionHitbox<T> getCollisionHitboxByName(@NotNull String name) {
        IHitbox<T> hitbox = this.collisionHitboxes.get(name);
        if (!(hitbox instanceof RiftLibCollisionHitbox<T> collisionHitbox)) {
            throw new IllegalArgumentException("Given name " + name + " does not correspond to any collision hitbox on this entity!");
        }
        return collisionHitbox;
    }

    public boolean hasCollisionHitboxes() {
        return !this.collisionHitboxes.isEmpty();
    }

    public boolean hasCollisionHitboxByName(@NotNull String name) {
        return this.collisionHitboxes.containsKey(name);
    }

    @NotNull
    public List<RiftLibCollisionHitbox<T>> getCollisionHitboxesByTag(@NotNull String tagName) {
        List<RiftLibCollisionHitbox<T>> hitboxList = this.collisionHitboxesByTag.get(tagName);
        if (hitboxList == null) {
            throw new IllegalArgumentException("Given tag " + tagName + " does not correspond to any tags for collision collisionHitboxes on this entity!");
        }
        return hitboxList.stream().map(new Function<IHitbox<T>, RiftLibCollisionHitbox<T>>() {
                    @Override
                    public RiftLibCollisionHitbox<T> apply(IHitbox<T> tiHitbox) {
                        return (RiftLibCollisionHitbox<T>) tiHitbox;
                    }
                }).toList();
    }

    public boolean createOffenseHitboxByName(@NotNull String name) {
        for (AnimatedBoundingBox animatedBoundingBox : this.animData.getAnimatedBoundingBoxes()) {
            if (!animatedBoundingBox.getName().equals(name)) continue;
            this.createOffenseHitbox(animatedBoundingBox);
            return true;
        }
        return false;
    }

    public boolean createOffenseHitboxesByTag(@NotNull String tagName) {
        if (!this.animData.getAnimatedBoundingBoxesByTag().containsKey(tagName)) return false;

        List<AnimatedBoundingBox> animatedBoundingBoxes = this.animData.getAnimatedBoundingBoxesByTag().get(tagName);
        for (AnimatedBoundingBox animatedBoundingBox : animatedBoundingBoxes) {
            this.createOffenseHitbox(animatedBoundingBox);
        }
        return true;
    }

    private void createOffenseHitbox(@NotNull AnimatedBoundingBox animatedBoundingBox) {
        RiftLibOffenseHitbox<T> offenseHitbox = new RiftLibOffenseHitbox<>(this.multiHitboxUser, animatedBoundingBox);
        this.offenseHitboxes.put(animatedBoundingBox.getName(), offenseHitbox);
        for (String tag : animatedBoundingBox.getTags()) {
            this.offenseHitboxesByTag.computeIfAbsent(tag, key -> new ArrayList<>()).add(offenseHitbox);
        }
    }

    public boolean removeOffenseHitboxByName(@NotNull String name) {
        RiftLibOffenseHitbox<T> offenseHitbox = this.offenseHitboxes.remove(name);
        if (offenseHitbox == null) return false;
        offenseHitbox.kill();
        for (List<RiftLibOffenseHitbox<T>> offenseHitboxList : this.offenseHitboxesByTag.values()) {
            offenseHitboxList.remove(offenseHitbox);
        }
        return true;
    }

    public boolean removeOffenseHitboxesByTag(@NotNull String tagName) {
        List<RiftLibOffenseHitbox<T>> offenseHitboxList = this.offenseHitboxesByTag.remove(tagName);
        if (offenseHitboxList == null) return false;

        for (RiftLibOffenseHitbox<T> offenseHitbox : offenseHitboxList) {
            offenseHitbox.kill();
            this.offenseHitboxes.remove(offenseHitbox.getBoundingBox().getName());
        }
        return true;
    }
}
