package anightdazingzoroark.riftlib;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.example.client.renderer.armor.GreenArmorRenderer;
import anightdazingzoroark.example.client.renderer.entity.BombProjectileRenderer;
import anightdazingzoroark.example.client.renderer.entity.DragonRenderer;
import anightdazingzoroark.example.client.renderer.entity.FlyingPufferfishRenderer;
import anightdazingzoroark.example.client.renderer.entity.ReplacedCreeperRenderer;
import anightdazingzoroark.example.client.renderer.tile.MerryGoRoundRenderer;
import anightdazingzoroark.example.entity.BombProjectile;
import anightdazingzoroark.example.entity.DragonEntity;
import anightdazingzoroark.example.entity.FlyingPufferfishEntity;
import anightdazingzoroark.example.entity.ReplacedCreeperEntity;
import anightdazingzoroark.example.entity.hitboxLinker.DragonHitboxLinker;
import anightdazingzoroark.example.entity.hitboxLinker.FlyingPufferfishHitboxLinker;
import anightdazingzoroark.example.entity.ridePosLinker.DragonRidePosLinker;
import anightdazingzoroark.example.item.GreenArmorItem;
import anightdazingzoroark.example.ui.HelloWorldUI;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitboxRenderer;
import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.particle.ParticleBuilder;
import anightdazingzoroark.riftlib.particle.ParticleTicker;
import anightdazingzoroark.riftlib.particle.RiftLibParticleEmitter;
import anightdazingzoroark.riftlib.renderers.geo.GeoArmorRenderer;
import anightdazingzoroark.riftlib.renderers.geo.GeoReplacedEntityRenderer;
import anightdazingzoroark.riftlib.resource.RiftLibCache;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientProxy extends ServerProxy {
    public static final List<RiftLibParticleEmitter> EMITTER_LIST = new ArrayList<>();
    public static int EMITTER_ID = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        RenderingRegistry.registerEntityRenderingHandler(EntityHitbox.class, new EntityHitboxRenderer.Factory());
        MinecraftForge.EVENT_BUS.register(new ParticleTicker());

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

            //armor renderer
            GeoArmorRenderer.registerArmorRenderer(GreenArmorItem.class, new GreenArmorRenderer());

            //block renderers
            ClientRegistry.bindTileEntitySpecialRenderer(MerryGoRoundTileEntity.class, new MerryGoRoundRenderer());
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
        ParticleBuilder builder = this.getParticleBuilder(name);

        //create an emitter
        if (builder != null) {
            RiftLibParticleEmitter emitter = new RiftLibParticleEmitter(builder, Minecraft.getMinecraft().world, x, y, z);
            EMITTER_LIST.add(emitter);
        }
    }

    private ParticleBuilder getParticleBuilder(String name) {
        Collection<ParticleBuilder> particleBuilders = RiftLibCache.getInstance().getParticleBuilders().values();

        for (ParticleBuilder particleBuilder : particleBuilders) {
            if (particleBuilder.identifier != null && particleBuilder.identifier.equals(name)) return particleBuilder;
        }
        return null;
    }
}