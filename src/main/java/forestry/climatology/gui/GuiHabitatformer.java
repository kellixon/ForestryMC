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
package forestry.climatology.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import forestry.api.climate.ClimateType;
import forestry.api.climate.IClimateState;
import forestry.climatology.api.climate.IClimateLogic;
import forestry.climatology.gui.widgets.WidgetClimateBar;
import forestry.climatology.gui.widgets.WidgetHabitatBar;
import forestry.climatology.network.packets.PacketSelectClimateTargeted;
import forestry.climatology.tiles.TileHabitatFormer;
import forestry.core.config.Constants;
import forestry.core.gui.GuiForestryTitled;
import forestry.core.gui.widgets.SocketWidget;
import forestry.core.utils.NetworkUtil;
import forestry.core.utils.Translator;

public class GuiHabitatformer extends GuiForestryTitled<ContainerHabitatformer> {

	private final TileHabitatFormer tile;
	private final IClimateLogic logic;

	public GuiHabitatformer(EntityPlayer player, TileHabitatFormer tile) {
		super(Constants.TEXTURE_PATH_GUI + "/habitat_former.png", new ContainerHabitatformer(player.inventory, tile), tile);
		this.logic = tile.getLogic();
		this.tile = tile;

		widgetManager.add(new WidgetHabitatBar(widgetManager, 38, 24, logic));
		widgetManager.add(new WidgetClimateBar(widgetManager, 26, 64, logic, ClimateType.TEMPERATURE));
		widgetManager.add(new WidgetClimateBar(widgetManager, 98, 64, logic, ClimateType.HUMIDITY));
		widgetManager.add(new SocketWidget(widgetManager, 150, 26, tile, 0));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
		drawCenteredString(Translator.translateToLocal("for.gui.habitatformer.climate.temperature"), 52, 54);
		drawCenteredString(Translator.translateToLocal("for.gui.habitatformer.climate.humidity"), 124, 54);
	}

	private void drawCenteredString(String text, int x, int y) {
		fontRenderer.drawStringWithShadow(text, guiLeft + (float) (x - (double) fontRenderer.getStringWidth(text) / 2), (float) guiTop + y, 16777215);
	}

	public void setClimate(IClimateState state) {
		logic.setTargetedState(state.copy());
		sendNetworkUpdate();
	}

	public IClimateState getClimate() {
		return logic.getTargetedState();
	}

	private void sendNetworkUpdate() {
		IClimateState targetedState = logic.getTargetedState();
		if (targetedState.isPresent()) {
			BlockPos pos = tile.getPos();
			NetworkUtil.sendToServer(new PacketSelectClimateTargeted(pos, targetedState));
		}
	}

	@Override
	protected void addLedgers() {
		addErrorLedger(tile);
		addPowerLedger(tile.getEnergyManager());
		addClimateLedger(tile);
		addHintLedger("habitatformer");
	}
}
