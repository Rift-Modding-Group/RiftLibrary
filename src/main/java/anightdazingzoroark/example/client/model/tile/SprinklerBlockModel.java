package anightdazingzoroark.example.client.model.tile;

import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.riftlib.RiftLib;
import anightdazingzoroark.riftlib.model.AnimatedGeoModel;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SprinklerBlockModel extends AnimatedGeoModel<SprinklerTileEntity> {
    @Override
    @NotNull
    public String getModId() {
        return RiftLib.ModID;
    }

    @Override
    public String getModelIdentifier(SprinklerTileEntity object) {
        return "geometry.sprinkler";
    }

    @Override
    public ResourceLocation getTextureLocation(SprinklerTileEntity object) {
        return new ResourceLocation(RiftLib.ModID, "textures/block/sprinkler.png");
    }

    @Override
    @NotNull
    public List<String> getAnimationIdentifiers(SprinklerTileEntity animatable) {
        return List.of(
                "animation.sprinkler.spinning",
                "animation.sprinkler.hose_water_zero",
                "animation.sprinkler.hose_water_one",
                "animation.sprinkler.hose_water_two",
                "animation.sprinkler.hose_water_three"
        );
    }
}
