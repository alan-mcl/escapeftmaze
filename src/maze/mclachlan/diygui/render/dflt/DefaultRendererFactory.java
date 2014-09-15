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

import mclachlan.diygui.toolkit.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;

/**
 *
 */
public class DefaultRendererFactory extends RendererFactory
{
	static final Color PANEL_BACKGROUND = Color.LIGHT_GRAY;
	static final Color LABEL_FOREGROUND = Color.GRAY.brighter();
	
	Map<String, Renderer> renderers = new HashMap<String, Renderer>();
	
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
}
