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
package forestry.climatology;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.registry.GameRegistry;

import forestry.api.circuits.ChipsetManager;
import forestry.api.circuits.CircuitSocketType;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.climate.ClimateType;
import forestry.api.core.ForestryAPI;
import forestry.api.core.Tabs;
import forestry.api.modules.ForestryModule;
import forestry.climatology.api.climate.IClimateSystem;
import forestry.climatology.blocks.BlockRegistryClimatology;
import forestry.climatology.circuits.CircuitHabitatFormer;
import forestry.climatology.climate.modifiers.AltitudeModifier;
import forestry.climatology.climate.modifiers.ClimateSourceModifier;
import forestry.climatology.climate.modifiers.TimeModifier;
import forestry.climatology.climate.modifiers.WeatherModifier;
import forestry.climatology.items.ItemRegistryClimatology;
import forestry.climatology.network.PacketRegistryClimatology;
import forestry.climatology.proxy.ProxyClimatology;
import forestry.climatology.tiles.TileClimatiser;
import forestry.climatology.tiles.TileFan;
import forestry.climatology.tiles.TileHabitatFormer;
import forestry.climatology.tiles.TileHeater;
import forestry.climatology.tiles.TileHygroregulator;
import forestry.core.CreativeTabForestry;
import forestry.core.ModuleCore;
import forestry.core.circuits.CircuitLayout;
import forestry.core.circuits.Circuits;
import forestry.core.config.Constants;
import forestry.core.items.EnumElectronTube;
import forestry.core.items.ItemRegistryCore;
import forestry.core.network.IPacketRegistry;
import forestry.modules.BlankForestryModule;
import forestry.modules.ForestryModuleUids;

@ForestryModule(containerID = Constants.MOD_ID, moduleID = ForestryModuleUids.CLIMATOLOGY, name = "Greenhouse", author = "Nedelosk", url = Constants.URL, unlocalizedDescription = "for.module.greenhouse.description")
public class ModuleClimatology extends BlankForestryModule {

	@SuppressWarnings("NullableProblems")
	@SidedProxy(clientSide = "forestry.climatology.proxy.ProxyClimatologyClient", serverSide = "forestry.climatology.proxy.ProxyClimatology")
	public static ProxyClimatology proxy;

	@Nullable
	private static BlockRegistryClimatology blocks;
	@Nullable
	private static ItemRegistryClimatology items;

	public static BlockRegistryClimatology getBlocks() {
		Preconditions.checkState(blocks != null);
		return blocks;
	}

	public static ItemRegistryClimatology getItems() {
		Preconditions.checkArgument(items != null);
		return items;
	}

	public static CreativeTabs getGreenhouseTab() {
		if (ForestryAPI.enabledModules.contains(new ResourceLocation(Constants.MOD_ID, ForestryModuleUids.FARMING))) {
			return Tabs.tabAgriculture;
		}
		return CreativeTabForestry.tabForestry;
	}

	@Override
	public void registerItemsAndBlocks() {
		blocks = new BlockRegistryClimatology();
		items = new ItemRegistryClimatology();
	}

	@Override
	public void preInit() {
		proxy.preInti();
		MinecraftForge.EVENT_BUS.register(new EventHandlerClimatology());
		proxy.initializeModels();

		ICircuitLayout layoutManaged = new CircuitLayout("habitat.former", CircuitSocketType.HABITAT_FORMER);
		ChipsetManager.circuitRegistry.registerLayout(layoutManaged);
	}

	@Override
	public void doInit() {
		GameRegistry.registerTileEntity(TileHabitatFormer.class, "forestry.HabitatFormer");
		GameRegistry.registerTileEntity(TileClimatiser.class, "forestry.ClimatiserLegacy");
		GameRegistry.registerTileEntity(TileHygroregulator.class, "forestry.ClimateSourceHygroregulator");
		GameRegistry.registerTileEntity(TileFan.class, "forestry.GreenhouseFan");
		GameRegistry.registerTileEntity(TileHeater.class, "forestry.GreenhouseHeater");

		IClimateSystem system = ClimateSystem.INSTANCE;
		system.registerModifier(new WeatherModifier());
		system.registerModifier(new TimeModifier());
		system.registerModifier(new AltitudeModifier());
		system.registerModifier(new ClimateSourceModifier());

		Circuits.climatiserTemperature1 = new CircuitHabitatFormer("climatiser.temperature.1", ClimateType.TEMPERATURE, 0.125F, 0.125F, 0.25F);
		Circuits.climatiserTemperature2 = new CircuitHabitatFormer("climatiser.temperature.2", ClimateType.TEMPERATURE, 0.25F, 0.25F, 0.5F);
		Circuits.climatiserHumidity1 = new CircuitHabitatFormer("climatiser.humidity.1", ClimateType.HUMIDITY, 0.125F, 0.125F, 0.25F);
		Circuits.climatiserHumidity2 = new CircuitHabitatFormer("climatiser.humidity.2", ClimateType.HUMIDITY, 0.25F, 0.25F, 0.5F);
		proxy.inti();
	}

	@Override
	public void registerRecipes() {
		ItemRegistryCore coreItems = ModuleCore.getItems();

		ICircuitLayout layout = ChipsetManager.circuitRegistry.getLayout("forestry.habitat.former");
		if (layout == null) {
			return;
		}
		ChipsetManager.solderManager.addRecipe(layout, coreItems.tubes.get(EnumElectronTube.GOLD, 1), Circuits.climatiserTemperature1);
		ChipsetManager.solderManager.addRecipe(layout, coreItems.tubes.get(EnumElectronTube.BLAZE, 1), Circuits.climatiserTemperature2);
		ChipsetManager.solderManager.addRecipe(layout, coreItems.tubes.get(EnumElectronTube.LAPIS, 1), Circuits.climatiserHumidity1);
		ChipsetManager.solderManager.addRecipe(layout, coreItems.tubes.get(EnumElectronTube.OBSIDIAN, 1), Circuits.climatiserHumidity2);
	}

	@Override
	public IPacketRegistry getPacketRegistry() {
		return new PacketRegistryClimatology();
	}

}
