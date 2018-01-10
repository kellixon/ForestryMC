package forestry.climatology;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import forestry.api.climate.IClimateState;
import forestry.api.core.ForestryAPI;
import forestry.climatology.api.climate.IClimateChunk;
import forestry.climatology.api.climate.IClimateLogic;

public class ClimateChunk implements IClimateChunk {
	private final long pos;
	@Nullable
	private IClimateLogic logic;
	private IClimateState climateState = ForestryAPI.states.absent();

	public ClimateChunk(long pos) {
		this.pos = pos;
	}

	@Override
	@Nullable
	public IClimateLogic getLogic() {
		return logic;
	}

	@Override
	public void setLogic(IClimateLogic logic) {
		this.logic = logic;
	}

	@Override
	public void cleanLogic() {
		logic = null;
	}

	@Override
	public long getPos() {
		return pos;
	}

	@Override
	public IClimateState getState() {
		if (logic == null) {
			return climateState;
		}
		return logic.getState();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return nbt;
	}
}
