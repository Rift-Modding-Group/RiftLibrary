package anightdazingzoroark.example.animatedblock;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;

public class AnimatedMerryGoRoundBlock extends AnimatedBlockStateHolder {
    public AnimatedMerryGoRoundBlock(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataBlock animationData) {
        animationData.addAnimationController(new AnimationController<AnimatedMerryGoRoundBlock, AnimationDataBlock>(
                this, "rotate", "default",
                new AnimationControllerState<AnimationDataBlock>("default")
                        .addAnimation("animation.merry_go_round.rotate")
        ));
    }
}
