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
package forestry.climatology.climate;

import forestry.api.climate.ClimateType;
import forestry.climatology.api.climate.source.IClimateSourceCircuitable;
import forestry.climatology.api.climate.source.IClimateSourceProxy;

public abstract class ClimateSourceCircuitable<P extends IClimateSourceProxy> extends ClimateSource<P> implements IClimateSourceCircuitable<P> {
	protected float changeMultiplierTemperature = 1.0f;
	protected float rangeMultiplierTemperature = 1.0f;
	protected float changeMultiplierHumidity = 1.0f;
	protected float rangeMultiplierHumidity = 1.0f;
	protected float energyChange = 1.0F;

	public ClimateSourceCircuitable(P proxy, float change, float boundModifier, ClimateSourceType sourceType) {
		super(proxy, change, boundModifier, sourceType);
	}

	@Override
	public void changeSourceConfig(ClimateType type, float changeChange, float rangeChange, float energyChange) {
		if (type == ClimateType.TEMPERATURE) {
			changeMultiplierTemperature += changeChange;
			rangeMultiplierTemperature += rangeChange;
		} else {
			changeMultiplierHumidity += changeChange;
			rangeMultiplierHumidity += rangeChange;
		}
		this.energyChange += energyChange;
	}

	public float getChangeMultiplier(ClimateType type) {
		float changeMultiplier;
		if (type == ClimateType.TEMPERATURE) {
			changeMultiplier = changeMultiplierTemperature;
		} else {
			changeMultiplier = changeMultiplierHumidity;
		}
		return changeMultiplier;
	}

	@Override
	protected float getBoundModifier(ClimateType type) {
		float rangeMultiplier;
		if (type == ClimateType.TEMPERATURE) {
			rangeMultiplier = rangeMultiplierTemperature;
		} else {
			rangeMultiplier = rangeMultiplierHumidity;
		}
		return super.getBoundModifier(type) * rangeMultiplier;
	}

	@Override
	protected float getChange(ClimateType type) {
		return super.getChange(type) * getChangeMultiplier(type);
	}
}
