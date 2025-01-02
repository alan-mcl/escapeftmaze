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

package mclachlan.diygui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.*;
import mclachlan.diygui.toolkit.*;


/**
 * 
 */
public class DIYScrollPane extends ContainerWidget
{
	private final ContainerWidget contents;

	private static final int internalInset = 2;
	private final int inset, scrollBarWidth, sliderWidth, sliderHeight;

	public Rectangle scrollBarBounds, upButtonBounds, downButtonBounds, sliderBounds;
	public DIYButton upButton, downButton, slider;

	private int position;
	private int sliderStart;
	private int maxPosition;
	private int positionIncrement;
	private int relativePosition;

	/*-------------------------------------------------------------------------*/
	public DIYScrollPane(ContainerWidget contents)
	{
		this(0, 0, 1, 1, contents);
	}

	/*-------------------------------------------------------------------------*/
	public DIYScrollPane(Rectangle bounds, ContainerWidget contents)
	{
		super(bounds);

		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();
		inset = rp.getProperty(RendererProperties.Property.INSET);
		scrollBarWidth = rp.getProperty(RendererProperties.Property.SCROLLBAR_WIDTH);
		sliderWidth = rp.getProperty(RendererProperties.Property.SLIDER_WIDTH);
		sliderHeight = rp.getProperty(RendererProperties.Property.SLIDER_HEIGHT);

		initScrollBar();

		this.add(upButton);
		this.add(downButton);
		this.add(slider);

		this.contents = contents;
		initContents();

		this.add(contents);
	}

	/*-------------------------------------------------------------------------*/
	public DIYScrollPane(int x, int y, int width, int height, ContainerWidget contents)
	{
		this(new Rectangle(x, y, width, height), contents);
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		this.initContents();
	}

	/*-------------------------------------------------------------------------*/
	private void initContents()
	{
		// this widget effectively has its own coordinate system
		contents.x = inset;
		contents.y = inset;
		Dimension ps = contents.getPreferredSize();
		contents.width = this.width-scrollBarWidth-inset*3;
		contents.height = ps.height;

		contents.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void initScrollBar()
	{
		scrollBarBounds = new Rectangle(
			x +width -scrollBarWidth -inset,
			y +inset,
			scrollBarWidth,
			height-inset*2);

		int buttonDimension = scrollBarWidth -internalInset*2;

		upButtonBounds = new Rectangle(
			scrollBarBounds.x +internalInset,
			scrollBarBounds.y +internalInset,
			buttonDimension,
			buttonDimension);
		
		downButtonBounds = new Rectangle(
			scrollBarBounds.x +internalInset,
			scrollBarBounds.y + scrollBarBounds.height -buttonDimension -internalInset,
			buttonDimension,
			buttonDimension);

		int sliderX = scrollBarBounds.x + internalInset;
		sliderStart = scrollBarBounds.y +internalInset +buttonDimension +internalInset;
		int sliderEnd = scrollBarBounds.y + scrollBarBounds.height - internalInset - buttonDimension - inset - sliderHeight;
		maxPosition = sliderEnd -sliderStart;
		
		sliderBounds = new Rectangle(
			sliderX,
			sliderStart +position,
			sliderWidth,
			sliderHeight);
		
		ActionListener actionListener = new ScrollPaneActionListener();
		
		upButton = new DIYButton("^");
		upButton.setBounds(upButtonBounds);
		upButton.addActionListener(actionListener);

		downButton = new DIYButton("v");
		downButton.setBounds(downButtonBounds);
		downButton.addActionListener(actionListener);

		slider = new DIYButton("|")
		{
			private Point dragRef;

			@Override
			public void processMousePressed(MouseEvent e)
			{
				this.dragRef = e.getPoint();
				super.processMousePressed(e);
			}

			@Override
			public void processMouseReleased(MouseEvent e)
			{
				this.dragRef = null;
				super.processMouseReleased(e);
			}

			@Override
			public void processMouseDragged(MouseEvent e)
			{
				if (this.dragRef != null)
				{
					if (e.getY() > dragRef.getY())
					{
						// dragged down
						slide((int)(e.getY() - dragRef.getY()));
						dragRef = e.getPoint();
					}
					else if (e.getY() < dragRef.getY())
					{
						// dragged up, we want a negative param to slide()
						slide((int)(e.getY() - dragRef.getY()));
						dragRef = e.getPoint();
					}
				}
			}
		};
		slider.setBounds(sliderBounds);

		int lineHeight = DIYToolkit.getDimension(1).height;
		positionIncrement = height/lineHeight;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public String getWidgetName()
	{
		return DIYToolkit.SCROLL_PANE;
	}
	
	/*-------------------------------------------------------------------------*/
	public ContainerWidget getContents()
	{
		return contents;
	}
	
	/*-------------------------------------------------------------------------*/
	@Override
	public void draw(Graphics2D g)
	{
		// hijack this method to not draw the contents
		this.renderer.render(g, x, y, width,  height, this);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void doLayout()
	{
		super.doLayout();
		initContents();
		initScrollBar();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Widget getChild(int x, int y)
	{
		if (this.upButtonBounds.contains(x, y))
		{
			return this.upButton;
		}
		else if (this.downButtonBounds.contains(x, y))
		{
			return this.downButton;
		}
		else if (this.sliderBounds.contains(x, y))
		{
			return this.slider;
		}
		else if (this.scrollBarBounds.contains(x, y))
		{
			return this;
		}
		else 
		{
			return this.contents.getChild(
				x-this.x, y-this.y+this.relativePosition);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public List<Widget> getChildren(int x, int y)
	{
		if (this.upButtonBounds.contains(x, y))
		{
			return List.of(this.upButton);
		}
		else if (this.downButtonBounds.contains(x, y))
		{
			return List.of(this.downButton);
		}
		else if (this.sliderBounds.contains(x, y))
		{
			return List.of(this.slider);
		}
		else if (this.scrollBarBounds.contains(x, y))
		{
			return List.of(this);
		}
		else
		{
			return this.contents.getChildren(
				x-this.x, y-this.y+this.relativePosition);
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean processMouseWheelMoved(MouseWheelEvent e)
	{
		double clicks = e.getPreciseWheelRotation();

		if (clicks != 0)
		{
			// clicks is already negative for upward wheel movements
			slide((int)(clicks * positionIncrement));
			return true;
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * @param inc
	 * 	negative for up, positive for down, in pixels of the slider
	 */
	private void slide(int inc)
	{
		position += inc;
		if (position < 0)
		{
			position = 0;
		}
		else if (position > maxPosition)
		{
			position = maxPosition;
		}

		int maxRel = Math.max(contents.height-this.height, 0);
		relativePosition = (int)(maxRel * position * 1D / maxPosition);

		sliderBounds.setLocation(sliderBounds.x, sliderStart+position);
		slider.setBounds(sliderBounds);
	}

	/*-------------------------------------------------------------------------*/
	public int getRelativePosition()
	{
		return relativePosition;
	}

	/*-------------------------------------------------------------------------*/
	private class ScrollPaneActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
		{
			if (event.getSource() == upButton)
			{
				slide(-positionIncrement);
				return true;
			}
			else if (event.getSource() == downButton)
			{
				slide(positionIncrement);
				return true;
			}

			return false;
		}
	}
}
