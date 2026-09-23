package anightdazingzoroark.riftlib.proxy;

import anightdazingzoroark.example.client.renderer.armor.GreenArmorRenderer;
import anightdazingzoroark.example.client.renderer.armor.SatelliteDishHelmetRenderer;
import anightdazingzoroark.example.client.renderer.block.MerryGoRoundRenderer;
import anightdazingzoroark.example.client.renderer.block.SprinklerRenderer;
import anightdazingzoroark.example.client.renderer.entity.*;
import anightdazingzoroark.example.entity.*;
import anightdazingzoroark.example.registry.BlockRegistry;
import anightdazingzoroark.example.registry.ItemRegistry;
import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import anightdazingzoroark.riftlib.block.AnimatedBlockModelLoader;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRendererTicker;
import anightdazingzoroark.riftlib.RiftLibMod;
import anightdazingzoroark.riftlib.hitbox.RiftLibCollisionHitbox;
import anightdazingzoroark.riftlib.hitbox.EntityHitboxRenderer;
import anightdazingzoroark.riftlib.hitbox.HitboxTicker;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.particle.RiftLibParticleHelper;
import anightdazingzoroark.riftlib.ray.RayTicker;
import anightdazingzoroark.riftlib.renderers.debug.WorldSpaceBoundingBoxRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoItemRendererTicker;
import anightdazingzoroark.riftlib.ridePositionLogic.DynamicRidePosTicker;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffect;
import anightdazingzoroark.riftlib.sounds.RiftLibSoundEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.model.ModelLoaderRegistry;
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
        ModelLoaderRegistry.registerLoader(new AnimatedBlockModelLoader());
        RenderingRegistry.registerEntityRenderingHandler(RiftLibCollisionHitbox.class, new EntityHitboxRenderer.Factory());
        MinecraftForge.EVENT_BUS.register(new ParticleTicker());
        MinecraftForge.EVENT_BUS.register(new RiftLibSoundEffectRegistry());
        MinecraftForge.EVENT_BUS.register(new GeoItemRendererTicker());
        MinecraftForge.EVENT_BUS.register(new GeoBlockRendererTicker());
        MinecraftForge.EVENT_BUS.register(new AnimatedBlockRegistry.Events());
        MinecraftForge.EVENT_BUS.register(new HitboxTicker.Client());
        MinecraftForge.EVENT_BUS.register(new RayTicker.Client());
        MinecraftForge.EVENT_BUS.register(new WorldSpaceBoundingBoxRenderer());
        MinecraftForge.EVENT_BUS.register(new DynamicRidePosTicker.Client());

        //these will only happen in a deobfuscated environment
        if (RiftLibMod.DEOBF_ENVIRONMENT && !RiftLibMod.DISABLE_IN_DEV) {
            //entity renderers
            RenderingRegistry.registerEntityRenderingHandler(DragonEntity.class, DragonRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(FlyingPufferfishEntity.class, FlyingPufferfishRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(BombProjectile.class, BombProjectileRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(AlarmClockEntity.class, AlarmClockRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(GoKartEntity.class, GoKartRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(AvianRunnerEntity.class, AvianRunnerRenderer::new);

            //block renderers
            GeoBlockRenderer.registerBlockRenderer(BlockRegistry.MERRY_GO_ROUND_BLOCK, new MerryGoRoundRenderer());
            GeoBlockRenderer.registerBlockRenderer(BlockRegistry.SPRINKLER_BLOCK, new SprinklerRenderer());

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
            GreenArmorRenderer greenArmorRenderer = new GreenArmorRenderer();
            GeoArmorRenderer.registerArmorRenderer(ItemRegistry.GREEN_HEAD, greenArmorRenderer);
            GeoArmorRenderer.registerArmorRenderer(ItemRegistry.GREEN_CHEST, greenArmorRenderer);
            GeoArmorRenderer.registerArmorRenderer(ItemRegistry.GREEN_LEGGINGS, greenArmorRenderer);
            GeoArmorRenderer.registerArmorRenderer(ItemRegistry.GREEN_BOOTS, greenArmorRenderer);
            GeoArmorRenderer.registerArmorRenderer(ItemRegistry.SATELLITE_DISH_HELMET, new SatelliteDishHelmetRenderer());
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
