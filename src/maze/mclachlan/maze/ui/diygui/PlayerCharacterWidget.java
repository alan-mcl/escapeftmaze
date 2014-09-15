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
import java.awt.event.MouseEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.util.HashMapMutableTree;
import mclachlan.diygui.util.MutableTree;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.EquipIntention;
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;

/**
 * Widget to display an actor's details.
 */
public class PlayerCharacterWidget extends ContainerWidget implements ActionListener, ActorActionOption.ActionOptionCallback
{
	private PlayerCharacter playerCharacter;
	private int index;
	private Rectangle leftHandBounds;
	private Rectangle rightHandBounds;

	private DIYButton levelUp;
	private DIYComboBox<ActorActionOption> action;
	
	private final Object pcMutex = new Object();

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget(Rectangle bounds, int index)
	{
		super(bounds);
		this.index = index;

		levelUp = new DIYButton(StringUtil.getUiLabel("pcw.levelup"));

		MutableTree<ActorActionOption> options = new HashMapMutableTree<ActorActionOption>();
		action = new DIYComboBox<ActorActionOption>(options, new Rectangle(0,0,1,1));
		action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action"));
		if (index % 2 == 0)
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.RIGHT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.RIGHT);
		}
		else
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.LEFT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.LEFT);
		}

		levelUp.addActionListener(this);
		action.addActionListener(this);

		this.add(action);
		this.add(levelUp);
	}
	
	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return MazeRendererFactory.PLAYER_CHARACTER_WIDGET;
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(PlayerCharacter playerCharacter)
	{
		synchronized(pcMutex)
		{
			this.playerCharacter = playerCharacter;
		}
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		if (playerCharacter == null)
		{
			levelUp.setVisible(false);
			action.setVisible(false);
			return;
		}
		else
		{
			Combat combat = Maze.getInstance().getCurrentCombat();
			action.setModel(playerCharacter.getCharacterActionOptions(combat));
			action.setVisible(true);
			action.setEnabled(!action.getModel().isEmpty());

			if (combat == null)
			{
				action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action"));

				if (Maze.getInstance().getState() != Maze.State.MOVEMENT)
				{
					action.setEnabled(false);
				}
				else
				{
					action.setEnabled(true);
				}
			}
			else
			{
				action.setEditorText(null);
				action.getSelected().select(playerCharacter, combat, this);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void processMouseClicked(MouseEvent e)
	{
		// this shit only works in movement mode
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			handleMovementOptions(e);
		}
		else if (Maze.getInstance().getState() == Maze.State.COMBAT)
		{

		}
	}

	/*-------------------------------------------------------------------------*/
	private void handleMovementOptions(MouseEvent e)
	{
		if (leftHandBounds.contains(e.getPoint())
			|| rightHandBounds.contains(e.getPoint()))
		{
			if (e.getButton() == MouseEvent.BUTTON3)
			{
				Item item;
				// right click to bring up item details
				if (rightHandBounds.contains(e.getPoint()))
				{
					item = playerCharacter.getPrimaryWeapon();
				}
				else
				{
					item = playerCharacter.getSecondaryWeapon();
				}

				if (item != null)
				{
					DiyGuiUserInterface.instance.popupItemDetailsWidget(item);
				}
			}
			else
			{
				// interpret any other click on the weapons as a swap
				playerCharacter.swapWeapons();
			}
		}
		else
		{
			inventory();
			super.processMouseClicked(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == levelUp 
			&& Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			Maze.getInstance().levelUp(this.playerCharacter);
		}
		else if (event.getSource() == action)
		{
			ActorActionOption selected = action.getSelected();

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				selected.select(this.getPlayerCharacter(), null, this);
			}
			else
			{
				selected.select(this.getPlayerCharacter(), Maze.getInstance().getCurrentCombat(), this);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public Object getPcMutex()
	{
		return pcMutex;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter()
	{
		return playerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public DIYButton getLevelUp()
	{
		return levelUp;
	}

	/*-------------------------------------------------------------------------*/
	public int getIndex()
	{
		return index;
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox<ActorActionOption> getAction()
	{
		return action;
	}

	/*-------------------------------------------------------------------------*/
	public void setLeftHandBounds(Rectangle leftHandBounds)
	{
		this.leftHandBounds = leftHandBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void setRightHandBounds(Rectangle rightHandBounds)
	{
		this.rightHandBounds = rightHandBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void selected(ActorActionIntention intention)
	{
		if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			if (intention instanceof EquipIntention)
			{
				inventory();
			}
			else
			{
				GameSys.getInstance().processPlayerCharacterIntentionOutsideCombat(
					intention, playerCharacter);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void inventory()
	{
		Maze.getInstance().getUi().characterSelected(this.playerCharacter);
		Maze.getInstance().setState(Maze.State.INVENTORY);
	}
}
