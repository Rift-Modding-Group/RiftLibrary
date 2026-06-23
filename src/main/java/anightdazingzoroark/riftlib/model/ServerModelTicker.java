package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class ServerModelTicker {
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != TickEvent.Phase.END) return;

        for (Entity entity : new ArrayList<>(event.world.getLoadedEntityList())) {
            if (!entity.isEntityAlive() || !(entity instanceof IAnimatable<?>)) continue;
            this.update((IAnimatable<?>) entity);
        }

        for (TileEntity tile : new ArrayList<>(event.world.loadedTileEntityList)) {
            if (tile.isInvalid() || !(tile instanceof IAnimatable<?>)) continue;
            this.update((IAnimatable<?>) tile);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) return;

        ServerModelRegistry.clearServerModels();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void update(IAnimatable<?> animatable) {
        AnimatedGeoModel model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return;
        model.setServerAnimations(animatable);
    }
}
