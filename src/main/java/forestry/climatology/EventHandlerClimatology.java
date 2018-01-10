package forestry.climatology;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.event.world.ChunkDataEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import forestry.climatology.api.climate.IClimateChunk;

public class EventHandlerClimatology {

	@SubscribeEvent
	public void onLoadChunk(ChunkDataEvent.Load event) {
		NBTTagCompound chunkData = event.getData();
		if (chunkData.hasKey("Climate")) {
			NBTTagCompound compound = chunkData.getCompoundTag("Climate");
			IClimateChunk chunk = ClimateSystem.INSTANCE.getOrCreateChunk(event.getWorld(), getPos(event.getChunk()));
			chunk.readFromNBT(compound);
		}
	}

	@SubscribeEvent
	public void onLoadChunk(ChunkDataEvent.Save event) {
		World world = event.getWorld();
		IClimateChunk chunk = ClimateSystem.INSTANCE.getChunk(world, getPos(event.getChunk()));
		if (chunk != null) {
			NBTTagCompound compound = new NBTTagCompound();
			chunk.writeToNBT(compound);
			event.getData().setTag("Climate", compound);
		}
	}

	private long getPos(Chunk chunk) {
		return ChunkPos.asLong(chunk.x, chunk.z);
	}
}
