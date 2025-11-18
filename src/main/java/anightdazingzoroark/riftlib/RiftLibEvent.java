package anightdazingzoroark.riftlib;

import anightdazingzoroark.riftlib.ui.RiftLibUIHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RiftLibEvent {
    @SubscribeEvent
    public void openUITest(PlayerInteractEvent.LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();

        if (!heldItem.isEmpty() && heldItem.getItem() == Items.PAPER && !event.getWorld().isRemote) {
            RiftLibUIHelper.showUI(player, "helloWorld", (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }
}
