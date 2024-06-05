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

package mclachlan.maze.ui.diygui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.MouseClickScript;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.FoeGroup;

/**
 * Widget to display the maze image.  The rendering operation is synchronised
 * on the CrusaderEngine instance given to this widget.
 */
public class MazeWidget extends ContainerWidget
{
	private CrusaderEngine engine;
	private final Rectangle bounds;

	private final FoeGroupWidget[] foeGroupWidgets =
		new FoeGroupWidget[Constants.MAX_FOE_GROUPS];
	
	private final FoeGroupWidget[] partyAllyWidgets =
		new FoeGroupWidget[Constants.MAX_PARTY_ALLIES];

	private int selectedFoeGroupWidget = -1;

	/*-------------------------------------------------------------------------*/
	/**
	 * @param raycaster
	 * 	Rendering is synchronised on this object.
	 */ 
	public MazeWidget(Rectangle bounds, CrusaderEngine raycaster)
	{
		super(bounds);
		this.bounds = bounds;

		if (engine != null)
		{
			setEngine(raycaster);
		}

		int rowHeight = 13;
		for (int i = 0; i < foeGroupWidgets.length; i++)
		{
			foeGroupWidgets[i] = new FoeGroupWidget(null,
				new Rectangle(x, y+i*rowHeight, width, rowHeight));
			add(foeGroupWidgets[i]);
		}

		int offset = 3;
		for (int i=0; i<partyAllyWidgets.length; i++)
		{
			partyAllyWidgets[i] = new FoeGroupWidget(null,
				new Rectangle(x, y+height-offset-((i+1)*rowHeight), width, rowHeight));
			add(partyAllyWidgets[i]);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setEngine(CrusaderEngine raycaster)
	{
		this.engine = raycaster;
	}

	/*-------------------------------------------------------------------------*/
	public void setFoes(List<FoeGroup> foes)
	{
		if (foes == null)
		{
			selectedFoeGroupWidget = -1;
			for (int i=0; i < Constants.MAX_FOE_GROUPS; i++)
			{
				foeGroupWidgets[i].setFoeGroup(null);
			}

			return;
		}

		addFoes(foes);

		selectedFoeGroupWidget = 0;
	}

	/*-------------------------------------------------------------------------*/
	public void addFoes(List<FoeGroup> foes)
	{
		int i = 0;

		int current = 0;
		while (foeGroupWidgets[current].getFoeGroup() != null)
		{
			current++;
		}

		int max = foes.size();
		for (i = 0; i < max; i++)
		{
			FoeGroup group = foes.get(i);
			if (group.numAlive() > 0)
			{
				foeGroupWidgets[current + i].setFoeGroup(group);
			}
			else
			{
				foeGroupWidgets[current + i].setFoeGroup(null);
			}
		}

//		for (i = current + i + 1; i < foeGroupWidgets.length; i++)
//		{
//			foeGroupWidgets[i].setFoeGroup(null);
//		}

		if (selectedFoeGroupWidget == -1)
		{
			selectedFoeGroupWidget = 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void removeFoeGroup(FoeGroup fg)
	{
		int i;
		for (i=0; i<foeGroupWidgets.length; i++)
		{
			if (foeGroupWidgets[i].getFoeGroup() == fg)
			{
				break;
			}
		}

		for (; i<foeGroupWidgets.length-1; i++)
		{
			if (i<foeGroupWidgets.length-1)
			{
				foeGroupWidgets[i].setFoeGroup(foeGroupWidgets[i + 1].getFoeGroup());
			}
			else
			{
				foeGroupWidgets[i].setFoeGroup(null);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setAllies(List<FoeGroup> allies)
	{
		if (allies == null)
		{
			for (FoeGroupWidget partyAllyWidget : partyAllyWidgets)
			{
				partyAllyWidget.setFoeGroup(null);
			}

			return;
		}

		int i=0;
		int max = allies.size();
		for (; i<max; i++)
		{
			FoeGroup group = allies.get(i);
			if (group.numAlive() > 0)
			{
				partyAllyWidgets[i].setFoeGroup(group);
			}
			else
			{
				partyAllyWidgets[i].setFoeGroup(null);
			}
		}

		for (; i < partyAllyWidgets.length; i++)
		{
			partyAllyWidgets[i].setFoeGroup(null);
		}
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getSelectedFoeGroup()
	{
		return foeGroupWidgets[selectedFoeGroupWidget].getFoeGroup();
	}

	/*-------------------------------------------------------------------------*/
	public void draw(Graphics2D g)
	{
		if (this.engine == null)
		{
			return;
		}
		
		Image temp;
		synchronized (this.engine)
		{
			temp = this.engine.render();
		}

		g.drawImage(
			temp, 
			bounds.x, 
			bounds.y, 
			bounds.width, 
			bounds.height, 
			Maze.getInstance().getComponent());

		// draw this first so that any text overlays the rectangle
		if (selectedFoeGroupWidget != -1)
		{
			Rectangle b = foeGroupWidgets[selectedFoeGroupWidget].getBounds();
			g.setColor(Color.BLUE);
			g.drawRect(b.x,  b.y, b.width, b.height);
		}

		for (FoeGroupWidget foeGroupWidget : foeGroupWidgets)
		{
			foeGroupWidget.draw(g);
		}
		for (FoeGroupWidget partyAllyWidget : partyAllyWidgets)
		{
			partyAllyWidget.draw(g);
		}

		if (DIYToolkit.debug)
		{
			g.setColor(Color.BLUE);
			g.drawRect(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-2);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void setSelectedFoeGroup(int index)
	{
		if (index > Constants.MAX_FOE_GROUPS
			|| foeGroupWidgets[index].getFoeGroup() == null)
		{
			return;
		}
		
		selectedFoeGroupWidget = index;
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		int x = e.getX()-super.x;
		int y = e.getY()-super.y;

		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			MouseClickScript script = engine.handleMouseClickAndReturnScript(x, y);

			// restrict what game states the scripts can run in
			if (script != null)
			{
				switch (Maze.getInstance().getState())
				{
					case MOVEMENT:
						// always execute in movement
						script.execute(Maze.getInstance().getCurrentZone().getMap());

					case COMBAT:
					case ENCOUNTER_ACTORS:
						// only execute foe info scripts in these modes
						if (script instanceof FoeInfoMouseClickScript)
						{
							script.execute(Maze.getInstance().getCurrentZone().getMap());
						}
						break;
				}
			}
		}

		// todo: weird behaviour if this triggers a click script behind the foe group widget?
		for (int i=0; i<Constants.MAX_FOE_GROUPS; i++)
		{
			if (foeGroupWidgets[i].getBounds().contains(e.getPoint())
				&& foeGroupWidgets[i].getFoeGroup() != null)
			{
				setSelectedFoeGroup(i);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}
}
