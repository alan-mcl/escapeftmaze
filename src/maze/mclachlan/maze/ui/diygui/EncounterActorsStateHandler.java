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

import java.awt.event.KeyEvent;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.PartyEvadesFoesEvent;
import mclachlan.maze.game.event.PartyWaitsEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.DefaultFoeAiScript;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class EncounterActorsStateHandler implements ActionListener, ConfirmCallback
{
	private Maze maze;
	private final int buttonRows;
	private final int inset;
	private MessageDestination msg;

	private DIYButton leave, attack, flee, wait, surprise, evade;
	private ActorEncounter actorEncounter;
	private NpcScript npcScript;

	/*-------------------------------------------------------------------------*/
	public EncounterActorsStateHandler(Maze maze, int buttonRows, int inset,
		MessageDestination msg)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		this.msg = msg;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getLeftPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		attack = new DIYButton(StringUtil.getUiLabel("poatw.attack"));
		attack.addActionListener(this);

		wait = new DIYButton(StringUtil.getUiLabel("poatw.wait"));
		wait.addActionListener(this);

		surprise = new DIYButton(StringUtil.getUiLabel("poatw.surprise"));
		surprise.addActionListener(this);

		result.add(attack);
		result.add(surprise);
		result.add(wait);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getRightPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		evade = new DIYButton(StringUtil.getUiLabel("poatw.evade"));
		evade.addActionListener(this);

		leave = new DIYButton(StringUtil.getUiLabel("poatw.leave"));
		leave.addActionListener(this);

		flee = new DIYButton(StringUtil.getUiLabel("poatw.flee"));
		flee.addActionListener(this);

		result.add(leave);
		result.add(evade);
		result.add(flee);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// encounter actor options
		if (obj == attack)
		{
			attack();
		}
		else if (obj == wait)
		{
			partyWaits();
		}
		else if (obj == flee)
		{
			flee();
		}
		else if (obj == leave)
		{
			leave();
		}
		else if (obj == evade)
		{
			evade();
		}
		else if (obj == surprise)
		{
			ambush();
		}


	}

	/*-------------------------------------------------------------------------*/
	private void attack()
	{
		if (attack.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.attacks"));
			maze.appendEvents(npcScript.attackedByParty());
		}
	}

	public void ambush()
	{
		if (surprise.isVisible())
		{
			//finished(UserInterface.CombatOption.SURPRISE_FOES); todo
		}
	}

	public void evade()
	{
		if (evade.isVisible())
		{
			maze.appendEvents(
				new PartyEvadesFoesEvent(maze, maze.getCurrentCombat()));
		}
	}

	public void partyWaits()
	{
		if (wait.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.waits"));
			maze.appendEvents(new PartyWaitsEvent(actorEncounter, maze, msg));
		}
	}

	public void flee()
	{
		if (flee.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.flees"));

			boolean success = GameSys.getInstance().attemptToRunAway(
				maze.getParty(), actorEncounter.getActors());

			if (!success)
			{
				maze.appendEvents(new PartyFleeFailedEvent());
				maze.appendEvents(new PartyWaitsEvent(actorEncounter, maze, msg));
			}
			else
			{
				maze.appendEvents(new SuccessEvent());
				maze.appendEvents(new PartyFleesEvent());
			}
		}
	}

	private void leave()
	{
		if (leave.isVisible())
		{
			switch (actorEncounter.getEncounterAttitude())
			{
				case WARY:
				case SCARED:
				case NEUTRAL:
					maze.appendEvents(npcScript.partyLeavesNeutral());
					break;
				case FRIENDLY:
				case ALLIED:
					maze.appendEvents(npcScript.partyLeavesFriendly());
					break;
				default:
					throw new MazeException("invalid leave option "+
						actorEncounter.getEncounterAttitude());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void confirm()
	{
		// confirming the exit to main
		maze.backToMain();
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_A: attack(); break;
			case KeyEvent.VK_W: partyWaits(); break;
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_L: leave(); break;
			case KeyEvent.VK_F: flee(); break;
			case KeyEvent.VK_E: evade(); break;
			case KeyEvent.VK_S: ambush(); break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setActorEncounter(ActorEncounter actorEncounter)
	{
		this.actorEncounter = actorEncounter;
		this.npcScript = getNpcScriptFromActorEncounter(actorEncounter);
		msg.setHeader(
			StringUtil.getUiLabel(
				"poatw.actor.encounter",
				actorEncounter.describe(),
				StringUtil.getEventText(
					"attitude." + actorEncounter.getEncounterAttitude().toString())));

		boolean mayAmbush =
			actorEncounter.getAmbushStatus() == Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES ||
			actorEncounter.getAmbushStatus() == Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES;

		boolean mayEvade =
			actorEncounter.getAmbushStatus() == Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES;

		// set button state based on encounter attitude
		switch (actorEncounter.getEncounterAttitude())
		{
			case ATTACKING:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(false);
				break;
			case AGGRESSIVE:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(true);
				leave.setVisible(false);
				break;
			case WARY:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(false);
				leave.setVisible(true);
				break;
			case SCARED:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(false);
				leave.setVisible(true);
				break;
			case NEUTRAL:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				break;
			case FRIENDLY:
				attack.setVisible(true);
				surprise.setVisible(false);
				evade.setVisible(false);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				break;
			case ALLIED:
				attack.setVisible(true);
				surprise.setVisible(false);
				evade.setVisible(false);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	private NpcScript getNpcScriptFromActorEncounter(ActorEncounter actorEncounter)
	{
		// todo: polymorphism so that we can treat NPCs and Foes the same!

		return new DefaultFoeAiScript(actorEncounter);
	}
}
