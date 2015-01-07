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

package mclachlan.maze.game;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.*;
import mclachlan.crusader.EngineObject;
import mclachlan.maze.audio.AudioPlayer;
import mclachlan.maze.audio.AudioThread;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.game.event.StartGameEvent;
import mclachlan.maze.game.event.ZoneChangeEvent;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.event.ConditionEvent;
import mclachlan.maze.stat.combat.event.SpeechBubbleEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.impl.RestingSleep;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.BlockingScreen;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.util.MazeException;

import static mclachlan.crusader.CrusaderEngine.Facing.*;

/**
 *
 */
public class Maze implements Runnable
{
	private static Maze instance;
	private final Object stateMutex = new Object();
	private Object statePopMutex;
	private Map<String, String> appConfig;
	private UserConfig userConfig;
	private Campaign campaign;
	private PlayerTilesVisited playerTilesVisited;

	/** the current game state is on top of the stack */
	private Stack<State> state = new Stack<State>();

	/** the current player party */
	private PlayerParty party;

	/** the current audio player implementation */
	private AudioPlayer audioPlayer;
	private AudioThread audioThread = new AudioThread();

	/** the current zone of play */
	private Zone zone;

	/** the current difficulty level */
	private DifficultyLevel difficultyLevel;

	/** the players tile coords within the current zone */
	private Point playerPos;

	/** the current user interface implementation */
	private UserInterface ui;

	/** the current game rules implentation */
	private GameSys gameSys;

	/** the current magic system implementation */
	private MagicSys magicSys;

	/** the current persitence implementation */
	private Database db;

	/** the current logging implementation */
	private static Log log;

	/** any combat currently underway */
	private Combat currentCombat;
	/** any chest that the player is currently encountering */
	private Chest currentChest;
	/** any NPC that the player is currently encountering */
	private Npc currentNpc;
	/** any portal that the player is currently encountering */
	private Portal currentPortal;

	/** pending formation changes in combat */
	private List<PlayerCharacter> pendingPartyOrder;
	private int pendingFormation;

	private Object eventMutex = new Object();
	private Map<String, PlayerCharacter> playerCharacterCache = 
		new HashMap<String, PlayerCharacter>();

	private EventProcessor processor;

	/*-------------------------------------------------------------------------*/
	public enum State
	{
		/** Displaying the main menu */
		MAINMENU,
		/** Creating a character */
		CREATE_CHARACTER,
		/** Save/Load screen */
		SAVE_LOAD,
		/** In combat */
		COMBAT,
		/** Basic exploration mode */
		MOVEMENT, 
		/** Displaying a character's modifiers */
		MODIFIERSDISPLAY,
		/** Displaying a character's stats */
		STATSDISPLAY,
		/** Displaying a character's properties */
		PROPERTIESDISPLAY,
		/** Displaying a character's inventory */
		INVENTORY,
		/** Displaying a character's magic */
		MAGIC,
		/** Displaying a sign board */
		SIGNBOARD,
		/** Granting items to the player */
		GRANT_ITEMS,
		/** Encountering a chest */
		ENCOUNTER_CHEST,
		/** Encountering an NPC */
		ENCOUNTER_NPC,
		/** Encountering a portal */
		ENCOUNTER_PORTAL,
		/** Encountering a tile */
		ENCOUNTER_TILE,
		/** Busy levelling up a character */
		LEVELLING_UP,
		/** Party is resting */
		RESTING,
		/** Game over */
		FINISHED
	}

	/*-------------------------------------------------------------------------*/
	public Maze(Map<String, String> config, Campaign campaign) throws Exception
	{
		instance = this;
		this.appConfig = config;
		this.campaign = campaign;
	}

	/*-------------------------------------------------------------------------*/
	public static Maze getInstance()
	{
		return instance;
	}

	public void initState()
	{
		this.state.push(State.MAINMENU);
	}

	public void initDb() throws Exception
	{
		log("init db");
		this.db = new Database();
		this.userConfig = db.getUserConfig();
	}

