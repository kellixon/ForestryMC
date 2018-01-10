package forestry.climatology.climate;

import javax.annotation.Nullable;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import forestry.climatology.ClimateChunk;
import forestry.climatology.api.climate.IClimateChunk;
import forestry.climatology.api.climate.IClimateLogic;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ClimateChunkManager {
	private final Map<Long, IClimateChunk> idToLogic = new Long2ObjectOpenHashMap<>(8192);

	public ClimateChunkManager(World world) {
	}

	@Nullable
	public IClimateChunk getChunk(long chunkPos) {
		return idToLogic.get(chunkPos);
	}

	@Nullable
	public IClimateChunk getChunk(BlockPos pos) {
		return getChunk(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
	}

	public IClimateChunk getOrCreateChunk(long chunkPos) {
		return idToLogic.computeIfAbsent(chunkPos, ClimateChunk::new);
	}

	public void setLogic(long chunkPos, IClimateLogic logic) {
		getOrCreateChunk(chunkPos).setLogic(logic);
	}

	@Nullable
	public IClimateLogic getLogic(long chunkPos) {
		IClimateChunk chunk = getChunk(chunkPos);
		if (chunk == null) {
			return null;
		}
		return chunk.getLogic();
	}

	public void removeLogic(long chunkPos) {
		IClimateChunk chunk = getChunk(chunkPos);
		if (chunk == null) {
			return;
		}
		chunk.cleanLogic();
	}
}
