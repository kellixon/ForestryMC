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
package forestry.core.climate;

import forestry.api.climatology.IClimateHousing;
import forestry.climatology.api.climate.IClimateFactory;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.climate.ClimateLogic;

public class ClimateFactory implements IClimateFactory{

	@Override
	public IClimateLogic createContainer(IClimateHousing climatedRegion) {
		return new ClimateLogic(climatedRegion);
	}

}