	public void initLog(Log log) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Maze.log = log;
	}

	public void initUi(UserInterface ui) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		log("init ui: "+ui.getClass());
		this.ui = ui;
		this.ui.setDebug(Boolean.valueOf(appConfig.get(AppConfig.UI_DEBUG)));
		this.ui.showMainMenu();
	}

	public void initSystems() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		String magicsys_impl = this.appConfig.get(AppConfig.MAGIC_SYS_IMPL);
		Class magicsys_class = Class.forName(magicsys_impl);
		log("init magicSys: "+magicsys_impl);
		this.magicSys = (MagicSys)magicsys_class.newInstance();

		String gamesys_impl = this.appConfig.get(AppConfig.GAME_SYS_IMPL);
		Class gamesys_class = Class.forName(gamesys_impl);
		log("init gameSys: "+gamesys_impl);
		this.gameSys = (GameSys)gamesys_class.newInstance();
	}

	public void initAudio(AudioPlayer player)
	{
		audioPlayer = player;
	}

	/*-------------------------------------------------------------------------*/
	public void startThreads()
	{
		log("starting main thread...");

		BlockingQueue<MazeEvent> q = new ArrayBlockingQueue<MazeEvent>(9999);
		processor = new EventProcessor(q);
		processor.start();

		new Thread(this, "MAZE DRAW THREAD").start();

		audioThread.start();
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Logs the given message at the {@link Log#MEDIUM} level.
	 */
	public static void log(String msg)
	{
		log(Log.MEDIUM, msg);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Logs the given message at the given level.
	 */
	public static void log(int lvl, String msg)
	{
		if (log != null)
		{
			log.log(lvl, msg);
		}
	}

	/*-------------------------------------------------------------------------*/
	public GameState getGameState()
	{
		return new GameState(
			zone,
			difficultyLevel,
			playerPos,
			getFacing(),
			party.getGold(),
			party.getSupplies(),
			party.getPartyNames(),
			party.getFormation(),
			getTurnNr());
	}

	/*-------------------------------------------------------------------------*/
	public void setGameState(GameState gs)
	{
		GameTime.setTurnNr(gs.getTurnNr());
		difficultyLevel = gs.getDifficultyLevel();
		party.setGold(gs.getPartyGold());
		party.setSupplies(gs.getPartySupplies());
		party.setFormation(gs.getFormation());
		changeZone(gs.getCurrentZone().getName(), gs.getPlayerPos(), gs.getFacing());
	}

	/*-------------------------------------------------------------------------*/
	public void saveGame(String name)
	{
		try
		{
			Saver saver = Database.getInstance().getSaver();

			saver.saveGameState(name, this.getGameState());
			saver.savePlayerCharacters(name, getPlayerCharacters());
			saver.savePlayerTilesVisited(name, playerTilesVisited);
			NpcManager.getInstance().saveGame(name, saver);
			saver.saveMazeVariables(name);
			ItemCacheManager.getInstance().saveGame(name, saver);
			ConditionManager.getInstance().saveGame(name, saver);
			
			ui.addMessage("Game Saved");
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void quickStart()
	{
		// get the first dificulty level, by sort order
		Map<String, DifficultyLevel> difficultyLevels = Database.getInstance().getDifficultyLevels();

		DifficultyLevel difficultyLevel = null;
		int min = Integer.MAX_VALUE;
		for (DifficultyLevel dl : difficultyLevels.values())
		{
			if (dl.getSortOrder() < min)
			{
				difficultyLevel = dl;
				min = dl.getSortOrder();
			}
		}

		// generate a random party of six
		Leveler leveler = new Leveler();

		List<PlayerCharacter> pcs;
		do
		{
			pcs = new ArrayList<PlayerCharacter>();

			for (int i=0; i<6; i++)
			{
				PlayerCharacter pc = leveler.createRandomPlayerCharacter();
				pcs.add(pc);
			}
		}
		while (!leveler.validateParty(pcs));

		Collections.sort(pcs, new Comparator<PlayerCharacter>()
		{
			public int compare(PlayerCharacter pc1, PlayerCharacter pc2)
			{
				CharacterClass.Focus f1 = pc1.getCharacterClass().getFocus();
				CharacterClass.Focus f2 = pc2.getCharacterClass().getFocus();

				return f1.getSortOrder() - f2.getSortOrder();
			}
		});

		// add to party and go go go
		for (PlayerCharacter pc : pcs)
		{
			this.addPlayerCharacterToParty(pc);
		}
		startGame(difficultyLevel.getName());
	}

	/*-------------------------------------------------------------------------*/
	public void startGame(String difficultyLevel)
	{
		try
		{
			// quiet ui
			ui.stopAllAnimations();
			ui.getMusic().stop();
			ui.getMusic().setState(null);

			// loading screen
			ui.showBlockingScreen(
				"screen/loading_screen",
				BlockingScreen.Mode.UNINTERRUPTABLE,
				null);

			// load gamestate
			//... not needed

			// construct player party
			//... already done
			
			// set difficulty level
			this.difficultyLevel = Database.getInstance().getDifficultyLevels().get(difficultyLevel);

			// load tiles visited
			this.playerTilesVisited = new PlayerTilesVisited();

			// clear maze vars
			MazeVariables.clearAll();
		
			// load NPCs
			NpcManager.getInstance().startGame();

			// load maze vars
			// load item caches
			//... not needed

			// start campaign
			MazeScript startingScript = Database.getInstance().getScript(campaign.getStartingScript());
			appendEvents(startingScript.getEvents());
			appendEvents(new StartGameEvent(this, party));
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void loadGame(String name)
	{
		try
		{
			// quiet ui
			ui.stopAllAnimations();
			ui.getMusic().stop();
			ui.getMusic().setState(null);

			// loading screen
			ui.showBlockingScreen(
				"screen/loading_screen", 
				BlockingScreen.Mode.UNINTERRUPTABLE,
				null);

			// load gamestate
			Loader loader = Database.getInstance().getLoader();
			GameState gs = loader.loadGameState(name);

			// construct player party
			playerCharacterCache = loader.loadPlayerCharacters(name);
			List<UnifiedActor> list = new ArrayList<UnifiedActor>();
			for (String s : gs.getPartyNames())
			{
				list.add(playerCharacterCache.get(s));
			}
			party = new PlayerParty(list);

			// set difficulty level
			difficultyLevel = gs.getDifficultyLevel();

			// load tiles visited
			playerTilesVisited = loader.loadPlayerTilesVisited(name);

			// clear maze vars
			MazeVariables.clearAll();

			// load NPCs
			NpcManager.getInstance().loadGame(name, loader);

			// load maze vars
			loader.loadMazeVariables(name);

			// load item caches
			ItemCacheManager.getInstance().loadGame(name, loader);

			// init state
			synchronized(stateMutex)
			{
				setGameState(gs);
				ui.setParty(party);
				this.setState(State.MOVEMENT);
			}

			// load conditions
			// done last, so that conditions on tiles can be loaded after the zone has been loaded
			ConditionManager.getInstance().loadGame(name, loader);

			// set message
			ui.addMessage("Game Loaded");

			// encounter tile
			encounterTile(playerPos, null, getFacing());
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
		finally
		{
			ui.clearBlockingScreen();
		}
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, PlayerCharacter> getPlayerCharacters()
	{
		return playerCharacterCache;
	}

	/*-------------------------------------------------------------------------*/
	public void run()
	{
		// display any intro configured for this campaign
		try
		{
			String script = campaign.getIntroScript();
			if (script != null && script.length() > 0)
			{
				final MazeScript intro = Database.getInstance().getScript(script);
				// this hack here so that the draw thread can draw any stuff 
				// required by these events.
				new Thread("Intro event thread")
				{
					public void run()
					{
						resolveEvents(intro.getEvents());
					}
				}.start();
			}

			while (state.peek() != State.FINISHED)
			{
				this.ui.draw();
				
/*
				synchronized(stateMutex)
				{
					if (getState() == State.RESTING)
					{
						// todo: should this really be done in the draw thread?
						// todo: throttling
						incTurn(true);
						ui.refreshResting();
					}
				}
*/
			}
		}
		catch (Exception e)
		{
			errorDialog(e);
		}
		System.exit(0);
	}

	/*-------------------------------------------------------------------------*/
	public void errorDialog(Exception e)
	{
		e.printStackTrace();
		log.log(e);
		
		StringBuilder s = new StringBuilder();
		
		s.append("FATAL ERROR\n\n");
		s.append(e.getMessage());
		s.append("\n\n");
		s.append("See log file at:\n\n");
		s.append(log.getLogPath());
		
		ui.errorDialog(s.toString());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the current state and destroys all previous state history.
	 */
	public void setState(State state)
	{
		synchronized(stateMutex)
		{
			this.state.pop();
			this.state.push(state);
			changeState(state);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Pushes the given state onto the stack
	 */
	public void pushState(State state)
	{
		synchronized(stateMutex)
		{
			this.state.push(state);
			changeState(state);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Pushes the given state onto the stack, and notifies the given mutex when
	 * the state is popped.
	 */
	public void pushState(State state, Object waiter)
	{
		synchronized(stateMutex)
		{
			this.state.push(state);
			this.statePopMutex = waiter;
			changeState(state);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the game state to the previous state
	 *
	 * @return
	 * 	The current game state.
	 */
	public State popState()
	{
		synchronized(stateMutex)
		{
			State s = this.state.pop();

			if (this.state.isEmpty())
			{
				// haxor alert: we simply go back to movement
				this.state.push(State.MOVEMENT);
			}

			if (this.statePopMutex != null)
			{
				synchronized(statePopMutex)
				{
					statePopMutex.notifyAll();
				}
			}

			changeState(this.state.peek());
			return s;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clearState(State state)
	{
		synchronized(stateMutex)
		{
			this.state.remove(state);
		}
	}

	/*-------------------------------------------------------------------------*/
	public State getState()
	{
		synchronized(stateMutex)
		{
			return this.state.peek();
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean containsState(State s)
	{
		synchronized(stateMutex)
		{
			return this.state.contains(s);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void changeState(State state)
	{
		ui.changeState(state);
	}

	/*-------------------------------------------------------------------------*/
	public void incTurn(boolean checkRandomEncounters)
	{
		ui.clearCombatEventDisplay();
		GameTime.incTurn();
		checkPartyStatus();
		reorderPartyToCompensateForDeadCharacters();

		if (checkRandomEncounters)
		{
			// do not trigger random encounters if there is an NPC on the tile
			Npc[] npcs = NpcManager.getInstance().getNpcsOnTile(
				this.getZone().getName(), this.getTile());

			if (npcs == null || npcs.length == 0)
			{
				Tile t = this.getCurrentTile();
				if (t != null)
				{
					boolean disable_random_spawns = Boolean.valueOf(
						Maze.getInstance().getAppConfig().get((
							AppConfig.DISABLE_RANDOM_SPAWNS)));

					if (!disable_random_spawns)
					{
						if (Dice.d1000.roll() <= GameSys.getInstance().getRandomEncounterChance(t))
						{
							// a random encounter occurs
							FoeEntry foeEntry = t.getRandomEncounters().getEncounterTable().getRandomItem();
							List<FoeGroup> foes = foeEntry.generate();
							this.encounter(foes, null);
						}
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public long getTurnNr()
	{
		return GameTime.getTurnNr();
	}

	/*-------------------------------------------------------------------------*/
	public void backToMain()
	{
		synchronized(stateMutex)
		{
			party = null;
			ui.setParty(null);
			zone = null;
			if (currentCombat != null)
			{
				ui.setFoes(null);
				ui.setAllies(null);
				currentCombat.endCombat();
				currentCombat = null;
			}
			currentChest = null;
			currentNpc = null;
			currentPortal = null;
			MazeVariables.clearAll();
			if (processor != null)
			{
				processor.queue.clear();
			}
			state.clear();
			ui.resetMainMenuState();
			ui.showMainMenu();
			pushState(Maze.State.MAINMENU);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void refreshCharacterData()
	{
		this.ui.refreshCharacterData();
	}

	/*-------------------------------------------------------------------------*/
	public void startResting()
	{
		ConditionTemplate sleep = Database.getInstance().getConditionTemplate(
			Constants.Conditions.RESTING_SLEEP);

		// reduce action points
		for (PlayerCharacter pc : getParty().getPlayerCharacters())
		{
			pc.addCondition(sleep.create(
				pc, pc, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE));

			int sneak = pc.getModifier(Stats.Modifiers.SNEAKING);
			CurMax s = pc.getActionPoints();
			if (sneak == 0 || s.getMaximum() <= sneak)
			{
				s.setCurrent(0);
			}
			else
			{
				s.setCurrent(Math.min(s.getMaximum()/sneak, sneak));
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void stopResting()
	{
		// remove all RESTING_SLEEP conditions from actors
		for (UnifiedActor pc : party.getActors())
		{
			ArrayList<Condition> list = new ArrayList<Condition>(pc.getConditions());
			for (Condition c : list)
			{
				if (c instanceof RestingSleep)
				{
					pc.removeCondition(c);
					break;
				}
			}
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void deductPartyGold(int amount)
	{
		party.setGold(party.getGold() - amount);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the encounter actually happens, false otherwise
	 */
	public boolean encounter(
		final List<FoeGroup> others,
		final String mazeVar)
	{
		//
		// check if there are actually any foes in here
		//
		boolean isFoes = false;
		for (FoeGroup fg : others)
		{
			if (fg.numAlive() > 0)
			{
				isFoes = true;
				break;
			}
		}
		
		if (!isFoes)
		{
			return false;
		}
			
		//
		// NPC faction check.  If any of these foes are associated with an NPC
		// faction that is friendly to the players, this encounter is stillborn.
		//
		for (FoeGroup fg : others)
		{
			Foe f = (Foe)fg.getActors().get(0);

			String faction = f.getFaction();
			if (faction != null)
			{
				NpcFaction nf = NpcManager.getInstance().getNpcFaction(faction);

				// 0 is hostile
				// 100 is friendly
				// use 50 here to allow for factions that start off neutral enough
				// not to attack the player but are not allied
				log(Log.DEBUG, "Attitude test for encounter ["+f.getName()+"] " +
					"["+f.getFaction()+"] ["+nf.getAttitude()+"]");
				if (nf.getAttitude() >= 50)
				{
					return false;
				}
			}
		}

		if (this.getState() == State.RESTING)
		{
			// players attacked while resting.  Give them a chance to wake up

			for (UnifiedActor pc : party.getActors())
			{
				ArrayList<Condition> list = new ArrayList<Condition>(pc.getConditions());
				for (Condition c : list)
				{
					if (c instanceof RestingSleep)
					{
						if (pc.getModifier(Stats.Modifiers.LIGHT_SLEEPER) > 0 ||
							Dice.d100.roll() <= 30)
						{
							pc.removeCondition(c);
						}
						break;
					}
				}
			}
		}

		if (this.getState() == State.COMBAT)
		{
			// hack to avoid the problem when one of the "dummy" combats results in
			// a real one: for eg, a failed Theft spell on an NPC resulting in the
			// NPC deciding to attack the party.
			this.setState(State.COMBAT);
			this.pushState(State.COMBAT);
		}
		else
		{
			this.setState(State.COMBAT);
		}

		// play the encounter fanfare
		MazeScript script = Database.getInstance().getScript("_ENCOUNTER_");
		resolveEvents(script.getEvents());

		// begin encounter speech
		int avgFoeLevel = 0;
		for (FoeGroup fg : others)
		{
			avgFoeLevel += fg.getAverageLevel();
		}
		avgFoeLevel /= others.size();
		SpeechUtil.getInstance().startCombatSpeech(avgFoeLevel, getParty().getPartyLevel());

		// execute any appearance scripts, picking from the first foe
		Foe f = (Foe)(others.get(0).getActors().get(0));
		if (f.getAppearanceScript() != null)
		{
			resolveEvents(f.getAppearanceScript().getEvents());
		}

		//
		// Combat is go!
		//
		currentCombat = new Combat(party, others, true);
		new Thread("Maze Combat Thread")
		{
			public void run()
			{
				try
				{
					if (!Maze.this.runCombat(party, others))
					{
						if (mazeVar != null)
						{
							// PC's did not win, clear the maze var
							MazeVariables.clear(mazeVar);
						}
					}
				}
				catch (Exception e)
				{
					errorDialog(e);
				}
			}
		}.start();

		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the combats ends with a PC victory, false otherwise
	 */
	private boolean runCombat(
		PlayerParty partyGroup,
		List<FoeGroup> others)
	{
		ArrayList<MazeEvent> evts = new ArrayList<MazeEvent>();
		evts.add(new FlavourTextEvent("ENCOUNTER!",
			Maze.getInstance().getUserConfig().getCombatDelay(), true));

		int avgFoeLevel = 0;
		for (FoeGroup fg : others)
		{
			avgFoeLevel += fg.getAverageLevel();
		}
		avgFoeLevel /= others.size();

		GameSys.getInstance().attemptManualIdentification(others, getParty(), 0);

		switch (currentCombat.getAmbushStatus())
		{
			case PARTY_MAY_EVADE_FOES:
				evts.add(new FlavourTextEvent("Party may evade foes!",
					Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
			case PARTY_AMBUSHES_FOES:
				evts.add(new FlavourTextEvent("Party surprises foes!",
					Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
			case FOES_MAY_EVADE_PARTY:
				Foe leader = GameSys.getInstance().getLeader(others);
				if (leader.shouldEvade(others, getParty()))
				{
					// cancel the encounter
					this.setState(State.MOVEMENT);
					return false;
				}
				else
				{
					evts.add(new FlavourTextEvent("Foes surprise party!",
						Maze.getInstance().getUserConfig().getCombatDelay(), true));
				}
				break;
			case FOES_AMBUSH_PARTY:
				evts.add(new FlavourTextEvent("Foes surprise party!",
					Maze.getInstance().getUserConfig().getCombatDelay(), true));
				break;
		}

		this.ui.setFoes(others);
		this.ui.showCombatOptions();

		this.resolveEvents(evts);

		if (currentCombat.getAmbushStatus() == Combat.AmbushStatus.PARTY_MAY_EVADE_FOES)
		{
			// give the player a choice.
			this.ui.showEvasionOptions();

			//------------------------------------------------------------------
			// blocking call:
			UserInterface.CombatOption actorActionOption = this.ui.getEvasionOption();
			//------------------------------------------------------------------

			if (actorActionOption == UserInterface.CombatOption.EVADE_FOES)
			{
				// give them the slip
				currentCombat.endCombat();
				ui.setFoes(null);
				this.setState(State.MOVEMENT);
				return false;
			}
		}

		int combatRound = 1;
		while (partyGroup.numAlive()>0 && getLiveFoes(others)>0)
		{
			Maze.log("--- combat round "+combatRound+" starts ---");

			//--- foe intentions
			java.util.List<ActorActionIntention[]> foeIntentionList = new ArrayList<ActorActionIntention[]>();

			for (FoeGroup other : others)
			{
				foeIntentionList.add(getFoeCombatIntentions(other));
			}

			//--- player character intentions
			ActorActionIntention[] partyIntentions
				= new ActorActionIntention[partyGroup.getActors().size()];
			if (currentCombat.getAmbushStatus() == Combat.AmbushStatus.FOES_AMBUSH_PARTY ||
				currentCombat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_EVADE_PARTY)
			{
				// party is surprised, cannot take action
				for (int i = 0; i < partyIntentions.length; i++)
				{
					partyIntentions[i] = ActorActionIntention.INTEND_NOTHING;
				}
			}
			else
			{
				this.ui.showFinalCombatOptions();
				//------------------------------------------------------------------
				// blocking call:
				UserInterface.CombatOption finalCombatOption = this.ui.getFinalCombatOption();
				//------------------------------------------------------------------

				if (finalCombatOption == UserInterface.CombatOption.START_ROUND)
				{
					// collect player character actions
					log(Log.DEBUG, "Collecting actor intentions for round "+combatRound);
					int max = partyGroup.size();
					int i = 0;
					while (i < max)
					{
						PlayerCharacter pc = (PlayerCharacter)partyGroup.getActors().get(i);

						try
						{
							ActorActionIntention actorActionOption;
							// dead characters should always be lined up at the end, so
							// a numAlive check works here
							if (i < partyGroup.numAlive())
							{
								// display character options
								if (GameSys.getInstance().askActorForCombatIntentions(pc))
								{
									actorActionOption = this.ui.getCombatIntention(pc);
									log(Log.DEBUG, pc.getName()+" at index "+i+" selects "+actorActionOption);
								}
								else
								{
									actorActionOption = ActorActionIntention.INTEND_NOTHING;
									log(Log.DEBUG, pc.getName()+" at index "+i+" cannot do anything and intends nothing");
								}

								partyIntentions[i++] = actorActionOption;
							}
							else
							{
								partyIntentions[i++] = ActorActionIntention.INTEND_NOTHING;
								log(Log.DEBUG, pc.getName()+" at index "+i+" is dead and intends nothing");
							}
						}
						catch (Exception e)
						{
							StringBuilder sb = new StringBuilder();
							for (ActorActionIntention ci : partyIntentions)
							{
								sb.append("ci = [").append(ci).append("]");
							}
							throw new MazeException(sb.toString(), e);
						}
					}
				}
				else if (finalCombatOption == UserInterface.CombatOption.TERMINATE_GAME)
				{
					backToMain();
					return false;
				}
			}

			// validate party intentions
			for (int i = 0; i < partyIntentions.length; i++)
			{
				if (partyIntentions[i] == null)
				{
					throw new MazeException("null actor intention at party index "+i);
				}
			}

			// execute any appearance scripts, picking from the first foe
			Foe f = (Foe)(others.get(0).getActors().get(0));
			if (f.getAppearanceScript() != null)
			{
				resolveEvents(f.getAppearanceScript().getEvents());
				try
				{
					Thread.sleep(Maze.getInstance().getUserConfig().getCombatDelay());
				}
				catch (InterruptedException e)
				{
					throw new MazeException(e);
				}
			}

			// Then, show the combat listener
			this.ui.showCombatDisplay();

			Iterator combatActions = currentCombat.combatRound(partyIntentions, foeIntentionList);
			while (combatActions.hasNext())
			{
				CombatAction action = (CombatAction)combatActions.next();
				List<MazeEvent> events = currentCombat.resolveAction(action);

				resolveEvents(events);

				checkPartyStatus();
				reorderPartyToCompensateForDeadCharacters();

				if (currentCombat == null)
				{
					// this means that something has suddenly terminated the combat
					return false;
				}
				else
				{
					UnifiedActor actor = action.getActor();
					CurMaxSub hp = actor.getHitPoints();
					if (hp.getSub() >= hp.getCurrent() && hp.getCurrent() > 0)
					{
						ConditionTemplate kot = Database.getInstance().getConditionTemplate(
							Constants.Conditions.FATIGUE_KO);
						Condition ko = kot.create(
							actor, actor, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE);
						resolveEvent(new ConditionEvent(actor, ko), true);
					}
				}
			}

			resolveEvents(currentCombat.endRound());
			this.ui.setFoes(others);

			if (pendingPartyOrder != null)
			{
				this.reorderParty(pendingPartyOrder, pendingFormation);
				
				pendingPartyOrder = null;
				pendingFormation = -1;
			}

			GameSys.getInstance().attemptManualIdentification(others, getParty(), combatRound);
			Maze.log("--- combat round "+combatRound+" ends ---");
			combatRound++;
			incTurn(false);
		}

		// speech bubble for the win
		SpeechUtil.getInstance().winCombatSpeech(avgFoeLevel, getParty().getPartyLevel());

		//finally, go back to movement
		ui.setFoes(null);
		ui.setAllies(null);
		
		checkPartyStatus();
		reorderPartyToCompensateForDeadCharacters();

		List<Item> loot = currentCombat.getLoot();
		int totalGold = TileScript.extractGold(loot);
		int totalExperience = currentCombat.getTotalExperience();
		int xp = totalExperience/party.numAlive();

		int extraPercent = 0;
		// calculate extra gold
		for (UnifiedActor pc : party.getActors())
		{
			extraPercent += pc.getModifier(Stats.Modifiers.EXTRA_GOLD);
		}
		totalGold += (totalGold*extraPercent/100);

		evts = new ArrayList<MazeEvent>();
		evts.add(new FlavourTextEvent("Victory!", Maze.getInstance().getUserConfig().getCombatDelay(), true));
		evts.add(new GrantExperienceEvent(xp, null));
		if (totalGold > 0)
		{
			evts.add(new GrantGoldEvent(totalGold));
		}
		evts.add(new GrantItemsEvent(loot));

		this.resolveEvents(evts);
		currentCombat.endCombat();
		currentCombat = null;

		this.setState(State.MOVEMENT);
		return true;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Checks to determine if the party is still alive, or if it's GAME OVER.
	 */
	public void checkPartyStatus()
	{
		if (party != null && party.numAlive() == 0)
		{
			// party is all dead;
			MazeScript script = Database.getInstance().getScript("_PARTY_DEAD_");
			script.getEvents().get(0).resolve();
			backToMain();
		}
	}

	/*-------------------------------------------------------------------------*/
	private ActorActionIntention[] getFoeCombatIntentions(FoeGroup foeGroup)
	{
		List<UnifiedActor> foes = foeGroup.getActors();
		ActorActionIntention[] result = new ActorActionIntention[foes.size()];

		for (int i=0; i<foes.size(); i++)
		{
			result[i] = ((Foe)(foes.get(i))).getCombatIntention();
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void addPartyAllies(List<FoeGroup> allies)
	{
		if (this.currentCombat != null)
		{
			this.currentCombat.addPartyAllies(allies);
			this.ui.setAllies(currentCombat.getPartyAllies());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addFoeAllies(List<FoeGroup> allies)
	{
		if (this.currentCombat != null)
		{
			this.currentCombat.addPartyAllies(allies);
			this.ui.setFoes(currentCombat.getFoes());
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean canSummon(UnifiedActor source, int nrGroups)
	{
		if (currentCombat == null)
		{
			// cannot summon outside of combat
			return false;
		}

		if (source instanceof Foe && ((Foe)source).isSummoned())
		{
			// summoned foes cannot themselves cast summon spells
			return false;
		}

		// limit each caster to one summoned group
		if (source.getCombatantData() != null && 
			!source.getCombatantData().canSummon())
		{
			return false;
		}

		// check that there is space
		if (source instanceof Foe && 
			(currentCombat.getFoes().size()+nrGroups > Constants.MAX_FOE_GROUPS))
		{
			return false;
		}
		else if (source instanceof PlayerCharacter && 
			(currentCombat.getFoes().size()+nrGroups > Constants.MAX_PARTY_ALLIES))
		{
			return false;
		}
		
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public AudioPlayer getAudioPlayer()
	{
		return this.audioPlayer;
	}

	/*-------------------------------------------------------------------------*/
	public AudioThread getAudioThread()
	{
		return this.audioThread;
	}

	/*-------------------------------------------------------------------------*/
	public void actorAttacks(UnifiedActor attacker)
	{
		this.ui.actorAttacks(attacker);
	}

	/*-------------------------------------------------------------------------*/
	public void actorDies(UnifiedActor victim)
	{
		victim.getHitPoints().setCurrent(0);
		if (victim instanceof Foe && this.currentCombat != null)
		{
			Foe foe = (Foe)victim;
			currentCombat.addDeadFoe(foe);
			ConditionManager.getInstance().removeConditions(foe);
		}
		this.ui.actorDies(victim);
	}

	/*-------------------------------------------------------------------------*/
	public void actorFlees(UnifiedActor coward)
	{
		if (coward instanceof Foe && this.currentCombat != null)
		{
			// a single foe flees from battle
			Foe foe = (Foe)coward;
			currentCombat.removeFoe(foe);
			this.ui.foeFlees(coward);
		}
		else if (coward instanceof PlayerCharacter && this.currentCombat != null)
		{
			// the party flees from combat
			this.currentCombat.endCombat();
			this.ui.setFoes(null);
			this.setState(State.MOVEMENT);
			this.currentCombat = null;
			this.ui.backPartyUp(-1);
		}
		else
		{
			throw new MazeException("Invalid actor: "+coward);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param maxKeys
	 * 	The maximum number of player keystrokes to reverse; may turn out to
	 * 	be less if there is less key history available.
	 */
	public void backPartyUp(int maxKeys)
	{
		this.ui.backPartyUp(maxKeys);
	}

	/*-------------------------------------------------------------------------*/
	private int getLiveFoes(List<FoeGroup> foes)
	{
		int sum = 0;
		for (ActorGroup g : foes)
		{
			sum += g.numAlive();
		}
		return sum;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Add an animation in combat.
	 *
	 * @param animation
	 * 	The animation to start.  A new instance will be spawned.
	 * @param mutex
	 * 	Any mutex that should be notified when the animation is complete.  May
	 * 	be null.
	 */
	public void startAnimation(Animation animation, Object mutex)
	{
		if (currentCombat == null)
		{
			return;
		}
		
		startAnimation(animation, mutex, currentCombat.getAnimationContext());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param animation
	 * 	The animation to start.  A new instance will be spawned.
	 * @param mutex
	 * 	Any mutex that should be notified when the animation is complete.  May
	 * 	be null.
	 */
	public void startAnimation(Animation animation,
		Object mutex,
		AnimationContext context)
	{
		if (!this.ui.supportsAnimation())
		{
			return;
		}

		Animation anim = animation.spawn(context);
		if (mutex != null)
		{
			anim.setMutex(mutex);
		}

		this.ui.addAnimation(anim);
	}

	/*-------------------------------------------------------------------------*/
	public void appendEvents(MazeEvent... events)
	{
		for (MazeEvent e : events)
		{
			processor.queue.offer(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void appendEvents(List<MazeEvent> events)
	{
		for (MazeEvent e : events)
		{
			processor.queue.offer(e);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void resolveEvents(List<MazeEvent> events)
	{
		this.resolveEvents(events, true);
	}
	
	/*-------------------------------------------------------------------------*/
	public void resolveEvents(List<MazeEvent> events, boolean displayEventText)
	{
		if (events == null)
		{
			return;
		}

		for (MazeEvent event : events)
		{
			resolveEvent(event, displayEventText);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void resolveEvent(MazeEvent event, boolean displayEventText)
	{
		List<MazeEvent> subEvents = event.resolve();
		
		// event resolution may alter some of it's state - thus only display
		// the event now.
		this.ui.displayMazeEvent(event, displayEventText);

		// only wait on delays and stuff if the event text needs to be shown
		if (displayEventText)
		{
			// (WAIT_ON_READLINE events have wait()'s in their resolve methods)
			if (event.getDelay() == MazeEvent.Delay.WAIT_ON_CLICK)
			{
				synchronized(event)
				{
					try
					{
						log(Log.DEBUG, "waiting on ["+event.getClass().getSimpleName()+
							"] ["+event.toString()+"]");
						event.wait();
					}
					catch (InterruptedException e)
					{
						throw new MazeException(e);
					}
				}
			}
			else if (event.getDelay() > MazeEvent.Delay.NONE)
			{
				synchronized(event)
				{
					try
					{
						// wait instead of sleep so that the user can
						// click past any text
						event.wait(event.getDelay());
					}
					catch (InterruptedException e)
					{
						Maze.log.log(e);
					}
				}
			}
		}

		checkPartyStatus();

		if (subEvents != null && !subEvents.isEmpty())
		{
			resolveEvents(subEvents, displayEventText);
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getPlayerSpeech()
	{
		return this.ui.getPlayerSpeech();
	}

	/*-------------------------------------------------------------------------*/
	public void addObject(EngineObject obj)
	{
		this.ui.addObject(obj);
	}
	
	/*-------------------------------------------------------------------------*/
	public void removeObject(EngineObject obj)
	{
		this.ui.removeObject(obj);
	}

	/*-------------------------------------------------------------------------*/
	public void removeObject(String objectName)
	{
		this.ui.removeObject(objectName);
	}

	/*-------------------------------------------------------------------------*/
	public void addObjectInFrontOfPlayer(
		EngineObject obj, double distance, double arcOffset, boolean randomStartingFrame)
	{
		this.ui.addObjectInFrontOfPlayer(obj, distance, arcOffset, randomStartingFrame);
	}
	
	/*-------------------------------------------------------------------------*/
	public void transferPlayerCharacterToParty(PlayerCharacter pc, Npc npc)
	{
		removePlayerCharacterFromGuild(pc, npc);
		addPlayerCharacterToParty(pc);
	}
	
	/*-------------------------------------------------------------------------*/
	public void transferPlayerCharacterToGuild(PlayerCharacter pc, Npc npc)
	{
		removePlayerCharacterFromParty(pc);
		addPlayerCharacterToGuild(pc, npc);
	}
	
	/*-------------------------------------------------------------------------*/
	public void removePlayerCharacterFromGuild(PlayerCharacter pc, Npc npc)
	{
		npc.getGuild().remove(pc.getName());
	}
	
	/*-------------------------------------------------------------------------*/
	public void addPlayerCharacterToGuild(PlayerCharacter pc, Npc npc)
	{
		playerCharacterCache.put(pc.getName(), pc);
		npc.getGuild().add(pc.getName());
	}

	/*-------------------------------------------------------------------------*/
	public void addPlayerCharacterToGuild(PlayerCharacter pc)
	{
		try
		{
			// todo: prevent duplicate names
			Database db = Database.getInstance();
			db.getCharacterGuild().put(pc.getName(), pc);
			db.getSaver().saveCharacterGuild(db.getCharacterGuild());
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void addPlayerCharacterToParty(PlayerCharacter pc)
	{
		playerCharacterCache.put(pc.getName(), pc);
		if (party == null || party.getActors().isEmpty())
		{
			// adding the first character
			ArrayList<UnifiedActor> chars = new ArrayList<UnifiedActor>();
			chars.add(pc);
	
			party = new PlayerParty(chars, 0, 10, 1);
			this.ui.setParty(party);
			this.ui.characterSelected(pc);
		}
		else
		{
			party.getActors().add(pc);
			if (party.size() <3)
			{
				party.setFormation(1);
			}
			else
			{
				party.setFormation(3);
			}
			this.ui.setParty(party);
			this.ui.characterSelected(pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void renamePlayerCharacter(PlayerCharacter character, String newName)
	{
		playerCharacterCache.remove(character.getName());
		character.setName(newName);
		playerCharacterCache.put(character.getName(), character);
		this.ui.refreshCharacterData();
	}
	
	/*-------------------------------------------------------------------------*/
	public void removePlayerCharacterFromParty(PlayerCharacter pc)
	{
		ui.stopAllAnimations();
		party.getActors().remove(pc);
		if (party.size() <3)
		{
			party.setFormation(1);
		}
		
		if (party.getActors().isEmpty())
		{
			party = null;
			this.ui.setParty(null);
		}
		else
		{
			ui.characterSelected(party.getPlayerCharacter(0));
			this.ui.setParty(party);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void removePlayerCharacterFromGuild(
		PlayerCharacter pc, 
		Map<String, PlayerCharacter> guild)
	{
		if (pc == null)
		{
			return;
		}
		
		guild.remove(pc.getName());
		
		try
		{
			Database.getInstance().getSaver().saveCharacterGuild(guild);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void reorderParty(List<PlayerCharacter> actors, int formation)
	{
		List<UnifiedActor> a = new ArrayList<UnifiedActor>();
		a.addAll(actors);
		this.party.setActors(a);
		this.party.setFormation(formation);
		this.ui.setParty(party);
		reorderPartyToCompensateForDeadCharacters();
	}
	
	/*-------------------------------------------------------------------------*/
	private void reorderPartyToCompensateForDeadCharacters()
	{
		if (party != null)
		{
			// move dead characters to the back of the party
			party.reorderPartyToCompensateForDeadCharacters();
			this.ui.setParty(party);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void partyHides()
	{
		GameSys.getInstance().partyHidesOutOfCombat(party, getCurrentTile());
		incTurn(true);
	}

	/*-------------------------------------------------------------------------*/
	public void setPendingFormationChanges(List<PlayerCharacter> actors, int formation)
	{
		this.pendingPartyOrder = actors;
		this.pendingFormation = formation;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Displays the signboard to the player.
	 */
	public void signBoard(String message, MazeEvent event)
	{
		this.ui.signBoard(message, event);
	}

	/*-------------------------------------------------------------------------*/
	public void speechBubble(String speechKey, PlayerCharacter pc)
	{
		this.speechBubble(speechKey, pc, pc.getPersonality(), getUi().getPlayerCharacterWidgetBounds(pc));
	}

	/*-------------------------------------------------------------------------*/
	public void speechBubble(String speechKey, PlayerCharacter pc, Personality p, Rectangle bounds)
	{
		SpeechBubbleEvent e = new SpeechBubbleEvent(
			pc,
			p,
			speechKey,
			bounds,
			false);

		getUi().stopAllAnimations();
		resolveEvent(e, false);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Kicks off the level up wizard for the given character.
	 */
	public void levelUp(PlayerCharacter playerCharacter)
	{
		this.ui.levelUp(playerCharacter);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Grants the given items to the player.  Any that are not taken are left
	 * on the ground.
	 */
	public void grantItems(List<Item> items)
	{
		if (items == null || items.size() == 0)
		{
			return;
		}

		// first, attempt a manual identify on all items
		for (Item i : items)
		{
			GameSys.getInstance().attemptManualIdentify(i, party);
		}

		this.ui.grantItems(items);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Drops the listed items on the ground.
	 */
	public void dropItemsOnCurrentTile(List<Item> items)
	{
		ItemCacheManager.getInstance().dropOnTile(zone, getTile(), items);
		// refresh the tile display
		this.ui.setTile(zone, getCurrentTile(), getTile());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param facing
	 * 	The current facing of the player, a constant from CrusaderEngine.Facing.
	 */
	public void encounterTile(Point tile, Point previousTile, int facing)
	{
		if (zone == null)
		{
			// something is borked
			return;
		}
		Tile t = zone.getTile(tile);

		playerTilesVisited.visitTile(zone.getName(), tile);

		this.playerPos = tile;
		Maze.getInstance().incTurn(true);
		this.ui.setTile(zone, t, tile);
		this.zone.encounterTile(instance, tile, previousTile, facing);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param tile
	 * 	The tile that the player is currently on
	 * @param facing
	 * @return
	 * 	true if the player is allowed to perform the move
	 */
	public Zone.Vector playerAttemptsMoveThroughWall(Point tile, int facing)
	{
		Portal portal = zone.getPortal(tile, facing);
		if (portal != null)
		{
			String state = portal.getState();
			
			if (state.equalsIgnoreCase(Portal.State.UNLOCKED))
			{
				Point destination;
				int resultantFacing;
				if (tile.equals(portal.getFrom()))
				{
					destination = portal.getTo();
					resultantFacing = reverseFacing(portal.getToFacing());
				}
				else
				{
					destination = portal.getFrom();
					resultantFacing = reverseFacing(portal.getFromFacing());
				}
				
				return new Zone.Vector(destination, resultantFacing, portal);
			}
			else if (state.equalsIgnoreCase(Portal.State.LOCKED))
			{
				MazeScript script = Database.getInstance().getScript("_OUCH_");
				resolveEvents(script.getEvents());
				ui.addMessage("LOCKED!");
				return null;
			}
			else if (state.equalsIgnoreCase(Portal.State.WALL_LIKE))
			{
				walkIntoWall();
				return null;
			}
			else
			{
				throw new MazeException("Invalid portal state: ["+state+"]");
			}
		}
		else
		{
			walkIntoWall();
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void walkIntoWall()
	{
		MazeScript script = Database.getInstance().getScript("_OUCH_");
		resolveEvents(script.getEvents());
		// todo: should probably make these temp messages maze events
		ui.addMessage("OUCH!");
	}

	/*-------------------------------------------------------------------------*/
	private int reverseFacing(int facing)
	{
		switch (facing)
		{
			case NORTH: return SOUTH;
			case SOUTH: return NORTH;
			case EAST: return WEST;
			case WEST: return EAST;
			default: throw new MazeException("invalid facing "+facing);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void encounterChest(Chest chest)
	{
		this.currentChest = chest;
		this.currentChest.refreshCurrentTrap();
		this.setState(State.ENCOUNTER_CHEST);
	}

	/*-------------------------------------------------------------------------*/
	public void encounterPortal(Portal portal)
	{
		this.currentPortal = portal;
		this.setState(State.ENCOUNTER_PORTAL);
	}

	/*-------------------------------------------------------------------------*/
	public void encounterNpc(final Npc npc, Point tile, Point previousTile)
	{
		if (previousTile != null && previousTile.equals(tile))
		{
			// todo: this prevents wandering NPCs from encountering the party
			return;
		}

		currentNpc = npc;
		setState(Maze.State.ENCOUNTER_NPC);

		new Thread("Maze NPC encounter thread")
		{
			public void run()
			{
				try
				{
					// first, any pre-encounter events need to be executed
					processNpcEventsInternal(npc.getScript().preAppearance());

					// add the NPC to the UI.
					FoeTemplate npcFoeTemplate = Database.getInstance().getFoeTemplate(npc.getFoeName());
					Foe foe = new Foe(npcFoeTemplate);

					// init foes
					ArrayList<FoeGroup> allFoes = new ArrayList<FoeGroup>();
					for (int i=0; i<1; i++)
					{
						List<UnifiedActor> foes = new ArrayList<UnifiedActor>();
						foes.add(foe);
						FoeGroup foesGroup = new FoeGroup(foes);
						allFoes.add(foesGroup);
					}

					ui.setFoes(allFoes);

					if (npc.getAttitude() < 0)
					{
						//NPC is pissed off and simply attacks the party
						processNpcEventsInternal(npc.getScript().attacksParty());
					}
					else
					{
						if (!npc.isFound())
						{
							// first meeting: process diplomacy bonus of the best diplomat
							int diplomacy = 0;
							for (UnifiedActor a : getParty().getActors())
							{
								if (a.getModifier(Stats.Modifiers.DIPLOMAT) > diplomacy)
								{
									diplomacy = a.getModifier(Stats.Modifiers.DIPLOMAT);
								}
							}
							if (diplomacy > 0)
							{
								npc.incAttitude(diplomacy);
							}
						}
						
						if (npc.getAttitude() >= 100)
						{
							if (!npc.isFound())
							{
								processNpcEventsInternal(npc.getScript().firstGreeting());
								npc.setFound(true);
							}
							else
							{
								processNpcEventsInternal(npc.getScript().subsequentGreeting());
							}
						}
						else
						{
							if (!npc.isFound())
							{
								processNpcEventsInternal(npc.getScript().firstGreeting());
								npc.setFound(true);
							}
							else
							{
								processNpcEventsInternal(npc.getScript().neutralGreeting());
							}
						}
					}
				}
				catch (Exception e)
				{
					errorDialog(e);
				}
			}
		}.start();
	}

	/*-------------------------------------------------------------------------*/
	public void processNpcScriptEvents(final List<MazeEvent> events)
	{
		new Thread("Maze NPC events thread")
		{
			public void run()
			{
				try
				{
					processNpcEventsInternal(events);
				}
				catch (Exception e)
				{
					errorDialog(e);
				}
			}
		}.start();
	}

	/*-------------------------------------------------------------------------*/
	public void processTileScriptEvents(List<MazeEvent> events)
	{
		if (events == null || events.isEmpty())
		{
			return;
		}

		appendEvents(new PushStateEvent(State.ENCOUNTER_TILE));
		appendEvents(events);
		appendEvents(new ClearStateEvent(State.ENCOUNTER_TILE));
		appendEvents(new MazeEvent()
		{
			public List<MazeEvent> resolve()
			{
				if (state.peek() == State.MOVEMENT)
				{
					Maze.this.setState(State.MOVEMENT);
				}
				return null;
			}
		});
	}

	/*-------------------------------------------------------------------------*/
	private void processNpcEventsInternal(List<MazeEvent> events)
	{
		resolveEvents(events);
		if (this.state.peek() == State.ENCOUNTER_NPC)
		{
			this.ui.showNpcScreen(currentNpc);
		}
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the action may proceed, false if not
	 */
	public boolean processPlayerAction(int playerAction, int facing)
	{
		return this.zone.processPlayerAction(instance, this.playerPos, facing, playerAction);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	true if the normal item use must continue, false otherwise
	 */
	public boolean processUseItem(Item item, PlayerCharacter user, int facing)
	{
		return this.zone.processUseItem(instance, this.playerPos, facing, item, user);
	}

	/*-------------------------------------------------------------------------*/
	public void changeZone(String zoneName, Point pos, int facing)
	{
		// todo: transferal of conditions on tiles in the old zone and the new zone

		int newFacing;
		if (facing == ZoneChangeEvent.Facing.UNCHANGED)
		{
			newFacing = this.ui.getFacing();
		}
		else
		{
			newFacing = facing;
		}

		this.zone = Database.getInstance().getZone(zoneName);
		this.ui.setZone(zone, pos, newFacing);
		this.zone.init(getTurnNr());
		
		Tile t = zone.getTile(pos);
		setPlayerPos(pos, newFacing);
	}
	
	/*-------------------------------------------------------------------------*/
	public void setPlayerPos(Point pos, int facing)
	{
		int newFacing;
		if (facing == ZoneChangeEvent.Facing.UNCHANGED)
		{
			newFacing = this.ui.getFacing();
		}
		else
		{
			newFacing = facing;
		}
		this.playerPos = pos;
		this.ui.setPlayerPos(pos, newFacing);
	}
	
	/*-------------------------------------------------------------------------*/
	public Component getComponent()
	{
		return this.ui.getComponent();
	}

	/*-------------------------------------------------------------------------*/
	public GameSys getGameSys()
	{
		return gameSys;
	}

	/*-------------------------------------------------------------------------*/
	public MagicSys getMagicSys()
	{
		return magicSys;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerParty getParty()
	{
		return party;
	}

	/*-------------------------------------------------------------------------*/
	public void setParty(PlayerParty party)
	{
		this.party = party;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	the player character, indexed from 0, null if no such exists.
	 */
	public PlayerCharacter getPlayerCharacter(int index)
	{
		List<UnifiedActor> actors = party.getActors();
		if (actors.size() <= index)
		{
			return null;
		}

		return (PlayerCharacter)actors.get(index);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	the player character, null if no such exists.
	 */
	public UnifiedActor getPlayerCharacter(String name)
	{
		if (party == null)
		{
			return null;
		}

		return playerCharacterCache.get(name);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	the actor group to which the given actor belongs
	 */
	public ActorGroup getActorGroup(UnifiedActor actor)
	{
		if (party.getActors().contains(actor))
		{
			return party;
		}

		if (currentCombat != null)
		{
			List<FoeGroup> list = currentCombat.getFoes();
			for (FoeGroup fg : list)
			{
				if (fg.getActors().contains(actor))
				{
					return fg;
				}
			}

			list = currentCombat.getPartyAllies();
			for (FoeGroup fg : list)
			{
				if (fg.getActors().contains(actor))
				{
					return fg;
				}
			}
		}

		// not found
		throw new MazeException("Actor not found ["+actor.getName()+"]");
	}

	/*-------------------------------------------------------------------------*/
	public Zone getZone()
	{
		return zone;
	}

	/*-------------------------------------------------------------------------*/
	public Object getEventMutex()
	{
		return eventMutex;
	}

	/*-------------------------------------------------------------------------*/
	public DifficultyLevel getDifficultyLevel()
	{
		return difficultyLevel;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerTilesVisited getPlayerTilesVisited()
	{
		return playerTilesVisited;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @return
	 * 	The tile that the party is currently on, or null if none.
	 */
	public Tile getCurrentTile()
	{
		if (zone == null)
		{
			return null;
		}

		return zone.getTile(this.playerPos);
	}

	public Chest getCurrentChest()
	{
		return currentChest;
	}

	public Combat getCurrentCombat()
	{
		return currentCombat;
	}

	public void setCurrentCombat(Combat currentCombat)
	{
		this.currentCombat = currentCombat;
	}

	public Npc getCurrentNpc()
	{
		return currentNpc;
	}

	public Portal getCurrentPortal()
	{
		return currentPortal;
	}

	/*-------------------------------------------------------------------------*/
	public Point getPlayerPos()
	{
		return playerPos;
	}
	public Point getTile()
	{
		return playerPos;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Return the players current facing, returns a constant from 
	 * {@link mclachlan.crusader.CrusaderEngine.Facing}
	 */
	public int getFacing()
	{
		return this.ui.getFacing();
	}

	/*-------------------------------------------------------------------------*/
	public Map<String, String> getAppConfig()
	{
		return this.appConfig;
	}

	/*-------------------------------------------------------------------------*/
	public UserConfig getUserConfig()
	{
		return this.userConfig;
	}

	/*-------------------------------------------------------------------------*/
	public void saveUserConfig()
	{
		saveUserConfig(this.userConfig);
	}

	/*-------------------------------------------------------------------------*/
	public void saveUserConfig(UserConfig userConfig)
	{
		boolean changeMusicVolume =
			userConfig.getMusicVolume() != this.userConfig.getMusicVolume();

		// copy the new values to memory
		this.userConfig.fromProperties(userConfig.toProperties());

		// commit to disk
		try
		{
			Database.getInstance().getSaver().saveUserConfig(userConfig);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}

		// update any running values
//		if (changeMusicVolume)
		{
			getUi().getMusic().setEnabled(userConfig.getMusicVolume() > 0);
			getUi().getMusic().setVolume(userConfig.getMusicVolume());
		}
	}

	/*-------------------------------------------------------------------------*/
	public Campaign getCampaign()
	{
		return this.campaign;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * For testing only.
	 */
	public static Campaign getStubCampaign()
	{
		return new Campaign(
			"default",
			"Episode 1 (default campaign)",
			"The default campaign",
			"arena load script",
			"Human",
			"portrait/human_female_margarita", 
			null);
	}

	/*-------------------------------------------------------------------------*/
	public UserInterface getUi()
	{
		return ui;
	}

	/*-------------------------------------------------------------------------*/
	public static class AppConfig
	{
		public static final String SCREEN_WIDTH = "mclachlan.maze.screen.width";
		public static final String SCREEN_HEIGHT = "mclachlan.maze.screen.height";

		public static final String LOG_IMPL = "mclachlan.maze.log.impl";
		public static final String LOG_LEVEL = "mclachlan.maze.log.level";

		public static final String UI_IMPL = "mclachlan.maze.ui.impl";
		public static final String UI_DEBUG = "mclachlan.maze.ui.debug";
		public static final String UI_RENDERER = "mclachlan.maze.ui.renderer";

		public static final String GAME_SYS_IMPL = "mclachlan.maze.gamesys.impl";
		public static final String MAGIC_SYS_IMPL = "mclachlan.maze.magicsys.impl";
		public static final String DB_LOADER_IMPL = "mclachlan.maze.db.loader.impl";
		public static final String DB_SAVER_IMPL = "mclachlan.maze.db.saver.impl";
		
		public static final String VERSION = "mclachlan.maze.version";
		public static final String CAMPAIGN = "mclachlan.maze.campaign";
		public static final String SHOW_LAUNCHER = "mclachlan.maze.show_launcher";

		public static final String DEBUG_KNOWLEDGE_EVENTS = "mclachlan.maze.game.debug_knowledge_events";
		public static final String DISABLE_RANDOM_SPAWNS = "mclachlan.maze.game.disable_random_spawns";
		public static final String DEFAULT_FONT = "mclachlan.maze.screen.default_font";
		public static final String DEFAULT_FONT_SIZE = "mclachlan.maze.screen.default_font_size";
	}

	/*-------------------------------------------------------------------------*/
	private static class EventProcessor extends Thread
	{
		BlockingQueue<MazeEvent> queue;

		public EventProcessor(BlockingQueue<MazeEvent> queue)
		{
			super("MAZE EVENT THREAD");
			this.queue = queue;
		}

		public void run()
		{
			while (1==1)
			{
				try
				{
					MazeEvent event = queue.take();
					Maze.getInstance().resolveEvent(event, true);
				}
				catch (Exception e)
				{
					Maze.getInstance().errorDialog(e);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public class PushStateEvent extends MazeEvent
	{
		State state;

		public PushStateEvent(State state)
		{
			this.state = state;
		}

		public List<MazeEvent> resolve()
		{
			Maze.this.pushState(state);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public class PopStateEvent extends MazeEvent
	{
		public List<MazeEvent> resolve()
		{
			Maze.this.popState();
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public class ClearStateEvent extends MazeEvent
	{
		State state;

		public ClearStateEvent(State state)
		{
			this.state = state;
		}

		public List<MazeEvent> resolve()
		{
			Maze.this.clearState(state);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public class SetStateEvent extends MazeEvent
	{
		State state;

		public SetStateEvent(State state)
		{
			this.state = state;
		}

		public List<MazeEvent> resolve()
		{
			Maze.this.setState(state);
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public class ShowCombatDisplayEvent extends MazeEvent
	{
		public List<MazeEvent> resolve()
		{
			Maze.this.getUi().showCombatDisplay();
			return null;
		}
	}
}
