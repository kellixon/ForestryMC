/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.climatology.circuits;

import javax.annotation.Nullable;

import forestry.api.climate.ClimateType;
import forestry.climatology.api.climate.source.IClimateSource;
import forestry.climatology.api.climate.source.IClimateSourceCircuitable;
import forestry.climatology.api.climate.source.IClimateSourceProxy;
import forestry.climatology.tiles.TileHabitatFormer;
import forestry.core.circuits.Circuit;

public class CircuitHabitatFormer extends Circuit {
	private ClimateType type;
	private float changeChange;
	private float rangeChange;
	private float energyChange;

	public CircuitHabitatFormer(String uid, ClimateType type, float changeChange, float rangeChange, float energyChange) {
		super(uid);
		this.type = type;
		this.changeChange = changeChange;
		this.rangeChange = rangeChange;
		this.energyChange = energyChange;
	}

	@Override
	public boolean isCircuitable(Object tile) {
		return tile instanceof TileHabitatFormer;
	}

	@Override
	public void onInsertion(int slot, Object tile) {
		IClimateSourceProxy<?> proxy = getCircuitable(tile);
		if (proxy == null) {
			return;
		}
		IClimateSource source = proxy.getNode();
		if (!(source instanceof IClimateSourceCircuitable)) {
			return;
		}
		((IClimateSourceCircuitable) source).changeSourceConfig(type, changeChange, rangeChange, energyChange);
	}

	@Override
	public void onLoad(int slot, Object tile) {
		onInsertion(slot, tile);
	}

	@Override
	public void onRemoval(int slot, Object tile) {
		IClimateSourceProxy<?> proxy = getCircuitable(tile);
		if (proxy == null) {
			return;
		}
		IClimateSource source = proxy.getNode();
		if (!(source instanceof IClimateSourceCircuitable)) {
			return;
		}
		((IClimateSourceCircuitable) source).changeSourceConfig(type, -changeChange, -rangeChange, -energyChange);
	}

	@Override
	public void onTick(int slot, Object tile) {

	}

	@Nullable
	private IClimateSourceProxy getCircuitable(Object tile) {
		if (!isCircuitable(tile)) {
			return null;
		}
		return (IClimateSourceProxy) tile;
	}
}
