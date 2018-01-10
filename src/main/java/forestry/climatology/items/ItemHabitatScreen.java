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
package forestry.climatology.items;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.climate.IClimateState;
import forestry.climatology.ClimateSystem;
import forestry.climatology.ModuleClimatology;
import forestry.core.items.IColoredItem;
import forestry.core.items.ItemForestry;
import forestry.core.utils.Translator;

public class ItemHabitatScreen extends ItemForestry implements IColoredItem {

	public static final String GREENHOUSE_KEY = "greenhouse";
	public static final String PREVIEW_KEY = "preview";

	public ItemHabitatScreen() {
		setCreativeTab(ModuleClimatology.getGreenhouseTab());
	}

	public static boolean isPreviewModeActive(ItemStack itemStack) {
		NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
		if (nbtTagCompound == null || !nbtTagCompound.hasKey(PREVIEW_KEY)) {
			return false;
		}
		return nbtTagCompound.getBoolean(PREVIEW_KEY);
	}

	public static void setPreviewMode(ItemStack itemStack, boolean preview) {
		itemStack.setTagInfo(PREVIEW_KEY, new NBTTagByte((byte) (preview ? 1 : 0)));
	}

	@Nullable
	public static BlockPos getGreenhousePos(ItemStack itemStack) {
		NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
		if (nbtTagCompound == null || !nbtTagCompound.hasKey(GREENHOUSE_KEY)) {
			return null;
		}
		NBTTagCompound compound = nbtTagCompound.getCompoundTag(GREENHOUSE_KEY);
		if (compound.hasNoTags()) {
			return null;
		}
		return NBTUtil.getPosFromTag(compound);
	}

	public static boolean hasGreenhousePos(ItemStack itemStack) {
		return getGreenhousePos(itemStack) != null;
	}

	public static boolean isValid(ItemStack stack, World world) {
		BlockPos pos = getGreenhousePos(stack);
		boolean isValid = true;
		if (pos == null || world == null || !world.isBlockLoaded(pos)) {
			isValid = false;
		} else {
			/*IHabitatFormerControllerInternal controller = MultiblockUtil.getController(world, pos, IArboretumComponent.class);
			if (controller == null || !controller.isAssembled()) {
				isValid = false;
			}*/
		}
		return isValid;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemStack = player.getHeldItem(hand);
		if (!player.isSneaking()) {
			boolean previewModeActive = isPreviewModeActive(itemStack);
			setPreviewMode(itemStack, !previewModeActive);

			if (world.isRemote) {
				player.sendStatusMessage(!previewModeActive ? new TextComponentTranslation("for.message.habitat_screen.preview.active") : new TextComponentTranslation("for.message.habitat_screen.preview.inactive"), true);
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		IClimateState state = ClimateSystem.INSTANCE.getChunkState(world, pos);
		if (state.isPresent()) {
			player.sendStatusMessage(new TextComponentString("Found State: " + state.toString()), true);
		} else {
			player.sendStatusMessage(new TextComponentString("No State Found"), true);
		}
		return EnumActionResult.SUCCESS;
	/*	ItemStack itemStack = player.getHeldItem(hand);
		BlockPos itemPos = getGreenhousePos(itemStack);
		if (!player.isSneaking()) {
			return EnumActionResult.PASS;
		}
		return handleSneaking(world, pos, player, itemStack, itemPos);*/
	}

	private EnumActionResult handleSneaking(World world, BlockPos pos, EntityPlayer player, ItemStack itemStack, BlockPos itemPos) {
		/*IArboretumComponent component = MultiblockUtil.getComponent(world, pos, IArboretumComponent.class);
		if (component != null) {
			IArboretumController controller = component.getMultiblockLogic().getController();
			if (!controller.isAssembled()) {
				if (!world.isRemote) {
					player.sendStatusMessage(new TextComponentTranslation("for.message.habitat_screen.notassembled"), true);
				}
				return EnumActionResult.PASS;
			}
			itemStack.setTagInfo(GREENHOUSE_KEY, NBTUtil.createPosTag(pos));
			if (!world.isRemote) {
				player.sendStatusMessage(new TextComponentTranslation("for.message.habitat_screen.position"), true);
			}
		} else {
			if (itemPos == null) {
				if (!world.isRemote) {
					player.sendStatusMessage(new TextComponentTranslation("for.message.habitat_screen.fail"), true);
				}
				return EnumActionResult.PASS;
			}
			if (!world.isBlockLoaded(pos)) {
				if (!world.isRemote) {
					player.sendStatusMessage(new TextComponentTranslation("for.message.habitat_screen.away"), true);
				}
				return EnumActionResult.PASS;
			}
			IArboretumController controller = MultiblockUtil.getController(world, itemPos, IArboretumComponent.class);
			if (controller == null || !controller.isAssembled()) {
				return EnumActionResult.PASS;
			}
			if (!world.isRemote) {
				player.sendStatusMessage(new TextComponentTranslation("for.message.habitat_screen.center", pos), true);
			}
		}*/
		return EnumActionResult.PASS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);
		boolean previewModeActive = isPreviewModeActive(stack);
		String previewMode = Translator.translateToLocal(previewModeActive ? "for.habitat_screen.mode.active" : "for.habitat_screen.mode.inactive");
		tooltip.add(Translator.translateToLocal(Translator.translateToLocalFormatted("for.habitat_screen.mode", previewMode)));
		boolean isValid = isValid(stack, world);
		BlockPos pos = getGreenhousePos(stack);
		String state = isValid ? Translator.translateToLocalFormatted("for.habitat_screen.state.linked", pos.getX(), pos.getY(), pos.getZ()) : Translator.translateToLocal("for.habitat_screen.state.fail");
		tooltip.add(Translator.translateToLocalFormatted("for.habitat_screen.state", state));
		if (!isValid) {
			return;
		}
		/*IHabitatFormerControllerInternal controller = MultiblockUtil.getController(world, pos, IArboretumComponent.class);
		IClimateState climateState = controller.getLogic().getState();
		tooltip.add(Translator.translateToLocalFormatted("for.habitat_screen.temperature", TextFormatting.GOLD + StringUtil.floatAsPercent(climateState.getTemperature())));
		tooltip.add(Translator.translateToLocalFormatted("for.habitat_screen.humidity", TextFormatting.BLUE + StringUtil.floatAsPercent(climateState.getHumidity())));*/
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		/*if (tintIndex == 2) {
			return isValid(stack, Minecraft.getMinecraft().world) ? 0x14B276 : 0xBA1F17;
		} else if (tintIndex == 1) {
			World world = Minecraft.getMinecraft().world;
			if (!isValid(stack, world)) {
				return 0xFFFFFF;
			}
			BlockPos pos = getGreenhousePos(stack);
			IHabitatFormerControllerInternal controller = MultiblockUtil.getController(world, pos, IArboretumComponent.class);
			IClimateLogic container = controller.getLogic();
			IClimateState state = container.getState();
			return ClimateUtil.getColor(EnumTemperature.getFromValue(state.getTemperature()));
		}*/
		return 0xFFFFFF;
	}
}
