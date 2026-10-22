package anightdazingzoroark.riftlib.renderers.geo;

import anightdazingzoroark.riftlib.geo.GeoBone;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implement on the default armor ModelBiped passed to Forge's armor model hook.
 * Return the wearer's already animated bones; armor then follows their complete
 * model-space pose instead of calculating a second vanilla animation cycle.
 */
public interface IGeoArmorPoseProvider {
    @Nullable
    GeoBone getArmorPoseBone(@NotNull EntityLivingBase wearer, @NotNull GeoArmorRenderer.ArmorBodyPart part);
}