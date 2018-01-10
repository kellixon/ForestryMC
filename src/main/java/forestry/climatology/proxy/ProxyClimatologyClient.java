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
package forestry.climatology.proxy;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.BiomeColorHelper;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.climate.IClimateState;
import forestry.climatology.ClimateSystem;

@SideOnly(Side.CLIENT)
public class ProxyClimatologyClient extends ProxyClimatology {

	@Nullable
	private static BlockPos grassPosition;
	@Nullable
	private static BlockPos foliagePosition;
	private static BiomeColorHelper.ColorResolver ORIGINAL_GRASS_COLOR;
	private static BiomeColorHelper.ColorResolver ORIGINAL_FOLIAGE_COLOR;

	@Override
	public void initializeModels() {
	}

	@Override
	public void preInti() {
		MinecraftForge.EVENT_BUS.register(this);

		ORIGINAL_GRASS_COLOR = BiomeColorHelper.GRASS_COLOR;
		BiomeColorHelper.GRASS_COLOR = (biome, blockPosition) -> {
			grassPosition = blockPosition;
			return ORIGINAL_GRASS_COLOR.getColorAtPos(biome, blockPosition);
		};

		ORIGINAL_FOLIAGE_COLOR = BiomeColorHelper.FOLIAGE_COLOR;
		BiomeColorHelper.FOLIAGE_COLOR = (biome, blockPosition) -> {
			foliagePosition = blockPosition;
			return ORIGINAL_FOLIAGE_COLOR.getColorAtPos(biome, blockPosition);
		};
	}

	@Override
	public void inti() {

	}

	@SubscribeEvent
	public void getFoliageColor(BiomeEvent.GetFoliageColor event) {
		if (foliagePosition != null) {
			IClimateState state = ClimateSystem.INSTANCE.getChunkState(Minecraft.getMinecraft().world, foliagePosition);
			if (state.isPresent()) {
				double temperature = MathHelper.clamp(state.getTemperature(), 0.0F, 1.0F);
				double humidity = MathHelper.clamp(state.getHumidity(), 0.0F, 1.0F);
				event.setNewColor(ColorizerGrass.getGrassColor(temperature, humidity));
			}
		}
	}

	@SubscribeEvent
	public void getGrassColor(BiomeEvent.GetGrassColor event) {
		if (grassPosition != null) {
			IClimateState state = ClimateSystem.INSTANCE.getChunkState(Minecraft.getMinecraft().world, grassPosition);
			if (state.isPresent()) {
				double temperature = MathHelper.clamp(state.getTemperature(), 0.0F, 1.0F);
				double humidity = MathHelper.clamp(state.getHumidity(), 0.0F, 1.0F);
				event.setNewColor(ColorizerGrass.getGrassColor(temperature, humidity));
			}
		}
	}

}
