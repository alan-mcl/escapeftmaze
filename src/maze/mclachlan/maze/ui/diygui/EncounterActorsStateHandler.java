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
import mclachlan.maze.game.event.ActorsTurnToAct;
import mclachlan.maze.game.event.PartyAmbushesFoesEvent;
import mclachlan.maze.game.event.PartyEvadesFoesEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.DefaultFoeAiScript;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.npc.InitiateGuildEvent;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcScript;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class EncounterActorsStateHandler implements ActionListener
{
	private final Maze maze;
	private final int buttonRows;
	private final int inset;
	private final MessageDestination msg;

	private final DIYButton leave, attack, flee, wait, surprise, evade, guild;
	private ActorEncounter actorEncounter;
	private NpcScript npcScript;
	private DIYPane leftPane;

	/*-------------------------------------------------------------------------*/
	public EncounterActorsStateHandler(Maze maze, int buttonRows, int inset,
		MessageDestination msg)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		this.msg = msg;

		attack = new DIYButton(StringUtil.getUiLabel("poatw.attack"));
		attack.addActionListener(this);

		wait = new DIYButton(StringUtil.getUiLabel("poatw.wait"));
		wait.addActionListener(this);

		surprise = new DIYButton(StringUtil.getUiLabel("poatw.surprise"));
		surprise.addActionListener(this);

		evade = new DIYButton(StringUtil.getUiLabel("poatw.evade"));
		evade.addActionListener(this);

		guild = new DIYButton(StringUtil.getUiLabel("poatw.guild"));
		guild.addActionListener(this);

		leave = new DIYButton(StringUtil.getUiLabel("poatw.leave"));
		leave.addActionListener(this);

		flee = new DIYButton(StringUtil.getUiLabel("poatw.flee"));
		flee.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getLeftPane()
	{
		leftPane = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		leftPane.add(attack);
		leftPane.add(surprise);
		leftPane.add(evade);
		leftPane.add(flee);
		leftPane.add(wait);
		leftPane.add(guild);
		leftPane.add(leave);

		return leftPane;
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getRightPane()
	{
		DIYPane result = new DIYPane(new DIYGridLayout(1, buttonRows, inset, inset));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();

		// encounter actor options
		if (obj == attack)
		{
			attack();
			return true;
		}
		else if (obj == wait)
		{
			partyWaits();
			return true;
		}
		else if (obj == flee)
		{
			flee();
			return true;
		}
		else if (obj == leave)
		{
			leave();
			return true;
		}
		else if (obj == evade)
		{
			evade();
			return true;
		}
		else if (obj == surprise)
		{
			ambush();
			return true;
		}
		else if (obj == guild)
		{
			guild();
			return true;
		}

		return false;
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

	private void guild()
	{
		if (guild.isVisible())
		{
			maze.appendEvents(new InitiateGuildEvent(actorEncounter.getLeader()));
		}
	}

	public void ambush()
	{
		if (surprise.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.ambushes"));
			maze.appendEvents(new PartyAmbushesFoesEvent(maze, actorEncounter));
		}
	}

	public void evade()
	{
		if (evade.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.evades"));
			maze.appendEvents(
				new PartyEvadesFoesEvent(maze, actorEncounter));
		}
	}

	public void partyWaits()
	{
		if (wait.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.waits"));
			maze.appendEvents(new ActorsTurnToAct(actorEncounter, maze, msg));
		}
	}

	public void flee()
	{
		if (flee.isVisible())
		{
			msg.addMessage(StringUtil.getEventText("msg.party.flees"));

			boolean success = GameSys.getInstance().attemptToRunAway(
				maze.getParty(), actorEncounter.getActorGroups());

			if (!success)
			{
				maze.appendEvents(new PartyFleeFailedEvent());
				maze.appendEvents(new ActorsTurnToAct(actorEncounter, maze, msg));
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
			case KeyEvent.VK_G: guild(); break;
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

		boolean isGuild =
			actorEncounter.getLeader().isGuildMaster();

		leftPane.remove(attack);
		leftPane.remove(surprise);
		leftPane.remove(evade);
		leftPane.remove(wait);
		leftPane.remove(flee);
		leftPane.remove(leave);
		leftPane.remove(guild);

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
				guild.setVisible(false);
				break;
			case AGGRESSIVE:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(true);
				leave.setVisible(false);
				guild.setVisible(false);
				break;
			case WARY:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(false);
				break;
			case SCARED:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(false);
				break;
			case NEUTRAL:
				attack.setVisible(true);
				surprise.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(false);
				break;
			case FRIENDLY:
				attack.setVisible(true);
				surprise.setVisible(false);
				evade.setVisible(false);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(isGuild);
				break;
			case ALLIED:
				attack.setVisible(true);
				surprise.setVisible(false);
				evade.setVisible(false);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(isGuild);
				break;
		}

		if (attack.isVisible()) leftPane.add(attack);
		if (surprise.isVisible()) leftPane.add(surprise);
		if (evade.isVisible()) leftPane.add(evade);
		if (wait.isVisible()) leftPane.add(wait);
		if (flee.isVisible()) leftPane.add(flee);
		if (guild.isVisible()) leftPane.add(guild);
		if (leave.isVisible()) leftPane.add(leave);

		leftPane.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private NpcScript getNpcScriptFromActorEncounter(ActorEncounter actorEncounter)
	{
		if (actorEncounter.getLeader() instanceof Npc)
		{
			return actorEncounter.getLeader().getActionScript();
		}
		else
		{
			return new DefaultFoeAiScript(actorEncounter);
		}
	}
}
