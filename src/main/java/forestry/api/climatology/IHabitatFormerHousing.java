/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.climatology;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.IErrorLogicSource;
import forestry.climatology.api.climate.IClimateLogic;

public interface IHabitatFormerHousing extends IErrorLogicSource, IClimateHousing{
	
	/**
	 * @return The current tempreture as an enum.
	 */
	EnumTemperature getTemperature();
	
	/**
	 * @return The current humidity as an enum.
	 */
	EnumHumidity getHumidity();
	
	/**
	 * @return The current tempreture as an float. Range: 0.0F ~ 2.0F
	 */
	float getExactTemperature();
	
	/**
	 * @return The current humidity as an float. Range: 0.0F ~ 2.0F
	 */
	float getExactHumidity();

	IClimateLogic getLogic();
}
