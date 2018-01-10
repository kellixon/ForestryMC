package forestry.climatology.api.climate;

import javax.annotation.Nullable;

import forestry.api.climate.IClimateState;
import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;

public interface IClimateChunk extends INbtWritable, INbtReadable {
	@Nullable
	IClimateLogic getLogic();

	void setLogic(IClimateLogic logic);

	void cleanLogic();

	long getPos();

	IClimateState getState();
}
