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
package forestry.climatology.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

import forestry.api.climate.IClimateState;
import forestry.api.climatology.IHabitatFormerHousing;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.core.network.ForestryPacket;
import forestry.core.network.IForestryPacketHandlerServer;
import forestry.core.network.IForestryPacketServer;
import forestry.core.network.PacketBufferForestry;
import forestry.core.network.PacketIdServer;
import forestry.core.tiles.TileUtil;

public class PacketSelectClimateTargeted extends ForestryPacket implements IForestryPacketServer {
	private final BlockPos pos;
	private final IClimateState climateState;

	public PacketSelectClimateTargeted(BlockPos pos, IClimateState climateState) {
		this.pos = pos;
		this.climateState = climateState;
	}

	@Override
	public PacketIdServer getPacketId() {
		return PacketIdServer.SELECT_CLIMATE_TARGETED;
	}

	@Override
	protected void writeData(PacketBufferForestry data) throws IOException {
		data.writeBlockPos(pos);
		data.writeClimateState(climateState);
	}

	public static class Handler implements IForestryPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferForestry data, EntityPlayerMP player) throws IOException {
			BlockPos pos = data.readBlockPos();
			IClimateState climateState = data.readClimateState();

			IHabitatFormerHousing housing = TileUtil.getTile(player.world, pos, IHabitatFormerHousing.class);
			if (housing != null) {
				IClimateLogic logic = housing.getLogic();
				logic.setTargetedState(climateState);
			}
		}
	}
}
