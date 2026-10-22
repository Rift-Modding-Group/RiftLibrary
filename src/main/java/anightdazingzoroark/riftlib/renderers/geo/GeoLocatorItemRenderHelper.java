package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoLocator;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.item.EntityItemRenderContext;
import anightdazingzoroark.riftlib.util.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders items in the entity's current model space, including bone animation.
 */
public class GeoLocatorItemRenderHelper {
    public static <A extends Entity> void renderItemsAtLocators(
            @NotNull IGeoEntityItemRenderer<A> renderer, @NotNull GeoModel model,
            @NotNull A entity, float partialTicks
    ) {
        for (GeoLocator locator : model.getAllLocators()) {
            ItemStack stack = renderer.getItemForLocator(entity, locator.name, partialTicks);
            if (stack.isEmpty()) continue;
            Matrix4f transform = getLocatorTransform(locator);
            if (transform == null) continue;

            ItemCameraTransforms.TransformType transformType = renderer.getItemTransformTypeForLocator(
                    entity, locator.name, stack, partialTicks
            );
            Matrix4f itemTransform = renderer.getItemRenderTransformForLocator(entity, locator.name, stack, partialTicks);
            if (itemTransform != null) transform.mul(itemTransform);
            boolean leftHanded = switch (transformType) {
                case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> true;
                default -> false;
            };
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            EntityLivingBase livingEntity = null;
            if (entity instanceof EntityLivingBase living) livingEntity = living;
            IBakedModel itemModel = renderItem.getItemModelWithOverrides(stack, entity.world, livingEntity);

            try (ItemRenderState state = ItemRenderState.capture(); EntityItemRenderContext context = new EntityItemRenderContext(entity, locator.name)) {
                FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
                matrix.put(new float[]{
                        transform.m00, transform.m10, transform.m20, transform.m30,
                        transform.m01, transform.m11, transform.m21, transform.m31,
                        transform.m02, transform.m12, transform.m22, transform.m32,
                        transform.m03, transform.m13, transform.m23, transform.m33
                });
                matrix.flip();
                GlStateManager.multMatrix(matrix);
                GlStateManager.enableRescaleNormal();
                GlStateManager.disableCull();
                renderItem.renderItemModel(stack, itemModel, transformType, leftHanded);
            }
        }
    }

    /**
     * Matches LayerHeldItem's placement for a standard biped arm. Attach the
     * locator at the animated arm pivot, with no extra hand rotation or offset.
     * Vanilla's (-1, -1, 1) model scale, -90 degree X rotation and 180 degree Y
     * rotation combine to -90 degrees around X in the geometry's Y-up space.
     * The signed 1/16, 2/16, -10/16 grip offset is then applied in item space.
     * Select the corresponding THIRD_PERSON display transform separately.
     */
    @NotNull
    public static Matrix4f getVanillaHandTransform(@NotNull EnumHandSide handSide) {
        MatrixStack matrices = new MatrixStack();
        matrices.rotateX((float) (-Math.PI / 2D));
        float horizontalOffset = handSide == EnumHandSide.RIGHT ? 1f / 16f : -1f / 16f;
        matrices.translate(horizontalOffset, 2f / 16f, -10f / 16f);
        return new Matrix4f(matrices.getModelMatrix());
    }

    /**
     * Uses the same pivot, rotation order, translation signs, and scale as
     * {@link IGeoRenderer#renderRecursively}. Hidden branches have no attachment.
     */
    @Nullable
    public static Matrix4f getLocatorTransform(@NotNull GeoLocator locator) {
        if (locator.isHidden()) return null;
        List<GeoBone> ancestors = new ArrayList<>();
        for (GeoBone bone = locator.parent; bone != null; bone = bone.parent) {
            if (bone.childBonesAreHiddenToo()) return null;
            ancestors.add(bone);
        }

        MatrixStack matrices = new MatrixStack();
        for (GeoBone bone : ancestors.reversed()) {
            matrices.translate(bone);
            matrices.moveToPivot(bone);
            matrices.rotate(bone);
            matrices.scale(bone);
            matrices.moveBackFromPivot(bone);
        }
        Vector3f position = locator.getPosition();
        matrices.translate(position.x / 16f, position.y / 16f, position.z / 16f);
        Vector3f rotation = locator.getRotation();
        matrices.rotateZ(rotation.z);
        matrices.rotateY(rotation.y);
        matrices.rotateX(rotation.x);
        return new Matrix4f(matrices.getModelMatrix());
    }

    private record ItemRenderState(
            int texture, boolean rescaleNormal, boolean blend, boolean cull, int cullFace,
            int blendSourceRgb, int blendDestinationRgb, int blendSourceAlpha, int blendDestinationAlpha,
            int alphaFunction, float alphaReference, @NotNull FloatBuffer color
    ) implements AutoCloseable {
        @NotNull
        private static ItemRenderState capture() {
            FloatBuffer color = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, color);
            ItemRenderState state = new ItemRenderState(
                    GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),
                    GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL), GL11.glIsEnabled(GL11.GL_BLEND),
                    GL11.glIsEnabled(GL11.GL_CULL_FACE),
                    GL11.glGetInteger(GL11.GL_CULL_FACE_MODE),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), GL11.glGetInteger(GL14.GL_BLEND_DST_RGB),
                    GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA),
                    GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC), GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF), color
            );
            GlStateManager.pushMatrix();
            return state;
        }

        @Override
        public void close() {
            GlStateManager.popMatrix();
            GlStateManager.bindTexture(this.texture);
            if (this.rescaleNormal) GlStateManager.enableRescaleNormal();
            else GlStateManager.disableRescaleNormal();
            if (this.blend) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            if (this.cull) GlStateManager.enableCull();
            else GlStateManager.disableCull();
            GlStateManager.cullFace(switch (this.cullFace) {
                case GL11.GL_FRONT -> GlStateManager.CullFace.FRONT;
                case GL11.GL_FRONT_AND_BACK -> GlStateManager.CullFace.FRONT_AND_BACK;
                default -> GlStateManager.CullFace.BACK;
            });
            GlStateManager.tryBlendFuncSeparate(this.blendSourceRgb, this.blendDestinationRgb, this.blendSourceAlpha, this.blendDestinationAlpha);
            GlStateManager.alphaFunc(this.alphaFunction, this.alphaReference);
            GlStateManager.color(this.color.get(0), this.color.get(1), this.color.get(2), this.color.get(3));
        }
    }
}
