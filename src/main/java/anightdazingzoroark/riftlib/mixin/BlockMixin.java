package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Block;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * applies animated rendering defaults to blocks associated with a GeoBlockRenderer
 * */
@Mixin(Block.class)
public abstract class BlockMixin {
    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private EnumBlockRenderType riftlib$getRenderType(EnumBlockRenderType original) {
        return AnimatedBlockRegistry.INSTANCE.isRegistered((Block) (Object) this) ? EnumBlockRenderType.INVISIBLE : original;
    }

    @ModifyReturnValue(method = {"isOpaqueCube", "isFullCube", "isFullBlock"}, at = @At("RETURN"))
    private boolean riftlib$getCubeProperties(boolean original) {
        return original && !AnimatedBlockRegistry.INSTANCE.isRegistered((Block) (Object) this);
    }
}
