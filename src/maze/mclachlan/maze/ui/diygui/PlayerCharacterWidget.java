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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.*;
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
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;

/**
 * Widget to display an actor's details.
 */
public class PlayerCharacterWidget extends ContainerWidget implements ActionListener, ActorActionOption.ActionOptionCallback
{
	private PlayerCharacter playerCharacter;
	private final int index;

	private Rectangle portraitBounds, leftHandBounds, rightHandBounds;

	private final DIYButton levelUp;
	private final DIYComboBox<ActorActionOption> action;
	private final DIYComboBox<PlayerCharacter.Stance> stance;

	private final Object pcMutex = new Object();

	private Map<Rectangle, Condition> conditionBounds;

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget(Rectangle bounds, int index)
	{
		super(bounds);
		this.index = index;

		levelUp = new DIYButton(StringUtil.getUiLabel("pcw.levelup"));

		MutableTree<ActorActionOption> options = new HashMapMutableTree<ActorActionOption>();
		action = new DIYComboBox<>(options, new Rectangle(0,0,1,1));
		action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", ""));
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

		ArrayList<PlayerCharacter.Stance> stances = new ArrayList<PlayerCharacter.Stance>();
		stance = new DIYComboBox<>(stances, new Rectangle(0,0,1,1));

		levelUp.addActionListener(this);
		action.addActionListener(this);
		stance.addActionListener(this);

		this.add(action);
		this.add(stance);
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

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		boolean thisEnabled = this.isEnabled();

		conditionBounds = new HashMap<>();

		if (playerCharacter == null)
		{
			levelUp.setVisible(false);
			action.setVisible(false);
			stance.setVisible(false);
			return;
		}
		else
		{
			Combat combat = Maze.getInstance().getCurrentCombat();
			action.setModel(playerCharacter.getCharacterActionOptions(Maze.getInstance(), combat));
			action.setVisible(true);
			action.setEnabled(thisEnabled && !action.getModel().isEmpty() && !(action.getModel().size()==1));

			stance.setModel(playerCharacter.getCharacterStanceOptions(Maze.getInstance(), combat));
			stance.setVisible(true);
			stance.setEnabled(thisEnabled && !stance.getModel().isEmpty() && !(stance.getModel().size()==1));

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT ||
				Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				if (combat == null)
				{
					action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", playerCharacter.getDisplayName()));
					action.setEnabled(!action.getModel().isEmpty());

					stance.setEnabled(false);
				}
				else
				{
					action.setEditorText(null);
					action.getSelected().select(playerCharacter, combat, this);

					stance.setEnabled(thisEnabled);
				}
			}

			this.getPlayerCharacter().setStance(stance.getSelected());
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
		else if (!popupConditionDialog(e.getPoint()))
		{
			inventory();
			super.processMouseClicked(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean popupConditionDialog(Point p)
	{
		for (Map.Entry<Rectangle, Condition> e : conditionBounds.entrySet())
		{
			if (e.getKey().contains(p))
			{
				DiyGuiUserInterface.instance.popupConditionDetailsDialog(e.getValue());
				return true;
			}
		}

		return false;
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
		else if (event.getSource() == stance)
		{
			this.getPlayerCharacter().setStance(stance.getSelected());
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
	public DIYComboBox<PlayerCharacter.Stance> getStance()
	{
		return stance;
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
	public void setPortraitBounds(Rectangle portraitBounds)
	{
		this.portraitBounds = portraitBounds;
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPortraitBounds()
	{
		return portraitBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void clearConditionBounds()
	{
		conditionBounds.clear();
	}

	/*-------------------------------------------------------------------------*/
	public void addConditionBounds(Rectangle r, Condition c)
	{
		conditionBounds.put(r, c);
	}

	/*-------------------------------------------------------------------------*/
	public void selected(ActorActionIntention intention)
	{
		if (Maze.getInstance().getState() != Maze.State.COMBAT)
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
