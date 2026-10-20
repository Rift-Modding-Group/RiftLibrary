package anightdazingzoroark.example.item;

import anightdazingzoroark.example.entity.AlarmClockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class AlarmClockItem extends Item {
    @Override
    @NotNull
    public EnumActionResult onItemUse(
            @NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos,
            @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ
    ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos spawnPos = pos.offset(facing);
        if (!player.canPlayerEdit(spawnPos, facing, stack)) return EnumActionResult.FAIL;

        AlarmClockEntity clock = new AlarmClockEntity(world);
        clock.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.rotationYaw, 0f);
        if (!world.getCollisionBoxes(clock, clock.getEntityBoundingBox()).isEmpty() || !world.checkNoEntityCollision(clock.getEntityBoundingBox())) {
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            if (stack.hasDisplayName()) clock.setCustomNameTag(stack.getDisplayName());
            if (!world.spawnEntity(clock)) return EnumActionResult.FAIL;
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
