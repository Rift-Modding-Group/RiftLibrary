package anightdazingzoroark.riftlib.ui;

import anightdazingzoroark.riftlib.ClientProxy;
import anightdazingzoroark.riftlib.ServerProxy;
import anightdazingzoroark.riftlib.internalMessage.RiftLibOpenUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class RiftLibUIHelper {
    public static void showUI(EntityPlayer player, String id, int x, int y, int z) {
        showUI(player, id, new NBTTagCompound(), x, y, z);
    }

    public static void showUI(EntityPlayer player, String id, RiftLibUIData data, int x, int y, int z) {
        showUI(player, id, data.getFinalNBT(), x, y, z);
    }

    public static void showUI(EntityPlayer player, String id, NBTTagCompound nbtTagCompound, int x, int y, int z) {
        if (player instanceof EntityPlayerMP) ServerProxy.MESSAGE_WRAPPER.sendTo(new RiftLibOpenUI(id, nbtTagCompound, x, y, z), (EntityPlayerMP) (player));
        else ClientProxy.showUI(id, nbtTagCompound, x, y, z);
    }
}
