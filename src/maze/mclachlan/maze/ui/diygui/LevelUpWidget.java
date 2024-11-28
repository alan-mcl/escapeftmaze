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
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.GameSys;
import mclachlan.maze.stat.Leveler;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 * There is no fun way to write a UI.
 */
public class LevelUpWidget extends ContainerWidget implements ActionListener
{
	private final RendererProperties rp;
	private final int buttonPaneHeight;

	private DIYButton next, previous, showClassChangeReq;
	private DIYPane buttonPane;
	private CardLayoutWidget cardLayout;

	private DIYPane bonusesPane;
	private DIYPane modifiersPane;
	private DIYPane spellsPane;

	private PlayerCharacter playerCharacter;

	private final Leveler leveler = new Leveler();
	private Leveler.LevelUpState levelUpState;

	// states
	private int state;
	private static final int CHOOSE_BONUS = 1;
	private static final int EDIT_MODIFIERS = 2;
	private static final int CHOOSE_SPELLS = 3;
	private static final int FINISHED = 4;

	// bonuses pane stuff
	private DIYLabel levelUpStateTitle;
	private DIYTextArea levelUpStateText;
	private DIYListBox bonuses;
	private Map<String, ContainerWidget> bonusDetailsMap;
	private CardLayoutWidget bonusDetails;
	private boolean appliedBonus;

	// modifiers edit pane
	private ModifiersEditWidget editModifiers;
	private boolean appliedModifiers;

	// spell selection pane
	private SpellLearningWidget spellLearner;
	private boolean appliedSpells;
	private DIYLabel title;

	/*-------------------------------------------------------------------------*/
	public LevelUpWidget(Rectangle bounds)
	{
		super(bounds);

		rp = DIYToolkit.getInstance().getRendererProperties();
		buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT) +
			rp.getProperty(RendererProperties.Property.INSET);

