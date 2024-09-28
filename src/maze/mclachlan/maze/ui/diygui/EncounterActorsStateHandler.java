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
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.event.ActorsTurnToAct;
import mclachlan.maze.game.event.PartyAmbushesFoesEvent;
import mclachlan.maze.game.event.PartyEvadesFoesEvent;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.DefaultFoeAiScript;
import mclachlan.maze.stat.combat.event.PartyFleeFailedEvent;
import mclachlan.maze.stat.combat.event.PartyFleesEvent;
import mclachlan.maze.stat.combat.event.SuccessEvent;
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

	private final DIYButton leave, attack, flee, wait, ambush, evade, guild;
	private ActorEncounter actorEncounter;
	private NpcScript npcScript;
	private DIYPane pane;

	/*-------------------------------------------------------------------------*/
	public EncounterActorsStateHandler(Maze maze, int buttonRows, int inset,
		MessageDestination msg)
	{
		this.maze = maze;
		this.buttonRows = buttonRows;
		this.inset = inset;
		this.msg = msg;

		attack = new DIYButton(StringUtil.getUiLabel("poatw.attack"));
		attack.setTooltip(StringUtil.getUiLabel("poatw.attack.tooltip"));
		attack.addActionListener(this);

		wait = new DIYButton(StringUtil.getUiLabel("poatw.wait"));
		wait.setTooltip(StringUtil.getUiLabel("poatw.wait.tooltip"));
		wait.addActionListener(this);

		ambush = new DIYButton(StringUtil.getUiLabel("poatw.surprise"));
		ambush.setTooltip(StringUtil.getUiLabel("poatw.surprise.tooltip"));
		ambush.addActionListener(this);

		evade = new DIYButton(StringUtil.getUiLabel("poatw.evade"));
		evade.setTooltip(StringUtil.getUiLabel("poatw.evade.tooltip"));
		evade.addActionListener(this);

		guild = new DIYButton(StringUtil.getUiLabel("poatw.guild"));
		guild.setTooltip(StringUtil.getUiLabel("poatw.guild.tooltip"));
		guild.addActionListener(this);

		leave = new DIYButton(StringUtil.getUiLabel("poatw.leave"));
		leave.setTooltip(StringUtil.getUiLabel("poatw.leave.tooltip"));
		leave.addActionListener(this);

		flee = new DIYButton(StringUtil.getUiLabel("poatw.flee"));
		flee.setTooltip(StringUtil.getUiLabel("poatw.flee.tooltip"));
		flee.addActionListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public ContainerWidget getStateHandlerPane()
	{
		pane = new DIYPane(new DIYGridLayout(4, buttonRows, inset, inset));

		pane.add(attack);
		pane.add(ambush);
		pane.add(evade);
		pane.add(flee);
		pane.add(wait);
		pane.add(guild);
		pane.add(leave);

		return pane;
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
		else if (obj == ambush)
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
		if (ambush.isVisible())
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
				case WARY, SCARED, NEUTRAL ->
					maze.appendEvents(npcScript.partyLeavesNeutral());
				case FRIENDLY, ALLIED ->
					maze.appendEvents(npcScript.partyLeavesFriendly());
				default -> throw new MazeException("invalid leave option " +
					actorEncounter.getEncounterAttitude());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (keyCode)
		{
			case KeyEvent.VK_A -> attack();
			case KeyEvent.VK_W -> partyWaits();
			case KeyEvent.VK_ESCAPE, KeyEvent.VK_L -> leave();
			case KeyEvent.VK_F -> flee();
			case KeyEvent.VK_E -> evade();
			case KeyEvent.VK_S -> ambush();
			case KeyEvent.VK_G -> guild();
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

		pane.removeAllChildren();

		Widget[][] buttonLayout;
		DIYLabel blank = new DIYLabel();

		// set button state based on encounter attitude
		switch (actorEncounter.getEncounterAttitude())
		{
			case ATTACKING ->
			{
				attack.setVisible(true);
				ambush.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(false);
				guild.setVisible(false);

				buttonLayout = new Widget[][]
				{
					{blank, blank, blank, blank},
					{mayAmbush?ambush:attack, mayAmbush?attack:blank, blank, mayEvade?evade:blank}
				};
			}
			case AGGRESSIVE ->
			{
				attack.setVisible(true);
				ambush.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(true);
				leave.setVisible(false);
				guild.setVisible(false);

				buttonLayout = new Widget[][]
				{
					{wait, blank, blank, blank},
					{mayAmbush?ambush:attack, mayAmbush?attack:blank, mayEvade?evade:blank, flee}
				};

			}
			case WARY, SCARED ->
			{
				attack.setVisible(true);
				ambush.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(true);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(false);

				buttonLayout = new Widget[][]
				{
					{wait, blank, blank, blank},
					{mayAmbush?ambush:attack, mayAmbush?attack:blank, mayEvade?evade:blank, leave}
				};

			}
			case NEUTRAL ->
			{
				attack.setVisible(true);
				ambush.setVisible(mayAmbush);
				evade.setVisible(mayEvade);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(false);

				buttonLayout = new Widget[][]
				{
					{blank, blank, blank, blank},
					{mayAmbush?ambush:attack, mayAmbush?attack:blank, mayEvade?evade:blank, leave}
				};

			}
			case FRIENDLY, ALLIED ->
			{
				attack.setVisible(true);
				ambush.setVisible(mayAmbush);
				evade.setVisible(false);
				wait.setVisible(false);
				flee.setVisible(false);
				leave.setVisible(true);
				guild.setVisible(isGuild);

				buttonLayout = new Widget[][]
				{
					{blank, blank, blank, blank},
					{mayAmbush?ambush:attack, mayAmbush?attack:blank, isGuild?guild:blank, leave}
				};
			}
			default ->
				throw new MazeException("Unexpected value: " + actorEncounter.getEncounterAttitude());
		}

		List<Widget> visibleButtons = new ArrayList<>();

		if (attack.isVisible()) visibleButtons.add(attack);
		if (ambush.isVisible()) visibleButtons.add(ambush);
		if (evade.isVisible()) visibleButtons.add(evade);
		if (wait.isVisible()) visibleButtons.add(wait);
		if (flee.isVisible()) visibleButtons.add(flee);
		if (guild.isVisible()) visibleButtons.add(guild);
		if (leave.isVisible()) visibleButtons.add(leave);

		// add to the grid layout
		for (int row=0; row<2; row++)
		{
			for (int col=0; col<4; col++)
			{
				Widget b = buttonLayout[row][col];
				pane.add(b);
				visibleButtons.remove(b);
			}
		}

		// assertion here that we have laid everything out
		if (!visibleButtons.isEmpty())
		{
			throw new MazeException("Invalid layout, unhandled buttons: "+visibleButtons);
		}

		pane.doLayout();
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

	/*-------------------------------------------------------------------------*/
	public void setEnabled(boolean b)
	{
		leave.setEnabled(b);
		attack.setEnabled(b);
		flee.setEnabled(b);
		wait.setEnabled(b);
		ambush.setEnabled(b);
		evade.setEnabled(b);
		guild.setEnabled(b);
	}
}
