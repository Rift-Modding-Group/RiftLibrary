package anightdazingzoroark.riftlib.mixin;

import anightdazingzoroark.riftlib.block.AnimatedBlockRegistry;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * updates animated block indexes after individual changes and decoded chunk sections.
 * */
@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Nullable
    @WrapMethod(method = "setBlockState")
    private IBlockState riftlib$updateAnimatedBlock(BlockPos pos, IBlockState state, Operation<IBlockState> original) {
        // Wrap the entire call so cancellable replacement implementations also update the index.
        IBlockState previous = original.call(pos, state);
        if (previous == null) return null;
        Chunk chunk = (Chunk) (Object) this;
        if (!chunk.getWorld().isRemote || !chunk.isLoaded()) return previous;
        if (!AnimatedBlockRegistry.INSTANCE.isRegistered(state.getBlock()) && !AnimatedBlockRegistry.INSTANCE.isRegistered(previous.getBlock())) return previous;
        AnimatedBlockRegistry.INSTANCE.update(chunk.getWorld(), pos, chunk.getBlockState(pos));
        return previous;
    }

    @WrapMethod(method = "read")
    private void riftlib$readAnimatedBlocks(PacketBuffer buffer, int sections, boolean fullChunk, Operation<Void> original) {
        original.call(buffer, sections, fullChunk);
        AnimatedBlockRegistry.INSTANCE.scan((Chunk) (Object) this, fullChunk ? -1 : sections);
    }
}
