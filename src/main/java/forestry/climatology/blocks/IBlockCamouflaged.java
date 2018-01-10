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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBlockCamouflaged<B extends Block & IBlockCamouflaged<B>> {

	@SideOnly(Side.CLIENT)
	boolean hasOverlaySprite(int meta, int layer);

	int getLayers();

	@SideOnly(Side.CLIENT)
	TextureAtlasSprite getOverlaySprite(@Nullable EnumFacing facing, @Nullable IBlockState state, int meta, int layer);
}
