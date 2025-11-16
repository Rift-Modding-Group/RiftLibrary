package anightdazingzoroark;

import anightdazingzoroark.example.block.tile.BotariumTileEntity;
import anightdazingzoroark.example.block.tile.FertilizerTileEntity;
import anightdazingzoroark.example.client.renderer.armor.PotatoArmorRenderer;
import anightdazingzoroark.example.client.renderer.entity.BombProjectileRenderer;
import anightdazingzoroark.example.client.renderer.entity.DragonRenderer;
import anightdazingzoroark.example.client.renderer.entity.FlyingPufferfishRenderer;
import anightdazingzoroark.example.client.renderer.entity.ReplacedCreeperRenderer;
import anightdazingzoroark.example.client.renderer.tile.BotariumTileRenderer;
import anightdazingzoroark.example.client.renderer.tile.FertilizerTileRenderer;
import anightdazingzoroark.example.entity.BombProjectile;
import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.example.entity.FlyingPufferfishEntity;
import anightdazingzoroark.example.entity.ReplacedCreeperEntity;
import anightdazingzoroark.example.entity.hitboxLinker.DragonHitboxLinker;
import anightdazingzoroark.example.entity.hitboxLinker.FlyingPufferfishHitboxLinker;
import anightdazingzoroark.example.entity.ridePosLinker.DragonRidePosLinker;
import anightdazingzoroark.example.item.PotatoArmorItem;
import anightdazingzoroark.riftlib.RiftLibLinkerRegistry;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitboxRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoReplacedEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class ClientProxy extends ServerProxy {
    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        RenderingRegistry.registerEntityRenderingHandler(EntityHitbox.class, new EntityHitboxRenderer.Factory());

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            //linkers
            RiftLibLinkerRegistry.registerEntityHitboxLinker(DragonEntity.class, new DragonHitboxLinker());
            RiftLibLinkerRegistry.registerDynamicRidePosLinker(DragonEntity.class, new DragonRidePosLinker());
            RiftLibLinkerRegistry.registerEntityHitboxLinker(FlyingPufferfishEntity.class, new FlyingPufferfishHitboxLinker());

            //entity renderers
            RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(FlyingPufferfishEntity.class, FlyingPufferfishRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(BombProjectile.class, BombProjectileRenderer::new);

            GeoArmorRenderer.registerArmorRenderer(PotatoArmorItem.class, new PotatoArmorRenderer());

            ClientRegistry.bindTileEntitySpecialRenderer(BotariumTileEntity.class, new BotariumTileRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(FertilizerTileEntity.class, new FertilizerTileRenderer());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            ReplacedCreeperRenderer creeperRenderer = new ReplacedCreeperRenderer(renderManager);
            renderManager.entityRenderMap.put(EntityCreeper.class, creeperRenderer);
            GeoReplacedEntityRenderer.registerReplacedEntity(ReplacedCreeperEntity.class, creeperRenderer);
        }
    }
}
