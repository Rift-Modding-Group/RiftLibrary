package anightdazingzoroark.riftlib;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.*;

public class RiftLibCoremod implements IFMLLoadingPlugin {
    @Override
    @Nullable
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    @Nullable
    public String getModContainerClass() {
        return null;
    }

    @Override
    @Nullable
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    @Nullable
    public String getAccessTransformerClass() {
        return null;
    }
}
