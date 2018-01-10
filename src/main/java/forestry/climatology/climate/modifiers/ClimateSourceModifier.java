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
package forestry.climatology.climate.modifiers;

import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.climate.ClimateStateType;
import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.api.climate.IClimateModifier;
import forestry.climatology.api.climate.source.IClimateSource;
import forestry.core.climate.ClimateStates;
import forestry.core.utils.StringUtil;
import forestry.core.utils.Translator;

public class ClimateSourceModifier implements IClimateModifier {

	public static final String RANGE_UP_NBT_KEY = "rangeUp";
	public static final String RANGE_DOWN_NBT_KEY = "rangeDown";
	public static final String CHANGE_NBT_KEY = "change";
	public static final float CLIMATE_CHANGE = 0.01F;

	@Override
	public IClimateState modifyTarget(IClimateLogic logic, IClimateState newState, IClimateState previousState, NBTTagCompound data) {
		Collection<IClimateSource> sources = logic.getClimateSources();
		if (sources.isEmpty()) {
			data.removeTag(RANGE_UP_NBT_KEY);
			data.removeTag(RANGE_DOWN_NBT_KEY);
			data.removeTag(CHANGE_NBT_KEY);
			return newState;
		}
		logic.recalculateBoundaries();

		IClimateState boundaryUp = logic.getBoundaryUp();
		IClimateState boundaryDown = logic.getBoundaryDown();

		//Send the boundaries to the client
		data.setTag(RANGE_UP_NBT_KEY, boundaryUp.writeToNBT(new NBTTagCompound()));
		data.setTag(RANGE_DOWN_NBT_KEY, boundaryDown.writeToNBT(new NBTTagCompound()));

		IClimateState targetedState = logic.getTargetedState();
		if (!targetedState.isPresent()) {
			return newState;
		}
		IClimateState target = getTargetOrBound(previousState, logic.getBoundaryDown(), logic.getBoundaryUp(), targetedState);
		IClimateState changeState = ClimateStates.extendedZero();

		for (IClimateSource source : logic.getClimateSources()) {
			newState = newState.add(source.getState());
		}

		for (IClimateSource source : logic.getClimateSources()) {
			IClimateState state = source.work(logic, previousState, target, newState, logic.getSizeModifier());
			changeState = changeState.add(source.getState());
			newState = newState.add(state);
		}
		data.setTag(CHANGE_NBT_KEY, changeState.writeToNBT(new NBTTagCompound()));
		return newState;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(IClimateLogic container, NBTTagCompound nbtData, ClimateType type, List<String> lines) {
		IClimateState rangeDown = ClimateStates.INSTANCE.create(nbtData.getCompoundTag(RANGE_DOWN_NBT_KEY), ClimateStateType.DEFAULT);
		IClimateState rangeUp = ClimateStates.INSTANCE.create(nbtData.getCompoundTag(RANGE_UP_NBT_KEY), ClimateStateType.DEFAULT);
		IClimateState change = ClimateStates.INSTANCE.create(nbtData.getCompoundTag(CHANGE_NBT_KEY), ClimateStateType.EXTENDED);
		if (type == ClimateType.HUMIDITY) {
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.range.up", StringUtil.floatAsPercent(rangeUp.getHumidity())));
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.range.down", StringUtil.floatAsPercent(rangeDown.getHumidity())));
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.change", StringUtil.floatAsPercent(change.getHumidity())));
		} else {
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.range.up", StringUtil.floatAsPercent(rangeUp.getTemperature())));
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.range.down", StringUtil.floatAsPercent(rangeDown.getTemperature())));
			lines.add(Translator.translateToLocalFormatted("for.gui.modifier.sources.change", StringUtil.floatAsPercent(change.getTemperature())));
		}
	}

	@Override
	public boolean canModify(ClimateType type) {
		return true;
	}

	@Override
	public String getName() {
		return Translator.translateToLocal("for.gui.modifier.sources.title");
	}

	@Override
	public int getPriority() {
		return -1;
	}

	/**
	 * @return The target if it is within the bounds and the bounds if it is above or below the bounds.
	 */
	private IClimateState getTargetOrBound(IClimateState climateState, IClimateState boundaryDown, IClimateState boundaryUp, IClimateState targetedState) {
		float temperature = climateState.getTemperature();
		float humidity = climateState.getHumidity();
		float targetTemperature = targetedState.getTemperature();
		float targetHumidity = targetedState.getHumidity();
		if (targetTemperature > temperature) {
			temperature = Math.min(targetTemperature, boundaryUp.getTemperature());
		} else if (targetTemperature < temperature) {
			temperature = Math.max(targetTemperature, boundaryDown.getTemperature());
		}
		if (targetHumidity > humidity) {
			humidity = Math.min(targetHumidity, boundaryUp.getHumidity());
		} else if (targetHumidity < humidity) {
			humidity = Math.max(targetHumidity, boundaryDown.getHumidity());
		}
		return ClimateStates.of(temperature, humidity);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return ItemStack.EMPTY;
	}
}
