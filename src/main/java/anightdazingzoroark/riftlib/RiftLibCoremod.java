package anightdazingzoroark.riftlib;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;

@IFMLLoadingPlugin.Name(RiftLib.ModID)
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class RiftLibCoremod implements IEarlyMixinLoader, IFMLLoadingPlugin {
    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("riftlib.default.mixin.json");
        //return new ArrayList<>();
    }

    public void onMixinConfigQueued(String mixinConfig) {
        System.out.println(mixinConfig+" has been queued");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
