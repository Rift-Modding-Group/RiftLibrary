package anightdazingzoroark.riftlibrary.example;

import anightdazingzoroark.riftlibrary.example.entity.RedDragonEntity;
import anightdazingzoroark.riftlibrary.main.RiftLibraryMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class CommonListener {
    @SubscribeEvent
    public void onRegisterEntities(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(RedDragonEntity.class)
                .name("red_dragon")
                .id(new ResourceLocation(RiftLibraryMod.MODID, "red_dragon"), id++)
                .tracker(160, 2, false)
                .build()
        );

        //egg registry
        EntityRegistry.registerEgg(new ResourceLocation(RiftLibraryMod.MODID, "red_dragon"), 0x980d0d, 0xca7824);
    }
}
