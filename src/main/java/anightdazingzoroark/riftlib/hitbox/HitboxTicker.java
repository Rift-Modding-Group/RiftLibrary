package anightdazingzoroark.riftlib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * One issue I've had with the hitbox system in previous versions was how there's a good chance that
 * when an entity is removed from the world, so do its hitboxes, mainly from how hitboxes were ticked
 * from the entity. This should fix this by cleaning up orphaned hitboxes that couldn't be removed
 * when the parent was disappeared.
 * */
public class HitboxTicker {
    private static final int CLEANUP_INTERVAL = 20;

    @SubscribeEvent
    public void tickHitboxesFromParent(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntity() instanceof IMultiHitboxUser hitboxUser)) return;

        Entity[] parts = hitboxUser.getMultiHitboxUser().getParts();
        if (parts == null) return;

        for (Entity part : parts) {
            if (!(part instanceof EntityHitbox entityHitbox)) continue;
            entityHitbox.onUpdate();
        }
    }

    @SubscribeEvent
    public void cleanupOrphanedHitboxes(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.getTotalWorldTime() % CLEANUP_INTERVAL != 0) return;

        for (Entity entity : event.world.getLoadedEntityList()) {
            if (!(entity instanceof EntityHitbox entityHitbox)) continue;

            Entity parent = entityHitbox.getParentAsEntityLiving();
            if (parent != null && parent.isEntityAlive() && !parent.isDead) continue;

            event.world.removeEntityDangerously(entityHitbox);
        }
    }
}
