package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.EnumBlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows the standard hit particle path to use generated animated block particle models.
 * */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {
    @Redirect(
            method = "addBlockHitEffects(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/state/IBlockState;getRenderType()Lnet/minecraft/util/EnumBlockRenderType;")
    )
    private EnumBlockRenderType riftlib$getParticleRenderType(IBlockState state) {
        return AnimatedBlockRegistry.INSTANCE.isRegistered(state.getBlock()) ? EnumBlockRenderType.MODEL : state.getRenderType();
    }
}