		buildGui(bounds);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds)
	{
		next = new DIYButton(StringUtil.getUiLabel("lu.next"));
		previous = new DIYButton(StringUtil.getUiLabel("lu.previous"));
		next.addActionListener(this);
		previous.addActionListener(this);
		showClassChangeReq = new DIYButton(StringUtil.getUiLabel("lu.class.change.req"));
		showClassChangeReq.addActionListener(this);

		int inset = rp.getProperty(RendererProperties.Property.INSET);
		DIYPane titlePane = new DIYPane(new DIYFlowLayout(7,7, DIYToolkit.Align.CENTER));
		title = new DIYLabel(StringUtil.getUiLabel("lu.title", ""), DIYToolkit.Align.CENTER);
		titlePane.setBounds(
			x + inset,
			y + inset,
			width,
			30);
		title.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		title.setFont(f);
		titlePane.add(title);
		this.add(titlePane);

		buttonPane = new DIYPane(new DIYFlowLayout(15,0,DIYToolkit.Align.RIGHT));
		buttonPane.setInsets(new Insets(5,0,5,20));
		buttonPane.setBounds(0, height -buttonPaneHeight -inset*2, width, buttonPaneHeight+inset);

		buttonPane.add(showClassChangeReq);
		buttonPane.add(previous);
		buttonPane.add(next);

		bonusesPane = getBonusesPane();
		modifiersPane = getModifiersPane();
		spellsPane = getSpellsPane();

		ArrayList<ContainerWidget> cards = new ArrayList<>();

		cards.add(bonusesPane);
		cards.add(modifiersPane);
		cards.add(spellsPane);

		cardLayout = new CardLayoutWidget(bounds, cards);
		this.add(cardLayout);
	}

	/*-------------------------------------------------------------------------*/
	void refresh(PlayerCharacter pc)
	{
		this.playerCharacter = pc;

		title.setText(StringUtil.getUiLabel("lu.title", pc.getName()));
		levelUpStateTitle.setText(StringUtil.getUiLabel("lu.level.up.inc", this.playerCharacter.getLevel() + 1));

		levelUpState = new Leveler.LevelUpState(pc, 0);

		setLevelUpStateText();

		appliedBonus = false;
		appliedModifiers = false;
		appliedSpells = false;
		levelUpState.setExtraAssignableModifiers(0);

		// reset bonus state
		ArrayList<String> availableBonuses = new ArrayList<>();
		availableBonuses.add(Leveler.BONUS_HIT_POINTS);
		availableBonuses.add(Leveler.BONUS_ACTION_POINTS);
		availableBonuses.add(Leveler.BONUS_MAGIC_POINTS);
		List<Stats.Modifier> raisableAttributes = pc.getRaisableAttributes();
		if (raisableAttributes.size() > 0)
		{
			List<ModifierListItem> items = new ArrayList<>();
			for (Stats.Modifier s : raisableAttributes)
			{
				items.add(new ModifierListItem(s));
			}
			availableBonuses.add(Leveler.BONUS_ATTRIBUTE);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.BONUS_ATTRIBUTE);
			lb.setItems(items);
			lb.setSelected(items.get(0));
		}
		availableBonuses.add(Leveler.BONUS_MODIFIERS);
		availableBonuses.add(Leveler.BONUS_SPELL_PICK);
		List<Stats.Modifier> unlockableModifiers = pc.getUnlockableModifiers();
		if (unlockableModifiers.size() > 0)
		{
			List<ModifierListItem> items = new ArrayList<>();
			for (Stats.Modifier s : unlockableModifiers)
			{
				items.add(new ModifierListItem(s));
			}
			availableBonuses.add(Leveler.UNLOCK_MODIFIER);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_MODIFIER);
			lb.setItems(items);
			lb.setSelected(items.get(0));
		}
		List<MagicSys.SpellBook> unlockableSpellLevels = pc.getUnlockableSpellLevels();
		if (unlockableSpellLevels.size() > 0)
		{
			List<SpellLevelListItem> items = new ArrayList<>();
			for (MagicSys.SpellBook sb : unlockableSpellLevels)
			{
				items.add(new SpellLevelListItem(sb, pc));
			}
			availableBonuses.add(Leveler.UNLOCK_SPELL_LEVEL);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_SPELL_LEVEL);
			lb.setItems(items);
			lb.setSelected(items.get(0));
		}
		availableBonuses.add(Leveler.REVITALISE);
		List<String> eligibleClasses = pc.getEligibleClasses();
		if (eligibleClasses.size() > 0)
		{
			availableBonuses.add(Leveler.CHANGE_CLASS);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.CHANGE_CLASS);
			lb.setItems(eligibleClasses);
			lb.setSelected(eligibleClasses.get(0));
		}
		List<String> eligibleSignatureWeapons = pc.getEligibleSignatureWeapons();
		if (eligibleSignatureWeapons.size() > 0)
		{
			availableBonuses.add(Leveler.UPGRADE_SIGNATURE_WEAPON);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.UPGRADE_SIGNATURE_WEAPON);
			lb.setItems(eligibleSignatureWeapons);
			lb.setSelected(eligibleSignatureWeapons.get(0));
		}
		List<String> eligibleModifierUpgrades = pc.getEligibleModifierUpgrades();
		if (eligibleModifierUpgrades.size() > 0)
		{
			availableBonuses.add(Leveler.MODIFIER_UPGRADE);
			DIYListBox lb = (DIYListBox)bonusDetailsMap.get(Leveler.MODIFIER_UPGRADE);
			lb.setItems(eligibleModifierUpgrades);
			lb.setSelected(eligibleModifierUpgrades.get(0));
		}

		bonuses.setItems(availableBonuses);

		// reset state
		this.state = CHOOSE_BONUS;
		updateState();
		this.cardLayout.show(bonusesPane);
		bonuses.setSelected(Leveler.BONUS_HIT_POINTS);
		bonusDetails.show(bonusDetailsMap.get(Leveler.BONUS_HIT_POINTS));
	}

	/*-------------------------------------------------------------------------*/

	private void setLevelUpStateText()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(StringUtil.getUiLabel("lu.state.desc",
			levelUpState.getHpInc(), levelUpState.getApInc(),
			levelUpState.getMpInc(), levelUpState.getExtraAssignableModifiers()));

		if (levelUpState.getSpellPicksInc() > 0)
		{
			sb.append("\n")
				.append(StringUtil.getUiLabel("lu.state.desc.spellpicks", levelUpState.getSpellPicksInc()));
		}

		// todo character class bonuses

		levelUpStateText.setText(sb.toString());
	}

	/*-------------------------------------------------------------------------*/
	private void applyInitialChanges(PlayerCharacter pc)
	{
		leveler.applyInitialChanges(pc, levelUpState);
	}

	/*-------------------------------------------------------------------------*/
	private void rollbackInitialChanges()
	{
		leveler.rollbackInitialChanges(playerCharacter, levelUpState);
	}

	/*-------------------------------------------------------------------------*/
	private void applyModifiers()
	{
		this.leveler.applyModifiers(playerCharacter, editModifiers.getStatModifier());
		this.appliedModifiers = true;
	}

	/*-------------------------------------------------------------------------*/
	private void rollbackModifiers()
	{
		this.leveler.rollbackModifiers(playerCharacter, editModifiers.getStatModifier());
		this.appliedModifiers = false;
	}

	/*-------------------------------------------------------------------------*/
	private void applyBonus(PlayerCharacter pc, String bonus)
	{
		Object bonusDetails = null;

		if (bonus.equalsIgnoreCase(Leveler.UNLOCK_MODIFIER))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_MODIFIER);
			bonusDetails = ((ModifierListItem)list.getSelected()).modifier;
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_ATTRIBUTE))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.BONUS_ATTRIBUTE);
			bonusDetails = ((ModifierListItem)list.getSelected()).modifier;
		}
		else if (bonus.equalsIgnoreCase(Leveler.UNLOCK_SPELL_LEVEL))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_SPELL_LEVEL);
			bonusDetails = ((SpellLevelListItem)list.getSelected()).book;
		}
		else if (bonus.equalsIgnoreCase(Leveler.CHANGE_CLASS))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.CHANGE_CLASS);
			bonusDetails = (String)list.getSelected();
		}
		else if (bonus.equalsIgnoreCase(Leveler.UPGRADE_SIGNATURE_WEAPON))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UPGRADE_SIGNATURE_WEAPON);
			bonusDetails = (String)list.getSelected();
		}
		else if (bonus.equalsIgnoreCase(Leveler.MODIFIER_UPGRADE))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.MODIFIER_UPGRADE);
			bonusDetails = (String)list.getSelected();
		}

		this.leveler.applyBonus(pc, levelUpState, bonus, bonusDetails);
		this.appliedBonus = true;
	}

	/*-------------------------------------------------------------------------*/
	private void rollbackBonus(PlayerCharacter pc, String bonus)
	{
		Object bonusDetails = null;

		if (bonus.equalsIgnoreCase(Leveler.UNLOCK_MODIFIER))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_MODIFIER);
			bonusDetails = ((ModifierListItem)list.getSelected()).modifier;
		}
		else if (bonus.equalsIgnoreCase(Leveler.BONUS_ATTRIBUTE))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.BONUS_ATTRIBUTE);
			bonusDetails = ((ModifierListItem)list.getSelected()).modifier;
		}
		else if (bonus.equalsIgnoreCase(Leveler.UNLOCK_SPELL_LEVEL))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UNLOCK_SPELL_LEVEL);
			bonusDetails = ((SpellLevelListItem)list.getSelected()).book;
		}
		else if (bonus.equalsIgnoreCase(Leveler.CHANGE_CLASS))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.CHANGE_CLASS);
			bonusDetails = (String)list.getSelected();
		}
		else if (bonus.equalsIgnoreCase(Leveler.UPGRADE_SIGNATURE_WEAPON))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.UPGRADE_SIGNATURE_WEAPON);
			bonusDetails = (String)list.getSelected();
		}
		else if (bonus.equalsIgnoreCase(Leveler.MODIFIER_UPGRADE))
		{
			DIYListBox list = (DIYListBox)bonusDetailsMap.get(Leveler.MODIFIER_UPGRADE);
			bonusDetails = (String)list.getSelected();
		}

		this.leveler.rollbackBonus(pc, levelUpState, bonus, bonusDetails);

		this.appliedBonus = false;
	}

	/*-------------------------------------------------------------------------*/
	private void applySpells()
	{
		this.leveler.applySpells(playerCharacter, this.spellLearner.getSelectedSpells());
	}

	/*-------------------------------------------------------------------------*/
	private void rollbackSpells()
	{
		this.leveler.rollbackSpells(playerCharacter, this.spellLearner.getSelectedSpells());
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getSpellsPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-2*inset)/3;

		int contentTop = 75;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int contentHeight = height - buttonPaneHeight - 100;

		DIYPane spellsPane = new DIYPane();

		DIYPanel spellsPanel = getFixedPanel(column1x, contentTop, columnWidth * 3, contentHeight);
		spellsPanel.setLayoutManager(null);

		DIYLabel spellsTitle = getSubTitle(StringUtil.getUiLabel("lu.select.spells"));
		spellsTitle.setAlignment(DIYToolkit.Align.LEFT);
		spellsTitle.setBounds(
			spellsPanel.x +panelBorderInset,
			spellsPanel.y +panelBorderInset,
			columnWidth, 20);

		spellLearner = new SpellLearningWidget(null,
			new Rectangle(
				spellsPanel.x +panelBorderInset,
				spellsPanel.y +panelBorderInset,
				spellsPanel.width -panelBorderInset*2,
				spellsPanel.height -panelBorderInset*2));

		spellsPanel.add(spellsTitle);
		spellsPanel.add(spellLearner);

