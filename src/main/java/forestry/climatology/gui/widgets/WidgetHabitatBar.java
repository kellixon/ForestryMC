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
package forestry.climatology.gui.widgets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.climate.IClimateState;
import forestry.api.core.ForestryAPI;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.gui.GuiHabitatformer;
import forestry.core.climate.ClimateStates;
import forestry.core.gui.tooltips.ToolTip;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.render.TextureManagerForestry;

public class WidgetHabitatBar extends Widget {
	private static final Comparator<ClimateButton> BUTTON_COMPERATOR = Comparator.comparingDouble(ClimateButton::getComparingCode);
	private final List<ClimateButton> buttons = new ArrayList<>();
	private final IClimateLogic logic;

	public WidgetHabitatBar(WidgetManager manager, int xPos, int yPos, IClimateLogic logic) {
		super(manager, xPos, yPos);
		this.width = 100;
		this.height = 20;
		this.logic = logic;
		for (EnumClimate climate : EnumClimate.values()) {
			buttons.add(new ClimateButton(this, climate, xPos + climate.ordinal() * 20, yPos));
		}
	}

	@Override
	public void draw(int startX, int startY) {
		for (ClimateButton button : buttons) {
			button.draw(startX, startY);
		}
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.bindTexture(manager.gui.textureFile);
		Optional<ClimateButton> optional = buttons.stream().min(BUTTON_COMPERATOR);
		if (!optional.isPresent()) {
			return;
		}
		ClimateButton button = optional.get();
		manager.gui.drawTexturedModalRect(startX + button.xPos - 2, startY + yPos - 2, 0, 166, 24, 24);
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		for (ClimateButton button : buttons) {
			if (button.isMouseOver(mouseX, mouseY)) {
				return button.getToolTip();
			}
		}
		return null;
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		mouseX -= manager.gui.getGuiLeft();
		mouseY -= manager.gui.getGuiTop();
		for (ClimateButton button : buttons) {
			if (button.isMouseOver(mouseX, mouseY)) {
				IClimateState climateState = button.climate.climateState;
				((GuiHabitatformer) manager.gui).setClimate(climateState);
			}
		}
	}

	@Override
	public boolean handleMouseRelease(int mouseX, int mouseY, int eventType) {
		return isMouseOver(mouseX, mouseY);
	}

	protected void drawSprite(TextureAtlasSprite sprite, int x, int y) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0F);
		TextureManagerForestry.getInstance().bindGuiTextureMap();
		manager.gui.drawTexturedModalRect(x, y, sprite, 16, 16);
	}

	private enum EnumClimate {
		ICY("habitats/snow", Biomes.ICE_PLAINS),
		COLD("habitats/taiga", Biomes.TAIGA),
		NORMAL("habitats/plains", Biomes.PLAINS),
		WARM("habitats/jungle", Biomes.JUNGLE),
		HOT("habitats/desert", Biomes.DESERT);
		private IClimateState climateState;
		private String spriteName;

		EnumClimate(String spriteName, Biome biome) {
			climateState = ClimateStates.of(biome.getDefaultTemperature(), biome.getRainfall());
			this.spriteName = spriteName;
		}

		@SideOnly(Side.CLIENT)
		public TextureAtlasSprite getSprite() {
			return ForestryAPI.textureManager.getDefault(spriteName);
		}
	}

	private class ClimateButton {
		final WidgetHabitatBar parent;
		final EnumClimate climate;
		protected final ToolTip toolTip = new ToolTip(250) {
			@Override
			@SideOnly(Side.CLIENT)
			public void refresh() {
				toolTip.clear();
				toolTip.add("T: " + climate.climateState.getTemperature());
				toolTip.add("H: " + climate.climateState.getHumidity());
			}
		};
		final int xPos;
		final int yPos;

		private ClimateButton(WidgetHabitatBar parent, EnumClimate climate, int xPos, int yPos) {
			this.parent = parent;
			this.climate = climate;
			this.xPos = xPos;
			this.yPos = yPos;
		}

		public void draw(int startX, int startY) {
			parent.drawSprite(climate.getSprite(), startX + xPos + 2, startY + yPos + 2);
		}

		public ToolTip getToolTip() {
			return toolTip;
		}

		public boolean isMouseOver(int mouseX, int mouseY) {
			return mouseX >= xPos && mouseX <= xPos + 20 && mouseY >= yPos && mouseY <= yPos + 20;
		}

		private double getComparingCode() {
			IClimateState target = logic.getTargetedState();
			IClimateState state = climate.climateState;
			double temp = target.getTemperature() - state.getTemperature();
			double hem = target.getHumidity() - state.getHumidity();
			return Math.abs(temp + hem);
		}
	}
}
