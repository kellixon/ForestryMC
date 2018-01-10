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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.climate.ClimateStateType;
import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.api.climatology.IClimateHousing;
import forestry.api.core.ForestryAPI;
import forestry.climatology.ClimateSystem;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.api.climate.IClimateModifier;
import forestry.climatology.api.climate.source.IClimateSource;
import forestry.core.climate.AbsentClimateState;
import forestry.core.climate.ClimateStates;
import forestry.core.config.Config;
import forestry.core.network.IStreamable;
import forestry.core.network.PacketBufferForestry;
import forestry.core.network.packets.PacketUpdateClimate;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.TickHelper;

public class ClimateLogic implements IClimateLogic, IStreamable {

	protected final IClimateHousing housing;
	protected final Set<IClimateSource> sources;
	private int delay;
	private IClimateState targetedState;
	private IClimateState boundaryUp;
	private IClimateState boundaryDown;
	private double sizeModifier;
	protected IClimateState state;
	protected int area;
	private NBTTagCompound modifierData;
	private TickHelper tickHelper;

	public ClimateLogic(IClimateHousing housing) {
		this.housing = housing;
		this.sources = new HashSet<>();
		this.delay = 20;
		this.state = housing.getDefaultClimate().copy();
		this.modifierData = new NBTTagCompound();
		this.boundaryUp = ClimateStates.INSTANCE.min();
		this.boundaryDown = ClimateStates.INSTANCE.min();
		this.targetedState = AbsentClimateState.INSTANCE;
		this.sizeModifier = 1.0D;
		this.tickHelper = new TickHelper();
	}

	@Override
	public IClimateHousing getHousing() {
		return housing;
	}

	@Override
	public void updateClimate() {
		if (tickHelper.updateOnInterval(delay)) {
			IClimateState lastState = state.copy(ClimateStateType.DEFAULT);
			state = housing.getDefaultClimate().copy(ClimateStateType.EXTENDED);
			for (IClimateModifier modifier : ClimateSystem.INSTANCE.getModifiers()) {
				state = modifier.modifyTarget(this, state, lastState, modifierData).copy(ClimateStateType.EXTENDED);
			}
			if (!state.equals(lastState)) {
				BlockPos coordinates = housing.getCoordinates();
				NetworkUtil.sendNetworkPacket(new PacketUpdateClimate(coordinates, this), coordinates, housing.getWorldObj());
			}
		}
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getArea() {
		return area;
	}

	@Override
	public double getSizeModifier() {
		return sizeModifier;
	}

	@Override
	public void recalculateBoundaries() {
		sizeModifier = Math.max((double) area / (double) Config.climateSourceRange, 1.0D);
		float temperatureUp = 0.0F;
		float humidityUp = 0.0F;
		float temperatureDown = 0.0F;
		float humidityDown = 0.0F;
		for (IClimateSource source : sources) {
			if (source.affectsClimateType(ClimateType.HUMIDITY)) {
				humidityUp += source.getBoundaryModifier(ClimateType.HUMIDITY, true);
				humidityDown += source.getBoundaryModifier(ClimateType.HUMIDITY, false);
			}
			if (source.affectsClimateType(ClimateType.TEMPERATURE)) {
				temperatureUp += source.getBoundaryModifier(ClimateType.TEMPERATURE, true);
				temperatureDown += source.getBoundaryModifier(ClimateType.TEMPERATURE, false);
			}
		}
		if (temperatureUp != 0) {
			temperatureUp /= sizeModifier;
		}
		if (temperatureDown != 0) {
			temperatureDown /= sizeModifier;
		}
		if (humidityUp != 0) {
			humidityUp /= sizeModifier;
		}
		if (humidityDown != 0) {
			humidityDown /= sizeModifier;
		}
		boundaryUp = housing.getDefaultClimate().add(ClimateStates.of(temperatureUp, humidityUp));
		boundaryDown = housing.getDefaultClimate().remove(ClimateStates.of(temperatureDown, humidityDown));
	}

	@Override
	public IClimateState getBoundaryDown() {
		return boundaryDown;
	}

	@Override
	public IClimateState getBoundaryUp() {
		return boundaryUp;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		state.writeToNBT(nbt);
		nbt.setTag("Target", targetedState.writeToNBT(new NBTTagCompound()));
		nbt.setTag("modifierData", modifierData.copy());
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		state = ForestryAPI.states.create(nbt);
		targetedState = ForestryAPI.states.create(nbt.getCompoundTag("Target"));
		modifierData = nbt.getCompoundTag("modifierData");
	}

	@Override
	public void setTargetedState(IClimateState state) {
		this.targetedState = state;
	}

	@Override
	public IClimateState getTargetedState() {
		return targetedState;
	}

	public void setState(IClimateState state) {
		this.state = state;
	}

	@Override
	public void onAddSource(IClimateSource source) {
		sources.add(source);
	}

	@Override
	public void onRemoveSource(IClimateSource source) {
		sources.remove(source);
	}

	@Override
	public Collection<IClimateSource> getClimateSources() {
		return sources;
	}

	@Override
	public void writeData(PacketBufferForestry data) {
		data.writeClimateState(state);
		data.writeClimateState(boundaryUp);
		data.writeClimateState(boundaryDown);
		data.writeClimateState(targetedState);
		data.writeCompoundTag(modifierData);
	}

	@Override
	public void readData(PacketBufferForestry data) throws IOException {
		state = data.readClimateState();
		boundaryUp = data.readClimateState();
		boundaryDown = data.readClimateState();
		targetedState = data.readClimateState();
		NBTTagCompound modifierTag = data.readCompoundTag();
		if (modifierTag == null) {
			modifierTag = new NBTTagCompound();
		}
		modifierData = modifierTag;
	}

	@Override
	public IClimateState getState() {
		return state;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IClimateLogic)) {
			return false;
		}
		IClimateLogic container = (IClimateLogic) obj;
		IClimateHousing parent = container.getHousing();
		if (parent.getCoordinates() == null || this.housing.getCoordinates() == null) {
			return false;
		}
		return this.housing.getCoordinates().equals(parent.getCoordinates());
	}

	@Override
	public int hashCode() {
		return housing.getCoordinates().hashCode();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addModifierInformation(IClimateModifier modifier, ClimateType type, List<String> lines) {
		if (!modifier.canModify(type)) {
			return;
		}
		modifier.addInformation(this, modifierData, type, lines);
	}
}
