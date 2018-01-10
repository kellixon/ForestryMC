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

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.api.core.IErrorLogic;
import forestry.climatology.tiles.TileHygroregulator;
import forestry.core.climate.ClimateStates;
import forestry.core.errors.EnumErrorCode;
import forestry.core.fluids.FilteredTank;
import forestry.core.recipes.HygroregulatorRecipe;

public class ClimateSourceHygroregulator extends ClimateSourceCircuitable<TileHygroregulator> {

	@Nullable
	private HygroregulatorRecipe currentRecipe;

	public ClimateSourceHygroregulator(TileHygroregulator proxy, float boundModifier) {
		super(proxy, 0.0F, boundModifier, ClimateSourceType.BOTH);
	}

	@Override
	protected void beforeWork() {
		currentRecipe = null;
		createRecipe();
		if (currentRecipe != null) {
			float tempChange = currentRecipe.tempChange;
			float humidChange = currentRecipe.humidChange;
			if (tempChange > 0) {
				setTemperatureMode(ClimateSourceMode.POSITIVE);
			} else if (tempChange < 0) {
				setTemperatureMode(ClimateSourceMode.NEGATIVE);
			} else {
				setTemperatureMode(ClimateSourceMode.NONE);
			}
			if (humidChange > 0) {
				setHumidityMode(ClimateSourceMode.POSITIVE);
			} else if (humidChange < 0) {
				setHumidityMode(ClimateSourceMode.NEGATIVE);
			} else {
				setHumidityMode(ClimateSourceMode.NONE);
			}
		} else {
			setTemperatureMode(ClimateSourceMode.NONE);
			setHumidityMode(ClimateSourceMode.NONE);
		}
	}

	@Override
	public boolean canWork(IClimateState currentState, ClimateSourceType oppositeType) {
		createRecipe();
		FilteredTank liquidTank = proxy.getLiquidTank();
		IErrorLogic errorLogic = proxy.getErrorLogic();
		if (currentRecipe != null && liquidTank.drainInternal(currentRecipe.liquid.amount, false) != null) {
			errorLogic.setCondition(false, EnumErrorCode.NO_RESOURCE_LIQUID);
			return true;
		}
		errorLogic.setCondition(true, EnumErrorCode.NO_RESOURCE_LIQUID);
		return false;
	}

	@Override
	protected void removeResources(IClimateState currentState, @Nullable ClimateSourceType oppositeType) {
		FilteredTank liquidTank = proxy.getLiquidTank();
		liquidTank.drainInternal(currentRecipe.liquid.amount, true);
	}

	@Override
	protected IClimateState getChange(ClimateSourceType type, IClimateState target, IClimateState currentState) {
		float temperature = 0.0F;
		float humidity = 0.0F;
		if (type.canChangeHumidity()) {
			humidity += currentRecipe.humidChange * getChangeMultiplier(ClimateType.HUMIDITY);
		}
		if (type.canChangeTemperature()) {
			temperature += currentRecipe.tempChange * getChangeMultiplier(ClimateType.TEMPERATURE);
		}
		return ClimateStates.extendedOf(temperature, humidity);
	}

	private void createRecipe() {
		if (currentRecipe == null) {
			FilteredTank liquidTank = proxy.getLiquidTank();
			FluidStack fluid = liquidTank.getFluid();
			if (fluid != null && fluid.amount > 0) {
				currentRecipe = proxy.getRecipe(fluid);
			} else {
				currentRecipe = null;
			}
		}
	}

}
