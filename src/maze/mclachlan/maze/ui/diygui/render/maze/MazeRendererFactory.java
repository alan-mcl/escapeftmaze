/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.ui.diygui.render.maze;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.util.MazeException;

/**
 * The bespoke "Maze" rendered mostly use J2D graphics to paint widgets.
 */
public class MazeRendererFactory extends RendererFactory
{
	public static final Color PANEL_BACKGROUND = Color.LIGHT_GRAY;
	public static final Color LABEL_FOREGROUND = Constants.Colour.LIGHT_GREY;
	public static final Color DISABLED_LABEL_FOREGROUND = Constants.Colour.MED_GREY;

	public static final String ITEM_WIDGET = "ItemWidget";
	public static final String DROPPED_ITEM_WIDGET = "DroppedItemWidget";
	public static final String ITEM_SELECTION_WIDGET = "ItemSelectionWidget";
	public static final String FORMATION_WIDGET = "FormationWidget";
	public static final String FOE_GROUP_WIDGET = "FoeGroupWidget";
	public static final String TRADING_WIDGET = "TradingWidget";
	public static final String MUGSHOT_WIDGET = "MugshotWidget";
	public static final String FILLED_BAR_WIDGET = "FilledBarWidget";

	private final Map<String, Renderer> renderers = new HashMap<String, Renderer>();
	private final MazeRendererProperties mazeRendererProperties = new MazeRendererProperties();


	/*-------------------------------------------------------------------------*/
	public MazeRendererFactory()
	{
		// standard renderers
		renderers.put(DIYToolkit.NONE, new NullRenderer());
		renderers.put(DIYToolkit.LABEL, new MazeLabelRenderer());
		renderers.put(DIYToolkit.PANE, new NullRenderer());
		renderers.put(DIYToolkit.PANEL, new MazePanelRenderer());
		renderers.put(DIYToolkit.BUTTON, new MazeButtonRenderer());
		renderers.put(DIYToolkit.SCROLL_PANE, new MazeScrollPaneRenderer());
		renderers.put(DIYToolkit.TEXT_AREA, new MazeTextAreaRenderer());
		renderers.put(DIYToolkit.TEXT_FIELD, new MazeTextFieldRenderer());
		renderers.put(DIYToolkit.CHECKBOX, new MazeCheckboxRenderer());
		renderers.put(DIYToolkit.RADIO_BUTTON, new MazeRadioButtonRenderer());
		renderers.put(DIYToolkit.LIST_BOX_ITEM, new MazeListBoxRenderer());
		renderers.put(DIYToolkit.COMBO_BOX, new MazeComboBoxRenderer());
		renderers.put(DIYToolkit.COMBO_ITEM, new MazeComboItemRenderer());

		// custom maze renderers
		renderers.put(ITEM_WIDGET, new ItemWidgetRenderer());
		renderers.put(DROPPED_ITEM_WIDGET, new DroppedItemWidgetRenderer());
		renderers.put(ITEM_SELECTION_WIDGET, new ItemSelectionWidgetRenderer());
		renderers.put(FORMATION_WIDGET, new FormationWidgetRenderer());
		renderers.put(FOE_GROUP_WIDGET, new FoeGroupWidgetRenderer());
		renderers.put(TRADING_WIDGET, new TradingWidgetRenderer());
		renderers.put(MUGSHOT_WIDGET, new MugshotWidgetRenderer());
		renderers.put(FILLED_BAR_WIDGET, new FilledBarWidgetRenderer());
	}

	/*-------------------------------------------------------------------------*/
	public Renderer getRenderer(String widgetName)
	{
		Renderer renderer = this.renderers.get(widgetName);

		if (renderer == null)
		{
			throw new MazeException("No renderer for [" + widgetName + "]");
		}

		return renderer;
	}

	@Override
	public RendererProperties getRendererProperties()
	{
		return mazeRendererProperties;
	}

	/*-------------------------------------------------------------------------*/
	private static class MazeRendererProperties implements RendererProperties
	{
		@Override
		public int getProperty(Property p)
		{
			return switch (p)
				{
					case TRANSPARENT_PANEL_BORDER -> 10;
					case PANEL_HEAVY_BORDER -> 20;
					case PANEL_MED_BORDER -> 15;
					case PANEL_LIGHT_BORDER -> 10;
					case IMAGE_BACK_PANEL_BORDER -> 10;
					case DIALOG_BORDER -> 20;

					case INSET -> 5;
					case TITLE_PANE_HEIGHT -> 20;
					case BUTTON_PANE_HEIGHT -> 25;

					case SCROLLBAR_WIDTH -> 20;
					case SLIDER_WIDTH -> 16;
					case SLIDER_HEIGHT -> 20;

					case ITEM_WIDGET_SIZE -> 40;
					case CONDITION_ICON_SIZE -> 22;

					case PCW_PORTRAIT_FRAME_BORDER -> 9;
					case PCW_PORTRAIT_WIDTH, PCW_PORTRAIT_HEIGHT -> 102;
				};
		}

		@Override
		public BufferedImage getImageResource(String imageId)
		{
			return Database.getInstance().getImage("ui/maze/"+imageId);
		}
	}

}
