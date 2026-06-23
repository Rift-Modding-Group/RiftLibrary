package anightdazingzoroark.riftlib.hitbox;

import anightdazingzoroark.riftlib.model.AnimatedLocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This optional hitbox deals with attacks performed by IMultiHitboxUser instances
 * if they ever wanna deal with soulslike combat hitboxes
 * */
public class RiftLibOffenseHitbox<T extends IMultiHitboxUser<?>> implements IHitbox<T> {
    public final int hitboxId;
    @NotNull
    private final T parent;
    @NotNull
    private final AnimatedLocator hitboxLocator;
    //these are the final definitive scales of the hitbox and will be used for such
    public final float fixedWidth;
    public final float fixedHeight;
    private final Set<Integer> alreadyHitEntities = new HashSet<>();
    private boolean isDead;

    public RiftLibOffenseHitbox(int hitboxId, @NotNull T parent, @NotNull AnimatedLocator hitboxLocator, float width, float height) {
        this.hitboxId = hitboxId;
        this.parent = parent;
        this.hitboxLocator = hitboxLocator;
        this.fixedWidth = width;
        this.fixedHeight = height;
    }

    /**
     * Only ever update on server
     * */
    public void onUpdate() {
        //-----dont update further when dead-----
        if (this.isDead) return;

        //-----set aabb-----
        float[] finalSize = this.getHitboxSize();
        Vec3d finalPosition = this.getHitboxPosition();
        AxisAlignedBB hitboxAABB = new AxisAlignedBB(
                finalPosition.x - finalSize[0] / 2f, finalPosition.y - finalSize[1] / 2f, finalPosition.z - finalSize[0] / 2f,
                finalPosition.x + finalSize[0] / 2f, finalPosition.y + finalSize[1] / 2f, finalPosition.z + finalSize[0] / 2f
        );

        //-----check aabb collisions-----
        EntityLivingBase parentEntityLiving = this.getParent().getMultiHitboxUser();
        List<Entity> candidates = this.getParent().getWorld().getEntitiesWithinAABBExcludingEntity(parentEntityLiving, hitboxAABB);

        //-----apply damage to hit entities-----
        for (Entity target : candidates) {
            if (!this.alreadyHitEntities.add(target.getEntityId())) continue;
            parentEntityLiving.attackEntityAsMob(target);
        }
    }

    public void kill() {
        this.isDead = true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    @Override
    @NonNull
    public T getParent() {
        return this.parent;
    }

    @Override
    @NonNull
    public AnimatedLocator getHitboxLocator() {
        return this.hitboxLocator;
    }

    @Override
    public float[] getFixedSize() {
        return new float[]{this.fixedWidth, this.fixedHeight};
    }
}
