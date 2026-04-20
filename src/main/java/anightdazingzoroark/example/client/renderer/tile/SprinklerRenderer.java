package anightdazingzoroark.example.client.renderer.tile;

import anightdazingzoroark.example.block.tile.SprinklerTileEntity;
import anightdazingzoroark.example.client.model.tile.SprinklerBlockModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoTileEntityRenderer;

public class SprinklerRenderer extends GeoTileEntityRenderer<SprinklerTileEntity> {
    public SprinklerRenderer() {
        super(new SprinklerBlockModel());
    }
}
