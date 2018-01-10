/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.climatology.api.climate.source;

import java.util.Collection;

import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateLogic;

/**
 * @since 5.8
 */
public interface IClimateSourceLogic {

	void onAddSource(IClimateSource source);

	void onRemoveSource(IClimateSource source);

	/**
	 * @return All climate sources of this container.
	 */
	Collection<IClimateSource> getClimateSources();

	/**
	 * The climate of the {@link IClimateLogic} that results out of the {@link IClimateState} modifications of the {@link IClimateSource}s can not be higher than this bound.
	 */
	IClimateState getBoundaryUp();

	/**
	 * The climate of the {@link IClimateLogic} that results out of the {@link IClimateState} modifications of the {@link IClimateSource}s can not be lower than this bound.
	 */
	IClimateState getBoundaryDown();

	double getSizeModifier();

	/**
	 * Calculates the up and down boundary and the size modifier.
	 */
	void recalculateBoundaries();

}
