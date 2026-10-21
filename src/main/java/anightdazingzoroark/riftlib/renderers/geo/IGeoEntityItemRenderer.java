package anightdazingzoroark.riftlib.renderers.geo;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;

/**
 * Item attachments shared by living and nonliving entity renderers.
 */
public interface IGeoEntityItemRenderer<A extends Entity> {
    /**
     * Selects the item attached to a named locator for this frame.
     * Return {@link ItemStack#EMPTY} to leave the locator empty.
     */
    @NotNull
    default ItemStack getItemForLocator(@NotNull A entity, @NotNull String locatorName, float partialTicks) {
        return ItemStack.EMPTY;
    }

    /**
     * Selects the item's JSON display transform, applied after the locator pose.
     * Left hand transform types also enable vanilla's left hand mirroring.
     */
    @NotNull
    default ItemCameraTransforms.TransformType getItemTransformTypeForLocator(
            @NotNull A entity, @NotNull String locatorName, @NotNull ItemStack stack, float partialTicks
    ) {
        return ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }

    /**
     * Item placement, applied after the animated locator pose and before
     * the item's JSON display transform. Values use blocks and renderer axes
     * (X points towards the entity's right, Y up, Z back).
     * Third-person hand displays default to the corresponding vanilla grip
     * transform and expect a locator at the animated arm pivot. Other displays
     * render directly at the locator. Override and return null to disable grip
     * placement, or return a custom matrix to replace it.
     */
    @Nullable
    default Matrix4f getItemRenderTransformForLocator(
            @NotNull A entity, @NotNull String locatorName, @NotNull ItemStack stack, float partialTicks
    ) {
        return switch (this.getItemTransformTypeForLocator(entity, locatorName, stack, partialTicks)) {
            case THIRD_PERSON_RIGHT_HAND -> GeoLocatorItemRenderHelper.getVanillaHandTransform(EnumHandSide.RIGHT);
            case THIRD_PERSON_LEFT_HAND -> GeoLocatorItemRenderHelper.getVanillaHandTransform(EnumHandSide.LEFT);
            default -> null;
        };
    }
}
