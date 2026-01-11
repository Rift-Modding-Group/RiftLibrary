package anightdazingzoroark.riftlib;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.example.client.renderer.armor.GreenArmorRenderer;
import anightdazingzoroark.example.client.renderer.armor.SatelliteDishHelmetRenderer;
import anightdazingzoroark.example.client.renderer.entity.*;
import anightdazingzoroark.example.client.renderer.tile.MerryGoRoundRenderer;
import anightdazingzoroark.example.client.renderer.tile.SprinklerRenderer;
import anightdazingzoroark.example.entity.*;
import anightdazingzoroark.example.entity.hitboxLinker.DragonHitboxLinker;
import anightdazingzoroark.example.entity.hitboxLinker.FlyingPufferfishHitboxLinker;
import anightdazingzoroark.example.entity.ridePosLinker.DragonRidePosLinker;
import anightdazingzoroark.example.item.GreenArmorItem;
import anightdazingzoroark.example.item.SatelliteDishHelmet;
import anightdazingzoroark.example.ui.HelloWorldUI;
import anightdazingzoroark.riftlib.animation.ItemAnimationTicker;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitboxRenderer;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoReplacedEntityRenderer;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffect;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffectRegistry;
import anightdazingzoroark.riftlib.ui.RiftLibUI;
import anightdazingzoroark.riftlib.ui.RiftLibUIRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy extends ServerProxy {
    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        RenderingRegistry.registerEntityRenderingHandler(EntityHitbox.class, new EntityHitboxRenderer.Factory());
        MinecraftForge.EVENT_BUS.register(new ParticleTicker());
        MinecraftForge.EVENT_BUS.register(new ItemAnimationTicker());
        MinecraftForge.EVENT_BUS.register(new RiftLibSoundEffectRegistry());

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            //uis
            RiftLibUIRegistry.registerUI("helloWorld", HelloWorldUI.class);

            //linkers
            RiftLibLinkerRegistry.registerEntityHitboxLinker(DragonEntity.class, new DragonHitboxLinker());
            RiftLibLinkerRegistry.registerDynamicRidePosLinker(DragonEntity.class, new DragonRidePosLinker());
            RiftLibLinkerRegistry.registerEntityHitboxLinker(FlyingPufferfishEntity.class, new FlyingPufferfishHitboxLinker());

            //entity renderers
            RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(FlyingPufferfishEntity.class, FlyingPufferfishRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(BombProjectile.class, BombProjectileRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(AlarmClock.class, AlarmClockRenderer::new);

            //armor renderer
            GeoArmorRenderer.registerArmorRenderer(GreenArmorItem.class, new GreenArmorRenderer());
            GeoArmorRenderer.registerArmorRenderer(SatelliteDishHelmet.class, new SatelliteDishHelmetRenderer());

            //block renderers
            ClientRegistry.bindTileEntitySpecialRenderer(MerryGoRoundTileEntity.class, new MerryGoRoundRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(SprinklerTileEntity.class, new SprinklerRenderer());

            //sound effects
            RiftLibSoundEffectRegistry.registerSoundEffect(
                    "entity.creeper.primed",
                    new RiftLibSoundEffect("bombLaunch")
            );
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

    @SideOnly(Side.CLIENT)
    @Override
    public <T extends RiftLibMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        if (messageContext.side.isServer()) super.handleMessage(message, messageContext);
        else {
            Minecraft.getMinecraft().addScheduledTask(() -> message.executeOnClient(
                    Minecraft.getMinecraft(),
                    message,
                    Minecraft.getMinecraft().player,
                    messageContext
            ));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void showUI(String id, NBTTagCompound nbtTagCompound, int x, int y, int z) {
        RiftLibUI ui = RiftLibUIRegistry.createUI(id, nbtTagCompound, x, y, z);
        if (ui != null) Minecraft.getMinecraft().displayGuiScreen(ui);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void spawnParticle(String name, double x, double y, double z) {
        //get particle builder
        ParticleBuilder builder = RiftLibParticleHelper.getParticleBuilder(name);

        //create an emitter
        if (builder != null) {
            RiftLibParticleEmitter emitter = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, x, y, z);
            ParticleTicker.EMITTER_LIST.add(emitter);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void spawnParticle(String name, double x, double y, double z, double rotationX, double rotationY) {
        //get particle builder
        ParticleBuilder builder = RiftLibParticleHelper.getParticleBuilder(name);

        //create an emitter
        if (builder != null) {
            RiftLibParticleEmitter emitter = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, x, y, z, rotationX, rotationY);
            ParticleTicker.EMITTER_LIST.add(emitter);
        }
    }
}