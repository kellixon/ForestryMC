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
package forestry.climatology.gui;

import net.minecraft.entity.player.InventoryPlayer;

import forestry.climatology.tiles.TileHygroregulator;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.TankWidget;

public class GuiHygroregulator extends GuiForestryTitled<ContainerHygroregulator> {
	private final TileHygroregulator tile;

	public GuiHygroregulator(InventoryPlayer inventory, TileHygroregulator tile) {
		super(Constants.TEXTURE_PATH_GUI + "/hygroregulator.png", new ContainerHygroregulator(inventory, tile), tile);
		this.tile = tile;

		widgetManager.add(new TankWidget(this.widgetManager, 104, 17, 0));
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
	}
}
