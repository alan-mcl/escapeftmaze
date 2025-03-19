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

package mclachlan.diygui.toolkit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.*;

/**
 * Base class for widgets
 */
public abstract class Widget
{
	public int x, y, width, height;
	protected ContainerWidget parent;
	protected Renderer renderer;
	private Color background, foreground;
	
	private final java.util.List<ActionListener> listeners = new ArrayList<>();
	
	private String actionMessage;
	private Object actionPayload;
	boolean focus;
	boolean enabled = true;
	boolean visible = true;

	private String tooltip;
	private TimerTask tooltipTimerTask;

	/*-------------------------------------------------------------------------*/
	protected Widget(int x, int y, int width, int height)
	{
		setBounds(x, y, width, height);
		RendererFactory rendererFactory = DIYToolkit.getInstance().getRendererFactory();
		renderer = rendererFactory.getRenderer(getWidgetName());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Draw this widget with the given graphics.
	 */ 
	public void draw(Graphics2D g)
	{
		if (visible)
		{
			renderer.render(g, x, y, width, height, this);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Return this widgets name.  The widget name is used by the toolkit to 
	 * determine which renderer to use.
	 */ 
	public abstract String getWidgetName();
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Return the preferred size of this widget, in pixels.
	 */ 
	public abstract Dimension getPreferredSize();
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget getParent()
	{
		return parent;
	}

	public int getHeight()
	{
		return height;
	}

	/*-------------------------------------------------------------------------*/
	public int getWidth()
	{
		return width;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Set the bounds of this component.
	 */ 
	public void setBounds(int x, int y, int width, int height)
	{
		this.height = height;
		this.width = width;
		this.x = x;
		this.y = y;
	}
	
	/*-------------------------------------------------------------------------*/
	public void setBounds(Rectangle r)
	{
		this.setBounds(r.x, r.y, r.width, r.height);
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getBounds()
	{
		return new Rectangle(x, y, width, height);
	}

	/*-------------------------------------------------------------------------*/
	public void addActionListener(ActionListener l)
	{
		this.listeners.add(l);
	}

	public List<ActionListener> getListeners()
	{
		return listeners;
	}

	/*-------------------------------------------------------------------------*/
	public void setActionMessage(String actionMessage)
	{
		this.actionMessage = actionMessage;
	}

	/*-------------------------------------------------------------------------*/
	public void setActionPayload(Object actionPayload)
	{
		this.actionPayload = actionPayload;
	}

	/*-------------------------------------------------------------------------*/
	public Object getActionPayload()
	{
		return actionPayload;
	}

	/*-------------------------------------------------------------------------*/
	public Color getBackgroundColour()
	{
		return background;
	}

	/*-------------------------------------------------------------------------*/
	public void setBackgroundColour(Color background)
	{
		this.background = background;
	}

	/*-------------------------------------------------------------------------*/
	public Color getForegroundColour()
	{
		return foreground;
	}

	/*-------------------------------------------------------------------------*/
	public void setForegroundColour(Color foreground)
	{
		this.foreground = foreground;
	}

	/*-------------------------------------------------------------------------*/
	public boolean hasFocus()
	{
		return focus;
	}

	/*-------------------------------------------------------------------------*/
	public void setFocus(boolean focus)
	{
		this.focus = focus;
		if (focus)
		{
			DIYToolkit.getInstance().setFocus(this);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean isEnabled()
	{
		return enabled;
	}

	/*-------------------------------------------------------------------------*/
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/*-------------------------------------------------------------------------*/
	public void setVisible(boolean b)
	{
		this.visible = b;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isVisible()
	{
		return visible;
	}

	/*-------------------------------------------------------------------------*/

	public TimerTask getTooltipTimerTask()
	{
		return tooltipTimerTask;
	}

	public void setTooltipTimerTask(TimerTask tooltipTimerTask)
	{
		this.tooltipTimerTask = tooltipTimerTask;
	}

	public String getTooltip()
	{
		return tooltip;
	}

	public void setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
	}

	/*-------------------------------------------------------------------------*/
	public void processMousePressed(MouseEvent e)
	{
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseReleased(MouseEvent e)
	{
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		if (!this.enabled || !this.visible)
		{
			return;
		}
		
		this.notifyListeners(e);
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseEntered(MouseEvent e)
	{
	}
	
	/*-------------------------------------------------------------------------*/
	public void processMouseExited(MouseEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseDragged(MouseEvent e)
	{

	}

	/*-------------------------------------------------------------------------*/
	public boolean processMouseWheelMoved(MouseWheelEvent e)
	{
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyPressed(KeyEvent e)
	{
	}
	
	/*-------------------------------------------------------------------------*/
	public void processKeyReleased(KeyEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void processKeyTyped(KeyEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void processHotKey(KeyEvent e)
	{

	}
	
	/*-------------------------------------------------------------------------*/
	public void notifyListeners(InputEvent e)
	{
		ActionEvent event = new ActionEvent(this, this.actionPayload, this.actionMessage, e);

		List<ActionListener> temp = new ArrayList<>(this.listeners);
		for (ActionListener listener : temp)
		{
			listener.actionPerformed(event);
		}
	}
}
