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
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.concurrent.*;
import mclachlan.crusader.*;
import mclachlan.crusader.script.*;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPane;
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
import mclachlan.maze.game.event.StopMusicEvent;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Tile;
import mclachlan.maze.map.Zone;
import mclachlan.maze.map.script.Chest;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.condition.Condition;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.ui.UserInterface;
import mclachlan.maze.util.MazeException;

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
	public static Rectangle LOW_BOUNDS, MAZE_WINDOW_BOUNDS;

	private static int PC_WIDTH;

	private static Font font = null;

	private static final Map<String, Font> fonts = new HashMap<>();

	public static HashMap<Integer, Integer> crusaderKeys = new HashMap<>();

	public static DIYToolkit gui;
	static DiyGuiUserInterface instance;

	private final List<Animation> animations = new ArrayList<>();
	private final ContentPaneActionListener contentPaneActionListener;

	// config
	private boolean fullScreen;
	private final Rectangle screenBounds;

	// draw method variables
	private int frameCount;
	private int frameCountRecord;
	private long counter;
	private long sumRenderTime;
	private long avgRenderTime;
	private long curTime;
	private final BufferStrategy strategy;

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
	PartyOptionsAndTextWidget partyOptionsAndTextWidget;
	ZoneDisplayWidget zoneDisplay;

	private List<FoeGroup> foeGroups;

	public static boolean acceptInput = true;

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
	public DiyGuiUserInterface()
	{
		initConfig();
		screenBounds = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

		instance = this;
		this.setTitle("Maze");

		BlockingQueue<InputEvent> queue = new ArrayBlockingQueue<>(1000);

		this.setFont(this.getDefaultFont());

		DiyGuiUserInterface.gui = new DIYToolkit(
			SCREEN_WIDTH,
			SCREEN_HEIGHT,
			this,
			queue,
			Maze.getInstance().getAppConfig().get(Maze.AppConfig.UI_RENDERER));

		contentPaneActionListener = new ContentPaneActionListener(this);
		gui.getContentPane().addActionListener(contentPaneActionListener);

		new EventProcessor(queue).start();

		if (fullScreen)
		{
			GraphicsDevice device =
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

			this.enableEvents(KeyEvent.KEY_EVENT_MASK);
			this.setUndecorated(true);

			device.setFullScreenWindow(this);
			this.enableInputMethods(false);
			device.setDisplayMode(getDisplayMode(device));
		}
		else
		{
			this.setUndecorated(true); // should we support the native menu bar?
			this.setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
			this.setVisible(true);
		}

		this.createBufferStrategy(2);

		// todo: not dynamic?
		int musicVolume = Maze.getInstance().getUserConfig().getMusicVolume();
		final boolean musicEnabled = (musicVolume > 0);
		music = new Music(Database.getInstance(), musicEnabled);
		music.setVolume(musicVolume);

		// FPS calculations
		frameCount = frameCountRecord = 0;

		counter = sumRenderTime = avgRenderTime = 0;

		strategy = this.getBufferStrategy();
		curTime = System.currentTimeMillis();
	}

	/*-------------------------------------------------------------------------*/
	public void initConfig()
	{
		java.util.Map<String, String> p = Maze.getInstance().getAppConfig();

		SCREEN_WIDTH = Integer.parseInt(p.get(Maze.AppConfig.SCREEN_WIDTH));
		SCREEN_HEIGHT = Integer.parseInt(p.get(Maze.AppConfig.SCREEN_HEIGHT));
		fullScreen = Boolean.valueOf(p.get(Maze.AppConfig.FULL_SCREEN));

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

			Font amaranth = Database.getInstance().getFont("amaranth/Amaranth-Regular.otf");
			fonts.put("amaranth", amaranth);

			Font asul = Database.getInstance().getFont("asul/Asul-Regular.ttf");
			fonts.put("asul", asul);

			Font eczar = Database.getInstance().getFont("eczar/eczar.regular.ttf");
			fonts.put("eczar", eczar);

			Font exo2 = Database.getInstance().getFont("exo2/Exo2-Regular.ttf");
			fonts.put("exo2", exo2);

			Font josefin = Database.getInstance().getFont("josefin/JosefinSans-Regular.ttf");
			fonts.put("josefin", josefin);

			Font nunito = Database.getInstance().getFont("nunito/Nunito-Regular.ttf");
			fonts.put("nunito", nunito);

			Font caudex = Database.getInstance().getFont("caudex/Caudex-Regular.ttf");
			fonts.put("caudex", caudex);

			Font im_fell = Database.getInstance().getFont("IM_Fell_English_SC/IMFellEnglishSC-Regular.ttf");
			fonts.put("im_fell", im_fell);

			Font goudy = Database.getInstance().getFont("goudy_medieval/Goudy Mediaeval Regular.ttf");
			fonts.put("goudy", goudy);

			Font f = fonts.get(defaultFont);
			DiyGuiUserInterface.font = f.deriveFont(Font.PLAIN, fontSize);
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void buildGui()
	{
		this.getGraphics().setFont(this.getDefaultFont());

//		mazeActionListener = new MazeActionListener();
//		DiyGuiUserInterface.gui.addGlobalListener(mazeActionListener);

		ArrayList<ContainerWidget> cards = new ArrayList<>();

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
		CardLayoutWidget mainLayout = new CardLayoutWidget(screenBounds, cards);

		DiyGuiUserInterface.gui.add(mainLayout);

		this.mainLayout = mainLayout;
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
				animation.destroy();
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
	public boolean mouseEventToAnimations(MouseEvent event)
	{
		boolean consumed = false;
		synchronized (animations)
		{
			for (Animation a : animations)
			{
				a.processMouseEvent(event);
			}
		}

		return consumed;
	}

	/*-------------------------------------------------------------------------*/
	public boolean keyEventToAnimations(KeyEvent event)
	{
		boolean consumed = false;
		synchronized (animations)
		{
			for (Animation a : animations)
			{
				consumed = consumed || a.processKeyEvent(event);
			}
		}

		return consumed;
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
				addMessage(StringUtil.getEventText("msg.combat.starts"), true);
				showCombatScreen();
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
			case LEVELLING_UP:
				break;
			case RESTING:
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
					animations.get(i).update(g);
				}

				ListIterator<Animation> li = animations.listIterator();
				while (li.hasNext())
				{
					Animation animation = li.next();
					if (animation.isFinished())
					{
						li.remove();
						animation.destroy();
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

			long renderTime = (System.nanoTime() - now) / 1000000;
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
	public ActorActionIntention getCombatIntention(PlayerCharacter pc)
	{
		PlayerCharacterWidget pcw = getPlayerCharacterWidget(pc);
		ActorActionOption selected = pcw.getAction().getSelected();
		return selected.getIntention();
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
	public Rectangle getObjectBounds(EngineObject obj)
	{
		if (raycaster != null)
		{
			// bounds in raycaster coords
			Rectangle objBounds = raycaster.getObjectBounds(obj);
			Rectangle thisBounds = mazeWidget.getBounds();

			return new Rectangle(
				thisBounds.x + objBounds.x,
				thisBounds.y + objBounds.y,
				objBounds.width,
				objBounds.height);
		}
		else
		{
			return null;
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
		DIYToolkit.getInstance().setDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void clearDialog()
	{
		DIYToolkit.getInstance().clearDialog();
	}

	/*-------------------------------------------------------------------------*/
	public void clearAllDialogs()
	{
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

	@Override
	public Font getSignboardFont()
	{
		return fonts.get("im_fell");
	}

	/*-------------------------------------------------------------------------*/
	public void disableInput()
	{
		// transparent modal dialog to block all user input while combat runs
		showDialog(getInputDisablingPane());

		this.charLowLeft.setEnabled(false);
		this.charLowRight.setEnabled(false);
		this.charMidLeft.setEnabled(false);
		this.charMidRight.setEnabled(false);
		this.charTopLeft.setEnabled(false);
		this.charTopRight.setEnabled(false);

		partyOptionsAndTextWidget.disableInput();
	}

	/*-------------------------------------------------------------------------*/
	public void enableInput()
	{
		clearDialog();

		this.charLowLeft.setEnabled(true);
		this.charLowRight.setEnabled(true);
		this.charMidLeft.setEnabled(true);
		this.charMidRight.setEnabled(true);
		this.charTopLeft.setEnabled(true);
		this.charTopRight.setEnabled(true);

		refreshCharacterData();

		partyOptionsAndTextWidget.enableInput();
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void refreshPcActionOptions()
	{
		this.charLowLeft.refreshPcActionOptions();
		this.charLowRight.refreshPcActionOptions();
		this.charMidLeft.refreshPcActionOptions();
		this.charMidRight.refreshPcActionOptions();
		this.charTopLeft.refreshPcActionOptions();
		this.charTopRight.refreshPcActionOptions();
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getInputDisablingPane()
	{
		DIYPane result = new DIYPane(screenBounds);

		BufferedImage img = Database.getInstance().getImage("screen/logo_divider");
		DIYLabel label = new DIYLabel(img);

		label.setBounds(
			SCREEN_WIDTH - img.getWidth(),
			SCREEN_HEIGHT - img.getHeight(),
			img.getWidth(),
			img.getHeight());

		result.add(label);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public void refreshCharacterData()
	{
		this.statsDisplay.refreshData();
		this.modifiersDisplay.refreshData();
		this.propertiesDisplay.refreshData();
		this.magicDisplay.refreshData();

		this.charLowLeft.refresh();
		this.charLowRight.refresh();
		this.charMidLeft.refresh();
		this.charMidRight.refresh();
		this.charTopLeft.refresh();
		this.charTopRight.refresh();

		this.partyOptionsAndTextWidget.refresh();

		this.partyDisplay.refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void refreshCharacterWidget(PlayerCharacter pc)
	{
		int index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);

		switch (index)
		{
			case 0 -> charTopLeft.refresh();
			case 1 -> charTopRight.refresh();
			case 2 -> charMidLeft.refresh();
			case 3 -> charMidRight.refresh();
			case 4 -> charLowLeft.refresh();
			case 5 -> charLowRight.refresh();
			default -> throw new MazeException("Invalid index " + index);
		};
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
	public void signBoard(String message, MazeEvent event)
	{
		BufferedImage img = Database.getInstance().getImage("screen/sign_board");

		Rectangle bounds = new Rectangle(LOW_BOUNDS);
		bounds.translate(0, -(SCREEN_HEIGHT / 3));

//		Rectangle bounds = new Rectangle(
//			SCREEN_WIDTH/2 -img.getWidth()/2,
//			LOW_BOUNDS.y-(SCREEN_HEIGHT/3),
//			img.getWidth(),
//			img.getHeight());

		SignBoardWidget sbw = new SignBoardWidget(bounds, img);

		sbw.setText(message);

		showDialog(sbw);
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
	public void showBlockingScreen(Image image, int delay, Object mutex)
	{
		DIYPanel dialog = new BlockingScreen(image, delay, mutex);
		showDialog(dialog);
	}

	/*-------------------------------------------------------------------------*/
	public void showBlockingScreen(ContainerWidget dialog, int delay,
		Object mutex)
	{
		DIYPanel bs = new BlockingScreen(dialog, delay, mutex);
		showDialog(bs);
	}

	/*-------------------------------------------------------------------------*/
	public void clearBlockingScreen()
	{
		clearDialog();
	}

	public void showCreateCharacterScreen()
	{
		if (!Maze.State.CREATE_CHARACTER.name().equals(getMusic().getState()))
		{
			executeMazeScript("_CREATE_CHARACTER_MUSIC_");
		}
		this.mainLayout.show(this.createCharacterScreen);
		this.createCharacter.refresh();
	}

	public void showCombatScreen()
	{
		this.mainLayout.show(this.movementScreen);
		this.partyOptionsAndTextWidget.setCurrentCombat(Maze.getInstance().getCurrentCombat());

		stopMusic();
	}

	private void stopMusic()
	{
		Maze.getInstance().appendEvents(new StopMusicEvent());
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
			executeMazeScript("_INVENTORY_MUSIC_");
		}
		clearAllDialogs();
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

		stopMusic();
	}

	public void showRestingScreen()
	{
		this.mainLayout.show(this.movementScreen);

		RestingDialog dialog = new RestingDialog(
			StringUtil.getUiLabel("rd.title"));
		showDialog(dialog);
	}

	public void showMainMenu()
	{
		if (!Maze.State.MAINMENU.name().equals(getMusic().getState()))
		{
			executeMazeScript("_MAIN_MENU_MUSIC_");
		}
		tipOfTheDayWidget.refresh();
		this.mainLayout.show(this.mainMenu);
	}

	private void executeMazeScript(String scriptName)
	{
		MazeScript script = Database.getInstance().getMazeScript(scriptName);
		Maze.getInstance().appendEvents(script.getEvents());
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
		partyOptionsAndTextWidget.setChest(chest);
		movementCardLayout.show(partyOptionsAndTextWidget);
		refreshPcActionOptions();
	}

	public void showEncounterActorsScreen(ActorEncounter actorEncounter)
	{
		this.mainLayout.show(this.movementScreen);
		partyOptionsAndTextWidget.setActorEncounter(actorEncounter);
		movementCardLayout.show(partyOptionsAndTextWidget);
		refreshPcActionOptions();
	}

	private void showPortalScreen(Portal currentPortal)
	{
		this.mainLayout.show(this.movementScreen);
		partyOptionsAndTextWidget.setPortal(currentPortal);
		movementCardLayout.show(partyOptionsAndTextWidget);
		refreshPcActionOptions();
	}

	@Override
	public void clearMessages()
	{
		partyOptionsAndTextWidget.clearMessages();
	}

	/*-------------------------------------------------------------------------*/
	private void initCommonWidgets()
	{
		int zoneDisplayHeight = SCREEN_HEIGHT / 9;
		MAZE_WIDTH = SCREEN_WIDTH / 2;
		MAZE_HEIGHT = SCREEN_HEIGHT * 7 / 12;

		int internalInset = 10;
		SCREEN_EDGE_INSET = 10;

		PC_WIDTH = (SCREEN_WIDTH - MAZE_WIDTH - SCREEN_EDGE_INSET * 2 - internalInset * 2) / 2;

		int column1x = SCREEN_EDGE_INSET;
		int column1Width = (SCREEN_WIDTH - MAZE_WIDTH - SCREEN_EDGE_INSET * 2 - internalInset * 2) / 2;

		int column2x = column1x + column1Width + internalInset;
		int column2Width = MAZE_WIDTH;

		int column3x = column2x + column2Width + internalInset;
		// col 3 width same as col 1

		int pcwHeight = (SCREEN_HEIGHT - SCREEN_EDGE_INSET * 2 - internalInset * 2) / 3;

		int internalOverlap = 8;

		// player character widgets

		charTopLeft = new PlayerCharacterWidget(0,
			new Rectangle(column1x, SCREEN_EDGE_INSET,
				column1Width, pcwHeight));

		charTopRight = new PlayerCharacterWidget(1,
			new Rectangle(column3x, SCREEN_EDGE_INSET,
				column1Width, pcwHeight));

		charMidLeft = new PlayerCharacterWidget(2,
			new Rectangle(column1x, charTopLeft.y + charTopLeft.height + internalInset,
				column1Width, pcwHeight));

		charMidRight = new PlayerCharacterWidget(3,
			new Rectangle(column3x, charTopRight.y + charTopRight.height + internalInset,
				column1Width, pcwHeight));

		charLowLeft = new PlayerCharacterWidget(4,
			new Rectangle(column1x, charMidLeft.y + charMidLeft.height + internalInset,
				column1Width, pcwHeight));

		charLowRight = new PlayerCharacterWidget(5,
			new Rectangle(column3x, charMidRight.y + charMidRight.height + internalInset,
				column1Width, pcwHeight));

		charTopLeft.setPlayerCharacter(null);
		charTopRight.setPlayerCharacter(null);
		charMidLeft.setPlayerCharacter(null);
		charMidRight.setPlayerCharacter(null);
		charLowLeft.setPlayerCharacter(null);
		charLowRight.setPlayerCharacter(null);

		// zone info display header
		zoneDisplay = new ZoneDisplayWidget(
			new Rectangle(
				column2x - internalInset / 2,
				SCREEN_EDGE_INSET,
				column2Width + internalOverlap,
				zoneDisplayHeight + internalOverlap));

		// the maze view with embedded raycaster
		mazeWidget = new MazeWidget(
			new Rectangle(
				column2x,
				SCREEN_EDGE_INSET + zoneDisplayHeight, // intentionally no inset here
				MAZE_WIDTH,
				MAZE_HEIGHT),
			raycaster);

		MAZE_WINDOW_BOUNDS = new Rectangle(mazeWidget.getBounds());

		// intentionally sized over the insets here, to emphasise the widget
		LOW_BOUNDS = new Rectangle(
			column2x - internalInset / 2,
			mazeWidget.y + mazeWidget.height - internalOverlap,
			column2Width + internalInset,
			SCREEN_HEIGHT - zoneDisplayHeight - mazeWidget.height - SCREEN_EDGE_INSET * 2 + internalOverlap);

		// char picker for the details screens
		partyDisplay = new PartyDisplayWidget(
			new Rectangle(
				SCREEN_EDGE_INSET,
				SCREEN_EDGE_INSET,
				SCREEN_WIDTH / 6,
				SCREEN_HEIGHT - SCREEN_EDGE_INSET * 2)
			, null);

		// button toolbar for the details screens
		buttonToolbar = new ButtonToolbar(
			new Rectangle(PC_WIDTH, SCREEN_HEIGHT - SCREEN_HEIGHT / 12,
				SCREEN_WIDTH - PC_WIDTH, SCREEN_HEIGHT / 12));
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getStatsDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent e)
			{
				if (Maze.getInstance().getState() == Maze.State.STATSDISPLAY)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_1:
							characterSelected(0);
							break;
						case KeyEvent.VK_2:
							characterSelected(1);
							break;
						case KeyEvent.VK_3:
							characterSelected(2);
							break;
						case KeyEvent.VK_4:
							characterSelected(3);
							break;
						case KeyEvent.VK_5:
							characterSelected(4);
							break;
						case KeyEvent.VK_6:
							characterSelected(5);
							break;
						case KeyEvent.VK_A:
							buttonToolbar.magic();
							break;
						case KeyEvent.VK_M:
							buttonToolbar.modifiers();
							break;
						case KeyEvent.VK_S:
							buttonToolbar.stats();
							break;
						case KeyEvent.VK_P:
							buttonToolbar.properties();
							break;
						case KeyEvent.VK_I:
							buttonToolbar.inventory();
							break;
						case KeyEvent.VK_E:
							buttonToolbar.exit();
							break;
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							if (Maze.getInstance().isInCombat())
							{
								Maze.getInstance().setState(Maze.State.COMBAT);
							}
							else
							{
								Maze.getInstance().setState(Maze.State.MOVEMENT);
							}
							break;
					}
				}
			}
		};

		statsDisplay = new StatsDisplayWidget(
			new Rectangle(
				partyDisplay.x + partyDisplay.width,
				0,
				SCREEN_WIDTH - partyDisplay.width - SCREEN_EDGE_INSET,
				SCREEN_HEIGHT));

		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/stats_back");
		screen.setBackgroundImage(back);

		screen.add(statsDisplay);
		screen.add(partyDisplay);
		screen.add(buttonToolbar);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getMagicScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent e)
			{
				if (Maze.getInstance().getState() == Maze.State.MAGIC)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_1:
							characterSelected(0);
							break;
						case KeyEvent.VK_2:
							characterSelected(1);
							break;
						case KeyEvent.VK_3:
							characterSelected(2);
							break;
						case KeyEvent.VK_4:
							characterSelected(3);
							break;
						case KeyEvent.VK_5:
							characterSelected(4);
							break;
						case KeyEvent.VK_6:
							characterSelected(5);
							break;
						case KeyEvent.VK_A:
							buttonToolbar.magic();
							break;
						case KeyEvent.VK_M:
							buttonToolbar.modifiers();
							break;
						case KeyEvent.VK_S:
							buttonToolbar.stats();
							break;
						case KeyEvent.VK_P:
							buttonToolbar.properties();
							break;
						case KeyEvent.VK_I:
							buttonToolbar.inventory();
							break;
						case KeyEvent.VK_E:
							buttonToolbar.exit();
							break;
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							if (Maze.getInstance().isInCombat())
							{
								Maze.getInstance().setState(Maze.State.COMBAT);
							}
							else
							{
								Maze.getInstance().setState(Maze.State.MOVEMENT);
							}
							break;
					}
				}
			}
		};

		magicDisplay = new MagicDisplayWidget(
			new Rectangle(
				partyDisplay.x + partyDisplay.width,
				0,
				SCREEN_WIDTH - partyDisplay.width - SCREEN_EDGE_INSET,
				SCREEN_HEIGHT));

		screen.add(magicDisplay);
		screen.add(partyDisplay);
		screen.add(buttonToolbar);

		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/magic_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getLevelUpScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds);
		levelUp = new LevelUpWidget(screenBounds);
		screen.add(levelUp);
		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/create_char_back");
		screen.setBackgroundImage(back);
		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getCreateCharacterScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds);
		createCharacter = new CreateCharacterWidget(screenBounds);
		screen.add(createCharacter);
		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/create_char_back");
		screen.setBackgroundImage(back);
		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getModifiersDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent e)
			{
				if (Maze.getInstance().getState() == Maze.State.MODIFIERSDISPLAY)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_1:
							characterSelected(0);
							break;
						case KeyEvent.VK_2:
							characterSelected(1);
							break;
						case KeyEvent.VK_3:
							characterSelected(2);
							break;
						case KeyEvent.VK_4:
							characterSelected(3);
							break;
						case KeyEvent.VK_5:
							characterSelected(4);
							break;
						case KeyEvent.VK_6:
							characterSelected(5);
							break;
						case KeyEvent.VK_A:
							buttonToolbar.magic();
							break;
						case KeyEvent.VK_M:
							buttonToolbar.modifiers();
							break;
						case KeyEvent.VK_S:
							buttonToolbar.stats();
							break;
						case KeyEvent.VK_P:
							buttonToolbar.properties();
							break;
						case KeyEvent.VK_I:
							buttonToolbar.inventory();
							break;
						case KeyEvent.VK_E:
							buttonToolbar.exit();
							break;
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							if (Maze.getInstance().isInCombat())
							{
								Maze.getInstance().setState(Maze.State.COMBAT);
							}
							else
							{
								Maze.getInstance().setState(Maze.State.MOVEMENT);
							}
							break;
					}
				}
			}
		};

		modifiersDisplay = new ModifiersDisplayWidget(
			new Rectangle(
				partyDisplay.x + partyDisplay.width,
				0,
				SCREEN_WIDTH - partyDisplay.width - SCREEN_EDGE_INSET,
				SCREEN_HEIGHT));

		screen.add(modifiersDisplay);
		screen.add(partyDisplay);
		screen.add(buttonToolbar);

		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/modifiers_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getPropertiesDisplayScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent e)
			{
				if (Maze.getInstance().getState() == Maze.State.PROPERTIESDISPLAY)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_1:
							characterSelected(0);
							break;
						case KeyEvent.VK_2:
							characterSelected(1);
							break;
						case KeyEvent.VK_3:
							characterSelected(2);
							break;
						case KeyEvent.VK_4:
							characterSelected(3);
							break;
						case KeyEvent.VK_5:
							characterSelected(4);
							break;
						case KeyEvent.VK_6:
							characterSelected(5);
							break;
						case KeyEvent.VK_A:
							buttonToolbar.magic();
							break;
						case KeyEvent.VK_M:
							buttonToolbar.modifiers();
							break;
						case KeyEvent.VK_S:
							buttonToolbar.stats();
							break;
						case KeyEvent.VK_P:
							buttonToolbar.properties();
							break;
						case KeyEvent.VK_I:
							buttonToolbar.inventory();
							break;
						case KeyEvent.VK_E:
							buttonToolbar.exit();
							break;
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							if (Maze.getInstance().isInCombat())
							{
								Maze.getInstance().setState(Maze.State.COMBAT);
							}
							else
							{
								Maze.getInstance().setState(Maze.State.MOVEMENT);
							}
							break;
					}
				}
			}
		};

		propertiesDisplay = new PropertiesDisplayWidget(
			new Rectangle(
				partyDisplay.x + partyDisplay.width,
				0,
				SCREEN_WIDTH - partyDisplay.width - SCREEN_EDGE_INSET,
				SCREEN_HEIGHT));

		screen.add(propertiesDisplay);
		screen.add(partyDisplay);
		screen.add(buttonToolbar);

		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/properties_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getInventoryScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent e)
			{
				if (Maze.getInstance().getState() == Maze.State.INVENTORY)
				{
					switch (e.getKeyCode())
					{
						case KeyEvent.VK_1:
							characterSelected(0);
							break;
						case KeyEvent.VK_2:
							characterSelected(1);
							break;
						case KeyEvent.VK_3:
							characterSelected(2);
							break;
						case KeyEvent.VK_4:
							characterSelected(3);
							break;
						case KeyEvent.VK_5:
							characterSelected(4);
							break;
						case KeyEvent.VK_6:
							characterSelected(5);
							break;
						case KeyEvent.VK_A:
							buttonToolbar.magic();
							break;
						case KeyEvent.VK_M:
							buttonToolbar.modifiers();
							break;
						case KeyEvent.VK_S:
							buttonToolbar.stats();
							break;
						case KeyEvent.VK_P:
							buttonToolbar.properties();
							break;
						case KeyEvent.VK_I:
							buttonToolbar.inventory();
							break;
						case KeyEvent.VK_E:
							buttonToolbar.exit();
							break;
						case KeyEvent.VK_C:
							inventoryDisplay.spell();
							break;
						case KeyEvent.VK_U:
							inventoryDisplay.use();
							break;
						case KeyEvent.VK_R:
							inventoryDisplay.craft();
							break;
						case KeyEvent.VK_D:
							inventoryDisplay.drop();
							break;
						case KeyEvent.VK_L:
							inventoryDisplay.split();
							break;
						case KeyEvent.VK_B:
							inventoryDisplay.disassemble();
							break;
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							if (Maze.getInstance().isInCombat())
							{
								Maze.getInstance().setState(Maze.State.COMBAT);
							}
							else
							{
								Maze.getInstance().setState(Maze.State.MOVEMENT);
							}
							break;
					}
				}
			}
		};

		inventoryDisplay = new InventoryDisplayWidget(
			new Rectangle(
				partyDisplay.x + partyDisplay.width,
				0,
				SCREEN_WIDTH - partyDisplay.width - SCREEN_EDGE_INSET,
				SCREEN_HEIGHT));

		screen.add(inventoryDisplay);
		screen.add(partyDisplay);
		screen.add(buttonToolbar);

		BufferedImage back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/inventory_back");
		screen.setBackgroundImage(back);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private MainMenu getMainMenu()
	{
		MainMenu screen = new MainMenu(screenBounds);

		screen.add(charTopLeft);
		screen.add(charMidLeft);
		screen.add(charLowLeft);
		screen.add(charTopRight);
		screen.add(charMidRight);
		screen.add(charLowRight);

		int tipWidth = SCREEN_WIDTH / 2;
		Rectangle tipBounds = new Rectangle(
			SCREEN_WIDTH / 2 - tipWidth / 2,
			SCREEN_HEIGHT / 6 * 5,
			tipWidth,
			SCREEN_HEIGHT / 7);
		tipOfTheDayWidget = new TipOfTheDayWidget(tipBounds);
		screen.add(tipOfTheDayWidget);

		return screen;
	}

	/*-------------------------------------------------------------------------*/
	private SaveLoadScreen getSaveLoad()
	{
		return new SaveLoadScreen(screenBounds);
	}

	/*-------------------------------------------------------------------------*/
	private ContainerWidget getMovementScreen()
	{
		DIYPanel screen = new DIYPanel(screenBounds)
		{
			@Override
			public void processHotKey(KeyEvent event)
			{
				if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
				{
					int code = event.getKeyCode();

					if (acceptInput)
					{
						if (DiyGuiUserInterface.crusaderKeys.containsKey(code) &&
							Maze.getInstance().getState() == Maze.State.MOVEMENT &&
							DIYToolkit.getInstance().getDialog() == null)
						{
							int crusaderKey = DiyGuiUserInterface.crusaderKeys.get(code);
							acceptInput = false;
							Maze.getInstance().appendEvents(
								new HandleKeyEvent(crusaderKey),
								new EnableInputEvent());
						}
					}

					partyOptionsAndTextWidget.handleKey(event.getKeyCode());
				}
				else if (Maze.getInstance().getState() == Maze.State.COMBAT)
				{
					partyOptionsAndTextWidget.handleKey(event.getKeyCode());
				}
				else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_PORTAL)
				{
					partyOptionsAndTextWidget.handleKey(event.getKeyCode());
				}
				else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_ACTORS)
				{
					partyOptionsAndTextWidget.handleKey(event.getKeyCode());
				}
				else if (Maze.getInstance().getState() == Maze.State.ENCOUNTER_CHEST)
				{
					partyOptionsAndTextWidget.handleKey(event.getKeyCode());
				}
				else if (Maze.getInstance().getState() == Maze.State.RESTING)
				{
					switch (event.getKeyCode())
					{
						case KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_D ->
							restingWidget.done();
					}
				}
			}
		};

		screen.add(charTopLeft);
		screen.add(charMidLeft);
		screen.add(charLowLeft);
		screen.add(charTopRight);
		screen.add(charMidRight);
		screen.add(charLowRight);

		screen.add(mazeWidget);
		screen.add(zoneDisplay);

		Rectangle rect = (Rectangle)DiyGuiUserInterface.LOW_BOUNDS.clone();

		partyOptionsAndTextWidget = new PartyOptionsAndTextWidget(rect);
		restingWidget = new RestingWidget(rect);

		ArrayList<ContainerWidget> list = new ArrayList<>();
		list.add(partyOptionsAndTextWidget);
		list.add(restingWidget);

		movementCardLayout = new CardLayoutWidget(DiyGuiUserInterface.LOW_BOUNDS, list);
		movementCardLayout.show(partyOptionsAndTextWidget);

		screen.add(movementCardLayout);

		Image back = DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/movement_back");

		screen.setBackgroundImage(back);

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

			partyOptionsAndTextWidget.setParty(null);

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

		partyOptionsAndTextWidget.setParty(party);
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
		raycaster.setPlayerPos(pos.x, pos.y, facing);
	}

	/*-------------------------------------------------------------------------*/
	public void setTile(Zone zone, Tile t, Point tile)
	{
		this.zoneDisplay.setTile(zone, t, tile);
		this.partyOptionsAndTextWidget.setTile(zone, t, tile);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void tempChangeTexture(EngineObject obj, Texture texture)
	{
		this.raycaster.addScript(new TempChangeTexture(obj, texture));
	}

	/*-------------------------------------------------------------------------*/
	public void actorAttacks(UnifiedActor attacker, UnifiedActor defender, AttackWith attackWith)
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

			Texture attackTexture = attackWith.isRanged() ?
				foe.getRangedAttackTexture().getTexture() :
				foe.getMeleeAttackTexture().getTexture();

			tempChangeTexture(obj, attackTexture);
		}
		// todo: PC attacks, highlight portrait?
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
	public void foeLeaves(UnifiedActor foe)
	{
		if (foe instanceof Foe)
		{
			EngineObject sprite = ((Foe)foe).getSprite();

			sprite.removeAllScripts();
			sprite.addScript(new DisappearanceToSide(Math.random() > .5, 500).spawnNewInstance(sprite, raycaster));
		}
	}

	/*-------------------------------------------------------------------------*/
	public List<MazeEvent> backPartyUp(int maxTiles, int facing)
	{
		List<Point> recentTiles = Maze.getInstance().getPlayerTilesVisited().getRecentTiles();

		for (int i = maxTiles; i > 0; i--)
		{
			int index = recentTiles.size() - i;

			if (index >= 0)
			{
				Point tile = recentTiles.get(index);

				try
				{
					Thread.sleep(300);
				}
				catch (InterruptedException e)
				{
					throw new MazeException(e);
				}

				return Maze.getInstance().setPlayerPos(tile, facing);
			}
		}

		return null;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Remove all current foes and add the given new ones.
	 *
	 * @param runAppearanceAnimations True if entrance animations should be run
	 *                                for the foes
	 */
	public void setFoes(List<FoeGroup> others, boolean runAppearanceAnimations)
	{
		// remove any existing sprites
		if (this.getFoeGroups() != null)
		{
			for (FoeGroup foeGroup1 : this.getFoeGroups())
			{
				removeFoeGroupSprites(foeGroup1);
			}
			this.setFoeGroups(null);
		}
		this.mazeWidget.setFoes(null);

		if (others != null)
		{
			addFoes(others, runAppearanceAnimations);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void removeFoeGroupSprites(FoeGroup foeGroup1)
	{
		List<UnifiedActor> group = foeGroup1.getActors();
		int maxFoeIndex = group.size();
		for (int foeIndex = 0; foeIndex < maxFoeIndex; foeIndex++)
		{
			Foe foe = (Foe)group.get(foeIndex);
			this.raycaster.removeObject(foe.getSprite());
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Adds the given foes to any already in view.
	 *
	 * @param runAppearanceAnimations True if appearance animations should be
	 *                                run
	 */
	public void addFoes(List<FoeGroup> others, boolean runAppearanceAnimations)
	{
		int maxFoeGroups = others.size();

		//
		// Add the foe group widgets
		//
		this.mazeWidget.addFoes(others);

		//
		// Add to the internal cache of foe groups
		//

		// this is an offset if we are adding groupd (e.g. leader calling for help)
		int startingRank;
		if (this.getFoeGroups() == null)
		{
			startingRank = 0;
			this.setFoeGroups(new ArrayList<>(others));
		}
		else
		{
			startingRank = getFoeGroups().size();
			this.addFoeGroups(others);
		}

		List<List<double[]>> coordList = placeFoeSprites2(startingRank, others);

		//
		// Add the foe sprites to the raycasting engine
		//
		for (int foeGroup = 0; foeGroup < maxFoeGroups; foeGroup++)
		{
			List<UnifiedActor> group = others.get(foeGroup).getActors();

			for (int foeIndex = 0; foeIndex < group.size(); foeIndex++)
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

				boolean shouldMirror = !foe.isNpc();

				if (shouldMirror && Dice.d2.roll("mirror") == 1)
				{
					for (int i = 0; i < textures.length; i++)
					{
						textures[i] = textures[i].mirrorHorizontal();
					}
				}

				EngineObject obj = new EngineObject(textures, false);

				obj.setVerticalAlignment(foe.getVerticalAlignment());
				obj.setMouseClickScript(new FoeInfoMouseClickScript(foe));

				if (foe.getSprite() != null)
				{
					this.raycaster.removeObject(foe.getSprite());
				}

				foe.setSprite(obj);

				if (foe.getHitPoints().getCurrent() > 0)
				{
					// todo: space out foes better
					// - more than one row for big groups
					// - less space between groups

					int rank = startingRank + foeGroup;

//					double[] coords = placeFoeSprite(group, foe, rank);
					double[] coords = coordList.get(foeGroup).get(foeIndex);

					this.raycaster.initObjectInFrontOfPlayer(obj, coords[0], coords[1], true);

					if (runAppearanceAnimations)
					{
						switch (foe.getAppearanceDirection())
						{
							case FROM_LEFT ->
								obj.addScript(new AppearanceFromSide(true, 500, foe.getAnimationScripts()).
									spawnNewInstance(obj, this.raycaster));
							case FROM_RIGHT ->
								obj.addScript(new AppearanceFromSide(false, 500, foe.getAnimationScripts()).
									spawnNewInstance(obj, this.raycaster));
							case FROM_LEFT_OR_RIGHT ->
								obj.addScript(new AppearanceFromSide(Math.random() > .5, 500, foe.getAnimationScripts()).
									spawnNewInstance(obj, this.raycaster));
							case FROM_TOP ->
								obj.addScript(new AppearanceFromTop(500, foe.getAnimationScripts()).
									spawnNewInstance(obj, raycaster));
							case FROM_FAR ->
								obj.addScript(new AppearanceFromFar(500, foe.getAnimationScripts()).
									spawnNewInstance(obj, raycaster));
							default ->
								throw new MazeException("invalid appearance direction " + foe.getAppearanceDirection());
						}
					}
					else
					{
						if (foe.getAnimationScripts() != null)
						{
							for (ObjectScript script : foe.getAnimationScripts())
							{
								obj.addScript(script.spawnNewInstance(obj, raycaster));
							}
						}
					}

					this.raycaster.addObject(obj, false);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Left-to-right spacing
	 * One FG per rank
	 */
	private double[] placeFoeSprite1(List<UnifiedActor> group, Foe foe, int rank)
	{
		int foeIndex = group.indexOf(foe);

		double increment = 1.0 / (group.size() + 1);
		double arc = increment + increment* foeIndex;
		double distance = 0.51 + (0.2 * rank);

		double[] coords = new double[]{distance, arc};
		return coords;
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * Center-out spacing
	 * One FG per rank
	 */
	private List<List<double[]>> placeFoeSprites2(int startingRank,
		List<FoeGroup> foeGroups)
	{
		final double baseDistance = 0.5;
		final double rankStep = 0.2;

		List<List<double[]>> result = new ArrayList<>();

		for (int g = 0; g < foeGroups.size(); g++)
		{
			FoeGroup group = foeGroups.get(g);
			List<Foe> foes = group.getFoes();
			int count = foes.size();
//			int count = group.numAlive(); todo

			List<double[]> groupCoords = new ArrayList<>(count);

			// Compute center-out arc spacing
			double spacing = 1.0 / (count + 1); // Leaves margin on edges
			double center = 0.5;

			for (int i = 0; i < count; i++)
			{
				int centerIndex = count / 2;
				double arcOffset = (i - centerIndex + (count % 2 == 0 ? 0.5 : 0)) * spacing;
				double arc = center + arcOffset;

				arc = Math.max(0.0, Math.min(1.0, arc)); // Clamp to [0,1]

				double distance = baseDistance + (startingRank + g) * rankStep;

				groupCoords.add(new double[]{distance, arc});
			}

			result.add(groupCoords);
		}

		return result;
	}

	/**
	 * Center-out spacing
	 * FGs spread across ranks.
	 * todo: not working!
	 */
	private List<List<double[]>> placeFoeSprites3(
		int startingRank,
		List<FoeGroup> foeGroups)
	{
		final double baseDistance = 0.5;
		final double rankStep = 0.2;
		final double arcMargin = 0.01; // spacing between sprites

		// Flatten all foes and track group index
		List<Foe> allFoes = new ArrayList<>();
		List<Integer> groupIndices = new ArrayList<>();
		for (int gi = 0; gi < foeGroups.size(); gi++)
		{
			for (Foe f : foeGroups.get(gi).getFoes())
			{
				allFoes.add(f);
				groupIndices.add(gi);
			}
		}

		// Result placeholder: one list per original group
		List<List<double[]>> result = new ArrayList<>();
		for (int i = 0; i < foeGroups.size(); i++)
		{
			result.add(new ArrayList<>());
		}

		int index = 0;
		int currentRank = startingRank;

		while (index < allFoes.size())
		{
			double distance = baseDistance + currentRank * rankStep;

			// Try to fit foes in this rank
			List<Foe> rankFoes = new ArrayList<>();
			List<Integer> rankGroups = new ArrayList<>();
			List<Double> arcWidths = new ArrayList<>();

			double totalArcUsed = 0.0;
			int tempIndex = index;

			while (tempIndex < allFoes.size())
			{
				Foe foe = allFoes.get(tempIndex);
				int spriteWidthPx = foe.getBaseTexture().getImageWidth();
				double arcWidth = computeArcWidth(spriteWidthPx, distance, SCREEN_WIDTH);
				double requiredArc = arcWidth + arcMargin;

				if (totalArcUsed + requiredArc > 1.0)
				{
					break;
				}

				totalArcUsed += requiredArc;
				rankFoes.add(foe);
				rankGroups.add(groupIndices.get(tempIndex));
				arcWidths.add(arcWidth);
				tempIndex++;
			}

			// Place foes symmetrically around arc = 0.5
			double arcStart = 0.5 - (totalArcUsed / 2.0);
			double currentArc = arcStart;

			for (int i = 0; i < rankFoes.size(); i++)
			{
				double arcWidth = arcWidths.get(i);
				double arcCenter = currentArc + arcWidth / 2.0;

				result.get(rankGroups.get(i)).add(new double[]{distance, arcCenter});
				currentArc += arcWidth + arcMargin;
			}

			index += rankFoes.size();
			currentRank++;
		}

		return result;
	}

	private double computeArcWidth(int imageWidthPx, double distanceInTiles,
		int screenWidthPx)
	{
		if (distanceInTiles <= 0.0)
		{
			distanceInTiles = 0.01; // Prevent divide-by-zero or extreme scaling
		}
		return imageWidthPx / (screenWidthPx * distanceInTiles);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public void rebalanceFoeSprites(Combat combat)
	{
		this.setFoeGroups(combat.getFoes());
		ListIterator<FoeGroup> foeGroupListIterator = getFoeGroups().listIterator();
		mazeWidget.setFoes(null);
		while (foeGroupListIterator.hasNext())
		{
			FoeGroup fg = foeGroupListIterator.next();

			if (fg.numAlive() == 0)
			{
				// time to remove this foe group
				removeFoeGroupSprites(fg);
				foeGroupListIterator.remove();
			}
			else
			{
				mazeWidget.addFoes(Collections.singletonList(fg));
			}
		}

		List<List<double[]>> coordList = placeFoeSprites2(0, this.getFoeGroups());

		// determine locations of live foes and move them there
		int rank = 0;
		for (FoeGroup fg : combat.getFoes())
		{
			int maxFoeIndex = fg.getFoes().size();
			for (int foeIndex = 0; foeIndex < maxFoeIndex; foeIndex++)
			{
				Foe foe = (Foe)fg.getFoes().get(foeIndex);

				if (foe.getHitPoints().getCurrent() > 0)
				{
					// todo: space out foes better
					// - more than one row for big groups
					// - less space between groups

//					double increment = 1.0 / (maxFoeIndex + 1);
//					double arc = increment * (foeIndex + 1) + (-0.1 * (rank % 2)); // stagger the groups
//					double distance = 0.51 + (0.2 * rank);
//					this.raycaster.moveObjectToFrontOfPlayer(foe.getSprite(), distance, arc);

					double[] coords = coordList.get(getFoeGroups().indexOf(fg)).get(foeIndex);
					this.raycaster.moveObjectToFrontOfPlayer(foe.getSprite(), coords[0], coords[1]);
				}
			}

			rank++;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setAllies(List<FoeGroup> others)
	{
		this.mazeWidget.setAllies(others);
	}

	/*-------------------------------------------------------------------------*/
	public void displayMazeEvent(MazeEvent event, boolean displayEventText)
	{
		if (event.getText() != null && displayEventText)
		{
			String eventDesc = event.getText();
			Maze.log(eventDesc);

			if (event.shouldClearText())
			{
				partyOptionsAndTextWidget.clearDisplayedMessages();
			}
			partyOptionsAndTextWidget.addMessage(eventDesc);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(int index, PlayerCharacter pc)
	{
		switch (index)
		{
			case 1 -> this.charTopLeft.setPlayerCharacter(pc);
			case 2 -> this.charTopRight.setPlayerCharacter(pc);
			case 3 -> this.charMidLeft.setPlayerCharacter(pc);
			case 4 -> this.charMidRight.setPlayerCharacter(pc);
			case 5 -> this.charLowLeft.setPlayerCharacter(pc);
			case 6 -> this.charLowRight.setPlayerCharacter(pc);
			default -> throw new MazeException("Invalid index " + index);
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
			zone.doShading(),
			zone.doLighting(),
			zone.getShadingDistance(),
			zone.getShadingMultiplier(),
			null,
			zone.getProjectionPlaneOffset(),
			zone.getPlayerFieldOfView(),
			zone.getScaleDistFromProjPlane(),
			-1,
			8,
			this);
	}

	/*-------------------------------------------------------------------------*/
	public void addTexture(Texture t)
	{
		this.raycaster.addTexture(t);
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
			case MAINMENU, MODIFIERSDISPLAY, STATSDISPLAY, PROPERTIESDISPLAY, INVENTORY, MAGIC ->
			{
				this.modifiersDisplay.setCharacter(pc);
				this.statsDisplay.setCharacter(pc);
				this.inventoryDisplay.setCharacter(pc);
				this.magicDisplay.setCharacter(pc);
				this.propertiesDisplay.setCharacter(pc);
			}
			default ->
			{
				// ignore
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void addMessage(String msg, boolean shouldJournal)
	{
		this.partyOptionsAndTextWidget.addMessage(msg);
		if (shouldJournal)
		{
			Maze.getInstance().journalInContext(msg);
		}
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

		return switch (index)
			{
				case 0 -> charTopLeft.getBounds();
				case 1 -> charTopRight.getBounds();
				case 2 -> charMidLeft.getBounds();
				case 3 -> charMidRight.getBounds();
				case 4 -> charLowLeft.getBounds();
				case 5 -> charLowRight.getBounds();
				default -> throw new MazeException("Invalid index " + index);
			};
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget getPlayerCharacterWidget(
		PlayerCharacter playerCharacter)
	{
		int index = Maze.getInstance().getParty().getPlayerCharacterIndex(playerCharacter);

		return switch (index)
			{
				case 0 -> charTopLeft;
				case 1 -> charTopRight;
				case 2 -> charMidLeft;
				case 3 -> charMidRight;
				case 4 -> charLowLeft;
				case 5 -> charLowRight;
				default -> throw new MazeException("Invalid index " + index);
			};
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPlayerCharacterPortraitBounds(PlayerCharacter defender)
	{
		return getPlayerCharacterWidget(defender).getPortraitBounds();
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
	public TextDialogWidget waitingDialog(String s)
	{
		//center it
		int x = DiyGuiUserInterface.SCREEN_WIDTH / 4;
		int y = DiyGuiUserInterface.SCREEN_HEIGHT / 4;
		Rectangle rectangle = new Rectangle(x, y,
			DiyGuiUserInterface.SCREEN_WIDTH / 2, DiyGuiUserInterface.SCREEN_HEIGHT / 2);

		TextDialogWidget d = new TextDialogWidget(
			rectangle, null, s, true);

		showDialog(d);

		return d;
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
	public void setPlayerCharacterActionOption(PlayerCharacter pc,
		Class<? extends ActorActionOption> option)
	{
		PlayerCharacterWidget pcw = getPlayerCharacterWidget(pc);

		for (ActorActionOption aao : charTopLeft.getAction().getModel().getNodes())
		{
			if (option == aao.getClass())
			{
				pcw.getAction().setSelected(aao);
			}
		}
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

	public CrusaderEngine getRaycaster()
	{
		return raycaster;
	}

	public List<FoeGroup> getFoeGroups()
	{
		return foeGroups;
	}

	public void setFoeGroups(List<FoeGroup> foeGroups)
	{
		if (foeGroups != null)
		{
			this.foeGroups = Collections.unmodifiableList(foeGroups);
		}
		else
		{
			this.foeGroups = null;
		}
	}

	public void addFoeGroups(List<FoeGroup> foeGroups)
	{
		ArrayList<FoeGroup> temp = new ArrayList<>(this.foeGroups);
		temp.addAll(foeGroups);
		this.foeGroups = Collections.unmodifiableList(temp);
	}

	/*-------------------------------------------------------------------------*/
	public static class EnableInputEvent extends MazeEvent
	{
		@Override
		public List<MazeEvent> resolve()
		{
			acceptInput = true;
			return null;
		}
	}

	/*-------------------------------------------------------------------------*/

	/**
	 * This is the thread responsible for processing all UI events.
	 * <p>
	 * It exists to disentangle the rendering and game logic from the AWT theads
	 * that are delivering system mouse and key events.
	 */
	static class EventProcessor extends Thread
	{
		private final BlockingQueue<InputEvent> queue;

		public EventProcessor(BlockingQueue<InputEvent> queue)
		{
			super("MAZE UI THREAD");
			this.queue = queue;
		}

		public void run()
		{
			long time, counter = 0, sumProcessingTime = 0;

			while (true)
			{
				try
				{
					time = System.nanoTime();
					DIYToolkit.getInstance().processEvent(queue.take());
					long diff = System.nanoTime() - time;
					counter++;
					sumProcessingTime += diff;

					if (counter == 10)
					{
						//double ave = 1D*sumProcessingTime/counter;//100000D;
						//System.out.println("ave = [" + ave + "]");

						counter = 0;
						sumProcessingTime = 0;
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