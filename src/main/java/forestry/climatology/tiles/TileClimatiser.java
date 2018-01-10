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
package forestry.climatology.tiles;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

import forestry.climatology.api.climate.source.IClimateSourceProxy;
import forestry.climatology.blocks.BlockClimatiser;
import forestry.climatology.climate.ClimateSource;
import forestry.climatology.gui.ContainerHabitatformer;
import forestry.climatology.gui.GuiHabitatformer;
import forestry.core.blocks.BlockBase;
import forestry.core.network.packets.PacketActiveUpdate;
import forestry.core.tiles.IActivatable;
import forestry.core.tiles.TileBase;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.NetworkUtil;

public abstract class TileClimatiser<P extends TileClimatiser> extends TileBase implements IClimateSourceProxy<ClimateSource>, IActivatable {

	private final ClimateSource source;

	private boolean active;

	protected TileClimatiser(IClimatiserDefinition<P> definition) {
		this.source = definition.createSource((P) this);
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		setActive(data.getBoolean("Active"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		data = super.writeToNBT(data);

		data.setBoolean("Active", active);

		return data;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public GuiContainer getGui(EntityPlayer player, int data) {
		TileHabitatFormer former = getFormer();
		if (former == null) {
			return null;
		}
		return new GuiHabitatformer(player, former);
	}

	@Nullable
	@Override
	public Container getContainer(EntityPlayer player, int data) {
		TileHabitatFormer former = getFormer();
		if (former == null) {
			return null;
		}
		return new ContainerHabitatformer(player.inventory, former);
	}

	@Nullable
	private TileHabitatFormer getFormer() {
		IBlockState blockState = world.getBlockState(pos);
		if (!(blockState.getBlock() instanceof BlockClimatiser)) {
			return null;
		}
		return TileUtil.getTile(world, pos.offset(blockState.getValue(BlockBase.FACING).getOpposite()), TileHabitatFormer.class);
	}

	@Override
	public ClimateSource getNode() {
		return source;
	}

	/* IActivatable */
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		if (this.active == active) {
			return;
		}

		this.active = active;

		if (world != null) {
			if (world.isRemote) {
				world.markBlockRangeForRenderUpdate(getCoordinates(), getCoordinates());
			} else {
				NetworkUtil.sendNetworkPacket(new PacketActiveUpdate(this), getCoordinates(), world);
			}
		}
	}
}
