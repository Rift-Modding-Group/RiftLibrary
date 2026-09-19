package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * updates animated block indexes after individual changes and decoded chunk sections.
 * */
@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void riftlib$updateAnimatedBlock(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> callback) {
        if (callback.getReturnValue() == null) return;
        Chunk chunk = (Chunk) (Object) this;
        if (!chunk.getWorld().isRemote || !chunk.isLoaded()) return;
        if (!AnimatedBlockRegistry.INSTANCE.isRegistered(state.getBlock()) && !AnimatedBlockRegistry.INSTANCE.isRegistered(callback.getReturnValue().getBlock())) return;
        AnimatedBlockRegistry.INSTANCE.update(chunk.getWorld(), pos, chunk.getBlockState(pos));
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void riftlib$readAnimatedBlocks(PacketBuffer buffer, int sections, boolean fullChunk, CallbackInfo callback) {
        AnimatedBlockRegistry.INSTANCE.scan((Chunk) (Object) this, fullChunk ? 0xffff : sections);
    }
}
