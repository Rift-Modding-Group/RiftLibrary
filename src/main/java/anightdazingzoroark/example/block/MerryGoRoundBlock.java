package anightdazingzoroark.example.block;

import anightdazingzoroark.example.block.tile.MerryGoRoundTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MerryGoRoundBlock extends Block implements ITileEntityProvider {
    public MerryGoRoundBlock() {
        super(Material.WOOD, MapColor.RED);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new MerryGoRoundTileEntity();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
