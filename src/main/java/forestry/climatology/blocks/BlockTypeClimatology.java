package forestry.climatology.blocks;

import forestry.climatology.tiles.TileClimatiser;
import forestry.climatology.tiles.TileFan;
import forestry.climatology.tiles.TileHabitatFormer;
import forestry.climatology.tiles.TileHeater;
import forestry.climatology.tiles.TileHygroregulator;
import forestry.core.blocks.IBlockTypeCustom;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.TileForestry;

public enum BlockTypeClimatology implements IBlockTypeCustom {
	HABITAT_FORMER(TileHabitatFormer.class, "habitat_former"),
	HYGROREGULATOR(TileHygroregulator.class, "hygroregulator"),
	HEATER(TileHeater.class, "heater"),
	FAN(TileFan.class, "fan");

	private final IMachineProperties machineProperties;

	<T extends TileForestry> BlockTypeClimatology(Class<T> teClass, String name) {
		if (TileClimatiser.class.isAssignableFrom(teClass)) {
			this.machineProperties = new ClimatiserProperties<>(teClass, name);
		} else {
			this.machineProperties = new MachineProperties<>(teClass, name);
		}
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return machineProperties;
	}

	@Override
	public String getName() {
		return getMachineProperties().getName();
	}
}
