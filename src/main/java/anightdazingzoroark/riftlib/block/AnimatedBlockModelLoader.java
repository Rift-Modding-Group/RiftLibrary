package anightdazingzoroark.riftlib.block;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;

/**
 * Loads generated particle models for block states associated with animated renderers.
 * */
@SideOnly(Side.CLIENT)
public class AnimatedBlockModelLoader implements ICustomModelLoader {
    @Override
    public boolean accepts(@NonNull ResourceLocation modelLocation) {
        return AnimatedBlockRegistry.INSTANCE.getModelTexture(modelLocation) != null;
    }

    @Override
    public IModel loadModel(@NonNull ResourceLocation modelLocation) {
        ResourceLocation particleTexture = AnimatedBlockRegistry.INSTANCE.getModelTexture(modelLocation);
        if (particleTexture == null) throw new IllegalArgumentException("No generated animated block model for " + modelLocation);
        return new IModel() {
            @Override
            public Collection<ResourceLocation> getTextures() {
                return Collections.singleton(particleTexture);
            }

            @Override
            public IBakedModel bake(@NonNull IModelState state, @NonNull VertexFormat format, @NonNull Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
                List<BakedQuad> quads = Collections.emptyList();
                Map<EnumFacing, List<BakedQuad>> faceQuads = new EnumMap<>(EnumFacing.class);
                for (EnumFacing face : EnumFacing.values()) faceQuads.put(face, quads);
                return new SimpleBakedModel(
                        quads,
                        faceQuads,
                        false,
                        false,
                        textureGetter.apply(particleTexture),
                        ItemCameraTransforms.DEFAULT,
                        ItemOverrideList.NONE
                );
            }
        };
    }

    @Override
    public void onResourceManagerReload(@NonNull IResourceManager resourceManager) {}
}
