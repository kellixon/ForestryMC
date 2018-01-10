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

import net.minecraft.nbt.NBTTagCompound;

import forestry.api.climate.ClimateStateType;
import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.api.climate.source.IClimateSource;
import forestry.climatology.api.climate.source.IClimateSourceProxy;
import forestry.core.climate.ClimateStates;

public abstract class ClimateSource<P extends IClimateSourceProxy> implements IClimateSource<P> {

	protected final P proxy;
	protected final float boundModifier;
	protected final ClimateSourceType sourceType;
	private IClimateState state;
	protected float change;
	protected ClimateSourceMode temperatureMode;
	protected ClimateSourceMode humidityMode;
	protected boolean isActive;

	public ClimateSource(P proxy, float change, float boundModifier, ClimateSourceType sourceType) {
		this.proxy = proxy;
		this.change = change;
		this.boundModifier = boundModifier;
		this.sourceType = sourceType;
		this.temperatureMode = ClimateSourceMode.NONE;
		this.humidityMode = ClimateSourceMode.NONE;
		this.state = ClimateStates.extendedZero();
	}

	@Override
	public P getProxy() {
		return proxy;
	}

	public void setHumidityMode(ClimateSourceMode humidityMode) {
		this.humidityMode = humidityMode;
	}

	public void setTemperatureMode(ClimateSourceMode temperatureMode) {
		this.temperatureMode = temperatureMode;
	}

	@Override
	public float getBoundaryModifier(ClimateType type, boolean boundaryUp) {
		if (type == ClimateType.HUMIDITY) {
			if (humidityMode == ClimateSourceMode.POSITIVE && boundaryUp || humidityMode == ClimateSourceMode.NEGATIVE && !boundaryUp) {
				return getBoundModifier(ClimateType.HUMIDITY);
			}
		} else {
			if (temperatureMode == ClimateSourceMode.POSITIVE && boundaryUp || temperatureMode == ClimateSourceMode.NEGATIVE && !boundaryUp) {
				return getBoundModifier(ClimateType.TEMPERATURE);
			}
		}
		return 0;
	}

	protected float getBoundModifier(ClimateType type) {
		return boundModifier;
	}

	protected float getChange(ClimateType type) {
		return change;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean affectsClimateType(ClimateType type) {
		return sourceType.affectClimateType(type);
	}

	@Override
	public final IClimateState work(IClimateLogic logic, IClimateState previousState, IClimateState targetState, IClimateState currentState, final double sizeModifier) {
		IClimateState newState = ClimateStates.INSTANCE.create(getState(), ClimateStateType.EXTENDED);
		IClimateState newChange = ClimateStates.extendedZero();
		IClimateState defaultState = logic.getHousing().getDefaultClimate();
		ClimateSourceType validType = getWorkType(currentState, targetState);
		ClimateSourceType oppositeType = getOppositeWorkType(currentState, defaultState);
		beforeWork();
		boolean work = canWork(newState, oppositeType);
		//Test if the source can work and test if the owner has enough resources to work.
		if (!work && oppositeType != null) {
			isActive = false;
			isNotValid();
			if (ClimateStates.isZero(newState)) {
				return newChange;
			} else if (ClimateStates.isNearZero(newState)) {
				setState(newChange);
				return newChange;
			} else if (ClimateStates.isNearTarget(currentState, targetState)) {
				return newChange;
			}
			//If the state is not already zero, remove one change state from the state.
			newChange = getChange(oppositeType, defaultState, currentState);
			newChange = ClimateStates.INSTANCE.create(-newChange.getTemperature(), -newChange.getHumidity(), ClimateStateType.EXTENDED);
		} else if (validType != null) {
			newChange = getChange(validType, targetState, previousState);
			IClimateState changedState = newState.add(newChange.scale(1 / sizeModifier));
			boolean couldWork = canWork(changedState, oppositeType);
			//Test if the owner could work with the changed state. If he can remove the resources for the changed state, if not only remove the resources for the old state.
			removeResources(couldWork ? changedState : newState, oppositeType);
			if (!couldWork) {
				newChange = ClimateStates.extendedZero();
			}
		} else if (oppositeType != null) {
			//Remove the resources if the owner has enough resources and the state is not the default state.
			removeResources(newState, oppositeType);
		}
		newState = newState.add(newChange.scale(1 / sizeModifier));
		if (ClimateStates.isZero(newState) || ClimateStates.isNearZero(newState)) {
			newState = ClimateStates.extendedZero();
		}
		setState(newState);
		return newChange;
	}

	protected void isNotValid() {

	}

	protected void beforeWork() {
	}

	/**
	 * @return true if the source can work, false if it can not.
	 */
	protected abstract boolean canWork(IClimateState currentState, @Nullable ClimateSourceType oppositeType);

	protected abstract void removeResources(IClimateState currentState, @Nullable ClimateSourceType oppositeType);

	protected abstract IClimateState getChange(ClimateSourceType type, IClimateState target, IClimateState currentState);

	@Nullable
	private ClimateSourceType getOppositeWorkType(IClimateState state, IClimateState target) {
		boolean canChangeHumidity = sourceType.canChangeHumidity() && canChange(state.getHumidity(), target.getHumidity(), humidityMode.getOpposite());
		boolean canChangeTemperature = sourceType.canChangeTemperature() && canChange(state.getTemperature(), target.getTemperature(), temperatureMode.getOpposite());
		if (canChangeHumidity) {
			return canChangeTemperature ? ClimateSourceType.BOTH : ClimateSourceType.HUMIDITY;
		} else {
			return canChangeTemperature ? ClimateSourceType.TEMPERATURE : null;
		}
	}

	@Nullable
	private ClimateSourceType getWorkType(IClimateState state, IClimateState target) {
		boolean canChangeHumidity = sourceType.canChangeHumidity() && canChange(state.getHumidity(), target.getHumidity(), humidityMode);
		boolean canChangeTemperature = sourceType.canChangeTemperature() && canChange(state.getTemperature(), target.getTemperature(), temperatureMode);
		if (canChangeHumidity) {
			return canChangeTemperature ? ClimateSourceType.BOTH : ClimateSourceType.HUMIDITY;
		} else {
			return canChangeTemperature ? ClimateSourceType.TEMPERATURE : null;
		}
	}

	private boolean canChange(float value, float target, ClimateSourceMode mode) {
		return mode == ClimateSourceMode.POSITIVE && value < target || mode == ClimateSourceMode.NEGATIVE && value > target;
	}

	@Override
	public IClimateState getState() {
		return state.copy();
	}

	protected void setState(IClimateState state) {
		this.state = state;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound sourceData = new NBTTagCompound();
		state.writeToNBT(sourceData);
		nbt.setTag("Source", sourceData);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagCompound sourceData = nbt.getCompoundTag("Source");
		if (sourceData.hasNoTags()) {
			return;
		}
		state = ClimateStates.INSTANCE.create(sourceData, ClimateStateType.EXTENDED);
	}

}
