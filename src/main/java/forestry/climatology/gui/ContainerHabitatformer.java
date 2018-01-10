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

import forestry.climatology.tiles.TileHabitatFormer;
import forestry.core.gui.ContainerSocketed;
import forestry.core.network.packets.PacketGuiUpdate;

public class ContainerHabitatformer extends ContainerSocketed<TileHabitatFormer> {

	public ContainerHabitatformer(InventoryPlayer playerInventory, TileHabitatFormer tile) {
		super(tile, playerInventory, 8, 84);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		PacketGuiUpdate packet = new PacketGuiUpdate(tile);
		sendPacketToListeners(packet);
	}

}
