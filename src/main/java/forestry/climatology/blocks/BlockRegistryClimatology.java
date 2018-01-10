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
package forestry.climatology.blocks;

import forestry.core.blocks.BlockRegistry;
import forestry.core.items.ItemBlockForestry;

public class BlockRegistryClimatology extends BlockRegistry {

	public final BlockHabitatformer habitatformer;

	public final BlockClimatiser hygroregulator;

	public final BlockClimatiser heater;
	public final BlockClimatiser fan;

	public BlockRegistryClimatology() {
		habitatformer = new BlockHabitatformer();
		registerBlock(habitatformer, new ItemBlockForestry<>(habitatformer), "habitatformer");

		hygroregulator = new BlockClimatiser(BlockTypeClimatology.HYGROREGULATOR);
		registerBlock(hygroregulator, new ItemBlockForestry<>(hygroregulator), "habitatformer_hygroregulator");

		heater = new BlockClimatiser(BlockTypeClimatology.HEATER);
		registerBlock(heater, new ItemBlockForestry<>(heater), "habitatformer_heater");

		fan = new BlockClimatiser(BlockTypeClimatology.FAN);
		registerBlock(fan, new ItemBlockForestry<>(fan), "habitatformer_fan");
	}

}
