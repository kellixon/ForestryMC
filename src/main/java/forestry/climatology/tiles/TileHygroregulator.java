package forestry.climatology.tiles;

import javax.annotation.Nullable;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.climatology.climate.ClimateSourceHygroregulator;
import forestry.climatology.gui.ContainerHygroregulator;
import forestry.climatology.gui.GuiHygroregulator;
import forestry.climatology.inventory.InventoryHygroregulator;
import forestry.core.config.Constants;
import forestry.core.fluids.FilteredTank;
import forestry.core.fluids.FluidHelper;
import forestry.core.fluids.Fluids;
import forestry.core.fluids.TankManager;
import forestry.core.recipes.HygroregulatorRecipe;
import forestry.core.tiles.ILiquidTankTile;

public class TileHygroregulator extends TileClimatiser<TileHygroregulator> implements ILiquidTankTile {
	private static final IClimatiserDefinition<TileHygroregulator> HYGROREGULATOR_DEFINITION = tile -> new ClimateSourceHygroregulator(tile, 2.5F);

	private final HygroregulatorRecipe[] recipes;

	private final TankManager tankManager;
	private final FilteredTank liquidTank;

	public TileHygroregulator() {
		super(HYGROREGULATOR_DEFINITION);

		setInternalInventory(new InventoryHygroregulator(this));

		Fluid water = FluidRegistry.WATER;
		Fluid lava = FluidRegistry.LAVA;
		Fluid liquidIce = Fluids.ICE.getFluid();

		this.liquidTank = new FilteredTank(Constants.PROCESSOR_TANK_CAPACITY).setFilters(water, lava, liquidIce);

		this.tankManager = new TankManager(this, liquidTank);

		this.recipes = new HygroregulatorRecipe[]{new HygroregulatorRecipe(new FluidStack(water, 1), 1, 0.05f, -0.005f),
			new HygroregulatorRecipe(new FluidStack(lava, 1), 10, -0.05f, +0.005f),
			new HygroregulatorRecipe(new FluidStack(liquidIce, 1), 10, 0.075f, -0.01f)};
	}

	public FilteredTank getLiquidTank() {
		return liquidTank;
	}

	@Override
	protected void updateServerSide() {
		if (updateOnInterval(20)) {
			// Check if we have suitable items waiting in the item slot
			FluidHelper.drainContainers(tankManager, this, 0);
		}
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		tankManager.readFromNBT(nbttagcompound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound = super.writeToNBT(nbttagcompound);
		tankManager.writeToNBT(nbttagcompound);
		return nbttagcompound;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiContainer getGui(EntityPlayer player, int data) {
		return new GuiHygroregulator(player.inventory, this);
	}

	@Override
	public Container getContainer(EntityPlayer player, int data) {
		return new ContainerHygroregulator(player.inventory, this);
	}

	/* UPDATING */
	@Nullable
	public HygroregulatorRecipe getRecipe(FluidStack liquid) {
		HygroregulatorRecipe recipe = null;
		for (HygroregulatorRecipe rec : recipes) {
			if (rec.liquid.isFluidEqual(liquid)) {
				recipe = rec;
				break;
			}
		}
		return recipe;
	}

	/* ILIQUIDTANKCONTAINER */
	@Override
	public TankManager getTankManager() {
		return tankManager;
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (super.hasCapability(capability, facing)) {
			return super.getCapability(capability, facing);
		}
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(tankManager);
		}
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
			super.hasCapability(capability, facing);
	}

	public HygroregulatorRecipe[] getRecipes() {
		return recipes;
	}
}
