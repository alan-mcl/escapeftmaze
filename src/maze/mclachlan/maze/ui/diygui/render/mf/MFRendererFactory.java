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

package mclachlan.maze.ui.diygui.render.mf;

import java.awt.image.BufferedImage;
import java.util.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.ui.diygui.render.maze.FoeGroupWidgetRenderer;
import mclachlan.maze.ui.diygui.render.maze.FormationWidgetRenderer;
import mclachlan.maze.ui.diygui.render.maze.ItemSelectionWidgetRenderer;
import mclachlan.maze.ui.diygui.render.maze.TradingWidgetRenderer;
import mclachlan.maze.util.MazeException;

/**
 * UI skin from 'Game UI Interface Pack - Medieval Fantasy Edition'.
 * https://graphicriver.net/user/eldamien
 */
public class MFRendererFactory extends RendererFactory
{
	public static final String ITEM_WIDGET = "ItemWidget";
	public static final String DROPPED_ITEM_WIDGET = "DroppedItemWidget";
	public static final String ITEM_SELECTION_WIDGET = "ItemSelectionWidget";
	public static final String FORMATION_WIDGET = "FormationWidget";
	public static final String FOE_GROUP_WIDGET = "FoeGroupWidget";
	public static final String TRADING_WIDGET = "TradingWidget";
	public static final String FILLED_BAR_WIDGET = "FilledBarWidget";

	private final Map<String, Renderer> renderers = new HashMap<String, Renderer>();
	private final MFRendererProperties mfRendererProperties = new MFRendererProperties();


	/*-------------------------------------------------------------------------*/
	public MFRendererFactory()
	{
		// standard MF renderers
		renderers.put(DIYToolkit.PANEL, new MFPanelRenderer());
		renderers.put(DIYToolkit.BUTTON, new MFButtonRenderer());
		renderers.put(DIYToolkit.SCROLL_PANE, new MFScrollPaneRenderer());
		renderers.put(DIYToolkit.RADIO_BUTTON, new MFRadioButtonRenderer());
		renderers.put(DIYToolkit.CHECKBOX, new MFCheckboxRenderer());
		renderers.put(DIYToolkit.TEXT_AREA, new MFTextAreaRenderer());
		renderers.put(DIYToolkit.LIST_BOX_ITEM, new MFListBoxRenderer());
		renderers.put(DIYToolkit.LABEL, new MFLabelRenderer());
		renderers.put(DIYToolkit.TEXT_FIELD, new MFTextFieldRenderer());
		renderers.put(DIYToolkit.TOOLTIP, new MFTooltipRenderer());
		renderers.put(DIYToolkit.COMBO_BOX, new MFComboBoxRenderer());
		renderers.put(DIYToolkit.COMBO_ITEM, new MFComboItemRenderer());

		// custom MF renderers
		renderers.put(FILLED_BAR_WIDGET, new MFFilledBarWidgetRenderer());
		renderers.put(ITEM_WIDGET, new MFItemWidgetRenderer());

		// Fallback to Maze renderers
		renderers.put(DIYToolkit.NONE, new NullRenderer());
		renderers.put(DIYToolkit.PANE, new NullRenderer());

		// Fallback to custom maze renderers
		renderers.put(ITEM_SELECTION_WIDGET, new ItemSelectionWidgetRenderer());
		renderers.put(FORMATION_WIDGET, new FormationWidgetRenderer());
		renderers.put(FOE_GROUP_WIDGET, new FoeGroupWidgetRenderer());
		renderers.put(TRADING_WIDGET, new TradingWidgetRenderer());
	}

	/*-------------------------------------------------------------------------*/
	public Renderer getRenderer(String widgetName)
	{
		Renderer renderer = this.renderers.get(widgetName);
		
		if (renderer == null)
		{
			throw new MazeException("No renderer for ["+widgetName+"]");
		}
		
		return renderer;
	}

	@Override
	public RendererProperties getRendererProperties()
	{
		return mfRendererProperties;
	}

	/*-------------------------------------------------------------------------*/
	private static class MFRendererProperties implements RendererProperties
	{
		@Override
		public int getProperty(Property p)
		{
			return switch (p)
				{
					case TRANSPARENT_PANEL_BORDER -> 10;
					case PANEL_HEAVY_BORDER -> 30;
					case PANEL_MED_BORDER -> 25;
					case PANEL_LIGHT_BORDER -> 12;
					case IMAGE_BACK_PANEL_BORDER -> 10;

					case DIALOG_BORDER -> 30;
					case INSET -> 5;
					case TITLE_PANE_HEIGHT -> 50;
					case BUTTON_PANE_HEIGHT -> 40;
					case SCROLLBAR_WIDTH -> 40;
					case SLIDER_WIDTH -> 36;
					case SLIDER_HEIGHT -> 99;

					case ITEM_WIDGET_SIZE -> 40;
					case CONDITION_ICON_SIZE -> 22;

					case PCW_PORTRAIT_FRAME_BORDER -> 9;
					case PCW_PORTRAIT_HEIGHT -> 102;
					case PCW_PORTRAIT_WIDTH -> 102;
				};
		}

		@Override
		public BufferedImage getImageResource(String imageId)
		{
			if ("screen/loading_screen".equals(imageId))
			{
				int index = Dice.d4.roll("MF loading screen");
				imageId = imageId +"_"+index;
			}

			return Database.getInstance().getImage("ui/mf/"+imageId);
		}
	}
}
