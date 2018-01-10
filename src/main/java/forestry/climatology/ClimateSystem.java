package forestry.climatology;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateChunk;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.api.climate.IClimateModifier;
import forestry.climatology.api.climate.IClimateSystem;
import forestry.climatology.climate.ClimateChunkManager;
import forestry.core.climate.AbsentClimateState;
import forestry.core.climate.ClimateManager;
import forestry.core.utils.World2ObjectMap;

public class ClimateSystem implements IClimateSystem {
	private static final Comparator<IClimateModifier> MODIFIER_COMPARATOR = Comparator.comparingInt(IClimateModifier::getPriority);

	public static final ClimateSystem INSTANCE = new ClimateSystem();

	private final World2ObjectMap<ClimateChunkManager> managers;
	private final List<IClimateModifier> modifiers;

	private ClimateSystem() {
		managers = new World2ObjectMap<>(ClimateChunkManager::new);
		modifiers = new ArrayList<>();
	}


	@Override
	public void addLogic(long pos, IClimateLogic logic) {
		World world = logic.getWorldObj();
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			return;
		}
		chunkManager.setLogic(pos, logic);
	}

	@Override
	public void removeLogic(long pos, IClimateLogic logic) {
		World world = logic.getWorldObj();
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			return;
		}
		chunkManager.removeLogic(pos);
	}

	@Nullable
	@Override
	public IClimateLogic getLogic(World world, BlockPos pos) {
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			return null;
		}
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		return chunkManager.getLogic(ChunkPos.asLong(chunkX, chunkZ));
	}

	@Override
	public IClimateState getChunkState(World world, BlockPos pos) {
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			return AbsentClimateState.INSTANCE;
		}
		IClimateChunk chunk = chunkManager.getChunk(pos);
		if (chunk == null) {
			return AbsentClimateState.INSTANCE;
		}
		return chunk.getState();
	}

	@Override
	public IClimateState getChunkOrBiomeState(World world, BlockPos pos) {
		IClimateState state = getChunkState(world, pos);
		if (state.isPresent()) {
			return state;
		}
		return ClimateManager.getInstance().getBiomeState(world, pos);
	}

	@Nullable
	@Override
	public IClimateChunk getChunk(World world, long pos) {
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			return null;
		}
		return chunkManager.getChunk(pos);
	}

	@Override
	public IClimateChunk getOrCreateChunk(World world, long pos) {
		ClimateChunkManager chunkManager = managers.get(world);
		if (chunkManager == null) {
			throw new IllegalStateException();
		}
		return chunkManager.getOrCreateChunk(pos);
	}

	@Override
	public void registerModifier(IClimateModifier modifier) {
		modifiers.add(modifier);
		modifiers.sort(MODIFIER_COMPARATOR);
	}

	@Override
	public Collection<IClimateModifier> getModifiers() {
		return modifiers;
	}
}
