package fi.dy.masa.litematica.event;

import net.minecraft.client.Minecraft;
import fi.dy.masa.malilib.event.PostGameOverlayRenderer;
import fi.dy.masa.malilib.event.PostWorldRenderer;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.util.game.wrap.GameUtils;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.render.LitematicaRenderer;
import fi.dy.masa.litematica.render.OverlayRenderer;
import fi.dy.masa.litematica.render.infohud.InfoHud;
import fi.dy.masa.litematica.render.infohud.ToolHud;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import fi.dy.masa.litematica.scheduler.tasks.SetSchematicPreviewTask;
import fi.dy.masa.litematica.tool.ToolMode;

public class RenderHandler implements PostGameOverlayRenderer, PostWorldRenderer
{
    @Override
    public void onPostWorldRender(float tickDelta)
    {
        if (Configs.Visuals.MAIN_RENDERING_TOGGLE.getBooleanValue())
        {
            boolean invert = Hotkeys.INVERT_SCHEMATIC_RENDER_STATE.isHeld();

            if (Configs.Visuals.SCHEMATIC_RENDERING.getBooleanValue() != invert &&
                Configs.Generic.BETTER_RENDER_ORDER.getBooleanValue() == false)
            {
                LitematicaRenderer.getInstance().renderSchematicWorld(tickDelta);
            }

            OverlayRenderer.getInstance().renderBoxes(tickDelta);

            if (Configs.InfoOverlays.VERIFIER_OVERLAY_RENDERING.getBooleanValue())
            {
                OverlayRenderer.getInstance().renderSchematicVerifierMismatches(tickDelta);
            }

            if (Configs.Visuals.RENDER_COLLIDING_BLOCK_AT_CURSOR.getBooleanValue())
            {
                boolean render = Configs.Visuals.SCHEMATIC_BLOCKS_RENDERING.getBooleanValue() &&
                                 Configs.Visuals.SCHEMATIC_RENDERING.getBooleanValue() != invert;

                if (render)
                {
                    OverlayRenderer.getInstance().renderHoveredSchematicBlock(tickDelta);
                }
            }

            if (DataManager.getToolMode() == ToolMode.SCHEMATIC_EDIT)
            {
                OverlayRenderer.getInstance().renderSchematicRebuildTargetingOverlay(tickDelta);
            }
        }
    }

    @Override
    public void onPostGameOverlayRender()
    {
        if (Configs.Visuals.MAIN_RENDERING_TOGGLE.getBooleanValue())
        {
            // The Info HUD renderers can decide if they want to be rendered in GUIs
            InfoHud.getInstance().renderHud();

            if (GuiUtils.getCurrentScreen() == null)
            {
                Minecraft mc = GameUtils.getClient();
                ToolHud.getInstance().renderHud();
                OverlayRenderer.getInstance().renderHoverInfo(mc);

                SetSchematicPreviewTask task = TaskScheduler.getInstanceClient().getFirstTaskOfType(SetSchematicPreviewTask.class);

                if (task != null)
                {
                    OverlayRenderer.getInstance().renderPreviewFrame();
                }
            }
        }
    }
}
