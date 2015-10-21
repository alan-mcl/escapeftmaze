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

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import mclachlan.crusader.CrusaderEngine;
import mclachlan.crusader.CrusaderEngine32;
import mclachlan.crusader.EngineObject;
import mclachlan.crusader.Texture;
import mclachlan.crusader.script.TempChangeTexture;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ContainerWidget;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.audio.Music;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class DiyGuiUserInterface extends Frame implements UserInterface
{
	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static int SCREEN_EDGE_INSET;
	public static int MAZE_WIDTH;
	public static int MAZE_HEIGHT;
	public static int PC_LEFT_X;
	public static int PC_WIDTH;
	public static int PC_RIGHT_X;
	public static Rectangle SCREEN_BOUNDS;
	private static Rectangle LOW_BOUNDS;
	private static int ZONE_DISPLAY_HEIGHT;

	private static Font font = null;

	private static Map<String, Font> fonts = new HashMap<String, Font>();

	public static HashMap<Integer, Integer> crusaderKeys = new HashMap<Integer, Integer>();

	public static DIYToolkit gui;
	static DiyGuiUserInterface instance;

	private final List<Animation> animations = new ArrayList<Animation>();

	// draw method variables
	private int frameCount;
	private int frameCountRecord;
	private long counter;
	private long sumRenderTime;
	private long avgRenderTime;
	private long curTime;
	private BufferStrategy strategy;

	// the ray caster
	CrusaderEngine raycaster;

	// music
	Music music;

	// game screens
	private CardLayoutWidget mainLayout;
	SaveLoadScreen saveLoad;

	private ContainerWidget movementScreen;
	private DIYPanel modifiersDisplayScreen;
	private DIYPanel statsDisplayScreen;
	private DIYPanel inventoryScreen;
	private DIYPanel magicScreen;
	private DIYPanel propertiesDisplayScreen;
	private ContainerWidget createCharacterScreen;
	private ContainerWidget levelUpScreen;

	// important widgets
	MainMenu mainMenu;
	TipOfTheDayWidget tipOfTheDayWidget;
	PlayerCharacterWidget charTopLeft;
	PlayerCharacterWidget charMidLeft;
	PlayerCharacterWidget charLowLeft;
	PlayerCharacterWidget charTopRight;
	PlayerCharacterWidget charMidRight;
	PlayerCharacterWidget charLowRight;
	CombatOptionsWidget combatOptions;
	RestingWidget restingWidget;
	MazeWidget mazeWidget;
	CreateCharacterWidget createCharacter;
	LevelUpWidget levelUp;
	PartyDisplayWidget partyDisplay;
	ModifiersDisplayWidget modifiersDisplay;
	StatsDisplayWidget statsDisplay;
	PropertiesDisplayWidget propertiesDisplay;
	InventoryDisplayWidget inventoryDisplay;
	MagicDisplayWidget magicDisplay;
	ButtonToolbar buttonToolbar;
	CardLayoutWidget movementCardLayout;
	CombatDisplayWidget combatDisplay;
	PartyOptionsAndTextWidget partyOptionsAndTextWidget;
	SignBoardWidget signBoardWidget;
	ChestOptionsWidget chestOptionsWidget;
	EncounterActorsWidget encounterActorsWidget;
	PortalOptionsWidget portalOptionsWidget;
	ZoneDisplayWidget zoneDisplay;
	PartyCloudSpellWidget partyCloudSpellWidget;

	final Object combatOptionsMutex = new Object();
	private CombatOption combatOption;

	private MazeActionListener mazeActionListener;
	private List<FoeGroup> foeGroups;

	/*-------------------------------------------------------------------------*/
	static
	{
		// set up the Crusader key mappings

		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_UP, CrusaderEngine.KeyStroke.FORWARD);
		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_DOWN, CrusaderEngine.KeyStroke.BACKWARD);
		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_LEFT, CrusaderEngine.KeyStroke.TURN_LEFT);
		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_RIGHT, CrusaderEngine.KeyStroke.TURN_RIGHT);
		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_COMMA, CrusaderEngine.KeyStroke.STRAFE_LEFT);
		DiyGuiUserInterface.crusaderKeys.put(KeyEvent.VK_PERIOD, CrusaderEngine.KeyStroke.STRAFE_RIGHT);
	}

	/*-------------------------------------------------------------------------*/
	private static void initConfig()
	{
		java.util.Map<String, String> p = Maze.getInstance().getAppConfig();

		SCREEN_WIDTH = Integer.parseInt(p.get(Maze.AppConfig.SCREEN_WIDTH));
		SCREEN_HEIGHT = Integer.parseInt(p.get(Maze.AppConfig.SCREEN_HEIGHT));
		ZONE_DISPLAY_HEIGHT = SCREEN_HEIGHT / 9;
		MAZE_WIDTH = SCREEN_WIDTH / 2;
		MAZE_HEIGHT = SCREEN_HEIGHT * 7 / 12;

		SCREEN_EDGE_INSET = SCREEN_WIDTH / 40;

		PC_LEFT_X = SCREEN_EDGE_INSET;
		PC_WIDTH = (SCREEN_WIDTH - MAZE_WIDTH - SCREEN_EDGE_INSET * 2) / 2;
		PC_RIGHT_X = SCREEN_WIDTH - PC_LEFT_X - PC_WIDTH;

		SCREEN_BOUNDS = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		LOW_BOUNDS = new Rectangle(
			SCREEN_WIDTH / 2 - MAZE_WIDTH / 2,
			SCREEN_EDGE_INSET + ZONE_DISPLAY_HEIGHT + MAZE_HEIGHT +10,
			MAZE_WIDTH,
			SCREEN_HEIGHT - (SCREEN_EDGE_INSET*2 + ZONE_DISPLAY_HEIGHT + MAZE_HEIGHT +10));

		try
		{
			String defaultFont = p.get(Maze.AppConfig.DEFAULT_FONT);
			int fontSize = Integer.parseInt(p.get(Maze.AppConfig.DEFAULT_FONT_SIZE));

			if (defaultFont == null)
			{
				throw new MazeException("Invalid font key [" + defaultFont + "]");
			}

			// init fonts
			Font anon = Database.getInstance().getFont("Anonymous/Anonymous.ttf");
			fonts.put("anonymous", anon);

			Font monaco = Database.getInstance().getFont("Monaco/Monaco.ttf");
			fonts.put("monaco", monaco);

			Font excalibur = Database.getInstance().getFont("excalibur/excalib.ttf");
			fonts.put("excalibur", excalibur);

			Font veramono = Database.getInstance().getFont("VeraMono/VeraMono.ttf");
			fonts.put("veramono", veramono);

			Font quicktypemono = Database.getInstance().getFont("QuickTypeMono/QuickType Mono.ttf");
			fonts.put("quicktypemono", quicktypemono);

			Font f = fonts.get(defaultFont);
			DiyGuiUserInterface.font = f.deriveFont(Font.PLAIN, fontSize);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public DiyGuiUserInterface()
	{
		initConfig();

		instance = this;
		this.setTitle("Maze");

		BlockingQueue<InputEvent> queue = new ArrayBlockingQueue<InputEvent>(1000);

		this.setFont(this.getDefaultFont());

		DiyGuiUserInterface.gui = new DIYToolkit(
			SCREEN_WIDTH,
			SCREEN_HEIGHT,
			this,
			queue,
			Maze.getInstance().getAppConfig().get(Maze.AppConfig.UI_RENDERER));

		gui.getContentPane().addActionListener(new ContentPaneActionListener(this));

		new EventProcessor(queue).start();

		GraphicsDevice device =
			GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		this.enableEvents(KeyEvent.KEY_EVENT_MASK);
		this.setUndecorated(true);

		device.setFullScreenWindow(this);
		this.enableInputMethods(false);
		device.setDisplayMode(getDisplayMode(device));

		this.createBufferStrategy(2);

		// todo: not dynamic?
		int musicVolume = Maze.getInstance().getUserConfig().getMusicVolume();
		final boolean musicEnabled = (musicVolume > 0);
		music = new Music(Database.getInstance(), musicEnabled);
		music.setVolume(musicVolume);

		buildGUI();

		// FPS calculations
		frameCount = frameCountRecord = 0;

		counter = sumRenderTime = avgRenderTime = 0;

		strategy = this.getBufferStrategy();
		curTime = System.currentTimeMillis();
	}

	/*-------------------------------------------------------------------------*/
	private DisplayMode getDisplayMode(GraphicsDevice device)
	{
		DisplayMode[] modes = device.getDisplayModes();

		Maze.log("Available graphics modes:");
		for (DisplayMode dm : modes)
		{
			Maze.log(dm.getWidth() + "x" + dm.getHeight() + ", " + dm.getBitDepth() + "bit, "
				+ dm.getRefreshRate() + "Hz");
		}

		for (DisplayMode dm : modes)
		{
			// look for the match that we want
			if (dm.getHeight() == DiyGuiUserInterface.SCREEN_HEIGHT &&
				dm.getWidth() == DiyGuiUserInterface.SCREEN_WIDTH &&
				(dm.getBitDepth() >= 32 || dm.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI))
			{
				return dm;
			}
		}

		// no supported display mode? die early
		throw new MazeException("No supported graphics display modes. " +
			"Screen size may be an issue. Try going opening maze.cfg and changing " +
			"the screen.width and screen.height.");
	}

	/*-------------------------------------------------------------------------*/
	public void addAnimation(Animation a)
	{
		synchronized (animations)
		{
			this.animations.add(a);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void stopAllAnimations()
	{
		synchronized (animations)
		{
			ListIterator<Animation> li = animations.listIterator();
			while (li.hasNext())
			{
				Animation animation = li.next();
				li.remove();
				if (animation.getMutex() != null)
				{
					synchronized (animation.getMutex())
					{
						// hack so that any WAIT_ON_CLICK events depending on this animation
						// can continue.
						animation.getMutex().notifyAll();
					}
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void mouseEventToAnimations(MouseEvent event)
	{
		synchronized (animations)
		{
			for (Animation a : animations)
			{
				a.processMouseEvent(event);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void keyEventToAnimations(KeyEvent event)
	{
		synchronized (animations)
		{
			for (Animation a : animations)
			{
				a.processKeyEvent(event);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void changeState(Maze.State state)
	{
		this.partyOptionsAndTextWidget.setGameState(state, Maze.getInstance());
		refreshCharacterData();// to refresh action options

		switch (state)
		{
			case MAINMENU:
				showMainMenu();
				break;
			case SAVE_LOAD:
				showSaveLoadScreen();
				break;
			case MOVEMENT:
				showMovementScreen();
				break;
			case MODIFIERSDISPLAY:
				showModifiersScreen();
				break;
			case STATSDISPLAY:
				showStatsScreen();
				break;
			case PROPERTIESDISPLAY:
				showPropertiesScreen();
				break;
			case INVENTORY:
				showInventoryScreen();
				break;
			case MAGIC:
				showMagicScreen();
				break;
			case FINISHED:
				break;
			case COMBAT:
				addMessage(StringUtil.getEventText("msg.combat.starts"));
				showCombatScreen();
				break;
			case SIGNBOARD:
				showMovementScreen();
				break;
			case ENCOUNTER_CHEST:
				showChestScreen(Maze.getInstance().getCurrentChest());
				break;
			case ENCOUNTER_ACTORS:
				showEncounterActorsScreen(Maze.getInstance().getCurrentActorEncounter());
				break;
			case ENCOUNTER_PORTAL:
				showPortalScreen(Maze.getInstance().getCurrentPortal());
				break;
			case ENCOUNTER_TILE:
			case LEVELLING_UP:
				break;
			case RESTING:
//				Maze.getInstance().startResting();
				showRestingScreen();
				break;
			case CREATE_CHARACTER:
				showCreateCharacterScreen();
				break;
			default:
				throw new MazeException("Illegal state: " + state);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void draw()
	{
		Graphics2D g = (Graphics2D)strategy.getDrawGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, DiyGuiUserInterface.SCREEN_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT);
		g.setFont(DiyGuiUserInterface.font);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setFont(this.getDefaultFont());

		try
		{
			long now = System.nanoTime();

			DiyGuiUserInterface.gui.draw(g);

			synchronized (animations)
			{
				int max = animations.size();
				for (int i = 0; i < max; i++)
				{
					animations.get(i).draw(g);
				}

				ListIterator<Animation> li = animations.listIterator();
				while (li.hasNext())
				{
					Animation animation = li.next();
					if (animation.isFinished())
					{
						li.remove();
						if (animation.getMutex() != null)
						{
							synchronized (animation.getMutex())
							{
								// hack so that any WAIT_ON_CLICK events depending on this animation
								// can continue.
								animation.getMutex().notifyAll();
							}
						}
					}
				}
			}

			long renderTime = (System.nanoTime() - now)/1000000;
			counter++;
			sumRenderTime += renderTime;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (DIYToolkit.debug)
		{
			g.setColor(Color.YELLOW);
			g.drawString("fps: " + frameCountRecord, 21, 21);
			g.setColor(Color.YELLOW);
			g.drawString("render ms: " + avgRenderTime, 21, 31);
			g.setColor(Color.YELLOW);
			g.drawString("turn nr: " + Maze.getInstance().getTurnNr(), 21, 41);
			g.drawString("queue: " + DIYToolkit.getInstance().getQueueLength(), 21, 51);
		}

		g.dispose();
		strategy.show();

		frameCount++;

		long now = System.currentTimeMillis();
		if (now - curTime > 1000)
		{
			frameCountRecord = frameCount;
			frameCount = 0;
			curTime = now;
			avgRenderTime = (counter == 0) ? sumRenderTime : sumRenderTime / counter;
			sumRenderTime = 0;
			counter = 0;
		}
	}

	/*-------------------------------------------------------------------------*/
	public Component getComponent()
	{
		return this;
	}

	/*-------------------------------------------------------------------------*/
	public void showCombatDisplay()
	{
		this.movementCardLayout.show(combatDisplay);
	}

	/*-------------------------------------------------------------------------*/
	public ActorActionIntention getCombatIntention(PlayerCharacter pc)
	{
		PlayerCharacterWidget pcw = getPlayerCharacterWidget(pc);
		ActorActionOption selected = pcw.getAction().getSelected();
		return selected.getIntention();
	}

	/*-------------------------------------------------------------------------*/
	public CombatOption getFinalCombatOption()
	{
		try
		{
			// wait on the mutex
			synchronized (combatOptionsMutex)
			{
				combatOptionsMutex.wait();
			}

			return this.combatOption;
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public CombatOption getEvasionOption()
	{
		try
		{
			// wait on the mutex
			synchronized (combatOptionsMutex)
			{
				combatOptionsMutex.wait();
			}

			return this.combatOption;
		}
		catch (Exception x)
		{
			throw new MazeException(x);
		}
	}

	/*-------------------------------------------------------------------------*/
	public int getFacing()
	{
		if (raycaster != null)
		{
			return raycaster.getPlayerFacing();
		}
		else
		{
			return -1;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void chooseACharacter(ChooseCharacterCallback callback)
	{
		ContainerWidget dialog = new WhoDialog(callback);
		showDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void showDialog(ContainerWidget dialog)
	{
		stopAllAnimations();
		DIYToolkit.getInstance().setDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void clearDialog()
	{
		stopAllAnimations();
		DIYToolkit.getInstance().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public void clearAllDialogs()
	{
		stopAllAnimations();
		DIYToolkit.getInstance().clearAllDialogs();
	}

	/*-------------------------------------------------------------------------*/
	public void setDebug(boolean debug)
	{
		DIYToolkit.debug = debug;
	}

	/*-------------------------------------------------------------------------*/
	public Font getDefaultFont()
	{
		return font;
	}

	/*-------------------------------------------------------------------------*/
	public void refreshCharacterData()
	{
		this.statsDisplay.refreshData();
		this.modifiersDisplay.refreshData();
		this.propertiesDisplay.refreshData();
		this.charLowLeft.refresh();
		this.charLowRight.refresh();
		this.charMidLeft.refresh();
		this.charMidRight.refresh();
		this.charTopLeft.refresh();
		this.charTopRight.refresh();
		this.partyOptionsAndTextWidget.refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void addObject(EngineObject obj)
	{
		this.raycaster.addObject(obj);
	}

	/*-------------------------------------------------------------------------*/
	public void removeObject(EngineObject obj)
	{
		this.raycaster.removeObject(obj);
	}

	/*-------------------------------------------------------------------------*/
	public void removeObject(String objectName)
	{
		this.raycaster.removeObject(objectName);
	}

	/*-------------------------------------------------------------------------*/
	public void addObjectInFrontOfPlayer(
		EngineObject obj, double distance, double arcOffset,
		boolean randomStartingFrame)
	{
		this.raycaster.addObjectInFrontOfPlayer(obj, distance, arcOffset, randomStartingFrame);
	}

	/*-------------------------------------------------------------------------*/
	public void signBoard(String message, MazeEvent event)
	{
		Maze.getInstance().setState(Maze.State.SIGNBOARD);
		signBoardWidget.setText(message);
		movementCardLayout.show(signBoardWidget);
	}

	/*-------------------------------------------------------------------------*/
	public void levelUp(PlayerCharacter playerCharacter)
	{
		Maze.getInstance().setState(Maze.State.LEVELLING_UP);
		levelUp.refresh(playerCharacter);
		this.mainLayout.show(levelUpScreen);
	}

	/*-------------------------------------------------------------------------*/
	public void grantItems(List<Item> items)
	{
		GrantItemsWidget giw = new GrantItemsWidget(items, null);
		showDialog(giw);
	}

	/*-------------------------------------------------------------------------*/
	public void showBlockingScreen(String imageResource, int delay, Object mutex)
	{
		DIYPanel dialog = new BlockingScreen(imageResource, delay, mutex);
		showDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void showBlockingScreen(ContainerWidget dialog, int delay, Object mutex)
	{
		DIYPanel bs = new BlockingScreen(dialog, delay, mutex);
		showDialog(bs);
	}

	/*-------------------------------------------------------------------------*/
	public void clearBlockingScreen()
	{
		clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI()
	{
		this.getGraphics().setFont(this.getDefaultFont());

		mazeActionListener = new MazeActionListener();
		DiyGuiUserInterface.gui.addGlobalListener(mazeActionListener);

		ArrayList<ContainerWidget> cards = new ArrayList<ContainerWidget>();

		this.initCommonWidgets();

		// The main menu
		mainMenu = getMainMenu();
		cards.add(mainMenu);

		// The save/load screen
		saveLoad = getSaveLoad();
		cards.add(saveLoad);

		// The exploration screen
		movementScreen = getMovementScreen();
		cards.add(movementScreen);

		// The modifiers display screen
		modifiersDisplayScreen = getModifiersDisplayScreen();
		cards.add(modifiersDisplayScreen);

		// The stats display screen
		statsDisplayScreen = getStatsDisplayScreen();
		cards.add(statsDisplayScreen);

		// The properties display screen
		propertiesDisplayScreen = getPropertiesDisplayScreen();
		cards.add(propertiesDisplayScreen);

		// The inventory screen
		inventoryScreen = getInventoryScreen();
		cards.add(inventoryScreen);

		// The magic screen
		magicScreen = getMagicScreen();
		cards.add(magicScreen);

		// The character creation screen
		createCharacterScreen = getCreateCharacterScreen();
		cards.add(createCharacterScreen);

		// The level up screen
		levelUpScreen = getLevelUpScreen();
		cards.add(levelUpScreen);

		// a layout to display the various screens.
		CardLayoutWidget mainLayout = new CardLayoutWidget(DiyGuiUserInterface.SCREEN_BOUNDS, cards);

		DiyGuiUserInterface.gui.add(mainLayout);

		this.mainLayout = mainLayout;
		this.mainLayout.show(mainMenu);
	}

	public void showCreateCharacterScreen()
	{
		if (!Maze.State.CREATE_CHARACTER.name().equals(getMusic().getState()))
		{
			getMusic().setState(Maze.State.CREATE_CHARACTER.name());
			executeMazeScript("_CREATE_CHARACTER_MUSIC_");
		}
		this.mainLayout.show(this.createCharacterScreen);
		this.createCharacter.refresh();
	}

	public void showCombatScreen()
	{
		this.mainLayout.show(this.movementScreen);
		this.partyOptionsAndTextWidget.setCurrentCombat(Maze.getInstance().getCurrentCombat());
	}

	public void showMagicScreen()
	{
		magicDisplay.setCharacter(partyDisplay.getSelectedCharacter());
		this.mainLayout.show(this.magicScreen);
	}

	public void showInventoryScreen()
	{
		if (!Maze.State.INVENTORY.name().equals(getMusic().getState()))
		{
			getMusic().setState(Maze.State.INVENTORY.name());
			executeMazeScript("_INVENTORY_MUSIC_");
		}
		stopAllAnimations();
		inventoryDisplay.setCharacter(partyDisplay.getSelectedCharacter());
		this.mainLayout.show(this.inventoryScreen);
	}

	public void showStatsScreen()
	{
		statsDisplay.setCharacter(partyDisplay.getSelectedCharacter());
		this.mainLayout.show(this.statsDisplayScreen);
	}

	public void showModifiersScreen()
	{
		modifiersDisplay.setCharacter(partyDisplay.getSelectedCharacter());
		this.mainLayout.show(this.modifiersDisplayScreen);
	}

	public void showPropertiesScreen()
	{
		propertiesDisplay.setCharacter(partyDisplay.getSelectedCharacter());
		this.mainLayout.show(this.propertiesDisplayScreen);
	}

	public void showMovementScreen()
	{
		this.refreshCharacterData();
		this.mainLayout.show(this.movementScreen);
		this.movementCardLayout.show(this.partyOptionsAndTextWidget);
	}

	public void showRestingScreen()
	{
		this.mainLayout.show(this.movementScreen);
//		this.restingWidget.start();
//		this.movementCardLayout.show(this.restingWidget);

		RestingDialog dialog = new RestingDialog(
			StringUtil.getUiLabel("rd.title"));
		showDialog(dialog);
	}

	public void showMainMenu()
	{
		if (!Maze.State.MAINMENU.name().equals(getMusic().getState()))
		{
			getMusic().setState(Maze.State.MAINMENU.name());
			executeMazeScript("_MAIN_MENU_MUSIC_");
		}
		tipOfTheDayWidget.refresh();
		this.mainLayout.show(this.mainMenu);
	}

	private void executeMazeScript(String scriptName)
	{
		MazeScript script = Database.getInstance().getScript(scriptName);
		Maze.getInstance().resolveEvents(script.getEvents());
	}

	public void resetMainMenuState()
	{
		this.mainMenu.updateState();
	}

	public void showSaveLoadScreen()
	{
		this.mainLayout.show(this.saveLoad);
	}

	public void showChestScreen(Chest chest)
	{
		this.mainLayout.show(this.movementScreen);
		chestOptionsWidget.setChest(chest);
		combatDisplay.clear();
		movementCardLayout.show(chestOptionsWidget);
	}

	public void showEncounterActorsScreen(ActorEncounter actorEncounter)
	{
		this.mainLayout.show(this.movementScreen);
		partyOptionsAndTextWidget.setActorEncounter(actorEncounter);
		movementCardLayout.show(partyOptionsAndTextWidget);
	}

	private void showPortalScreen(Portal currentPortal)
	{
		this.mainLayout.show(this.movementScreen);
		portalOptionsWidget.setPortal(currentPortal);
		combatDisplay.clear();
		movementCardLayout.show(portalOptionsWidget);
	}

	@Override
	public void clearCombatEventDisplay()
	{
		combatDisplay.clear();
	}

	@Override
	public void clearMessages()
	{
		partyOptionsAndTextWidget.clearMessages();
	}

	/*-------------------------------------------------------------------------*/
	private void initCommonWidgets()
	{
		// characters
		int y = DiyGuiUserInterface.SCREEN_EDGE_INSET;
		int w = DiyGuiUserInterface.PC_WIDTH;
		charTopLeft = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_LEFT_X, y, w, w), 0);
		charTopRight = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_RIGHT_X, y, w, w), 1);

		y += DiyGuiUserInterface.PC_WIDTH;
		charMidLeft = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_LEFT_X, y, w, w), 2);
		charMidRight = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_RIGHT_X, y, w, w), 3);

		y += DiyGuiUserInterface.PC_WIDTH;
		charLowLeft = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_LEFT_X, y, w, w), 4);
		charLowRight = new PlayerCharacterWidget(new Rectangle(DiyGuiUserInterface.PC_RIGHT_X, y, w, w), 5);

		// char picker
		partyDisplay = new PartyDisplayWidget(
			new Rectangle(0, 0, DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT), null);

		// button toolbar
		buttonToolbar = new ButtonToolbar(
			new Rectangle(DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT - DiyGuiUserInterface.SCREEN_HEIGHT / 12,
				DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT / 12));

		// the maze
		mazeWidget = new MazeWidget(
			new Rectangle(
				DiyGuiUserInterface.SCREEN_WIDTH / 2 - DiyGuiUserInterface.MAZE_WIDTH / 2,
				SCREEN_EDGE_INSET + ZONE_DISPLAY_HEIGHT,
				DiyGuiUserInterface.MAZE_WIDTH,
				DiyGuiUserInterface.MAZE_HEIGHT),
			raycaster);

		zoneDisplay = new ZoneDisplayWidget(
			new Rectangle(
				SCREEN_WIDTH / 2 - MAZE_WIDTH / 2,
				SCREEN_EDGE_INSET,
				MAZE_WIDTH,
				ZONE_DISPLAY_HEIGHT));
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getStatsDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		statsDisplay = new StatsDisplayWidget(
			new Rectangle(DiyGuiUserInterface.PC_WIDTH, 0, DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT));

		BufferedImage back = Database.getInstance().getImage("screen/stats_back");
		screen.setBackgroundImage(back);

		screen.add(buttonToolbar);
		screen.add(partyDisplay);
		screen.add(statsDisplay);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getMagicScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		DIYLabel topLabel = new DIYLabel("Magic", DIYToolkit.Align.CENTER);
		topLabel.setBounds(162, 0, DiyGuiUserInterface.SCREEN_WIDTH - 162, 30);
		topLabel.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize() + 5);
		topLabel.setFont(f);

		magicDisplay = new MagicDisplayWidget(
			new Rectangle(
				DiyGuiUserInterface.PC_WIDTH,
				35,
				DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH,
				DiyGuiUserInterface.SCREEN_HEIGHT));

		screen.add(topLabel);
		screen.add(buttonToolbar);
		screen.add(partyDisplay);
		screen.add(magicDisplay);

		BufferedImage back = Database.getInstance().getImage("screen/magic_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getLevelUpScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);
		levelUp = new LevelUpWidget(DiyGuiUserInterface.SCREEN_BOUNDS);
		screen.add(levelUp);
		BufferedImage back = Database.getInstance().getImage("screen/create_char_back");
		screen.setBackgroundImage(back);
		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getCreateCharacterScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);
		createCharacter = new CreateCharacterWidget(DiyGuiUserInterface.SCREEN_BOUNDS);
		screen.add(createCharacter);
		BufferedImage back = Database.getInstance().getImage("screen/create_char_back");
		screen.setBackgroundImage(back);
		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getModifiersDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		modifiersDisplay = new ModifiersDisplayWidget(
			new Rectangle(DiyGuiUserInterface.PC_WIDTH, 0, DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT));

		screen.add(buttonToolbar);
		screen.add(partyDisplay);
		screen.add(modifiersDisplay);

		BufferedImage back = Database.getInstance().getImage("screen/modifiers_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getPropertiesDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		propertiesDisplay = new PropertiesDisplayWidget(
			new Rectangle(DiyGuiUserInterface.PC_WIDTH, 0, DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH, DiyGuiUserInterface.SCREEN_HEIGHT));

		screen.add(buttonToolbar);
		screen.add(partyDisplay);
		screen.add(propertiesDisplay);

		BufferedImage back = Database.getInstance().getImage("screen/properties_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getInventoryScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		inventoryDisplay = new InventoryDisplayWidget(
			new Rectangle(
				DiyGuiUserInterface.PC_WIDTH,
				0,
				DiyGuiUserInterface.SCREEN_WIDTH - DiyGuiUserInterface.PC_WIDTH,
				DiyGuiUserInterface.SCREEN_HEIGHT));

		screen.add(buttonToolbar);
		screen.add(partyDisplay);
		screen.add(inventoryDisplay);

		BufferedImage invBack = Database.getInstance().getImage("screen/inventory_back");
		screen.setBackgroundImage(invBack);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private MainMenu getMainMenu()
	{
		MainMenu screen = new MainMenu(DiyGuiUserInterface.SCREEN_BOUNDS);

		screen.add(charTopLeft);
		screen.add(charMidLeft);
		screen.add(charLowLeft);
		screen.add(charTopRight);
		screen.add(charMidRight);
		screen.add(charLowRight);

		int tipWidth = SCREEN_WIDTH / 2;
		Rectangle tipBounds = new Rectangle(
			SCREEN_WIDTH / 2 - tipWidth / 2,
			SCREEN_HEIGHT / 4 * 3,
			tipWidth,
			SCREEN_HEIGHT / 5);
		tipOfTheDayWidget = new TipOfTheDayWidget(tipBounds);
		screen.add(tipOfTheDayWidget);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private SaveLoadScreen getSaveLoad()
	{
		return new SaveLoadScreen(DiyGuiUserInterface.SCREEN_BOUNDS);
	}

	/*-------------------------------------------------------------------------*/
	private ContainerWidget getMovementScreen()
	{
		DIYPanel screen = new DIYPanel(DiyGuiUserInterface.SCREEN_BOUNDS);

		screen.add(charTopLeft);
		screen.add(charMidLeft);
		screen.add(charLowLeft);
		screen.add(charTopRight);
		screen.add(charMidRight);
		screen.add(charLowRight);

		screen.add(zoneDisplay);
		screen.add(mazeWidget);

		Rectangle rect = (Rectangle)DiyGuiUserInterface.LOW_BOUNDS.clone();

		partyOptionsAndTextWidget = new PartyOptionsAndTextWidget(rect);
		signBoardWidget = new SignBoardWidget(DiyGuiUserInterface.LOW_BOUNDS,
			Database.getInstance().getImage("screen/signBoard"));
		chestOptionsWidget = new ChestOptionsWidget(rect);
		encounterActorsWidget = new EncounterActorsWidget(rect);
		portalOptionsWidget = new PortalOptionsWidget(rect);
		combatDisplay = new CombatDisplayWidget(rect);
		combatOptions = new CombatOptionsWidget(rect, null);
		restingWidget = new RestingWidget(rect);

		partyCloudSpellWidget = new PartyCloudSpellWidget(
			null,
			new Rectangle(
				SCREEN_WIDTH / 2 - MAZE_WIDTH / 2,
				SCREEN_HEIGHT / 12 - 23,
				MAZE_WIDTH,
				22));

		screen.add(partyCloudSpellWidget);

		ArrayList<ContainerWidget> list = new ArrayList<ContainerWidget>();
		list.add(partyOptionsAndTextWidget);
		list.add(signBoardWidget);
		list.add(chestOptionsWidget);
		list.add(portalOptionsWidget);
		list.add(encounterActorsWidget);
		list.add(combatDisplay);
		list.add(combatOptions);
		list.add(restingWidget);

		movementCardLayout = new CardLayoutWidget(DiyGuiUserInterface.LOW_BOUNDS, list);
		movementCardLayout.show(partyOptionsAndTextWidget);

		screen.add(movementCardLayout);

		Image movementBack = Database.getInstance().getImage("screen/slate_back");

		screen.setBackgroundImage(movementBack);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	public void setParty(PlayerParty party)
	{
		this.partyDisplay.setParty(party);

		if (party == null)
		{
			this.statsDisplay.setCharacter(null);
			this.modifiersDisplay.setCharacter(null);
			this.propertiesDisplay.setCharacter(null);
			this.inventoryDisplay.setCharacter(null);
			this.propertiesDisplay.setCharacter(null);
			for (int i = 0; i < 6; i++)
			{
				setPlayerCharacter(i + 1, null);
			}
			this.charLowLeft.refresh();
			this.charLowRight.refresh();
			this.charMidLeft.refresh();
			this.charMidRight.refresh();
			this.charTopLeft.refresh();
			this.charTopRight.refresh();

			partyCloudSpellWidget.setParty(null);

			return;
		}

		for (int i = 0; i < 6; i++)
		{
			if (i < party.getActors().size())
			{
				setPlayerCharacter(i + 1, (PlayerCharacter)party.getActors().get(i));
			}
			else
			{
				setPlayerCharacter(i + 1, null);
			}
		}

		this.charLowLeft.refresh();
		this.charLowRight.refresh();
		this.charMidLeft.refresh();
		this.charMidRight.refresh();
		this.charTopLeft.refresh();
		this.charTopRight.refresh();

		partyCloudSpellWidget.setParty(party);
	}

	/*-------------------------------------------------------------------------*/
	public void setZone(Zone zone, Point pos, int facing)
	{
		this.raycaster = getCrusaderEngine(zone);
		raycaster.setPlayerPos(pos.x, pos.y, facing);
		this.mazeWidget.setEngine(raycaster);
		this.zoneDisplay.setZone(zone);
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerPos(Point pos, int facing)
	{
		Point oldTile = Maze.getInstance().getPlayerPos();
		CrusaderEngine rc = DiyGuiUserInterface.instance.raycaster;
		rc.setPlayerPos(pos.x, pos.y, facing);
		Maze.getInstance().encounterTile(pos, oldTile, facing);
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Zone zone, Tile t, Point tile)
	{
		this.zoneDisplay.setTile(zone, t, tile);
	}

	/*-------------------------------------------------------------------------*/
	public void actorAttacks(UnifiedActor attacker)
	{
		if (attacker instanceof Foe)
		{
			Foe foe = ((Foe)attacker);
			EngineObject obj = foe.getSprite();

			if (obj == null)
			{
				// probably the case that this is a party ally
				return;
			}

			this.raycaster.addScript(
				new TempChangeTexture(obj, Foe.Animation.MELEE_ATTACK, raycaster));
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actorDies(UnifiedActor victim)
	{
		if (victim instanceof Foe)
		{
			this.raycaster.removeObject(((Foe)victim).getSprite());
		}
		else
		{
			// todo: player character dies
		}
	}

	/*-------------------------------------------------------------------------*/
	public void foeFlees(UnifiedActor coward)
	{
		if (coward instanceof Foe)
		{
			this.raycaster.removeObject(((Foe)coward).getSprite());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void backPartyUp(int maxKeys)
	{
		// clone the last few keypresses
		List<Integer> keys = new ArrayList<Integer>(mazeActionListener.keyCodeHistory);

		if (keys.size() == 0)
		{
			// simulate one step
			keys.add(CrusaderEngine.KeyStroke.FORWARD);
		}

		// simulate a 180
		keys.add(0, CrusaderEngine.KeyStroke.TURN_LEFT);
		keys.add(0, CrusaderEngine.KeyStroke.TURN_LEFT);

		int count = 0;

		// reverse the players recent movements
		for (Integer key1 : keys)
		{
			// todo: 
			// this will simply prevent random encounters while the player
			// is running away.  It would be more amusing to allow them and
			// stop the flight when one happens.
			mazeActionListener.handleKeyCode(getFleeKey(key1), false);
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				throw new MazeException(e);
			}

			if (maxKeys > 0 && count++ >= maxKeys)
			{
				break;
			}
		}

		showMovementScreen();
	}

	/*-------------------------------------------------------------------------*/
	private int getFleeKey(int key)
	{
		switch (key)
		{
			case CrusaderEngine.KeyStroke.FORWARD:
				return CrusaderEngine.KeyStroke.FORWARD;
			case CrusaderEngine.KeyStroke.BACKWARD:
				return CrusaderEngine.KeyStroke.BACKWARD;
			case CrusaderEngine.KeyStroke.STRAFE_LEFT:
				return CrusaderEngine.KeyStroke.STRAFE_LEFT;
			case CrusaderEngine.KeyStroke.STRAFE_RIGHT:
				return CrusaderEngine.KeyStroke.STRAFE_RIGHT;
			case CrusaderEngine.KeyStroke.TURN_LEFT:
				return CrusaderEngine.KeyStroke.TURN_RIGHT;
			case CrusaderEngine.KeyStroke.TURN_RIGHT:
				return CrusaderEngine.KeyStroke.TURN_LEFT;
			default:
				return key;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setFoes(List<FoeGroup> others)
	{
		if (others == null)
		{
			// remove any existing sprites
			if (this.foeGroups != null)
			{
				for (FoeGroup foeGroup1 : this.foeGroups)
				{
					List<UnifiedActor> group = foeGroup1.getActors();
					int maxFoeIndex = group.size();
					for (int foeIndex = 0; foeIndex < maxFoeIndex; foeIndex++)
					{
						Foe foe = (Foe)group.get(foeIndex);
						this.raycaster.removeObject(foe.getSprite());
					}
				}
			}
			this.mazeWidget.setFoes(null);
			return;
		}

		int maxFoeGroups = others.size();

		//
		// Add the foe group widgets
		//
		this.mazeWidget.setFoes(others);

		this.foeGroups = others;

		//
		// Add the foe sprites to the raycasting engine
		//
		for (int foeGroup = 0; foeGroup < maxFoeGroups; foeGroup++)
		{
			java.util.List<UnifiedActor> group = others.get(foeGroup).getActors();
			int maxFoeIndex = group.size();
			for (int foeIndex = 0; foeIndex < maxFoeIndex; foeIndex++)
			{
				Foe foe = (Foe)group.get(foeIndex);
				Texture[] textures = new Texture[]
					{
						foe.getBaseTexture().getTexture(),
						foe.getMeleeAttackTexture().getTexture(),
						foe.getRangedAttackTexture().getTexture(),
						foe.getCastSpellTexture().getTexture(),
						foe.getSpecialAbilityTexture().getTexture(),
					};

				EngineObject obj = new EngineObject(textures, 0, false);

				if (foe.getSprite() != null)
				{
					this.raycaster.removeObject(foe.getSprite());
				}

				foe.setSprite(obj);

				if (foe.getHitPoints().getCurrent() > 0)
				{
					double increment = 1.0 / (maxFoeIndex + 1);
					double arc = increment * (foeIndex + 1) + (-0.1 * (foeGroup % 2)); // stagger the groups
					double distance = 0.5 + (0.5 * foeGroup);
					this.addObjectInFrontOfPlayer(obj, distance, arc, true);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setAllies(List<FoeGroup> others)
	{
		this.mazeWidget.setAllies(others);
	}

	/*-------------------------------------------------------------------------*/
	public void showCombatOptions()
	{
		this.refreshCharacterData();
//		movementCardLayout.show(combatOptions);
	}

	/*-------------------------------------------------------------------------*/
	public void showEvasionOptions()
	{
		combatOptions.showEvasionOptions();
//		movementCardLayout.show(combatOptions);
	}

	/*-------------------------------------------------------------------------*/
	public void showFinalCombatOptions()
	{
		combatOptions.showFinalCombatOptions();
//		movementCardLayout.show(combatOptions);
	}

	/*-------------------------------------------------------------------------*/
	public void displayMazeEvent(MazeEvent event, boolean displayEventText)
	{
/*
		if (event.getText() != null && displayEventText)
		{
			this.showCombatDisplay();
		}
		combatDisplay.setCurrentEvent(event, displayEventText);
*/
		if (event.getText() != null && displayEventText)
		{
			String eventDesc = event.getText();
			Maze.log(eventDesc);

			partyOptionsAndTextWidget.addMessage(eventDesc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(int index, PlayerCharacter pc)
	{
		switch (index)
		{
			case 1:
				this.charTopLeft.setPlayerCharacter(pc);
				break;
			case 2:
				this.charTopRight.setPlayerCharacter(pc);
				break;
			case 3:
				this.charMidLeft.setPlayerCharacter(pc);
				break;
			case 4:
				this.charMidRight.setPlayerCharacter(pc);
				break;
			case 5:
				this.charLowLeft.setPlayerCharacter(pc);
				break;
			case 6:
				this.charLowRight.setPlayerCharacter(pc);
				break;
			default:
				throw new MazeException("Invalid index " + index);
		}
	}

	/*-------------------------------------------------------------------------*/
	private CrusaderEngine getCrusaderEngine(Zone zone)
	{
		return new CrusaderEngine32(
			zone.getMap(),
			DiyGuiUserInterface.MAZE_WIDTH,
			DiyGuiUserInterface.MAZE_HEIGHT,
			CrusaderEngine.MovementMode.DISCRETE,
			zone.getShadeTargetColor(),
			zone.getTransparentColor(),
			zone.doShading(),
			zone.doLighting(),
			zone.getShadingDistance(),
			zone.getShadingMultiplier(),
			zone.getProjectionPlaneOffset(),
			zone.getPlayerFieldOfView(),
			zone.getScaleDistFromProjPlane(),
			this);
	}

	/*-------------------------------------------------------------------------*/
	public void characterSelected(int index)
	{
		PlayerCharacter pc = Maze.getInstance().getPlayerCharacter(index);

		if (pc != null)
		{
			characterSelected(pc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void characterSelected(PlayerCharacter pc)
	{
		if (pc == null)
		{
			return;
		}

		this.partyDisplay.setSelectedCharacter(pc);

		switch (Maze.getInstance().getState())
		{
			case MAINMENU:
			case MODIFIERSDISPLAY:
			case STATSDISPLAY:
			case PROPERTIESDISPLAY:
			case INVENTORY:
			case MAGIC:
				this.modifiersDisplay.setCharacter(pc);
				this.statsDisplay.setCharacter(pc);
				this.inventoryDisplay.setCharacter(pc);
				this.magicDisplay.setCharacter(pc);
				this.propertiesDisplay.setCharacter(pc);
				break;
			default:
				// ignore
		}
	}

	/*-------------------------------------------------------------------------*/
	public String getPlayerSpeech()
	{
		return this.combatDisplay.getPlayerSpeech();
	}

	/*-------------------------------------------------------------------------*/
	public boolean combatDisplayIsVisible()
	{
		return this.movementCardLayout.getCurrentWidget() == this.combatDisplay;
	}

	/*-------------------------------------------------------------------------*/
	public void addMessage(String msg)
	{
		this.partyOptionsAndTextWidget.addMessage(msg);
		Maze.getInstance().journalInContext(msg);
		Maze.log(msg);
	}

	@Override
	public MessageDestination getMessageDestination()
	{
		return partyOptionsAndTextWidget;
	}

	/*-------------------------------------------------------------------------*/
	public void refreshResting()
	{
		this.restingWidget.refresh();
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPlayerCharacterWidgetBounds(
		PlayerCharacter playerCharacter)
	{
		int index = Maze.getInstance().getParty().getPlayerCharacterIndex(playerCharacter);

		switch (index)
		{
			case 0:
				return charTopLeft.getBounds();
			case 1:
				return charTopRight.getBounds();
			case 2:
				return charMidLeft.getBounds();
			case 3:
				return charMidRight.getBounds();
			case 4:
				return charLowLeft.getBounds();
			case 5:
				return charLowRight.getBounds();
			default:
				throw new MazeException("Invalid index " + index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget getPlayerCharacterWidget(
		PlayerCharacter playerCharacter)
	{
		int index = Maze.getInstance().getParty().getPlayerCharacterIndex(playerCharacter);

		switch (index)
		{
			case 0:
				return charTopLeft;
			case 1:
				return charTopRight;
			case 2:
				return charMidLeft;
			case 3:
				return charMidRight;
			case 4:
				return charLowLeft;
			case 5:
				return charLowRight;
			default:
				throw new MazeException("Invalid index " + index);
		}
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPortraitWidgetBounds(PlayerCharacter pc)
	{
		return partyDisplay.getCharacterBounds(pc);
	}

	/*-------------------------------------------------------------------------*/
	public void errorDialog(String s)
	{
		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 4;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 2, DiyGuiUserInterface.SCREEN_HEIGHT / 2);

		OkDialogWidget d = new OkDialogWidget(rectangle, "Oops, something went badly wrong!", s)
		{
			protected void exitDialog()
			{
				clearDialog();
				System.exit(1);
			}
		};

		showDialog(d);
	}

	/*-------------------------------------------------------------------------*/
	public void waitingDialog(String s)
	{
		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 4;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 2, DiyGuiUserInterface.SCREEN_HEIGHT / 2);

		TextDialogWidget d = new TextDialogWidget(
			rectangle, null, s, true);

		showDialog(d);
	}

	/*-------------------------------------------------------------------------*/
	void popupItemDetailsWidget(Item item)
	{
		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 6;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 6;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 3 * 2, DiyGuiUserInterface.SCREEN_HEIGHT / 3 * 2);

		showDialog(new ItemDetailsWidget(rectangle, item));
	}

	/*-------------------------------------------------------------------------*/
	void popupSpellDetailsDialog(Spell spell, PlayerCharacter pc)
	{
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 6;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 6;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 3 * 2, DiyGuiUserInterface.SCREEN_HEIGHT / 3 * 2);

		showDialog(new SpellDetailsDialog(rectangle, spell, pc));
	}

	/*-------------------------------------------------------------------------*/
	void popupConditionDetailsDialog(Condition condition)
	{
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 6;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 6;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 3 * 2, DiyGuiUserInterface.SCREEN_HEIGHT / 3 * 2);

		showDialog(new ConditionDetailsWidget(rectangle, condition));
	}

	/*-------------------------------------------------------------------------*/
	public Music getMusic()
	{
		return music;
	}

	/*-------------------------------------------------------------------------*/
	public void setSelectedFoeGroup(int i)
	{
		mazeWidget.setSelectedFoeGroup(i);
	}

	/*-------------------------------------------------------------------------*/
	public boolean supportsAnimation()
	{
		return true;
	}

	/*-------------------------------------------------------------------------*/
	public ActorGroup getSelectedFoeGroup()
	{
		return mazeWidget.getSelectedFoeGroup();
	}

	/*-------------------------------------------------------------------------*/
	public void setCombatOption(CombatOption combatOption)
	{
		this.combatOption = combatOption;
	}

	/*-------------------------------------------------------------------------*/
	class EventProcessor extends Thread
	{
		BlockingQueue queue;

		public EventProcessor(BlockingQueue queue)
		{
			super("MAZE UI THREAD");
			this.queue = queue;
		}

		public void run()
		{
			long time, counter=0, sumProcessingTime=0;
			while (1 == 1)
			{
				try
				{
					time = System.nanoTime();
					DIYToolkit.getInstance().processEvent(queue.take());
					long diff = System.nanoTime() - time;
					counter++;
					sumProcessingTime += diff;

					if (counter==10)
					{
						double ave = 1D*sumProcessingTime/counter;//100000D;
//						System.out.println("ave = [" + ave + "]");
						counter=0;
						sumProcessingTime=0;
					}

				}
				catch (Exception e)
				{
					Maze.getInstance().errorDialog(e);
				}
			}
		}
	}

}