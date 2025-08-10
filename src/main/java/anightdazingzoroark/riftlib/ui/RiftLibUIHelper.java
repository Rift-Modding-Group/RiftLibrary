package anightdazingzoroark.riftlib.ui;

import anightdazingzoroark.riftlib.message.RiftLibMessage;
import anightdazingzoroark.riftlib.message.RiftLibOpenUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class RiftLibUIHelper {
    public static Object SCREEN;

    public static void showUI(EntityPlayer player, RiftLibUI screen) {
        SCREEN = screen;
        if (player instanceof EntityPlayerSP) Minecraft.getMinecraft().displayGuiScreen((RiftLibUI) RiftLibUIHelper.SCREEN);
        else if (player instanceof EntityPlayerMP) RiftLibMessage.WRAPPER.sendTo(new RiftLibOpenUI(), (EntityPlayerMP) (player));
    }
}
