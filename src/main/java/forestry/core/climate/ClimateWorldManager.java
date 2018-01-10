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
package forestry.core.climate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import forestry.api.climate.IClimateState;
import forestry.climatology.ClimateSystem;

public class ClimateWorldManager{

	private final Cache<BlockPos, IClimateState> stateCache;
	private final forestry.core.climate.ClimateManager parent;

	public ClimateWorldManager(forestry.core.climate.ClimateManager parent) {
		this.stateCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();
		this.parent = parent;
	}
	
	public IClimateState getClimateState(World world, BlockPos pos) {
		IClimateState cacheState = stateCache.getIfPresent(pos);
		if(cacheState == null){
			IClimateState state = ClimateSystem.INSTANCE.getChunkState(world, pos);
			if(state.isPresent()){
				cacheState = state.copy();
			}
			if(cacheState == null){
				cacheState = parent.getBiomeState(world, pos);
			}
			stateCache.put(pos, cacheState);
		}
		return cacheState;
	}
	
}