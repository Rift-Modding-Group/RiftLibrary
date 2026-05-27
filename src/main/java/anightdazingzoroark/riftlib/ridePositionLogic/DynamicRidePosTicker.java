package anightdazingzoroark.riftlib.ridePositionLogic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * This client-side ticker is for smoothing out rider positions on the client on IDynamicRideUsers.
 * */
public class DynamicRidePosTicker {
    @SideOnly(Side.CLIENT)
    public static class Client {
        private final Map<Entity, EntityPositionSnapshot> originalPassengerPositions = new IdentityHashMap<>();

        @SubscribeEvent
        public void onRenderTick(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                World world = Minecraft.getMinecraft().world;
                if (world == null) return;

                for (Entity entity : world.getLoadedEntityList()) {
                    if (!(entity instanceof IDynamicRideUser<?> dynamicRideUser)) continue;

                    EntityLivingBase rideUser = dynamicRideUser.getDynamicRideUser();
                    if (!rideUser.isBeingRidden() || dynamicRideUser.ridePosList().isEmpty()) continue;

                    Entity controller = rideUser.getControllingPassenger();
                    Vec3d controllerRenderPos = dynamicRideUser.ridePosList().getControllerRenderPos(event.renderTickTime);
                    List<Vec3d> passengerRenderPositions = dynamicRideUser.ridePosList().getPassengerRenderPositions(event.renderTickTime);

                    for (Entity passenger : rideUser.getPassengers()) {
                        Vec3d ridePos = null;
                        if (controller != null && controller.equals(passenger)) {
                            ridePos = controllerRenderPos;
                        }
                        else {
                            int passengerPosIndex = dynamicRideUser.getPassengerPositionIndex(passenger);
                            if (passengerPosIndex >= 0 && passengerPosIndex < passengerRenderPositions.size()) {
                                ridePos = passengerRenderPositions.get(passengerPosIndex);
                            }
                        }

                        if (ridePos == null) continue;

                        this.originalPassengerPositions.putIfAbsent(passenger, new EntityPositionSnapshot(passenger));
                        dynamicRideUser.applyRidePosition(passenger, ridePos);
                        passenger.lastTickPosX = passenger.posX;
                        passenger.lastTickPosY = passenger.posY;
                        passenger.lastTickPosZ = passenger.posZ;
                        passenger.prevPosX = passenger.posX;
                        passenger.prevPosY = passenger.posY;
                        passenger.prevPosZ = passenger.posZ;
                    }
                }
            }
            else if (event.phase == TickEvent.Phase.END) {
                for (Map.Entry<Entity, EntityPositionSnapshot> entry : this.originalPassengerPositions.entrySet()) {
                    Entity passenger = entry.getKey();
                    EntityPositionSnapshot snapshot = entry.getValue();
                    passenger.setPosition(snapshot.posX, snapshot.posY, snapshot.posZ);
                    passenger.lastTickPosX = snapshot.lastTickPosX;
                    passenger.lastTickPosY = snapshot.lastTickPosY;
                    passenger.lastTickPosZ = snapshot.lastTickPosZ;
                    passenger.prevPosX = snapshot.prevPosX;
                    passenger.prevPosY = snapshot.prevPosY;
                    passenger.prevPosZ = snapshot.prevPosZ;
                }
                this.originalPassengerPositions.clear();
            }
        }
    }

    private static class EntityPositionSnapshot {
        private final double posX;
        private final double posY;
        private final double posZ;
        private final double lastTickPosX;
        private final double lastTickPosY;
        private final double lastTickPosZ;
        private final double prevPosX;
        private final double prevPosY;
        private final double prevPosZ;

        private EntityPositionSnapshot(Entity entity) {
            this.posX = entity.posX;
            this.posY = entity.posY;
            this.posZ = entity.posZ;
            this.lastTickPosX = entity.lastTickPosX;
            this.lastTickPosY = entity.lastTickPosY;
            this.lastTickPosZ = entity.lastTickPosZ;
            this.prevPosX = entity.prevPosX;
            this.prevPosY = entity.prevPosY;
            this.prevPosZ = entity.prevPosZ;
        }
    }
}
