package anightdazingzoroark.example.animatedblock;

import anightdazingzoroark.riftlib.block.AnimatedBlockStateHolder;
import anightdazingzoroark.riftlib.core.controller.AnimationController;
import anightdazingzoroark.riftlib.core.controller.AnimationControllerState;
import anightdazingzoroark.riftlib.core.manager.AnimationDataBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;

public class AnimatedSprinklerBlock extends AnimatedBlockStateHolder {
    public AnimatedSprinklerBlock(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
    }

    @Override
    public void initializeAnimationData(@NonNull AnimationDataBlock animationData) {
        animationData.addAnimation("animation.sprinkler.spinning");
        animationData.addAnimation("animation.sprinkler.hose_water_zero");
        animationData.addAnimation("animation.sprinkler.hose_water_one");
        animationData.addAnimation("animation.sprinkler.hose_water_two");
        animationData.addAnimation("animation.sprinkler.hose_water_three");
    }
}
