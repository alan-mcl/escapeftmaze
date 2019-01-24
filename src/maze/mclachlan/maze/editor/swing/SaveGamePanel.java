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

package mclachlan.maze.editor.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.DifficultyLevel;
import mclachlan.maze.game.GameState;
import mclachlan.maze.game.MazeVariables;
import mclachlan.maze.game.PlayerTilesVisited;
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.stat.ItemCacheManager;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class SaveGamePanel extends JPanel
{
	private String saveGameName;
	private GameStatePanel gameStatePanel;
	private SaveGamePlayerCharactersPanel saveGamePlayerCharacterPanel;
	private NpcFactionPanel npcFactionPanel;

	/*-------------------------------------------------------------------------*/
	public SaveGamePanel(String saveGameName)
	{
		this.saveGameName = saveGameName;
		this.setLayout(new BorderLayout(5,5));

		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		
		tabs.add("Game State", getGameStatePanel());
		tabs.add("Player Characters", getPlayerCharactersPanel());

		// conditions currently configured at the various condition bearer
		// locations (not all of which are done yet).
		// Is it worth having a central tab?
//		tabs.add("Conditions", getConditionsPanel());

		tabs.add("Item Caches", getItemCachesPanel());
		tabs.add("NPC Factions", getNpcFactionsPanel());
		tabs.add("NPCs", getNpcPanel());
		tabs.add("Tiles Visited", getPlayerTilesVisitedPanel());
		tabs.add("Journals", getJournalsPanel());

		this.add(tabs);
	}

	/*-------------------------------------------------------------------------*/
	public Component getPlayerTilesVisitedPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getNpcPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getNpcFactionsPanel()
	{
		npcFactionPanel = new NpcFactionPanel(saveGameName);
		return npcFactionPanel;
	}

	private Component getItemCachesPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}

	private Component getPlayerCharactersPanel()
	{
		saveGamePlayerCharacterPanel = new SaveGamePlayerCharactersPanel(saveGameName);
		return saveGamePlayerCharacterPanel;
	}

	private Component getGameStatePanel()
	{
		gameStatePanel = new GameStatePanel(saveGameName);
		return gameStatePanel;
	}

//	private Component getConditionsPanel()
//	{
//		todo (and remember to add to SwingEditor.addDynamicDataTab)
//		return new JLabel("todo");
//	}

	private Component getJournalsPanel()
	{
		// todo (and remember to add to SwingEditor.addDynamicDataTab)
		return new JLabel("todo");
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		gameStatePanel.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		try
		{
			// sync with mclachlan.maze.game.Maze.loadGame()

			Loader loader = Database.getInstance().getLoader();

			// load gamestate
			GameState gs = loader.loadGameState(saveGameName);

			// construct player party
			Map<String, PlayerCharacter> playerCharacterCache = loader.loadPlayerCharacters(saveGameName);
			java.util.List<UnifiedActor> list = new ArrayList<UnifiedActor>();
			for (String s : gs.getPartyNames())
			{
				list.add(playerCharacterCache.get(s));
			}
			PlayerParty party = new PlayerParty(list);

			// set difficulty level
			DifficultyLevel difficultyLevel = gs.getDifficultyLevel();

			// load tiles visited
			PlayerTilesVisited playerTilesVisited = loader.loadPlayerTilesVisited(saveGameName);

			// clear maze vars
			MazeVariables.clearAll();

			// load NPCs
			NpcManager.getInstance().loadGame(saveGameName, loader, playerCharacterCache);

			// load maze vars
			loader.loadMazeVariables(saveGameName);

			// load item caches
			ItemCacheManager.getInstance().loadGame(saveGameName, loader, playerCharacterCache);

			// init state
			// no op

			// load journals
			JournalManager.getInstance().loadGame(saveGameName, loader);

			// load conditions
			// done last, so that conditions on tiles can be loaded after the zone has been loaded
			ConditionManager.getInstance().loadGame(saveGameName, loader, playerCharacterCache);

			// set the UI state
			gameStatePanel.refresh(gs);
			saveGamePlayerCharacterPanel.refresh(playerCharacterCache);
			npcFactionPanel.refresh(NpcManager.getInstance());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void save()
	{
		Saver saver = Database.getInstance().getSaver();

		try
		{
			saveGamePlayerCharacterPanel.commit(null);
			npcFactionPanel.commit(null);

			saver.saveGameState(saveGameName, gameStatePanel.getGameState());
			saver.savePlayerCharacters(saveGameName, saveGamePlayerCharacterPanel.getPlayerCharacters());
			saver.saveNpcFactions(saveGameName, npcFactionPanel.getNpcFactions());
			ConditionManager.getInstance().saveGame(saveGameName, saver);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	public SaveGamePlayerCharactersPanel getSaveGamePlayerCharacterPanel()
	{
		return saveGamePlayerCharacterPanel;
	}

	public NpcFactionPanel getNpcFactionPanel()
	{
		return npcFactionPanel;
	}
}
