package anightdazingzoroark.example.client.renderer.tile;

import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.example.client.model.tile.SprinklerBlockModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoBlockRenderer;

public class SprinklerRenderer extends GeoBlockRenderer<SprinklerTileEntity> {
    public SprinklerRenderer() {
        super(new SprinklerBlockModel());
    }
}
