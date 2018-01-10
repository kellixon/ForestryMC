package forestry.climatology.tiles;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import forestry.climatology.api.climate.source.IClimateSource;
import forestry.climatology.api.climate.source.IClimateSourceProxy;
import forestry.core.tiles.AdjacentTileCache;

public class AdjacentNodeCache implements AdjacentTileCache.ICacheListener {
	private final AdjacentTileCache cache;
	private final List<IClimateSource> nodes = new LinkedList<>();
	private final IClimateSource[] sides = new IClimateSource[6];
	private boolean changed = true;

	public AdjacentNodeCache(AdjacentTileCache cache) {
		this.cache = cache;
	}

	@Nullable
	public IClimateSource getAdjacentNode(EnumFacing side) {
		checkChanged();
		return sides[side.ordinal()];
	}

	public Collection<IClimateSource> getAdjacentNodes() {
		checkChanged();
		return nodes;
	}

	@Override
	public void changed() {
		changed = true;
	}

	@Override
	public void purge() {
		nodes.clear();
		Arrays.fill(sides, null);
	}

	private void checkChanged() {
		cache.refresh();
		if (changed) {
			changed = false;
			purge();
			for (EnumFacing side : EnumFacing.values()) {
				TileEntity tile = cache.getTileOnSide(side);
				if (tile instanceof IClimateSourceProxy) {
					IClimateSource inv = ((IClimateSourceProxy) tile).getNode();
					sides[side.ordinal()] = inv;
					nodes.add(inv);
				}
			}
		}
	}
}
