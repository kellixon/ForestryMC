package forestry.climatology.tiles;

import javax.annotation.Nullable;
import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.climatology.climate.ClimateSource;
import forestry.climatology.climate.ClimateSourceElectrical;
import forestry.climatology.climate.ClimateSourceType;
import forestry.core.network.IStreamableGui;
import forestry.core.network.PacketBufferForestry;
import forestry.energy.EnergyManager;
import forestry.energy.EnergyTransferMode;

public abstract class TileElectricalClimatiser extends TileClimatiser<TileElectricalClimatiser> implements IStreamableGui {
	static final ClimatiserDefinition HEATER = new ClimatiserDefinition(0.075F, 2.5F, ClimateSourceType.TEMPERATURE);
	static final ClimatiserDefinition FAN = new ClimatiserDefinition(-0.075F, 2.5F, ClimateSourceType.TEMPERATURE);
	static final ClimatiserDefinition HUMIDIFIER = new ClimatiserDefinition(0.075F, 2.5F, ClimateSourceType.HUMIDITY);
	static final ClimatiserDefinition DEHUMIDIFIER = new ClimatiserDefinition(-0.075F, 2.5F, ClimateSourceType.HUMIDITY);

	private final EnergyManager energyManager;

	protected TileElectricalClimatiser(ClimatiserDefinition definition) {
		super(definition);
		this.energyManager = new EnergyManager(1200, 8000);
		this.energyManager.setExternalMode(EnergyTransferMode.RECEIVE);
	}

	public EnergyManager getEnergyManager() {
		return energyManager;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		energyManager.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		energyManager.readFromNBT(nbt);
	}

	@Override
	public void writeGuiData(PacketBufferForestry data) {
		energyManager.writeData(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void readGuiData(PacketBufferForestry data) throws IOException {
		energyManager.readData(data);
	}

	/* IPowerHandler */
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return energyManager.hasCapability(capability) || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		T energyCapability = energyManager.getCapability(capability);
		if (energyCapability != null) {
			return energyCapability;
		}
		return super.getCapability(capability, facing);
	}

	public static class ClimatiserDefinition implements IClimatiserDefinition<TileElectricalClimatiser> {
		private final float change;
		private final float boundModifier;
		private final ClimateSourceType type;

		private ClimatiserDefinition(float change, float boundModifier, ClimateSourceType type) {
			this.change = change;
			this.boundModifier = boundModifier;
			this.type = type;
		}

		@Override
		public ClimateSource createSource(TileElectricalClimatiser proxy) {
			return new ClimateSourceElectrical(proxy, type, change, boundModifier);
		}
	}
}
