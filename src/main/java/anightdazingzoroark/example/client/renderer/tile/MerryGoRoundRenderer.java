package anightdazingzoroark.example.client.renderer.tile;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import anightdazingzoroark.example.client.model.tile.MerryGoRoundBlockModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoTileEntityRenderer;

public class MerryGoRoundRenderer extends GeoTileEntityRenderer<MerryGoRoundTileEntity> {
    public MerryGoRoundRenderer() {
        super(new MerryGoRoundBlockModel());
    }
}
