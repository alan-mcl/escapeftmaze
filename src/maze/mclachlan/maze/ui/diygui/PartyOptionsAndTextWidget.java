
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
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
import mclachlan.diygui.DIYScrollPane;
import mclachlan.diygui.DIYTextArea;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYBorderLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.npc.Npc;

import static mclachlan.maze.game.Maze.State.*;

/**
 * Widget for the bottom center that:
 * <ul>
 *    <li>displays a scrolling text area of events as things happen</li>
 *    <li>displays buttons for party options (i.e. options not tied to a specific
 *    character) to the left and right of it.</li>
 *    <li>changes those displayed buttons based on game state</li>
 *    <li>works for all maze-window centric game states (i.e. movement, combat,
 *    NPC, chest, door, etc)</li>
 * </ul>
 */
public class PartyOptionsAndTextWidget extends DIYPane
	implements MessageDestination
{
	public static final int BUTTON_HEIGHT = 20;
	public static final int INSET = 4;
	private static final int BUFFER_SIZE = 100;
	private final Maze maze;

	// card layouts for the left and right
	private CardLayoutWidget leftCards;
	private CardLayoutWidget rightCards;

	// central header text, common to all game states
	private DIYLabel header;

	// the central scrolling text area, common to all game states
	private DIYTextArea textArea;
	private List<String> messages = new ArrayList<String>();

	// movement options
	private MovementStateHandler movementHandler;

	// encounter actors options
	private EncounterActorsStateHandler encounterActorsStateHandler;

	// combat party options
	private CombatStateHandler combatStateHandler;

	// encounter NPC options
	private EncounterNpcStateHandler encounterNpcStateHandler;

	/*-------------------------------------------------------------------------*/
	public PartyOptionsAndTextWidget(Rectangle bounds)
	{
		super(bounds.x, bounds.y, bounds.width, bounds.height);

		maze = Maze.getInstance();
		int buttonRows = height / BUTTON_HEIGHT;
		setLayoutManager(new DIYBorderLayout(INSET, INSET));

		// init the state handlers
		movementHandler = new MovementStateHandler(maze, buttonRows, INSET);
		encounterActorsStateHandler = new EncounterActorsStateHandler(maze, buttonRows, INSET, this);
		combatStateHandler = new CombatStateHandler(maze, buttonRows, INSET, this);
		encounterNpcStateHandler = new EncounterNpcStateHandler(maze, buttonRows, buttonRows, this);

		// pack card layout widgets for the right and left panes
		Map<Object, ContainerWidget> leftCardsWidgets = getLeftCardsWidgets();
		leftCards = new CardLayoutWidget(new Rectangle(), leftCardsWidgets);

		Map<Object, ContainerWidget> rightCardsWidgets = getRightCardsWidgets();
		rightCards = new CardLayoutWidget(new Rectangle(), rightCardsWidgets);

		// init the header and text area
		header = new DIYLabel("", DIYToolkit.Align.CENTER);
		header.setForegroundColour(Color.WHITE);

		textArea = new DIYTextArea("");
		textArea.setTransparent(true);
		textArea.setAlignment(DIYToolkit.Align.CENTER);
		DIYScrollPane scrollPane = new DIYScrollPane(textArea);

		// add widgets
		this.add(header, DIYBorderLayout.Constraint.NORTH);
		this.add(leftCards, DIYBorderLayout.Constraint.WEST);
		this.add(rightCards, DIYBorderLayout.Constraint.EAST);
		this.add(scrollPane, DIYBorderLayout.Constraint.CENTER);

		this.doLayout();
	}

	/*-------------------------------------------------------------------------*/
	private Map<Object, ContainerWidget> getLeftCardsWidgets()
	{
		Map<Object, ContainerWidget> result = new HashMap<Object, ContainerWidget>();

		result.put(MOVEMENT, movementHandler.getLeftPane());
		result.put(ENCOUNTER_ACTORS, encounterActorsStateHandler.getLeftPane());
		result.put(COMBAT, combatStateHandler.getLeftPane());
		result.put(ENCOUNTER_NPC, encounterNpcStateHandler.getLeftPane());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Map<Object, ContainerWidget> getRightCardsWidgets()
	{
		Map<Object, ContainerWidget> result = new HashMap<Object, ContainerWidget>();

		result.put(MOVEMENT, movementHandler.getRightPane());
		result.put(ENCOUNTER_ACTORS, encounterActorsStateHandler.getRightPane());
		result.put(COMBAT, combatStateHandler.getRightPane());
		result.put(ENCOUNTER_NPC, encounterNpcStateHandler.getRightPane());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void addMessage(String message)
	{
		messages.add(0, message);
		if (messages.size() > BUFFER_SIZE)
		{
			messages.remove(messages.size()-1);
		}

		StringBuilder sb = new StringBuilder();
		for (String s : messages)
		{
			sb.append(s).append("\n");
		}

		textArea.setText(sb.toString());
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void setHeader(String text)
	{
		header.setText(text);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void clearMessages()
	{
		messages.clear();
		header.setText("");
	}

	/*-------------------------------------------------------------------------*/
	public void setGameState(Maze.State state, Maze maze)
	{
		switch (state)
		{
			case MOVEMENT:
				String zoneName = maze.getCurrentZone().getName();
				header.setText(StringUtil.getUiLabel("poatw.exploring", zoneName));
				leftCards.show(MOVEMENT);
				rightCards.show(MOVEMENT);
				break;
			case ENCOUNTER_ACTORS:
				leftCards.show(ENCOUNTER_ACTORS);
				rightCards.show(ENCOUNTER_ACTORS);
				break;
			case COMBAT:
				leftCards.show(COMBAT);
				rightCards.show(COMBAT);
				break;
			case ENCOUNTER_NPC:
				leftCards.show(ENCOUNTER_NPC);
				rightCards.show(ENCOUNTER_NPC);
			default:
				// do nothing
		}
	}

	/*-------------------------------------------------------------------------*/
	public void handleKey(int keyCode)
	{
		switch (maze.getState())
		{
			case MOVEMENT:
				movementHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_ACTORS:
				encounterActorsStateHandler.handleKey(keyCode);
				break;
			case COMBAT:
				combatStateHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_CHEST:
				break;
			case ENCOUNTER_NPC:
				encounterNpcStateHandler.handleKey(keyCode);
				break;
			case ENCOUNTER_PORTAL:
				break;

			// no impact on these states
			case MAINMENU:
			case CREATE_CHARACTER:
			case SAVE_LOAD:
			case MODIFIERSDISPLAY:
			case STATSDISPLAY:
			case PROPERTIESDISPLAY:
			case INVENTORY:
			case LEVELLING_UP:
			case MAGIC:
				break;

			// todo: do we really need these states?
			case SIGNBOARD:
			case GRANT_ITEMS:
			case ENCOUNTER_TILE:
			case RESTING:
			case FINISHED:
				break;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setActorEncounter(ActorEncounter actorEncounter)
	{
		encounterActorsStateHandler.setActorEncounter(actorEncounter);
	}

	/*-------------------------------------------------------------------------*/
	public void setCurrentCombat(Combat currentCombat)
	{
		combatStateHandler.setCurrentCombat(currentCombat);
	}

	/*-------------------------------------------------------------------------*/
	public void setNpc(Npc npc)
	{
		encounterNpcStateHandler.setNpc(npc);
	}
}
