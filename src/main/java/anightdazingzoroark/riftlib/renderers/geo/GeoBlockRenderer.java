package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import anightdazingzoroark.riftlib.core.IAnimatableModel;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.util.Color;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import anightdazingzoroark.riftlib.util.TriFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

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
     * Associates both rendering and holder creation with a block. Call during client initialization.
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
