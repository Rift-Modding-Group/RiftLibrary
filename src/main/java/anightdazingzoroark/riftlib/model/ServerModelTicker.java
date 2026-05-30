package anightdazingzoroark.riftlib.model;

import anightdazingzoroark.riftlib.core.IAnimatable;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ServerModelTicker {
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient()) return;
        if (event.phase != TickEvent.Phase.END) return;

        for (Entity entity : event.world.getLoadedEntityList()) {
            if (entity instanceof IAnimatable<?, ?> animatable) {
                this.update(animatable);
            }
        }

        for (TileEntity tileEntity : event.world.loadedTileEntityList) {
            if (tileEntity instanceof IAnimatable<?, ?> animatable) {
                this.update(animatable);
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) return;

        ServerModelRegistry.clearServerModels();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void update(IAnimatable<?, ?> animatable) {
        AnimatedGeoModel model = ServerModelRegistry.getServerModel(animatable);
        if (model == null) return;
        model.setServerAnimations(animatable);
    }
}
