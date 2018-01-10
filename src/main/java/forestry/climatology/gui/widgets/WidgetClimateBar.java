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

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;

import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.gui.GuiHabitatformer;
import forestry.core.climate.ClimateStates;
import forestry.core.gui.tooltips.ToolTip;
import forestry.core.gui.widgets.Widget;
import forestry.core.gui.widgets.WidgetManager;
import forestry.core.utils.Translator;

public class WidgetClimateBar extends Widget {

	private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("#.##");

	public static final float MAX_VALUE = 2.0F;

	private final IClimateLogic housing;
	private final ClimateType type;

	public WidgetClimateBar(WidgetManager manager, int xPos, int yPos, IClimateLogic housing, ClimateType type) {
		super(manager, xPos, yPos);
		this.width = 52;
		this.height = 12;
		this.housing = housing;
		this.type = type;
	}

	@Override
	public void draw(int startX, int startY) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.bindTexture(manager.gui.textureFile);
		setGLColorFromInt(type == ClimateType.TEMPERATURE ? 0xFFD700 : 0x7ff4f4);
		int progressScaled = getProgressScaled();
		manager.gui.drawTexturedModalRect(startX + xPos, startY + yPos, 24, 166, progressScaled, height);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		manager.gui.drawTexturedModalRect(startX + xPos + getPointerPosition(), startY + yPos, 24, 178, width, height);
		manager.gui.drawTexturedModalRect(startX + xPos, startY + yPos, 76, 166, width, height);
	}

	private int getProgressScaled() {
		float value = housing.getState().get(type);
		return (int) (value * width / MAX_VALUE);
	}

	private int getPointerPosition() {
		float targetedValue = housing.getTargetedState().get(type);
		return (int) (targetedValue * 49 / MAX_VALUE);
	}

	private static void setGLColorFromInt(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;

		GlStateManager.color(red, green, blue, 1.0F);
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		final int x = mouseX - manager.gui.getGuiLeft() - xPos;
		final int y = mouseY - manager.gui.getGuiTop() - yPos;
		if (x <= 1 || y <= 1 || x >= width - 1 || y >= height - 1) {
			return;
		}
		final float quotient = x / (float) (width - 2);
		final float value = 2.0F * quotient;
		GuiHabitatformer former = (GuiHabitatformer) manager.gui;
		IClimateState climateState = former.getClimate();
		IClimateState newState;
		if (type == ClimateType.TEMPERATURE) {
			newState = ClimateStates.of(value, climateState.getHumidity());
		} else {
			newState = ClimateStates.of(climateState.getTemperature(), value);
		}
		former.setClimate(newState);
	}

	@Override
	public ToolTip getToolTip(int mouseX, int mouseY) {
		IClimateState targetedState = housing.getTargetedState();
		IClimateState state = housing.getState();
		ToolTip toolTip = new ToolTip();
		toolTip.add(Translator.translateToLocalFormatted("for.gui.habitat_former.climate.target", VALUE_FORMAT.format(targetedState.get(type))));
		toolTip.add(Translator.translateToLocalFormatted("for.gui.habitat_former.climate.value", VALUE_FORMAT.format(state.get(type))));
		return toolTip;
	}
}
