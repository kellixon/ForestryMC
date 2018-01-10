package forestry.climatology;

import net.minecraftforge.client.event.RenderWorldLastEvent;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

	@SubscribeEvent
	public void onWorldRenderLast(RenderWorldLastEvent event) {
	}
}
