package anightdazingzoroark.example.registry;

import anightdazingzoroark.example.entity.*;
import anightdazingzoroark.riftlib.RiftLib;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class EntityRegistry {
    @SubscribeEvent
    public void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;

        //entity registerVariable
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(DragonEntity.class)
                .name("dragon")
                .id(new ResourceLocation(RiftLib.ModID, "dragon"), id++)
                .tracker(160, 2, false)
                .build()
        );
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(FlyingPufferfishEntity.class)
                .name("flying_pufferfish")
                .id(new ResourceLocation(RiftLib.ModID, "flying_pufferfish"), id++)
                .tracker(160, 2, false)
                .build()
        );
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(BombProjectile.class)
                .name("bomb_projectile")
                .id(new ResourceLocation(RiftLib.ModID, "bomb_projectile"), id++)
                .tracker(160, 1, true)
                .build()
        );
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(AlarmClockEntity.class)
                .name("alarm_clock")
                .id(new ResourceLocation(RiftLib.ModID, "alarm_clock"), id++)
                .tracker(160, 2, false)
                .build()
        );
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(GoKartEntity.class)
                .name("go_kart")
                .id(new ResourceLocation(RiftLib.ModID, "go_kart"), id++)
                .tracker(160, 2, false)
                .build()
        );
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(AvianRunnerEntity.class)
                .name("avian_runner")
                .id(new ResourceLocation(RiftLib.ModID, "avian_runner"), id++)
                .tracker(160, 2, false)
                .build()
        );

        //egg registry
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(new ResourceLocation(RiftLib.ModID, "dragon"), 0x980d0d, 0xca7824);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(new ResourceLocation(RiftLib.ModID, "flying_pufferfish"), 0xffae00, 0xbfc700);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(new ResourceLocation(RiftLib.ModID, "alarm_clock"), 0x0000ff, 0xffffff);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(new ResourceLocation(RiftLib.ModID, "go_kart"), 0xbf2c12, 0xb3b3b3);
        net.minecraftforge.fml.common.registry.EntityRegistry.registerEgg(new ResourceLocation(RiftLib.ModID, "avian_runner"), 0x383a88, 0xc7ccd2);
    }
}
