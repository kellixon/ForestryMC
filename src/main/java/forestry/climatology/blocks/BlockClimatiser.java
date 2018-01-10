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
package forestry.climatology.blocks;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.climatology.ModuleClimatology;
import forestry.core.blocks.BlockBase;
import forestry.core.render.MachineStateMapper;
import forestry.core.utils.ItemTooltipUtil;

public class BlockClimatiser extends BlockBase<BlockTypeClimatology> implements IItemModelRegister, ITileEntityProvider {

	public BlockClimatiser(BlockTypeClimatology type) {
		super(type);

		setHardness(1.0f);
		setHarvestLevel("pickaxe", 0);
		setCreativeTab(ModuleClimatology.getGreenhouseTab());
	}

	/* MODELS */
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerStateMapper() {
		ModelLoader.setCustomStateMapper(this, new MachineStateMapper<>(blockType, "habitatformer/"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel(Item item, IModelManager manager) {
		ResourceLocation registry = getRegistryName();
		manager.registerItemModel(item, 0, "habitatformer/" + registry.getResourcePath().replace("habitatformer_", ""));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		ItemTooltipUtil.addShiftInformation(stack, world, tooltip, flag);
	}
}
