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
import mclachlan.maze.audio.OggAudioPlayer;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.event.*;
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.map.*;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.map.script.FlavourTextEvent;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatAction;
import mclachlan.maze.stat.combat.DefendIntention;
import mclachlan.maze.stat.combat.event.AnimationEvent;
import mclachlan.maze.stat.combat.event.PersonalitySpeechBubbleEvent;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.condition.ConditionManager;
import mclachlan.maze.stat.condition.ConditionTemplate;
import mclachlan.maze.stat.condition.impl.RestingSleep;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.stat.npc.NpcManager;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.ui.diygui.*;
import mclachlan.maze.ui.diygui.animation.AnimationContext;
import mclachlan.maze.ui.diygui.animation.FadeToBlackAnimation;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.util.MazeException;
import mclachlan.maze.util.PerfLog;

import static mclachlan.crusader.CrusaderEngine.Facing.*;
import static mclachlan.maze.stat.combat.Combat.AmbushStatus.NONE;

/**
 *
 */
public class Maze implements Runnable
{
	private static Maze instance;
	private Object statePopMutex;
	private final Map<String, String> appConfig;
	private UserConfig userConfig;
	private final Campaign campaign;
	private PlayerTilesVisited playerTilesVisited;

	/** the current game state */
	private State state;

	/** the current player party */
	private PlayerParty party;

	/** the current audio player implementation */
	private AudioPlayer audioPlayer;
	private final AudioThread audioThread = new AudioThread();

	/** the current zone of play */
	private Zone zone;

	/** the current difficulty level */
	private DifficultyLevel difficultyLevel;

	/** the players tile coords within the current zone */
	private Point playerPos;

	/** the current user interface implementation */
	private UserInterface ui;

	/** the current game rules implementation */
	private GameSys gameSys;

	/** the current magic system implementation */
	private MagicSys magicSys;

	/** the current logging implementation */
	private static Log log;
	private static PerfLog perfLog;

	/** any combat currently underway */
	private Combat currentCombat;
	/** any chest that the player is currently encountering */
	private Chest currentChest;
	/** any portal that the player is currently encountering */
	private Portal currentPortal;
	/** any actors currently being encountered */
	private ActorEncounter currentActorEncounter;

	/** pending formation changes in combat */
	private List<PlayerCharacter> pendingPartyOrder;
	private int pendingFormation;

	private final Object eventMutex = new Object();
	private Map<String, PlayerCharacter> playerCharacterCache = 
		new HashMap<>();

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
		/** Encountering a chest */
		ENCOUNTER_CHEST,
		/** Encountering actors, could be foes or an NPC */
		ENCOUNTER_ACTORS,
		/** Encountering a portal */
		ENCOUNTER_PORTAL,
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

	/*-------------------------------------------------------------------------*/
	public static void destroy()
	{
		instance = null;
	}

	/*-------------------------------------------------------------------------*/
	public void init() throws Exception
	{
		Database db = new Database();
		this.userConfig = db.getUserConfig();

		log("init db");
		Database.getInstance().initImpls();
		initUi(getUi(appConfig));

		log("start threads");
		startThreads();

		int MAX_PROGRESS = 35;

		LoadingScreen screen = new LoadingScreen(MAX_PROGRESS);
		ui.showDialog(screen);
		ProgressListener progress = screen.getProgressListener();

		Database.getInstance().initCaches(progress);

		progress.message(StringUtil.getUiLabel("ls.init.audio"));
//		initAudio(new WavAudioPlayer());
		initAudio(new OggAudioPlayer());
		progress.incProgress(1);

		progress.message(StringUtil.getUiLabel("ls.init.logs"));
		initLog(getLog(appConfig));
		initPerfLog(getPerfLog(appConfig));
		progress.incProgress(1);

		progress.message(StringUtil.getUiLabel("ls.init.state"));
		initState();
		progress.incProgress(1);

		// Beware the dependencies between components here.
		progress.message(StringUtil.getUiLabel("ls.init.systems"));
		initSystems();
		progress.incProgress(1);

		progress.message(StringUtil.getUiLabel("ls.build.gui"));
		getUi().buildGui();
		progress.incProgress(1);

		this.ui.setDebug(Boolean.valueOf(appConfig.get(AppConfig.UI_DEBUG)));
		this.ui.clearDialog();
		this.ui.showMainMenu();
	}

	/*-------------------------------------------------------------------------*/
	private Log getLog(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String log_impl = config.get(Maze.AppConfig.LOG_IMPL);
		Class<Log> log_class = (Class<Log>)Class.forName(log_impl);
		Log log = (Log)log_class.newInstance();
		int logLevel = Integer.parseInt(config.get(Maze.AppConfig.LOG_LEVEL));
		log.setLevel(logLevel);
		int bufferSize = Integer.parseInt(config.get(Maze.AppConfig.LOG_BUFFER_SIZE));
		log.setBufferSize(bufferSize);

		return log;
	}

	/*-------------------------------------------------------------------------*/
	private PerfLog getPerfLog(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String log_impl = config.get(Maze.AppConfig.PERF_LOG_IMPL);
		Class<PerfLog> log_class = (Class<PerfLog>)Class.forName(log_impl);
		PerfLog log = (PerfLog)log_class.newInstance();
		int logLevel = Integer.parseInt(config.get(Maze.AppConfig.PERF_LOG_LEVEL));
		log.setLevel(logLevel);
		int bufferSize = Integer.parseInt(config.get(Maze.AppConfig.LOG_BUFFER_SIZE));
		log.setBufferSize(bufferSize);

		return log;
	}

