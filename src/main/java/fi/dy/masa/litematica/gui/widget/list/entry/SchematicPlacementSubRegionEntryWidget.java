package fi.dy.masa.litematica.gui.widget.list.entry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import fi.dy.masa.malilib.gui.BaseScreen;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.gui.widget.IconWidget;
import fi.dy.masa.malilib.gui.widget.button.GenericButton;
import fi.dy.masa.malilib.gui.widget.button.OnOffButton;
import fi.dy.masa.malilib.gui.widget.list.entry.BaseDataListEntryWidget;
import fi.dy.masa.malilib.gui.widget.list.entry.DataListEntryWidgetData;
import fi.dy.masa.malilib.render.text.StyledTextLine;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.gui.SchematicPlacementSubRegionSettingsScreen;
import fi.dy.masa.litematica.gui.util.LitematicaIcons;
import fi.dy.masa.litematica.schematic.ISchematicRegion;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.util.PositionUtils;

public class SchematicPlacementSubRegionEntryWidget extends BaseDataListEntryWidget<SubRegionPlacement>
{
    protected final SchematicPlacementManager manager;
    protected final SchematicPlacement placement;
    protected final GenericButton configureButton;
    protected final GenericButton toggleEnabledButton;
    protected final IconWidget modificationNoticeIcon;
    protected int buttonsStartX;

    public SchematicPlacementSubRegionEntryWidget(SubRegionPlacement data,
                                                  DataListEntryWidgetData constructData,
                                                  SchematicPlacement placement)
    {
        super(data, constructData);

        this.manager = DataManager.getSchematicPlacementManager();
        this.placement = placement;

        this.configureButton     = GenericButton.create(18, "litematica.button.misc.configure", this::openConfigurationMenu);
        this.toggleEnabledButton = OnOffButton.onOff(18, "litematica.button.placement_list.placement_enabled", data::isEnabled, this::toggleEnabled);
        this.modificationNoticeIcon = new IconWidget(LitematicaIcons.NOTICE_EXCLAMATION_11);
        this.modificationNoticeIcon.translateAndAddHoverString("litematica.hover.placement_list.icon.placement_modified");

        String key = data.isEnabled() ? "litematica.button.schematic_placement_settings.entry_name.enabled" :
                                        "litematica.button.schematic_placement_settings.entry_name.disabled";
        this.setText(StyledTextLine.translate(key, data.getName()));

        this.getBackgroundRenderer().getNormalSettings().setEnabledAndColor(true, this.isOdd ? 0xA0101010 : 0xA0303030);
        this.getBackgroundRenderer().getHoverSettings().setEnabledAndColor(true, 0xA0707070);
        this.addHoverInfo();
    }

    @Override
    public void reAddSubWidgets()
    {
        super.reAddSubWidgets();

        this.addWidget(this.configureButton);
        this.addWidget(this.toggleEnabledButton);

        if (this.data.isRegionPlacementModifiedFromDefault())
        {
            this.addWidget(this.modificationNoticeIcon);
        }
    }

    @Override
    public void updateSubWidgetPositions()
    {
        super.updateSubWidgetPositions();

        this.modificationNoticeIcon.centerVerticallyInside(this);
        this.configureButton.centerVerticallyInside(this);
        this.toggleEnabledButton.centerVerticallyInside(this);

        this.toggleEnabledButton.setRight(this.getRight() - 2);
        this.configureButton.setRight(this.toggleEnabledButton.getX() - 1);
        this.modificationNoticeIcon.setRight(this.configureButton.getX() - 2);

        this.buttonsStartX = this.modificationNoticeIcon.getX() - 1;
    }

    @Override
    protected boolean isSelected()
    {
        SchematicPlacement placement = this.manager.getSelectedSchematicPlacement();
        return placement != null && placement.getSelectedSubRegionPlacement() == this.data;
    }

    @Override
    public boolean canHoverAt(int mouseX, int mouseY, int mouseButton)
    {
        return mouseX <= this.buttonsStartX && super.canHoverAt(mouseX, mouseY, mouseButton);
    }

    protected void openConfigurationMenu()
    {
        SchematicPlacementSubRegionSettingsScreen screen = new SchematicPlacementSubRegionSettingsScreen(this.placement, this.data);
        screen.setParent(GuiUtils.getCurrentScreen());
        BaseScreen.openScreen(screen);
    }

    protected void toggleEnabled()
    {
        DataManager.getSchematicPlacementManager().toggleSubRegionEnabled(this.placement, this.data.getName());
        this.listWidget.refreshEntries();
    }

    protected void addHoverInfo()
    {
        List<String> lines = new ArrayList<>();

        if (this.data.isRegionPlacementModifiedFromDefault())
        {
            lines.add(StringUtils.translate("litematica.hover.placement_list.sub_region_modified"));
        }

        ISchematicRegion region = this.placement.getSchematic().getSchematicRegion(this.data.getName());
        Vec3i size = region != null ? region.getSize() : null;

        if (size != null)
        {
            lines.add(StringUtils.translate("litematica.hover.placement_list.sub_region_size",
                                            size.getX(), size.getY(), size.getZ()));
        }

        lines.add(StringUtils.translate("litematica.hover.placement_list.rotation",
                                        PositionUtils.getRotationNameShort(this.data.getRotation())));
        lines.add(StringUtils.translate("litematica.hover.placement_list.mirror",
                                        PositionUtils.getMirrorName(this.data.getMirror())));

        BlockPos relativeOrigin = this.data.getPos();
        BlockPos o = PositionUtils.getTransformedBlockPos(relativeOrigin, this.placement.getMirror(),
                                                          this.placement.getRotation()).add(this.placement.getOrigin());
        lines.add(StringUtils.translate("litematica.hover.placement_list.origin", o.getX(), o.getY(), o.getZ()));

        this.getHoverInfoFactory().addStrings(lines);
    }
}
