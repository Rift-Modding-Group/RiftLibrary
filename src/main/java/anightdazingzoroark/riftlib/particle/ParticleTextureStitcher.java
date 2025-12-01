package anightdazingzoroark.riftlib.particle;

import anightdazingzoroark.riftlib.resource.RiftLibCache;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ParticleTextureStitcher {
    public static final Map<ResourceLocation, TextureAtlasSprite> SPRITES = new HashMap<>();

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        HashMap<ResourceLocation, ParticleBuilder> particleBuilders = RiftLibCache.getInstance().getParticleBuilders();

        //first get all the texture parameters in every single particle file
        for (ParticleBuilder builder : particleBuilders.values()) {
            if (builder.texture == null) continue;
            //avoid re-registering the same sprite if multiple particles use it
            if (SPRITES.containsKey(builder.texture)) continue;

            TextureAtlasSprite sprite = event.getMap().registerSprite(builder.texture);
            SPRITES.put(builder.texture, sprite);
        }
    }
}
