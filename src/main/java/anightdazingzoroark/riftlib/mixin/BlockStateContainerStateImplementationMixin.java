package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;

import net.minecraft.block.Block;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

/**
 * intercept state queries as well, so subclass overrides and forge extended states use the registered renderer's defaults
 * */
@Mixin(targets = "net.minecraft.block.state.BlockStateContainer$StateImplementation")
public abstract class BlockStateContainerStateImplementationMixin {
    @Shadow
    @Final
    private Block block;

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private EnumBlockRenderType riftlib$getRenderType(EnumBlockRenderType original) {
        return AnimatedBlockRegistry.INSTANCE.isRegistered(this.block) ? EnumBlockRenderType.INVISIBLE : original;
    }

    @ModifyReturnValue(method = {"isOpaqueCube", "isFullCube", "isFullBlock"}, at = @At("RETURN"))
    private boolean riftlib$getCubeProperties(boolean original) {
        return original && !AnimatedBlockRegistry.INSTANCE.isRegistered(this.block);
    }
}
