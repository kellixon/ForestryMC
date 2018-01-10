package forestry.climatology.api.climate;

import javax.annotation.Nullable;
import java.util.Collection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import forestry.api.climate.IClimateState;

public interface IClimateSystem {
	void addLogic(long pos, IClimateLogic logic);

	void removeLogic(long pos, IClimateLogic logic);

	@Nullable
	IClimateLogic getLogic(World world, BlockPos pos);

	@Nullable
	IClimateChunk getChunk(World world, long pos);

	IClimateChunk getOrCreateChunk(World world, long pos);

	void registerModifier(IClimateModifier modifier);

	Collection<IClimateModifier> getModifiers();

	IClimateState getChunkState(World world, BlockPos pos);

	IClimateState getChunkOrBiomeState(World world, BlockPos pos);
}
