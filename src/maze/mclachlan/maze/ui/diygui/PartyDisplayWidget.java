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

import java.awt.Rectangle;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.ActorGroup;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class PartyDisplayWidget extends ContainerWidget
{
	private PlayerCharacter selectedCharacter;
	private ActorGroup party;
	private final MugshotWidget[] mugShots = new MugshotWidget[6];

	/*-------------------------------------------------------------------------*/
	public PartyDisplayWidget(Rectangle bounds, ActorGroup party)
	{
		super(bounds);

		this.buildGui();

		if (party != null)
		{
			this.setParty(party);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui()
	{
		this.setLayoutManager(new mclachlan.diygui.toolkit.DIYGridLayout(1,6,0,4));

		for (int i = 0; i < 6; i++)
		{
			mugShots[i] = new MugshotWidget();
			this.add(mugShots[i]);
		}

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	public void setParty(ActorGroup party)
	{
		this.party = party;
		
		if (party == null)
		{
			return;
		}
		
		this.setSelectedCharacter((PlayerCharacter)party.getActors().get(0));
		
		for (int i = 0; i < 6; i++)
		{
			if (i < this.party.getActors().size())
			{
				PlayerCharacter pc = (PlayerCharacter)party.getActors().get(i);

				if (pc == null)
				{
					continue;
				}

				this.mugShots[i].setCharacter(pc);
			}
			else
			{
				this.mugShots[i].setCharacter(null);
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void setSelectedCharacter(PlayerCharacter selectedCharacter)
	{
		this.selectedCharacter = selectedCharacter;

		int selectedIndex = party.getActors().indexOf(selectedCharacter);

		if (selectedIndex == -1)
		{
			throw new MazeException("Character not in party: "+selectedCharacter);
		}

		for (MugshotWidget portrait : mugShots)
		{
			portrait.setSelected(false);
		}
		mugShots[selectedIndex].setSelected(true);
	}
	
	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getSelectedCharacter()
	{
		return selectedCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getSelectedCharacterBounds()
	{
		for (int i = 0; i < mugShots.length; i++)
		{
			if (mugShots[i].isSelected())
			{
				return mugShots[i].getBounds();
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.PANE;
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getCharacterBounds(PlayerCharacter pc)
	{
		for (int i = 0; i < mugShots.length; i++)
		{
			if (mugShots[i].getCharacter() == pc)
			{
				return mugShots[i].getBounds();
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		for (int i = 0; i < mugShots.length; i++)
		{
			mugShots[i].refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	private class PartyDisplayActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
		{
			if (Maze.getInstance().isInCombat())
			{
				// during an Equip in combat, only the current character can be edited
				return false;
			}

			for (int i = 0; i < Maze.getInstance().getParty().getActors().size(); i++)
			{
				if (event.getSource() == mugShots[i]
					|| mugShots[i].getChildren().contains(event.getSource()))
				{
					Maze.getInstance().getUi().characterSelected(
						(PlayerCharacter)party.getActors().get(i));
					return true;
				}
			}

			return false;
		}
	}
}
