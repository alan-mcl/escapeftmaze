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

import mclachlan.diygui.toolkit.*;
import java.awt.*;


/**
 * 
 */
public class DIYScrollPane extends ContainerWidget
{
	private ContainerWidget contents;
	
	private int inset = 2;
	private int scrollBarWidth = 17, sliderWidth=scrollBarWidth-inset*2;
	public Rectangle scrollBarBounds, upButtonBounds, downButtonBounds, sliderBounds;
	public DIYButton upButton, downButton;
	public int position = 0, sliderStart, sliderEnd, sliderX, maxPosition, 
		positionIncrement, relativePosition;

	/*-------------------------------------------------------------------------*/
	public DIYScrollPane(ContainerWidget contents)
	{
		this(0, 0, 1, 1, contents);
	}

	/*-------------------------------------------------------------------------*/
	public DIYScrollPane(int x, int y, int width, int height, ContainerWidget contents)
	{
		super(x, y, width, height);
		this.add(contents);
		this.contents = contents;

		initContents();
		initScrollBar();
	}

	/*-------------------------------------------------------------------------*/
	private void initContents()
	{
		// this widget effectively has it's own coordinate system
		contents.x = 0;
		contents.y = 0;
		Dimension ps = contents.getPreferredSize();
		contents.width = this.width-scrollBarWidth-inset*2;
		contents.height = ps.height;

		contents.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private void initScrollBar()
	{
		scrollBarBounds = new Rectangle(
			x+width-scrollBarWidth-inset,
			y+inset,
			scrollBarWidth,
			height-inset*2);
		
		upButtonBounds = new Rectangle(
			x+width-scrollBarWidth,
			y+inset*2,
			sliderWidth,
			sliderWidth);
		
		downButtonBounds = new Rectangle(
			x+width-scrollBarWidth,
			y+height-scrollBarWidth,
			sliderWidth,
			sliderWidth);
		
		sliderX = x+width-scrollBarWidth; 
		sliderStart = y +inset*3 +sliderWidth;
		sliderEnd = y +height -inset*3 -sliderWidth*2;
		maxPosition = sliderEnd-sliderStart;
		
		sliderBounds = new Rectangle(
			sliderX,
			sliderStart +position,
			sliderWidth,
			sliderWidth);
		
		ActionListener actionListener = new ScrollPaneActionListener();
		
		upButton = new DIYButton("^");
		upButton.setBounds(upButtonBounds);
		upButton.addActionListener(actionListener);
		downButton = new DIYButton("v");
		downButton.setBounds(downButtonBounds);
		downButton.addActionListener(actionListener);

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
		else 
		{
			return this.contents.getChild(
				x-this.x, y-this.y+this.relativePosition);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private class ScrollPaneActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if (event.getSource() == upButton)
			{
				position -= positionIncrement;
				if (position < 0)
				{
					position = 0;
				}

				relativePosition = position * (maxPosition / positionIncrement);

				sliderBounds.setLocation(sliderBounds.x, sliderStart+position);
			}
			else if (event.getSource() == downButton)
			{
				if (position < maxPosition && positionIncrement > 0)
				{
					position += positionIncrement;
					if (position > maxPosition)
					{
						position = maxPosition;
					}

					relativePosition = position * (maxPosition / positionIncrement);

					sliderBounds.setLocation(sliderBounds.x, sliderStart+position);
				}
			}
		}
	}
}
