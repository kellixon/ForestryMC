package forestry.climatology.tiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import forestry.api.circuits.ChipsetManager;
import forestry.api.circuits.CircuitSocketType;
import forestry.api.circuits.ICircuitBoard;
import forestry.api.circuits.ICircuitSocketType;
import forestry.api.climate.IClimateState;
import forestry.api.climatology.IHabitatFormerHousing;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.climatology.ClimateSystem;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.climate.ClimateLogic;
import forestry.climatology.gui.ContainerHabitatformer;
import forestry.climatology.gui.GuiHabitatformer;
import forestry.core.circuits.ISocketable;
import forestry.core.climate.ClimateManager;
import forestry.core.climate.ClimateStates;
import forestry.core.inventory.InventoryAdapter;
import forestry.core.network.PacketBufferForestry;
import forestry.core.tiles.IClimatised;
import forestry.core.tiles.TilePowered;

public class TileHabitatFormer extends TilePowered implements IHabitatFormerHousing, IClimatised, ISocketable {
	private static final String CONTAINER_NBT_KEY = "Container";

	private final AdjacentNodeCache nodeCache;
	private final InventoryAdapter sockets = new InventoryAdapter(1, "sockets");

	private ClimateLogic logic;
	private IClimateState defaultState;
	private long[] chunks;
	private float radius;

	public TileHabitatFormer() {
		super(800, 10000);
		this.nodeCache = new AdjacentNodeCache(getTileCache());
		this.defaultState = ClimateStates.INSTANCE.absent();
		this.logic = new ClimateLogic(this);
		this.chunks = new long[0];
		this.radius = 2.9F;
	}

	@Override
	protected void updateServerSide() {
		if (!defaultState.isPresent()) {
			defaultState = ClimateManager.getInstance().getBiomeState(world, pos);
			if (!logic.getTargetedState().isPresent()) {
				logic.setState(defaultState.copy());
				logic.setTargetedState(defaultState);
			}
			addChunks();
		}
		logic.updateClimate();
		super.updateServerSide();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		removeChunks();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		removeChunks();
	}

	@Override
	public void validate() {
		super.validate();
		addChunks();
	}

	private void addChunks() {
		Set<Long> chunkSet = new HashSet<>();
		Vector center = new Vector(pos.getX() >> 4, pos.getZ() >> 4);
		Vector start = new Vector(center.x - radius, center.z - radius);
		Vector area = new Vector(radius * 2.0f + 1.0f);
		for (int x = (int) start.x; x < (int) start.x + area.x; ++x) {
			for (int z = (int) start.z; z < (int) start.z + area.z; ++z) {
				Vector current = new Vector(x, z);
				if (current.distance(center) <= radius + 0.01) {
					if (current.distance(center) < radius - 0.5f) {
						ClimateSystem.INSTANCE.addLogic(current.toChunkPos(), logic);
						chunkSet.add(current.toChunkPos());
					}
				}
			}
		}
		this.chunks = chunkSet.stream().mapToLong(l -> l).toArray();

		markChunksForRenderUpdate();
		logic.setArea(chunkSet.size());
	}

	private void removeChunks() {
		for (long chungPos : chunks) {
			ClimateSystem.INSTANCE.removeLogic(chungPos, logic);
		}
		chunks = new long[0];
		markChunksForRenderUpdate();
		radius = 0;
		logic.setArea(0);
	}

	private void markChunksForRenderUpdate() {
		if (world.isRemote) {
			int range = (int) (16 * (radius - 1)) + 8;
			BlockPos startPos = pos.add(-range, -16, -range);
			BlockPos endPos = pos.add(range, 16, range);
			world.markBlockRangeForRenderUpdate(startPos, endPos);
		}
	}

	@Override
	public boolean hasWork() {
		return false;
	}

	@Override
	protected boolean workCycle() {
		return false;
	}

	@Override
	public GuiContainer getGui(EntityPlayer player, int data) {
		return new GuiHabitatformer(player, this);
	}

	@Override
	public Container getContainer(EntityPlayer player, int data) {
		return new ContainerHabitatformer(player.inventory, this);
	}

	@Override
	public EnumTemperature getTemperature() {
		return EnumTemperature.getFromValue(getExactTemperature());
	}

	@Override
	public EnumHumidity getHumidity() {
		return EnumHumidity.getFromValue(getExactHumidity());
	}

	@Override
	public float getExactTemperature() {
		return logic.getState().getTemperature();
	}

	@Override
	public float getExactHumidity() {
		return logic.getState().getHumidity();
	}

	/* Methods - Implement IGreenhouseHousing */
	@Override
	public void onUpdateClimate() {
	}

	@Override
	public IClimateState getDefaultClimate() {
		return defaultState;
	}

	@Override
	public IClimateLogic getLogic() {
		return logic;
	}

	/* Methods - Implement IStreamableGui */
	@Override
	public void writeGuiData(PacketBufferForestry data) {
		super.writeGuiData(data);
		logic.writeData(data);
	}

	@Override
	public void readGuiData(PacketBufferForestry data) throws IOException {
		super.readGuiData(data);
		logic.readData(data);
	}

	/* Methods - Implement ISocketable */
	@Override
	public int getSocketCount() {
		return sockets.getSizeInventory();
	}

	@Override
	public ItemStack getSocket(int slot) {
		return sockets.getStackInSlot(slot);
	}

	@Override
	public void setSocket(int slot, ItemStack stack) {

		if (!stack.isEmpty() && !ChipsetManager.circuitRegistry.isChipset(stack)) {
			return;
		}

		// Dispose correctly of old chipsets
		if (!sockets.getStackInSlot(slot).isEmpty()) {
			if (ChipsetManager.circuitRegistry.isChipset(sockets.getStackInSlot(slot))) {
				ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(sockets.getStackInSlot(slot));
				if (chipset != null) {
					chipset.onRemoval(this);
				}
			}
		}

		sockets.setInventorySlotContents(slot, stack);
		if (stack.isEmpty()) {
			return;
		}

		ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(stack);
		if (chipset != null) {
			chipset.onInsertion(this);
		}
	}

	@Override
	public ICircuitSocketType getSocketType() {
		return CircuitSocketType.HABITAT_FORMER;
	}

	/* Methods - SAVING & LOADING */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setTag(CONTAINER_NBT_KEY, logic.writeToNBT(new NBTTagCompound()));

		sockets.writeToNBT(data);

		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		if (data.hasKey(CONTAINER_NBT_KEY)) {
			NBTTagCompound nbtTag = data.getCompoundTag(CONTAINER_NBT_KEY);
			logic.readFromNBT(nbtTag);
		}

		sockets.readFromNBT(data);

		ItemStack chip = sockets.getStackInSlot(0);
		if (!chip.isEmpty()) {
			ICircuitBoard chipset = ChipsetManager.circuitRegistry.getCircuitBoard(chip);
			if (chipset != null) {
				chipset.onLoad(this);
			}
		}
	}

	private static class Vector {
		private final float x;
		private final float z;

		private Vector(float value) {
			this(value, value);
		}

		private Vector(float x, float z) {
			this.x = x;
			this.z = z;
		}

		public double distance(Vector other) {
			return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(z - other.z, 2.0));
		}

		private long toChunkPos() {
			return ChunkPos.asLong((int) x, (int) z);
		}
	}
}