	/*-------------------------------------------------------------------------*/
	private UserInterface getUi(Map<String, String> config)
		throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		String ui_impl = config.get(Maze.AppConfig.UI_IMPL);
		Class<UserInterface> ui_class = (Class<UserInterface>)Class.forName(ui_impl);
		return (DiyGuiUserInterface)ui_class.newInstance();
	}

	/*-------------------------------------------------------------------------*/
	public void initState()
	{
		this.state = State.MAINMENU;
	}

	/*-------------------------------------------------------------------------*/
	public void initLog(Log log)
	{
		Maze.log = log;
	}

	/*-------------------------------------------------------------------------*/
	public void initPerfLog(PerfLog log)
	{
		Maze.perfLog = log;
	}

	/*-------------------------------------------------------------------------*/
	public void initUi(UserInterface ui) throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		log("init ui: "+ui.getClass());
		this.ui = ui;
	}

	/*-------------------------------------------------------------------------*/
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

	/*-------------------------------------------------------------------------*/
	public void initAudio(AudioPlayer player)
	{
		audioPlayer = player;
	}

	/*-------------------------------------------------------------------------*/
	public void startThreads()
	{
		log("starting main thread...");

		BlockingQueue<MazeEvent> q = new ArrayBlockingQueue<>(9999);
		processor = new EventProcessor(q);
		processor.start();

		new Thread(this, "MAZE RENDER THREAD").start();

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
	 * Logs the given message at the {@link Log#DEBUG} level.
	 */
	public static void logDebug(String msg)
	{
		log(Log.DEBUG, msg);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Logs the given message at the given level.
	 */
	public static void log(int lvl, String msg)
	{
		log(lvl, msg, (Object)null);
	}

	/*-------------------------------------------------------------------------*/
	public static void log(int lvl, String msg, Object... args)
	{
		if (log != null)
		{
			log.log(lvl, msg, args);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static PerfLog getPerfLog()
	{
		return perfLog;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Journal's the given text in the context of any current NPC or Zone
	 */
	public void journalInContext(String text)
	{
		if (getCurrentActorEncounter() != null)
		{
			Foe leader = getCurrentActorEncounter().getLeader();
			if (leader.isNpc())
			{
				JournalManager.getInstance().npcJournal(text);
			}
		}

		if (getCurrentZone() != null)
		{
			JournalManager.getInstance().zoneJournal(text);
		}
	}

	/*-------------------------------------------------------------------------*/
	public GameState getGameState()
	{
		return new GameState(
			zone.getName(),
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
	public List<MazeEvent> setGameState(GameState gs)
	{
		GameTime.setTurnNr(gs.getTurnNr());
		difficultyLevel = gs.getDifficultyLevel();
		party.setGold(gs.getPartyGold());
		party.setSupplies(gs.getPartySupplies());
		party.setFormation(gs.getFormation());

		return changeZone(gs.getCurrentZone(), gs.getPlayerPos(), gs.getFacing());
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
			JournalManager.getInstance().saveGame(name, saver);
			ConditionManager.getInstance().saveGame(name, saver);
			
			ui.addMessage("Game Saved", true);
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void quickStart()
	{
		TextDialogWidget dialog =
			ui.waitingDialog(StringUtil.getUiLabel("mm.rolling.characters"));
		dialog.addText("\n\n");

		// get the first dificulty level, by sort order
		Map<String, DifficultyLevel> difficultyLevels =
			Database.getInstance().getDifficultyLevels();

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

		int maxTextLen = 1000;
		List<PlayerCharacter> pcs;
		do
		{
			pcs = new ArrayList<>();

			int startIndex = getParty() == null ? 0 : getParty().size();
			for (int i= startIndex; i<6; i++)
			{
				PlayerCharacter pc = leveler.createRandomPlayerCharacter();
				pcs.add(pc);

				String text = dialog.getText();
				text += pc.getName()+"... ";

				if (text.length() > maxTextLen)
				{
					text = text.substring(text.length()-maxTextLen);
				}

				dialog.setText(text);
			}
		}
		while (!leveler.validateParty(pcs));

		pcs.sort((pc1, pc2) -> {
			CharacterClass.Focus f1 = pc1.getCharacterClass().getFocus();
			CharacterClass.Focus f2 = pc2.getCharacterClass().getFocus();

			return f1.getSortOrder() - f2.getSortOrder();
		});

		// add PC to the party and go go go!
		for (PlayerCharacter pc : pcs)
		{
			this.addPlayerCharacterToParty(pc);
		}

		ui.clearDialog();

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

			final int MAX_PROGRESS = 6;

			// loading screen
			LoadingScreen screen = new LoadingScreen(MAX_PROGRESS);
			ui.showDialog(screen);
			ProgressListener progress = screen.getProgressListener();

			// load gamestate
			//... not needed
			progress.message(StringUtil.getUiLabel("ls.gametime"));
			GameTime.startGame();
			progress.incProgress(1);

			// construct player party
			//... already done
			
			// set difficulty level
			progress.message(StringUtil.getUiLabel("ls.difficulty"));
			this.difficultyLevel = Database.getInstance().getDifficultyLevels().get(difficultyLevel);
			progress.incProgress(1);

			// load tiles visited
			progress.message(StringUtil.getUiLabel("ls.tiles.visited"));
			this.playerTilesVisited = new PlayerTilesVisited();
			progress.incProgress(1);

			// clear maze vars
			progress.message(StringUtil.getUiLabel("ls.clear.maze.vars"));
			MazeVariables.clearAll();
			progress.incProgress(1);
		
			// load NPCs
			progress.message(StringUtil.getUiLabel("ls.npc"));
			NpcManager.getInstance().startGame();
			progress.incProgress(1);

			// load maze vars
			// load item caches
			//... not needed

			// init journals
			progress.message(StringUtil.getUiLabel("ls.journals"));
			JournalManager.getInstance().startGame();
			progress.incProgress(1);

			// ui cleanup for the transition
			ui.clearMessages();
			ui.clearDialog();
			ui.showBlockingScreen("screen/blank_screen", -1, null);

			// start campaign
			party.setSupplies(party.size()*4);
			MazeScript startingScript = Database.getInstance().getMazeScript(campaign.getStartingScript());
			appendEvents(startingScript.getEvents());
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
			// sync with mclachlan.maze.editor.swing.SaveGamePanel.refresh()

			// quiet ui
			ui.stopAllAnimations();
			ui.getMusic().stop();
			ui.getMusic().setState(null);

			final int MAX_PROGRESS = 11;

			// loading screen
			LoadingScreen screen = new LoadingScreen(MAX_PROGRESS);
			ui.showDialog(screen);

			Loader loader = Database.getInstance().getLoader();
			ProgressListener progress = screen.getProgressListener();

			// load gamestate
			progress.message(StringUtil.getUiLabel("ls.gamestate"));
			GameState gs = loader.loadGameState(name);
			progress.incProgress(1);

			// construct player party
			progress.message(StringUtil.getUiLabel("ls.party"));
			playerCharacterCache = loader.loadPlayerCharacters(name);
			List<UnifiedActor> list = new ArrayList<>();
			for (String s : gs.getPartyNames())
			{
				list.add(playerCharacterCache.get(s));
			}
			party = new PlayerParty(list);
			progress.incProgress(1);

			// set difficulty level
			progress.message(StringUtil.getUiLabel("ls.difficulty"));
			difficultyLevel = gs.getDifficultyLevel();
			progress.incProgress(1);

			// load tiles visited
			progress.message(StringUtil.getUiLabel("ls.tiles.visited"));
			playerTilesVisited = loader.loadPlayerTilesVisited(name);
			progress.incProgress(1);

			// clear maze vars
			progress.message(StringUtil.getUiLabel("ls.clear.maze.vars"));
			MazeVariables.clearAll();
			progress.incProgress(1);

			// load NPCs
			progress.message(StringUtil.getUiLabel("ls.npc"));
			NpcManager.getInstance().loadGame(name, loader, playerCharacterCache);
			progress.incProgress(1);

			// load maze vars
			progress.message(StringUtil.getUiLabel("ls.maze.vars"));
			loader.loadMazeVariables(name);
			progress.incProgress(1);

			// load item caches
			progress.message(StringUtil.getUiLabel("ls.item.caches"));
			ItemCacheManager.getInstance().loadGame(name, loader, playerCharacterCache);
			progress.incProgress(1);

			// init state
			progress.message(StringUtil.getUiLabel("ls.init"));
			List<MazeEvent> gameStateEvents = setGameState(gs);
			ui.setParty(party);
			this.setState(State.MOVEMENT);
			progress.incProgress(1);

			// load journals
			progress.message(StringUtil.getUiLabel("ls.journals"));
			JournalManager.getInstance().loadGame(name, loader);
			progress.incProgress(1);

			// load conditions
			// done last, so that conditions on tiles can be loaded after the zone has been loaded
			progress.message(StringUtil.getUiLabel("ls.conditions"));
			ConditionManager.getInstance().loadGame(name, loader, playerCharacterCache);
			progress.incProgress(1);

			// ui cleanup for the transition
			ui.clearDialog();
			ui.clearMessages();
			ui.showBlockingScreen("screen/blank_screen", -1, null);

			// set message
			ui.addMessage(StringUtil.getUiLabel("ls.game.loaded", name), true);

			// at this point we transition control to the event processing thread
			appendEvents(gameStateEvents);
		}
		catch (Exception x)
		{
			throw new MazeException(x);
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
			while (this.ui == null)
			{
				Thread.sleep(500);
			}

			String script = campaign.getIntroScript();
			if (script != null && script.length() > 0)
			{
				final MazeScript intro = Database.getInstance().getMazeScript(script);
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

			while (getState() != State.FINISHED)
			{
				this.ui.draw();
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

		log.dumpBuffer();

		ui.errorDialog(s.toString());
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the current state and destroys all previous state history.
	 */
	public void setState(State state)
	{
		setState(state, null);
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Sets the current state and notifies the given mutex when
	 * the state is changed.
	 */
	public void setState(State state, Object waiter)
	{
		this.state = state;
		this.changeState(state);

		if (this.statePopMutex != null)
		{
			synchronized (statePopMutex)
			{
				this.statePopMutex.notifyAll();
				this.statePopMutex = null;
			}
		}

		this.statePopMutex = waiter;
	}

	/*-------------------------------------------------------------------------*/
	public State getState()
	{
		return this.state;
	}

	/*-------------------------------------------------------------------------*/
	private void changeState(State state)
	{
		log(Log.MEDIUM, "Change State: "+state.name());
		ui.changeState(state);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> incTurn(boolean checkRandomEncounters)
	{
		Maze.getPerfLog().enter("Maze::incTurn");
		List<MazeEvent> result = new ArrayList<>();

		addAll(result, GameTime.incTurn());

		if (!checkPartyStatus())
		{
			return result;
		}

		reorderPartyToCompensateForDeadCharacters();

		if (checkRandomEncounters)
		{
			// do not trigger random encounters if there is an NPC on the tile
			Npc[] npcs = NpcManager.getInstance().getNpcsOnTile(
				this.getCurrentZone().getName(), this.getTile());

			if (npcs == null || npcs.length == 0)
			{
				Tile t = this.getCurrentTile();
				if (t != null)
				{
					boolean disable_random_spawns = Boolean.valueOf(
						Maze.getInstance().getAppConfig().get((
							AppConfig.DISABLE_RANDOM_SPAWNS)));
					boolean no_encounters = Boolean.valueOf(
						Maze.getInstance().getAppConfig().get((
							AppConfig.NO_ENCOUNTERS)));

					if (!(disable_random_spawns || no_encounters))
					{
						if (Dice.d1000.roll("Wandering monster roll") <= GameSys.getInstance().getRandomEncounterChance(t))
						{
							// a random encounter occurs
							FoeEntry foeEntry = t.getRandomEncounters().getEncounterTable().getRandomItem();
							List<FoeGroup> foes = foeEntry.generate();
							addAll(result, this.encounterActors(new ActorEncounter(foes, null, null, null, null, null, null, null)));
						}
					}
				}
			}
		}

		Maze.getPerfLog().exit("Maze::incTurn");
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public long getTurnNr()
	{
		return GameTime.getTurnNr();
	}

	/*-------------------------------------------------------------------------*/
	public void backToMain()
	{
		party = null;
		ui.setParty(null);
		ui.clearDialog();
		ui.stopAllAnimations();
		zone = null;
		if (currentCombat != null)
		{
			ui.setFoes(null, false);
			ui.setAllies(null);
			currentCombat.endCombat();
			currentCombat = null;
		}
		currentChest = null;
		currentPortal = null;
		currentActorEncounter = null;
		MazeVariables.clearAll();
		if (processor != null)
		{
			processor.queue.clear();
		}
		state = null;
		ui.resetMainMenuState();
		ui.showMainMenu();
		setState(Maze.State.MAINMENU);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> refreshCharacterData()
	{
//		Maze.log("refreshing character data...");
		return Collections.singletonList(
			new MazeEvent()
			{
				@Override
				public List<MazeEvent> resolve()
				{
					ui.refreshCharacterData();
					return null;
				}
			});
//		Maze.log("finished refreshing character data");
	}

	/*-------------------------------------------------------------------------*/
	public void startResting()
	{
		ConditionTemplate sleep = Database.getInstance().getConditionTemplate(
			Constants.Conditions.RESTING_SLEEP);

		// reduce action points
		for (PlayerCharacter pc : getParty().getPlayerCharacters())
		{
			// do not process maze events - everyone just goes to sleep
			pc.addCondition(sleep.create(
				pc, pc, 1, MagicSys.SpellEffectType.NONE, MagicSys.SpellEffectSubType.NONE));
		}
	}
	
	/*-------------------------------------------------------------------------*/
	public void stopResting()
	{
		// remove all RESTING_SLEEP conditions from actors
		for (UnifiedActor pc : party.getActors())
		{
			ArrayList<Condition> list = new ArrayList<>(pc.getConditions());
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
	 * Execute any pre-combat special abilities.
	 */
	public void executePreCombatActions(Combat combat)
	{
		//
		// foe intentions
		//
		List<ActorActionIntention[]> foeIntentionList = new ArrayList<ActorActionIntention[]>();
		for (FoeGroup foeGroup : combat.getFoes())
		{
			foeIntentionList.add(getFoePreCombatIntentions(foeGroup, combat));
		}

		//
		// player character intentions
		//
		ActorActionIntention[] partyIntentions = new ActorActionIntention[
			combat.getPlayerParty().size()];
		for (int i=0; i<combat.getPlayerParty().size(); i++)
		{
			partyIntentions[i] = combat.getPlayerParty().getPlayerCharacter(i).
				getPreCombatIntentions(combat);
		}

		//
		// append the results of combat actions
		//
		Iterator<CombatAction> combatActions =
			combat.combatRound(partyIntentions, foeIntentionList);
		while (combatActions.hasNext())
		{
			CombatAction nextAction = combatActions.next();
			appendEvents(new ResolveCombatActionEvent(combat, nextAction));
			appendEvents(new CheckPartyStatusEvent());
			appendEvents(new CheckCombatStatusEvent(this, combat));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void executeCombatRound(Combat combat)
	{
		getUi().addMessage(
			StringUtil.getEventText("msg.combat.round.starts", combat.getRoundNr()),
			true);

		//
		// foe intentions
		//
		List<ActorActionIntention[]> foeIntentionList = new ArrayList<>();
		for (FoeGroup other : combat.getFoes())
		{
			foeIntentionList.add(getFoeCombatIntentions(other));
		}

		//
		// player character intentions
		//
		ActorActionIntention[] partyIntentions = new ActorActionIntention[getParty().size()];
		boolean isSurpriseRoundAgainstParty =
			combat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY ||
			combat.getAmbushStatus() == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY;

		// collect player character actions
		log(Log.DEBUG, "Collecting actor intentions for round "+combat.getRoundNr());
		int max = getParty().size();
		int i = 0;
		while (i < max)
		{
			PlayerCharacter pc = getParty().getPlayerCharacter(i);

			// if the party is surprised, only QUICK WITS enables actions
			// during the surprise round
			if (isSurpriseRoundAgainstParty &&
				pc.getModifier(Stats.Modifier.QUICK_WITS) <= 0)
			{
				partyIntentions[i++] = ActorActionIntention.INTEND_NOTHING;
			}
			else
			{
				try
				{
					ActorActionIntention actorActionOption;

					// dead characters should always be lined up at the end, so
					// a numAlive check works here
					if (i < getParty().numAlive())
					{
						// display character options
						if (GameSys.getInstance().askActorForCombatIntentions(pc))
						{
							actorActionOption = this.ui.getCombatIntention(pc);
							log(Log.DEBUG, pc.getName() + " at index " + i +
								" selects " + actorActionOption);
						}
						else
						{
							actorActionOption = ActorActionIntention.INTEND_NOTHING;
							log(Log.DEBUG, pc.getName() + " at index " + i +
								" cannot do anything and intends nothing");
						}

						partyIntentions[i++] = actorActionOption;
					}
					else
					{
						partyIntentions[i++] = ActorActionIntention.INTEND_NOTHING;
						log(Log.DEBUG, pc.getName() + " at index " + i +
							" is dead and intends nothing");
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

		//
		// validate party intentions
		//
		for (int j = 0; j < partyIntentions.length; j++)
		{
			if (partyIntentions[j] == null)
			{
				log("null actor intention at party index "+j);
				partyIntentions[j] = new DefendIntention();
			}
		}

		//
		// execute an appearance script, picking from the first foe
		//
		Foe f = (Foe)(combat.getFoes().get(0).getActors().get(0));
		if (f.getAppearanceScript() != null)
		{
			appendEvents(f.getAppearanceScript().getEvents());
		}

		//
		// append the results of combat actions
		//
		Iterator<CombatAction> combatActions =
			combat.combatRound(partyIntentions, foeIntentionList);
		while (combatActions.hasNext())
		{
			CombatAction action = combatActions.next();
			Maze.log("combat action: "+action.getActor().getName()+", "+action.getClass().getName());

			appendEvents(new ResolveCombatActionEvent(combat, action));
			appendEvents(new CheckPartyStatusEvent());
			appendEvents(new CheckCombatStatusEvent(this, combat));
		}

		//
		// append end of round events
		//
		appendEvents(new EndCombatRoundEvent(this, combat, combat.getRoundNr()));
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * Checks to determine if the party is still alive, or if it's GAME OVER.
	 * @return
	 * 	True if the game carries on, false if it's game over
	 */
	public boolean checkPartyStatus()
	{
		if (party != null && party.numAlive() == 0)
		{
			// party is all dead

			// clear all other pending events
			processor.queue.clear();

			// clear any animations
			getUi().stopAllAnimations();

			// grab the party death script
			MazeScript script = Database.getInstance().getMazeScript("_PARTY_DEAD_");

			// don't go via the regular resolveEvents methods because they
			// will then recursively call into here.
			for (MazeEvent e: script.getEvents())
			{
				e.resolve();
			}

			// clearing the queue may have disabled input
			new DiyGuiUserInterface.EnableInputEvent().resolve();

			backToMain();

			return false;
		}

		return true;
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
	private ActorActionIntention[] getFoePreCombatIntentions(FoeGroup foeGroup, Combat combat)
	{
		List<UnifiedActor> foes = foeGroup.getActors();
		ActorActionIntention[] result = new ActorActionIntention[foes.size()];

		for (int i=0; i<foes.size(); i++)
		{
			result[i] = ((Foe)(foes.get(i))).getPreCombatIntentions(combat);
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
			this.currentCombat.addFoeAllies(allies);
			this.ui.addFoes(allies, true);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean canSummon(UnifiedActor source, int nrGroups)
	{
		if (currentCombat == null)
		{
			// cannot summon outside of combat
			Maze.logDebug(source.getName()+" can't summon outside of combat");
			return false;
		}

		if (source instanceof Foe && ((Foe)source).isSummoned())
		{
			// summoned foes cannot themselves cast summon spells
			Maze.logDebug(source.getName()+" is a summoned foe, can't summon");
			return false;
		}

		// limit each caster to one summoned group
		if (source.getCombatantData() != null && 
			!source.getCombatantData().canSummon())
		{
			Maze.logDebug(source.getName()+" has a summoned group already, can't summon.");
			return false;
		}

		// check that there is space
		if (source instanceof Foe && 
			(currentCombat.getFoes().size()+nrGroups > Constants.MAX_FOE_GROUPS))
		{
			Maze.logDebug(source.getName()+": max foe groups exceeded");
			return false;
		}
		else if (source instanceof PlayerCharacter && 
			(currentCombat.getFoes().size()+nrGroups > Constants.MAX_PARTY_ALLIES))
		{
			Maze.logDebug(source.getName()+": max party allies exceeded");
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
	public void actorAttacks(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
	{
		this.ui.actorAttacks(attacker, defender, attackWith);
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
	public List<MazeEvent> actorFlees(UnifiedActor coward)
	{
		if (coward instanceof Foe && this.currentCombat != null)
		{
			// a single foe flees from battle
			Foe foe = (Foe)coward;
			currentCombat.removeFoe(foe);
			this.ui.foeLeaves(coward);
			return new ArrayList<>();
		}
		else if (coward instanceof PlayerCharacter)
		{
			return partyFlees();
		}
		else
		{
			throw new MazeException("Invalid actor: "+coward);
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> partyFlees()
	{
		if (currentCombat != null)
		{
			// the party flees from combat
			currentCombat.endCombat();
		}

		// nuke anything else that may happen this combat
		this.processor.queue.clear();

		// remove foes
		this.ui.setFoes(null, false);

		// back to movement
		this.setState(State.MOVEMENT);

		// nuke current combat
		this.ui.enableInput();
		this.currentCombat = null;

		// back up the party along their previous path of travel
		List<MazeEvent> result = new ArrayList<>();

		result.add(new AnimationEvent(new FadeToBlackAnimation(1500)));

		int randomFacing = Dice.d4.roll("random facing");
		result.addAll(this.ui.backPartyUp(3+Dice.d4.roll("Party flees"), randomFacing));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	/**
	 * @param maxTiles
	 * 	The maximum number of player tiles visited to reverse; may turn out to
	 * 	be less if there is less tile history available.
	 */
	public List<MazeEvent> backPartyUp(int maxTiles, int facing)
	{
		return this.ui.backPartyUp(maxTiles, facing);
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

	/**
	 * Should be used to pass events from the UI thread to the game thread
	 * for processing.
	 */
	public void appendEvents(MazeEvent... events)
	{
		if (events != null)
		{
			appendEvents(Arrays.asList(events));
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Should be used to pass events from the UI thread to the game thread
	 * for processing.
	 */
	public void appendEvents(List<MazeEvent> events)
	{
		if (events != null)
		{
			processor.queue.addAll(events);
		}
	}
	
	/*-------------------------------------------------------------------------*/
	private void resolveEvents(List<MazeEvent> events)
	{
		this.resolveEvents(events, true);
	}
	
	/*-------------------------------------------------------------------------*/
	private void resolveEvents(List<MazeEvent> events, boolean displayEventText)
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
	private void resolveEvent(MazeEvent event, boolean displayEventText)
	{
		// todo: remove debugging~~~~
//		System.out.println("<"+Thread.currentThread().getName()+"> event = [" + event + "]");
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~

		String perfTag = "Maze::resolveEvent [" + event.getClass().getName() + "]";
		if (getPerfLog() != null)
		{
			Maze.getPerfLog().enter(perfTag);
		}

		List<MazeEvent> subEvents;
		synchronized (getEventMutex())
		{
			subEvents = event.resolve();

			// event resolution may alter some of it's state - thus only display the event now.
			this.ui.displayMazeEvent(event, displayEventText);

			// only wait on delays and stuff if the event text needs to be shown
			if (displayEventText)
			{
				// (WAIT_ON_READLINE events have wait()'s in their resolve methods)
				if (event.getDelay() == MazeEvent.Delay.WAIT_ON_CLICK)
				{
					log(Log.DEBUG, "waiting on ["+event.getClass().getSimpleName()+ "] ["+ event +"]");

					try
					{
						getEventMutex().wait();
					}
					catch (InterruptedException e)
					{
						throw new MazeException(e);
					}
				}
				else if (event.getDelay() > MazeEvent.Delay.NONE && Maze.getInstance().getCurrentCombat() != null)
				{
					// only apply the combat delay in combat

					try
					{
						// wait instead of sleep so that the user can
						// click past any text
						getEventMutex().wait(event.getDelay());
					}
					catch (InterruptedException e)
					{
						Maze.log.log(e);
					}
				}
			}
		}

		if (checkPartyStatus())
		{
			// only execute these if the party is still going

			if (subEvents != null && !subEvents.isEmpty())
			{
				resolveEvents(subEvents, displayEventText);
			}
		}

		if (getPerfLog() != null)
		{
			Maze.getPerfLog().exit(perfTag);
		}
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
	public boolean transferPlayerCharacterToParty(PlayerCharacter pc, Foe npc)
	{
		if (addPlayerCharacterToParty(pc))
		{
			removePlayerCharacterFromGuild(pc, npc);
			return true;
		}

		return false;
	}
	
	/*-------------------------------------------------------------------------*/
	public void transferPlayerCharacterToGuild(PlayerCharacter pc, Foe npc)
	{
		removePlayerCharacterFromParty(pc);
		addPlayerCharacterToGuild(pc, npc);
	}
	
	/*-------------------------------------------------------------------------*/
	public void removePlayerCharacterFromGuild(PlayerCharacter pc, Foe npc)
	{
		npc.getGuild().remove(pc.getName());
	}
	
	/*-------------------------------------------------------------------------*/
	public void addPlayerCharacterToGuild(PlayerCharacter pc, Foe npc)
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
	public boolean addPlayerCharacterToParty(PlayerCharacter pc)
	{
		playerCharacterCache.put(pc.getName(), pc);
		if (party == null || party.getActors().isEmpty())
		{
			// adding the first character
			ArrayList<UnifiedActor> chars = new ArrayList<>();
			chars.add(pc);
	
			party = new PlayerParty(chars, 0, 0, 1);
			this.ui.setParty(party);
			this.ui.characterSelected(pc);

			return true;
		}
		else if (party.getPlayerCharacters().size() < 6)
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

			return true;
		}

		return false;
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
	public void reorderPartyIfPending()
	{
		if (pendingPartyOrder != null)
		{
			this.reorderParty(pendingPartyOrder, pendingFormation);

			pendingPartyOrder = null;
			pendingFormation = -1;
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
	public void reorderPartyToCompensateForDeadCharacters()
	{
		if (party != null)
		{
			// move dead characters to the back of the party
			party.reorderPartyToCompensateForDeadCharacters();
			this.ui.setParty(party);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setPendingFormationChanges(List<PlayerCharacter> actors, int formation)
	{
		this.pendingPartyOrder = actors;
		this.pendingFormation = formation;
	}

	/*-------------------------------------------------------------------------*/
	public List<PlayerCharacter> getPendingPartyOrder()
	{
		return pendingPartyOrder;
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
		this.speechBubble(speechKey, pc, pc.getPersonality(), getUi().getPlayerCharacterWidgetBounds(pc), null);
	}

	/*-------------------------------------------------------------------------*/
	public void speechBubble(
		String speechKey,
		PlayerCharacter pc,
		Personality p,
		Rectangle origin,
		SpeechBubble.Orientation orientation)
	{
		PersonalitySpeechBubbleEvent e = new PersonalitySpeechBubbleEvent(
			pc,
			p,
			speechKey,
			origin,
			orientation,
			false);

//		getUi().stopAllAnimations();
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
	public List<MazeEvent> encounterTile(Point tile, Point previousTile, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		if (zone == null)
		{
			// something is borked
			return result;
		}
		Tile t = zone.getTile(tile);

		this.playerPos = tile;
		this.ui.setTile(zone, t, tile);

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				playerTilesVisited.visitTile(zone.getName(), tile);
				return null;
			}
		});
		addAll(result, this.zone.encounterTile(instance, tile, previousTile, facing));

		return result;
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
				MazeScript script = Database.getInstance().getMazeScript("_OUCH_");
				resolveEvents(script.getEvents());
				ui.addMessage(StringUtil.getEventText("event.locked"), true);
				Maze.getInstance().processPlayerAction(
					TileScript.PlayerAction.LOCKS,
					Maze.getInstance().getFacing());
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
		MazeScript script = Database.getInstance().getMazeScript("_OUCH_");
		resolveEvents(script.getEvents());
		// todo: should probably make these temp messages maze events
		ui.addMessage("OUCH!", false);
	}

	/*-------------------------------------------------------------------------*/
	private int reverseFacing(int facing)
	{
		return switch (facing)
			{
				case NORTH -> SOUTH;
				case SOUTH -> NORTH;
				case EAST -> WEST;
				case WEST -> EAST;
				case NORTH_EAST -> SOUTH_EAST;
				case NORTH_WEST -> SOUTH_WEST;
				case SOUTH_EAST -> NORTH_WEST;
				case SOUTH_WEST -> NORTH_EAST;
				default -> throw new MazeException("invalid facing " + facing);
			};
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
	/**
	 * @return
	 * 	true if the encounter actually happens, false otherwise
	 */
	public List<MazeEvent> encounterActors(
		ActorEncounter actorEncounter)
	{
		List<MazeEvent> result = new ArrayList<>();

		final List<FoeGroup> actors = actorEncounter.getActors();
		final NpcFaction.Attitude encounterAttitude = actorEncounter.getEncounterAttitude();
		final String mazeVar = actorEncounter.getMazeVar();

		//
		// check if there are actually any foes in here
		//
		boolean isFoes = false;
		for (ActorGroup fg : actors)
		{
			if (fg.numAlive() > 0)
			{
				isFoes = true;
				break;
			}
		}

		if (!isFoes)
		{
			return result;
		}

		//
		// Determine the attitude of the encounter
		//

		NpcFaction.Attitude attitude = null;
		// first, is there an encounter flag on the encounter event?
		if (encounterAttitude != null)
		{
			attitude = encounterAttitude;
		}

		// second, try for an NPC faction
		if (attitude == null)
		{
			for (ActorGroup ag : actors)
			{
				UnifiedActor actor = ag.getActors().get(0);

				String faction = actor.getFaction();
				if (faction != null)
				{
					NpcFaction npcFaction = NpcManager.getInstance().getNpcFaction(faction);

					log(Log.DEBUG, "Found NPC faction for ["+actor.getName()+"] " +
						"["+actor.getFaction()+"] ["+npcFaction.getAttitude()+"]");

					attitude = npcFaction.getAttitude();
				}
			}
		}

		// no faction? look for the worst starting attitude
		if (attitude == null)
		{
			for (ActorGroup ag : actors)
			{
				// by now we can assume Foes
				NpcFaction.Attitude at = ((Foe)ag.getActors().get(0)).getDefaultAttitude();

				if (attitude == null || at.getSortOrder() < attitude.getSortOrder())
				{
					attitude = at;
				}
			}
		}

		// assert that we have an attitude
		if (attitude == null)
		{
			throw new MazeException("can't determine attitude ["+attitude+"] " +
				"["+mazeVar+"] ["+actors+"]");
		}

		// players attacked while resting.  Give them a chance to wake up
		if (this.getState() == State.RESTING)
		{
			for (UnifiedActor pc : party.getActors())
			{
				ArrayList<Condition> list = new ArrayList<>(pc.getConditions());
				for (Condition c : list)
				{
					if (c instanceof RestingSleep)
					{
						if (pc.getModifier(Stats.Modifier.LIGHT_SLEEPER) > 0 ||
							Dice.d100.roll("Resting combat wakeup") <= 30)
						{
							pc.removeCondition(c);
						}
						break;
					}
				}
			}
		}

		// determine the leader
		final Foe leader = (Foe)GameSys.getInstance().getLeader(actors);

		// determine ambush status
		if (actorEncounter.getAmbushStatus() == null)
		{
			actorEncounter.setAmbushStatus(
				GameSys.getInstance().determineAmbushStatus(party, attitude, actors));
		}
		Combat.AmbushStatus ambushStatus = actorEncounter.getAmbushStatus();

		if (ambushStatus ==
			Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY)
		{
			if (leader.shouldEvade(actors, getParty()))
			{
				// cancel the encounter, the party never knows about it
				this.setState(State.MOVEMENT);
				return result;
			}
		}

		Maze.this.currentActorEncounter =
			new ActorEncounter(actors, mazeVar, attitude, ambushStatus,
				actorEncounter.getPreScript(),
				actorEncounter.getPostAppearanceScript(),
				actorEncounter.getPartyLeavesNeutralScript(),
				actorEncounter.getPartyLeavesFriendlyScript());

		// first, any pre-encounter events need to be executed
		addAll(result, currentActorEncounter.getPreScript());

		result.add(new InitEncounterEvent(actors, leader));
		result.add(new CueActorsEvent(ambushStatus, leader, attitude));

		return result;
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
	public List<MazeEvent> processUseItem(Item item, UnifiedActor user)
	{
		return this.zone.processUseItem(instance, this.playerPos, this.getFacing(), item, user);
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> changeZone(String zoneName, Point pos, int facing)
	{
		// todo: persistance of conditions on tiles in the old zone and the new zone

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

		List<MazeEvent> result = new ArrayList<>();

		addAll(result, this.zone.initZoneScript(getTurnNr()));

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				ui.setZone(zone, pos, newFacing);
				zone.initialise(getTurnNr());
				getPlayerTilesVisited().resetRecentTiles();
				ui.refreshPcActionOptions();
				return null;
			}
		});

		if (pos.x >= 0 || pos.y >= 0)
		{
			addAll(result, setPlayerPos(pos, newFacing));
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> setPlayerPos(Point pos, int facing)
	{
		List<MazeEvent> result = new ArrayList<>();

		int newFacing;
		if (facing == ZoneChangeEvent.Facing.UNCHANGED)
		{
			newFacing = this.ui.getFacing();
		}
		else
		{
			newFacing = facing;
		}
		Point oldTile = Maze.getInstance().getPlayerPos();

		result.add(new MazeEvent()
		{
			@Override
			public List<MazeEvent> resolve()
			{
				playerPos = pos;
				ui.setPlayerPos(pos, newFacing);

				setState(Maze.State.MOVEMENT);
				ui.showMovementScreen();

				ui.clearBlockingScreen();
				ui.enableInput();

				return null;
			}
		});

		addAll(result, Maze.getInstance().encounterTile(pos, oldTile, facing));

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private void addAll(List<MazeEvent> result, List<MazeEvent> events)
	{
		if (events != null)
		{
			result.addAll(events);
		}
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
			return currentCombat.getActorGroup(actor);
		}

		// not found
		throw new MazeException("Actor not found ["+actor.getName()+"]");
	}

	/*-------------------------------------------------------------------------*/
	public Zone getCurrentZone()
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

	public boolean isInCombat()
	{
		return getCurrentCombat() != null;
	}

	public boolean isInGame()
	{
		return getParty() != null && zone != null;
	}

	public void setCurrentCombat(Combat currentCombat)
	{
		this.currentCombat = currentCombat;
	}

	public Portal getCurrentPortal()
	{
		return currentPortal;
	}

	public ActorEncounter getCurrentActorEncounter()
	{
		return currentActorEncounter;
	}

	public void setCurrentActorEncounter(ActorEncounter currentActorEncounter)
	{
		this.currentActorEncounter = currentActorEncounter;
	}

	/*-------------------------------------------------------------------------*/
	public Point getPlayerPos()
	{
		return playerPos;
	}

	/*-------------------------------------------------------------------------*/
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
	public boolean alreadyQueued(Class eventClass)
	{
		for (MazeEvent e : processor.queue)
		{
			if (e.getClass() == eventClass)
			{
				return true;
			}
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteQueuedEvents(Class eventClass)
	{
		processor.queue.removeIf(eventClass::isInstance);
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
			null,
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
		public static final String FULL_SCREEN = "mclachlan.maze.full.screen";

		public static final String LOG_IMPL = "mclachlan.maze.log.impl";
		public static final String LOG_LEVEL = "mclachlan.maze.log.level";
		public static final String LOG_BUFFER_SIZE = "mclachlan.maze.log.buffer.size";
		public static final String PERF_LOG_IMPL = "mclachlan.maze.perflog.impl";
		public static final String PERF_LOG_LEVEL = "mclachlan.maze.perflog.level";

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
		public static final String NO_ENCOUNTERS = "mclachlan.maze.game.no_encounters";
		public static final String ROVING_SPRITES_MODE = "mclachlan.maze.game.roving_sprites";
		public static final String DEFAULT_FONT = "mclachlan.maze.screen.default_font";
		public static final String DEFAULT_FONT_SIZE = "mclachlan.maze.screen.default_font_size";
	}

	/*-------------------------------------------------------------------------*/
	private static class EventProcessor extends Thread
	{
		private BlockingQueue<MazeEvent> queue;

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

	private class InitEncounterEvent extends MazeEvent
	{
		private final List<FoeGroup> actors;
		private final Foe leader;

		public InitEncounterEvent(List<FoeGroup> actors, Foe leader)
		{
			this.actors = actors;
			this.leader = leader;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			// Clear any dialogs (e.g. spells, resting, etc)
			getUi().clearDialog();

			// Change game state
			Maze.this.setState(State.ENCOUNTER_ACTORS);

			Maze.this.getUi().disableInput();

			// show the foe sprites on the screen
			getUi().setFoes(actors, true);

			// attempt identification of these actors
			GameSys.getInstance().attemptManualIdentification(actors, getParty(), 0);

			// Appearance scripts of the leader
			if (leader.getAppearanceScript() != null)
			{
				return leader.getAppearanceScript().getEvents();
			}
			else
			{
				return null;
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private class CueActorsEvent extends MazeEvent
	{
		private final Combat.AmbushStatus fAmbushStatus;
		private final Foe leader;
		private final NpcFaction.Attitude fAttitude;

		public CueActorsEvent(Combat.AmbushStatus fAmbushStatus, Foe leader,
			NpcFaction.Attitude fAttitude)
		{
			this.fAmbushStatus = fAmbushStatus;
			this.leader = leader;
			this.fAttitude = fAttitude;
		}

		@Override
		public List<MazeEvent> resolve()
		{
			List<MazeEvent> result = new ArrayList<>();

			//
			// Display and journal any needed messages
			//
			String encounterMsg = StringUtil.getEventText("msg.encounter.actors",
				currentActorEncounter.describe());

			StringBuilder sb = new StringBuilder(encounterMsg);

			switch (fAmbushStatus)
			{
				case NONE:
					break;
				case PARTY_MAY_AMBUSH_FOES:
					sb.append("\n\n").append(StringUtil.getEventText("msg.party.may.ambush"));
					break;
				case FOES_MAY_AMBUSH_PARTY:
				case FOES_MAY_AMBUSH_OR_EVADE_PARTY:
					// by this point the foes have chosen to ambush not evade
					sb.append("\n\n").append(StringUtil.getEventText("msg.foes.surprise.party"));
					break;
				case PARTY_MAY_AMBUSH_OR_EVADE_FOES:
					sb.append("\n\n").append(StringUtil.getEventText("msg.party.may.ambush.or.evade"));
					break;
			}

			if (fAmbushStatus != NONE)
			{
				if (party.hasModifier(Stats.Modifier.QUICK_WITS))
				{
					sb.append("\n\n").append(StringUtil.getEventText("msg.party.quick.wits"));
				}
				result.add((new FlavourTextEvent(sb.toString())));
			}
			else
			{
				getUi().addMessage(sb.toString(), true);
			}

			// any post-appearance events from the encounter
			List<MazeEvent> postAppearanceScript = currentActorEncounter.getPostAppearanceScript();
			if (postAppearanceScript !=null && postAppearanceScript.size() > 0)
			{
				addAll(result, postAppearanceScript);
			}

			if (!leader.isFound())
			{
				result.add(new EnableInputEvent());
				addAll(result, leader.getActionScript().firstGreeting());
				result.add(new DisableInputEvent());
				leader.setFound(true);
			}
			else
			{
				result.add(new EnableInputEvent());
				if (fAttitude == NpcFaction.Attitude.FRIENDLY ||
					fAttitude == NpcFaction.Attitude.ALLIED)
				{
					addAll(result, leader.getActionScript().subsequentGreeting());
				}
				else
				{
					addAll(result, leader.getActionScript().neutralGreeting());
				}
				result.add(new DisableInputEvent());
			}

			//
			// Is this a direct transition into COMBAT?
			//
			if (fAttitude == NpcFaction.Attitude.ATTACKING &&
				fAmbushStatus != Combat.AmbushStatus.PARTY_MAY_AMBUSH_FOES &&
				fAmbushStatus != Combat.AmbushStatus.PARTY_MAY_AMBUSH_OR_EVADE_FOES)
			{
				result.add(new EnableInputEvent());
				addAll(result, leader.getActionScript().attacksParty(fAmbushStatus));
				result.add(new DisableInputEvent());
			}
			else if (fAmbushStatus == Combat.AmbushStatus.FOES_MAY_AMBUSH_OR_EVADE_PARTY ||
				fAmbushStatus == Combat.AmbushStatus.FOES_MAY_AMBUSH_PARTY)
			{
				result.add(
					new ActorsTurnToAct(
						currentActorEncounter,
						Maze.this,
						getUi().getMessageDestination()));
			}

			result.add(new EnableInputEvent());

			return result;
		}

		private class EnableInputEvent extends MazeEvent
		{
			@Override
			public List<MazeEvent> resolve()
			{
				Maze.this.getUi().enableInput();
				Maze.this.getUi().refreshPcActionOptions();
				return null;
			}
		}

		private class DisableInputEvent extends MazeEvent
		{
			@Override
			public List<MazeEvent> resolve()
			{
				Maze.this.getUi().disableInput();
				return null;
			}
		}
	}
}