//		spellsPane.add(spellsTitle);
//		spellsPane.add(spellLearner);
		spellsPane.add(spellsPanel);
		spellsPane.add(buttonPane);

		return spellsPane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getModifiersPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-2*inset)/3;

		int contentTop = 75;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int contentHeight = height - buttonPaneHeight - 100;

		DIYPane modifiersPane = new DIYPane();

		DIYPanel modifiersEditPanel = getFixedPanel(column1x, contentTop, columnWidth * 3, contentHeight);
		modifiersEditPanel.setLayoutManager(null);

		editModifiers = new ModifiersEditWidget(
			modifiersEditPanel.x +panelBorderInset,
			modifiersEditPanel.y +panelBorderInset +inset,
			modifiersEditPanel.width -panelBorderInset*2,
			modifiersEditPanel.height -panelBorderInset*2 -inset ,
			this, false, leveler);

		modifiersEditPanel.add(editModifiers);

		modifiersPane.add(modifiersEditPanel);
		modifiersPane.add(buttonPane);

		return modifiersPane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getBonusesPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/3;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;

		int contentTop = 75;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int titleHeight = 20;
		int contentHeight = height - buttonPaneHeight - 200;

		DIYPane bonusesPane = new DIYPane();

		DIYPanel levelUpStatePanel = getFixedPanel(column1x, contentTop, columnWidth, contentHeight);
		levelUpStatePanel.setLayoutManager(null);

		levelUpStateTitle = getSubTitle(StringUtil.getUiLabel("lu.level.up.inc", ""));
		levelUpStateTitle.setBounds(levelUpStatePanel.x, levelUpStatePanel.y +panelBorderInset, columnWidth, titleHeight);

		levelUpStateText = new DIYTextArea("");
		levelUpStateText.setTransparent(true);
		levelUpStateText.setBounds(
			levelUpStatePanel.x +panelBorderInset,
			levelUpStateTitle.y +levelUpStateTitle.height +inset,
			levelUpStatePanel.width -panelBorderInset*2,
			levelUpStatePanel.height -panelBorderInset*2 -levelUpStateTitle.height -inset);

		levelUpStatePanel.add(levelUpStateTitle);
		levelUpStatePanel.add(levelUpStateText);

		DIYPanel bonusListPanel = getFixedPanel(column2x, contentTop, columnWidth, contentHeight);
		bonusListPanel.setLayoutManager(null);

		DIYLabel bonusesTitle = getSubTitle(StringUtil.getUiLabel("lu.bonus.title"));
		bonusesTitle.setBounds(bonusListPanel.x, bonusListPanel.y +panelBorderInset, columnWidth, titleHeight);

		bonuses = new DIYListBox(new ArrayList<String>());
		bonuses.setBounds(
			bonusListPanel.x +panelBorderInset,
			contentTop +panelBorderInset +bonusesTitle.height +inset/2,
			columnWidth -panelBorderInset*2,
			contentHeight -panelBorderInset*2 -bonusesTitle.height);
		bonuses.addActionListener(this);

		bonusListPanel.add(bonusesTitle);
		bonusListPanel.add(bonuses);

		DIYPanel bonusDetailsPanel = getFixedPanel(column3x, contentTop, columnWidth, contentHeight);
		bonusDetailsPanel.setLayoutManager(null);

		Rectangle bonusDetailsBounds = new Rectangle(
			bonusDetailsPanel.x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			contentHeight -panelBorderInset*2);
		bonusDetailsMap = new HashMap<>();

		bonusDetailsMap.put(Leveler.BONUS_HIT_POINTS, getLabelPane(StringUtil.getUiLabel("lu.bonus.hp"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_ACTION_POINTS, getLabelPane(StringUtil.getUiLabel("lu.bonus.ap"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_MAGIC_POINTS, getLabelPane(StringUtil.getUiLabel("lu.bonus.mp"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_ATTRIBUTE, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_MODIFIERS, getLabelPane(StringUtil.getUiLabel("lu.bonus.mod"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_SPELL_PICK, getLabelPane(StringUtil.getUiLabel("lu.bonus.spell"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.REVITALISE, getLabelPane(StringUtil.getUiLabel("lu.bonus.revitalise"), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.CHANGE_CLASS, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UNLOCK_MODIFIER, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UNLOCK_SPELL_LEVEL, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UPGRADE_SIGNATURE_WEAPON, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.MODIFIER_UPGRADE, new DIYListBox(new ArrayList<String>(), bonusDetailsBounds));

		ArrayList<ContainerWidget> widgets = new ArrayList<>(bonusDetailsMap.values());

		bonusDetails = new CardLayoutWidget(bonusDetailsBounds, widgets);

		bonusDetailsPanel.add(bonusDetails);

		bonusesPane.add(levelUpStatePanel);
		bonusesPane.add(bonusListPanel);
		bonusesPane.add(bonusDetailsPanel);
		bonusesPane.add(buttonPane);

		return bonusesPane;
	}

	/*-------------------------------------------------------------------------*/
	DIYPane getLabelPane(String s, Rectangle bounds)
	{
		DIYPane result = new DIYPane();
		result.setBounds(bounds);
		DIYTextArea text = new DIYTextArea(s);
		text.setTransparent(true);
		text.setBounds(bounds);
		result.add(text);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPanel getFixedPanel(int panelX, int panelY, int panelWidth, int panelHeight)
	{
		DIYPanel result = new DIYPanel(panelX, panelY, panelWidth, panelHeight);
		result.setLayoutManager(new DIYFlowLayout(0,0, DIYToolkit.Align.LEFT));
		result.setStyle(DIYPanel.Style.PANEL_MED);
		result.setInsets(new Insets(25, 25, 25, 25));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize() + 3);
		title.setFont(f);
		return title;
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.NONE;
	}

	/*-------------------------------------------------------------------------*/
	private boolean mustChooseSpells()
	{
		return playerCharacter.getSpellsThatCanBeLearned().size() > 0 &&
			playerCharacter.getSpellPicks() > 0;
	}

	/*-------------------------------------------------------------------------*/
	private void updateState()
	{
		DiyGuiUserInterface.instance.refreshCharacterData();

		if (state <= 0)
		{
			finished();
			return;
		}
		else if (state == CHOOSE_BONUS)
		{
			this.previous.setText("Cancel");
		}
		else
		{
			this.previous.setText("< Previous");
		}

		switch (state)
		{
			case CHOOSE_BONUS ->
			{
				if (appliedBonus)
				{
					rollbackBonus(playerCharacter, (String)bonuses.getSelected());
					rollbackInitialChanges();
				}
				if (appliedModifiers)
				{
					rollbackModifiers();
				}
				this.cardLayout.show(bonusesPane);
			}
			case EDIT_MODIFIERS ->
			{
				if (!appliedBonus)
				{
					applyBonus(playerCharacter, (String)bonuses.getSelected());
					applyInitialChanges(playerCharacter);
				}
				if (appliedSpells)
				{
					rollbackSpells();
				}
				if (appliedModifiers)
				{
					rollbackModifiers();
				}
				int assignable = leveler.getAssignableModifiers(playerCharacter, levelUpState);
				editModifiers.refresh(
					playerCharacter,
					assignable,
					GameSys.getInstance().getMaxAssignableToAModifierOnLevelUp());
				this.cardLayout.show(modifiersPane);
			}
			case CHOOSE_SPELLS ->
			{
				if (!appliedModifiers)
				{
					applyModifiers();
				}
				this.spellLearner.refresh(playerCharacter);
				this.cardLayout.show(spellsPane);
			}
			case FINISHED ->
			{
				if (!appliedSpells)
				{
					applySpells();
				}
				finished();
			}
			default -> throw new MazeException("Illegal state: " + state);
		}

		if (state == EDIT_MODIFIERS && !mustChooseSpells() ||
			state == CHOOSE_SPELLS)
		{
			next.setText("Finish");
		}
		else
		{
			next.setText("Next >");
			next.setActionMessage(null);
		}

		this.next.setEnabled(this.canProceed());
	}

	/*-------------------------------------------------------------------------*/
	private void finished()
	{
		Maze.getInstance().setState(Maze.State.MOVEMENT);
	}

	/*-------------------------------------------------------------------------*/
	private boolean canProceed()
	{
		switch (state)
		{
			case 0:
				// ignore
				return true;
			case CHOOSE_BONUS:
			{
				// ok as long as the list boxes always have a default selection
				return true;
			}
			case EDIT_MODIFIERS:
			{
				// gotta assign all modifiers
				return editModifiers.getBonuses().getCurrent() == 0;
			}
			case CHOOSE_SPELLS:
			{
				// can save spell picks
				return true;
			}
			case FINISHED:
				return true;
			default:
				throw new MazeException("Illegal state: " + state);
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == bonuses)
		{
			bonusDetails.show(bonusDetailsMap.get((String)bonuses.getSelected()));
			return true;
		}
		else if (obj == next)
		{
			this.state++;
			updateState();
			return true;
		}
		else if (obj == previous)
		{
			this.state--;
			updateState();
			return true;
		}
		else if (obj == showClassChangeReq)
		{
			int x = 10;
			int y = 10;
			Rectangle rectangle = new Rectangle(x, y,
				DiyGuiUserInterface.SCREEN_WIDTH - 20, DiyGuiUserInterface.SCREEN_HEIGHT - 20);

			ContainerWidget c = new ClassChangeRequirementsWidget(playerCharacter);
			Maze.getInstance().getUi().showDialog(new ContainerDialog(
				"Class Change Requirements", c, rectangle));

			return true;
		}

		this.next.setEnabled(this.canProceed());
		return false;
	}

	/*-------------------------------------------------------------------------*/
	static class ModifierListItem
	{
		Stats.Modifier modifier;
		String displayName;

		public ModifierListItem(Stats.Modifier modifier)
		{
			this.modifier = modifier;
			this.displayName = StringUtil.getModifierName(modifier);
		}

		public String toString()
		{
			return displayName;
		}
	}

	/*-------------------------------------------------------------------------*/
	static class SpellLevelListItem
	{
		MagicSys.SpellBook book;
		PlayerCharacter pc;
		String display;

		public SpellLevelListItem(MagicSys.SpellBook book, PlayerCharacter pc)
		{
			this.book = book;
			this.pc = pc;
			display = book.getName();//+" (max spell level "+(pc.getSpellBook().getLimit(book)+1)+")";
		}

		public String toString()
		{
			return display;
		}
	}
}
