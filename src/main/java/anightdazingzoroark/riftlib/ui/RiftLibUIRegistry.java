package anightdazingzoroark.riftlib.ui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class RiftLibUIRegistry {
    private static final Map<String, Class<? extends RiftLibUI>> FACTORIES = new HashMap<>();

    @SideOnly(Side.CLIENT)
    public static void registerUI(String name, Class<? extends RiftLibUI> uiClass) {
        FACTORIES.put(name, uiClass);
    }

    @SideOnly(Side.CLIENT)
    public static RiftLibUI createUI(String id, NBTTagCompound nbtTagCompound, int x, int y, int z) {
        try {
            return FACTORIES.get(id)
                    .getDeclaredConstructor(NBTTagCompound.class, int.class, int.class, int.class)
                    .newInstance(nbtTagCompound, x, y, z);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
