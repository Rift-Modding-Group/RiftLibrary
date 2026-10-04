package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoCube;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.geo.GeoQuad;
import anightdazingzoroark.riftlib.geo.GeoVertex;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.block.AnimatedBlockParticleFace;
import anightdazingzoroark.riftlib.util.MatrixStack;
import anightdazingzoroark.riftlib.util.TriFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public abstract class GeoBlockRenderer<A extends AnimatedBlockStateHolder> implements IGeoRenderer<A> {
    @NotNull
    private final AnimatedGeoModel<A> modelProvider;
    @NotNull
    private final TriFunction<World, BlockPos, IBlockState, A> holderCreator;

    static {
        AnimationController.addModelFetcher(object -> {
            if (object instanceof AnimatedBlockStateHolder holder) {
                GeoBlockRenderer<?> renderer = getRenderer(holder.getBlockState().getBlock());
                if (renderer != null) return (IAnimatableModel) renderer.getGeoModelProvider();
            }
            return null;
        });
    }

    protected GeoBlockRenderer(@NotNull AnimatedGeoModel<A> modelProvider, @NotNull TriFunction<World, BlockPos, IBlockState, A> holderCreator) {
        this.modelProvider = modelProvider;
        this.holderCreator = holderCreator;
    }

    public A createHolder(World world, BlockPos pos, IBlockState state) {
        return Objects.requireNonNull(this.holderCreator.apply(world, pos, state));
    }

    /**
     * Resolves the animated model texture used by vanilla block particles for a block state.
     * The temporary holder has no world and exists only while client models are prepared.
     * */
    public ResourceLocation getParticleTexture(IBlockState state) {
        A holder = this.createHolder(null, BlockPos.ORIGIN, state);
        String path = this.modelProvider.getTextureLocation(holder);
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        return new ResourceLocation(this.modelProvider.getModId(), path);
    }

    /**
     * Divides every visible model face into texture-aligned particle sections and records its transformed surface position.
     * */
    public List<AnimatedBlockParticleFace> createParticleFaces(World world, BlockPos pos, IBlockState state) {
        A holder = this.createHolder(world, pos, state);
        GeoModel model = this.modelProvider.getModel(holder);
        int[] textureSize = model.getTextureSize();
        TextureAtlasSprite texture = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        List<AnimatedBlockParticleFace> faces = new ArrayList<>();
        MatrixStack matrices = new MatrixStack();
        float scale = holder.getAnimationData().getScale();
        matrices.scale(scale, scale, scale);

        EnumFacing facing = EnumFacing.NORTH;
        if (state.getPropertyKeys().contains(BlockHorizontal.FACING)) facing = state.getValue(BlockHorizontal.FACING);
        else if (state.getPropertyKeys().contains(BlockDirectional.FACING)) facing = state.getValue(BlockDirectional.FACING);
        if (facing == EnumFacing.SOUTH) matrices.rotateY((float) Math.PI);
        else if (facing == EnumFacing.WEST) matrices.rotateY((float) Math.PI / 2F);
        else if (facing == EnumFacing.EAST) matrices.rotateY((float) Math.PI * 1.5F);
        else if (facing == EnumFacing.UP) matrices.rotateX((float) Math.PI / 2F);
        else if (facing == EnumFacing.DOWN) matrices.rotateX((float) -Math.PI / 2F);

        for (GeoBone bone : model.getTopLevelBones()) {
            this.collectParticleFaces(bone, matrices, texture, textureSize[0], textureSize[1], faces);
        }
        return faces;
    }

    private void collectParticleFaces(
            GeoBone bone, MatrixStack matrices, TextureAtlasSprite texture,
            int textureWidth, int textureHeight, List<AnimatedBlockParticleFace> faces
    ) {
        matrices.push();
        matrices.translate(bone);
        matrices.moveToPivot(bone);
        matrices.rotate(bone);
        matrices.scale(bone);
        matrices.moveBackFromPivot(bone);

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.childCubes) {
                matrices.push();
                matrices.moveToPivot(cube);
                matrices.rotate(cube);
                matrices.moveBackFromPivot(cube);
                for (GeoQuad quad : cube.getGeoQuads()) {
                    GeoVertex[] vertices = quad.geoVertices();
                    float minU = 1F;
                    float maxU = 0F;
                    float minV = 1F;
                    float maxV = 0F;
                    for (GeoVertex vertex : vertices) {
                        minU = Math.min(minU, vertex.textureU);
                        maxU = Math.max(maxU, vertex.textureU);
                        minV = Math.min(minV, vertex.textureV);
                        maxV = Math.max(maxV, vertex.textureV);
                    }
                    if (maxU <= minU || maxV <= minV) continue;

                    Vector3f topLeft = null;
                    Vector3f topRight = null;
                    Vector3f bottomLeft = null;
                    Vector3f bottomRight = null;
                    for (GeoVertex vertex : vertices) {
                        Vector4f transformed = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1F);
                        matrices.getModelMatrix().transform(transformed);
                        Vector3f position = new Vector3f(transformed.getX(), transformed.getY(), transformed.getZ());
                        if (vertex.textureU == minU && vertex.textureV == minV) topLeft = position;
                        else if (vertex.textureU == maxU && vertex.textureV == minV) topRight = position;
                        else if (vertex.textureU == minU && vertex.textureV == maxV) bottomLeft = position;
                        else if (vertex.textureU == maxU && vertex.textureV == maxV) bottomRight = position;
                    }
                    if (topLeft == null || topRight == null || bottomLeft == null || bottomRight == null) continue;

                    Vector3f horizontal = new Vector3f(topRight);
                    Vector3f vertical = new Vector3f(bottomLeft);
                    horizontal.sub(topLeft);
                    vertical.sub(topLeft);
                    if (horizontal.lengthSquared() <= 0F || vertical.lengthSquared() <= 0F) continue;

                    int columns = Math.max(1, (int) Math.ceil((maxU - minU) * textureWidth / 4F));
                    int rows = Math.max(1, (int) Math.ceil((maxV - minV) * textureHeight / 4F));
                    Vector3f normal = new Vector3f(quad.getNormal().getX(), quad.getNormal().getY(), quad.getNormal().getZ());
                    matrices.getNormalMatrix().transform(normal);
                    normal.normalize();
                    float particleScale = Math.clamp((horizontal.length() / columns + vertical.length() / rows) / 0.4F, 0.35F, 1.1F);
                    faces.add(new AnimatedBlockParticleFace(
                            texture,
                            minU,
                            maxU,
                            minV,
                            maxV,
                            topLeft,
                            topRight,
                            bottomLeft,
                            bottomRight,
                            normal,
                            columns,
                            rows,
                            particleScale
                    ));
                }
                matrices.pop();
            }
        }
        if (!bone.childBonesAreHiddenToo()) {
            for (GeoBone childBone : bone.childBones) {
                this.collectParticleFaces(childBone, matrices, texture, textureWidth, textureHeight, faces);
            }
        }
        matrices.pop();
    }

    /**
     * Associates rendering, holder creation, and generated particle models with a block. Call during ModelRegistryEvent.
     * Automatically disables vanilla model rendering and opaque/full-cube rendering properties.
     * */
    public static <A extends AnimatedBlockStateHolder> void registerBlockRenderer(Block block, GeoBlockRenderer<A> renderer) {
        AnimatedBlockRegistry.INSTANCE.addRenderer(block, renderer);
    }

    public static GeoBlockRenderer<?> getRenderer(Block block) {
        return AnimatedBlockRegistry.INSTANCE.getRenderer(block);
    }

    /**
     * Override for models extending beyond a single block. Bounds are in world coordinates.
     * */
    @NotNull
    public AxisAlignedBB getRenderBoundingBox(IBlockState state, BlockPos pos) {
        return new AxisAlignedBB(pos);
    }

    public double getRenderDistanceSquared() {
        return 4096;
    }

    public void render(A holder, double x, double y, double z, float partialTicks) {
        GeoModel model = this.modelProvider.getModel(holder);
        this.modelProvider.setClientAnimations(holder);
        this.modelProvider.createAndUpdateAnimatedLocators(holder);

        int light = holder.getWorld().getCombinedLight(holder.getPos(), holder.getBlockState().getLightValue(holder.getWorld(), holder.getPos()));
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, light & 65535, light >> 16);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.01, z + 0.5);
        float scaleValue = holder.getAnimationData().getScale();
        GlStateManager.scale(scaleValue, scaleValue, scaleValue);

        IBlockState state = holder.getBlockState();
        EnumFacing facing = EnumFacing.NORTH;
        if (state.getPropertyKeys().contains(BlockHorizontal.FACING)) facing = state.getValue(BlockHorizontal.FACING);
        else if (state.getPropertyKeys().contains(BlockDirectional.FACING)) facing = state.getValue(BlockDirectional.FACING);
        switch (facing) {
            case SOUTH -> GlStateManager.rotate(180, 0, 1, 0);
            case WEST -> GlStateManager.rotate(90, 0, 1, 0);
            case EAST -> GlStateManager.rotate(270, 0, 1, 0);
            case UP -> GlStateManager.rotate(90, 1, 0, 0);
            case DOWN -> GlStateManager.rotate(90, -1, 0, 0);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(this.getTextureLocation(holder));
        Color color = this.getRenderColor(holder, partialTicks);
        this.render(model, holder, partialTicks, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GlStateManager.popMatrix();
    }

    @Override
    public AnimatedGeoModel<A> getGeoModelProvider() {
        return this.modelProvider;
    }
}
