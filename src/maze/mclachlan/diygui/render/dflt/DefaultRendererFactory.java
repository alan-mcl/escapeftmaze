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

package mclachlan.diygui.render.dflt;

import java.awt.image.BufferedImage;
import mclachlan.diygui.toolkit.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import mclachlan.maze.data.Database;

/**
 *
 */
public class DefaultRendererFactory extends RendererFactory
{
	static final Color PANEL_BACKGROUND = Color.LIGHT_GRAY;
	static final Color LABEL_FOREGROUND = Color.GRAY.brighter();
	private final DefaultRendererProperties defaultRendererProperties = new DefaultRendererProperties();

	Map<String, Renderer> renderers = new HashMap<>();

	/*-------------------------------------------------------------------------*/
	public DefaultRendererFactory()
	{
		renderers.put(DIYToolkit.NONE, new NullRenderer());
		renderers.put(DIYToolkit.LABEL, new DefaultLabelRenderer());
		renderers.put(DIYToolkit.PANE, new NullRenderer());
		renderers.put(DIYToolkit.PANEL, new DefaultPanelRenderer());
		renderers.put(DIYToolkit.BUTTON, new DefaultButtonRenderer());
		renderers.put(DIYToolkit.SCROLL_PANE, new DefaultScrollPaneRenderer());
		renderers.put(DIYToolkit.TEXT_AREA, new DefaultTextAreaRenderer());
		renderers.put(DIYToolkit.TEXT_FIELD, new DefaultTextFieldRenderer());
		renderers.put(DIYToolkit.CHECKBOX, new DefaultCheckboxRenderer());
		renderers.put(DIYToolkit.RADIO_BUTTON, new DefaultRadioButtonRenderer());
		renderers.put(DIYToolkit.LIST_BOX_ITEM, new DefaultListBoxRenderer());
		renderers.put(DIYToolkit.COMBO_BOX, new DefaultComboBoxRenderer());
		renderers.put(DIYToolkit.COMBO_ITEM, new DefaultComboItemRenderer());
		renderers.put(DIYToolkit.TOOLTIP, new DefaultTooltipRenderer());
	}

	/*-------------------------------------------------------------------------*/
	public Renderer getRenderer(String widgetName)
	{
		Renderer renderer = this.renderers.get(widgetName);
		
		if (renderer == null)
		{
			throw new DIYException("No renderer for ["+widgetName+"]");
		}
		
		return renderer;
	}

	@Override
	public RendererProperties getRendererProperties()
	{
		return defaultRendererProperties;
	}

	/*-------------------------------------------------------------------------*/
	private static class DefaultRendererProperties implements RendererProperties
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
				case BUTTON_PANE_HEIGHT -> 20;

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
			return Database.getInstance().getImage(imageId);
		}
	}
}
