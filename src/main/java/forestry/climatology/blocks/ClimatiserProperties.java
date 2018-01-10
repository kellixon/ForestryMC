package forestry.climatology.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import forestry.core.blocks.BlockBase;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.TileForestry;

public class ClimatiserProperties<T extends TileForestry> extends MachineProperties<T> {
	private static final AxisAlignedBB BOUNDING_BOX_NORTH = new AxisAlignedBB(0.1875F, 0.0F, 0.375F, 0.8125F, 0.875F, 1.0F);
	private static final AxisAlignedBB BOUNDING_BOX_SOUTH = new AxisAlignedBB(0.1875F, 0.0F, 0.0F, 0.8125F, 0.875F, 0.625F);
	private static final AxisAlignedBB BOUNDING_BOX_EAST = new AxisAlignedBB(0.0F, 0.0F, 0.1875F, 0.625F, 0.875F, 0.8125F);
	private static final AxisAlignedBB BOUNDING_BOX_WEST = new AxisAlignedBB(0.375F, 0.0F, 0.1875F, 1.0F, 0.875F, 0.8125F);

	ClimatiserProperties(Class<T> teClass, String name) {
		super(teClass, name);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state) {
		AxisAlignedBB boundingBox;
		switch (state.getValue(BlockBase.FACING)) {
			case NORTH:
				boundingBox = BOUNDING_BOX_NORTH;
				break;
			case SOUTH:
				boundingBox = BOUNDING_BOX_SOUTH;
				break;
			case EAST:
				boundingBox = BOUNDING_BOX_EAST;
				break;
			case WEST:
				boundingBox = BOUNDING_BOX_WEST;
				break;
			default:
				boundingBox = BOUNDING_BOX_NORTH;
				break;
		}
		return boundingBox;
	}
}
