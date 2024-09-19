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

import java.util.*;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.NullRenderer;
import mclachlan.diygui.toolkit.Renderer;
import mclachlan.diygui.toolkit.RendererFactory;
import mclachlan.maze.ui.diygui.render.maze.*;
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
	public static final String PLAYER_CHARACTER_WIDGET = "PlayerCharacterWidget";
	public static final String MUGSHOT_WIDGET = "MugshotWidget";
	public static final String FILLED_BAR_WIDGET = "FilledBarWidget";

	private final Map<String, Renderer> renderers = new HashMap<String, Renderer>();


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

		// custom MF renderers
		renderers.put(MUGSHOT_WIDGET, new MFMugshotWidgetRenderer());
		renderers.put(PLAYER_CHARACTER_WIDGET, new MFPlayerCharacterWidgetRenderer());

		// Fallback to Maze renderers
		renderers.put(DIYToolkit.NONE, new NullRenderer());
		renderers.put(DIYToolkit.PANE, new NullRenderer());
		renderers.put(DIYToolkit.COMBO_BOX, new MazeComboBoxRenderer());
		renderers.put(DIYToolkit.COMBO_ITEM, new MazeComboItemRenderer());

		// Fallback to custom maze renderers
		renderers.put(ITEM_WIDGET, new ItemWidgetRenderer());
		renderers.put(DROPPED_ITEM_WIDGET, new DroppedItemWidgetRenderer());
		renderers.put(ITEM_SELECTION_WIDGET, new ItemSelectionWidgetRenderer());
		renderers.put(FORMATION_WIDGET, new FormationWidgetRenderer());
		renderers.put(FOE_GROUP_WIDGET, new FoeGroupWidgetRenderer());
		renderers.put(TRADING_WIDGET, new TradingWidgetRenderer());
		renderers.put(FILLED_BAR_WIDGET, new FilledBarWidgetRenderer());
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

}
