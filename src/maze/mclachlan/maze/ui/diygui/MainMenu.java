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

import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYGridLayout;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Campaign;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Personality;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.PlayerParty;
import mclachlan.maze.stat.SpeechUtil;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class MainMenu extends DIYPanel 
	implements ActionListener, ChooseCharacterCallback,
	GuildCallback, StartGameCallback
{
	private final DIYButton quickStart;
	private final DIYButton createCharacter;
	private final DIYButton exit;
	private final DIYButton addCharacter;
	private final DIYButton startGame;
	private final DIYButton loadGame;
	private final DIYButton removeCharacter;
	private final DIYButton options;

	/*-------------------------------------------------------------------------*/
	public MainMenu(Rectangle bounds)
	{
		super(bounds);

		Campaign campaign = Maze.getInstance().getCampaign();

		DIYLabel topLabel = new DIYLabel(campaign.getDisplayName(), DIYToolkit.Align.CENTER);
		topLabel.setBounds(0, 60, DiyGuiUserInterface.SCREEN_WIDTH, 30);
		topLabel.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
		topLabel.setFont(f);
		this.add(topLabel);
		
		DIYPanel menu = new DIYPanel(new DIYGridLayout(1,9,5,5));
		menu.setStyle(Style.PANEL_MED);
		menu.setInsets(new Insets(25,25,25,25));

		int width = 300;
		int height = 500;
		menu.setBounds(
			DiyGuiUserInterface.SCREEN_WIDTH/2-width/2, 
			DiyGuiUserInterface.SCREEN_HEIGHT/2-height/2, 
			width, 
			height);

		quickStart = new DIYButton(StringUtil.getUiLabel("mm.quick.start"));
		quickStart.setTooltip(StringUtil.getUiLabel("mm.quick.start.tooltip"));
		quickStart.addActionListener(this);

		startGame = new DIYButton(StringUtil.getUiLabel("mm.start.game"));
		startGame.setTooltip(StringUtil.getUiLabel("mm.start.game.tooltip"));
		startGame.addActionListener(this);
		
		createCharacter = new DIYButton(StringUtil.getUiLabel("mm.create.character"));
		createCharacter.setTooltip(StringUtil.getUiLabel("mm.create.character.tooltip"));
		createCharacter.addActionListener(this);
		
		addCharacter = new DIYButton(StringUtil.getUiLabel("mm.add.character"));
		addCharacter.setTooltip(StringUtil.getUiLabel("mm.add.character.tooltip"));
		addCharacter.addActionListener(this);
		
		removeCharacter = new DIYButton(StringUtil.getUiLabel("mm.remove.character"));
		removeCharacter.setTooltip(StringUtil.getUiLabel("mm.remove.character.tooltip"));
		removeCharacter.addActionListener(this);

		loadGame = new DIYButton(StringUtil.getUiLabel("mm.load.game"));
		loadGame.setTooltip(StringUtil.getUiLabel("mm.load.game.tooltip"));
		loadGame.addActionListener(this);

		options = new DIYButton(StringUtil.getUiLabel("mm.settings"));
		options.setTooltip(StringUtil.getUiLabel("mm.settings.tooltip"));
		options.addActionListener(this);

		exit = new DIYButton(StringUtil.getUiLabel("mm.quit"));
		exit.addActionListener(this);
		
		menu.add(quickStart);
		menu.add(startGame);
		menu.add(createCharacter);
		menu.add(addCharacter);
		menu.add(removeCharacter);
		menu.add(loadGame);
		menu.add(options);
		menu.add(new DIYLabel());
		menu.add(exit);
		
		DIYLabel versionLabel = new DIYLabel();
		versionLabel.setBounds(DiyGuiUserInterface.SCREEN_WIDTH-100, DiyGuiUserInterface.SCREEN_HEIGHT-25, 60, 15);
		versionLabel.setForegroundColour(Constants.Colour.GOLD);
		versionLabel.setText(Maze.getInstance().getAppConfig().get("mclachlan.maze.version"));
		
		setBackgroundImage(DIYToolkit.getInstance().getRendererProperties().getImageResource("screen/main_menu_back"));

		add(menu);
		add(versionLabel);
		
		updateState();
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		if (event.getSource() == startGame)
		{
			startGame();
			return true;
		}
		else if (event.getSource() == quickStart)
		{
			quickStart();
			return true;
		}
		else if (event.getSource() == createCharacter)
		{
			createCharacter();
			return true;
		}
		else if (event.getSource() == exit)
		{
			quit();
			return true;
		}
		else if (event.getSource() == addCharacter)
		{
			addCharacter();
			return true;
		}
		else if (event.getSource() == loadGame)
		{
			saveOrLoad();
			return true;
		}
		else if (event.getSource() == removeCharacter)
		{
			removeCharacter();
			return true;
		}
		else if (event.getSource() == options)
		{
			showSettingsDialog();
			return true;
		}
		
		updateState();
		return false;
	}

	/*-------------------------------------------------------------------------*/
	public void removeCharacter()
	{
		if (removeCharacter.isEnabled())
		{
			DiyGuiUserInterface.instance.chooseACharacter(this);
		}
	}

	public void saveOrLoad()
	{
		if (loadGame.isEnabled())
		{
			Maze.getInstance().setState(Maze.State.SAVE_LOAD);
		}
	}

	public void addCharacter()
	{
		Maze.getInstance().getUi().showDialog(
			new GuildDisplayDialog(
				Database.getInstance().getCharacterGuild(),
				this));
	}

	public void quit()
	{
		if (exit.isEnabled())
		{
			Maze.getInstance().setState(Maze.State.FINISHED);
		}
	}

	public void createCharacter()
	{
		if (createCharacter.isEnabled())
		{
			Maze.getInstance().setState(Maze.State.CREATE_CHARACTER);
		}
	}

	public void quickStart()
	{
		Maze.getInstance().quickStart();
	}

	public void startGame()
	{
		if (startGame.isEnabled())
		{
			Maze.getInstance().getUi().showDialog(new StartGameOptionsDialog(this));
		}
	}

	public void showSettingsDialog()
	{
		Maze.getInstance().getUi().showDialog(new SettingsDialog());
	}

	/*-------------------------------------------------------------------------*/
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		Maze.getInstance().removePlayerCharacterFromParty(pc);
		updateState();
		return true;
	}
	
	/*-------------------------------------------------------------------------*/
	/**
	 * Enable and disable various buttons based on guild and party state.
	 */
	public void updateState()
	{
		PlayerParty party = Maze.getInstance().getParty();
		List<PlayerCharacter> guild = new ArrayList<>(
			Database.getInstance().getCharacterGuild().values());
		boolean partyFull = false;
		
		if (party != null)
		{
			guild.removeAll(party.getActors());
			partyFull = party.getActors().size() == 6;
		}

		// Start game only enabled if there is a party
		startGame.setEnabled(party != null && !party.getActors().isEmpty());
		
		// Retainer only enabled if retainers in the guild
		addCharacter.setEnabled(guild.size() > 0 && !partyFull);
		
		// Remove only enabled if there is a party
		removeCharacter.setEnabled(party != null && !party.getActors().isEmpty());
		
		// Load only enabled if there are saved games
		loadGame.setEnabled(!Database.getInstance().getLoader().getSaveGames().isEmpty());
	}

	/*-------------------------------------------------------------------------*/
	public void createCharacter(int createPrice)
	{
		// not applicable to guild dialog
	}

	/*-------------------------------------------------------------------------*/
	public void transferPlayerCharacterToParty(PlayerCharacter pc, int recruitPrice)
	{
		PlayerCharacter playerCharacter = pc;
		if (pc != null)
		{
			// in this case the player character has been taken from a game guild,
			// not an NPC guild, so for consistency we clone the character
			playerCharacter = new PlayerCharacter(pc);
			Maze.getInstance().addPlayerCharacterToParty(playerCharacter);
		}

		SpeechUtil.getInstance().genericSpeech(
			Personality.BasicSpeech.CHARACTER_RECRUITED.getKey(),
			playerCharacter,
			playerCharacter.getPersonality(),
			Maze.getInstance().getUi().getPlayerCharacterWidgetBounds(playerCharacter),
			null);

		updateState();
	}

	/*-------------------------------------------------------------------------*/
	public void removeFromParty(PlayerCharacter pc, int recruitPrice)
	{
		// not applicable to guild dialog
	}

	/*-------------------------------------------------------------------------*/
	public void startGame(String difficultyLevel)
	{
		if (startGame.isEnabled())
		{
			Maze.getInstance().startGame(difficultyLevel);
		}
	}
}
