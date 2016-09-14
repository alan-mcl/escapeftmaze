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
	private DIYButton next, previous, showClassChangeReq;
	private DIYPane buttonPane;
	private CardLayoutWidget cardLayout;
	private static int buttonPaneHeight = 50;

	private DIYPane bonusesPane;
	private DIYPane modifiersPane;
	private DIYPane spellsPane;

	private PlayerCharacter playerCharacter;

	private Leveler leveler = new Leveler();
	private Leveler.LevelUpState levelUpState;

	// states
	private int state;
	private static final int CHOOSE_BONUS = 1;
	private static final int EDIT_MODIFIERS = 2;
	private static final int CHOOSE_SPELLS = 3;
	private static final int FINISHED = 4;

	// bonuses pane stuff
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
		buildGui(bounds);
	}

	/*-------------------------------------------------------------------------*/
	private void buildGui(Rectangle bounds)
	{
		DIYPane titlePane = new DIYPane(new DIYFlowLayout(7, 7, DIYToolkit.Align.CENTER));
		title = new DIYLabel("", DIYToolkit.Align.CENTER);
		titlePane.setBounds(x, y, width, 30);
		title.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		title.setFont(f);
		titlePane.add(title);
		this.add(titlePane);

		next = new DIYButton("Next >");
		previous = new DIYButton("< Previous");
		next.addActionListener(this);
		previous.addActionListener(this);

		showClassChangeReq = new DIYButton("Show Class Change Requirements");
		showClassChangeReq.addActionListener(this);

		buttonPane = new DIYPane(new DIYFlowLayout(15,10,DIYToolkit.Align.RIGHT));
		buttonPane.setInsets(new Insets(0, 0, 0, 20));
		buttonPane.setBounds(0, height - buttonPaneHeight, width, buttonPaneHeight);

		buttonPane.add(showClassChangeReq);
		buttonPane.add(previous);
		buttonPane.add(next);

		bonusesPane = getBonusesPane();
		modifiersPane = getModifiersPane();
		spellsPane = getSpellsPane();

		ArrayList<ContainerWidget> cards = new ArrayList<ContainerWidget>();

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

		title.setText("Level Up "+pc.getName());

		levelUpState = new Leveler.LevelUpState(pc, 0);

		appliedBonus = false;
		appliedModifiers = false;
		appliedSpells = false;
		levelUpState.setExtraAssignableModifiers(0);

		// reset bonus state
		ArrayList<String> availableBonuses = new ArrayList<String>();
		availableBonuses.add(Leveler.BONUS_HIT_POINTS);
		availableBonuses.add(Leveler.BONUS_ACTION_POINTS);
		availableBonuses.add(Leveler.BONUS_MAGIC_POINTS);
		List<Stats.Modifier> raisableAttributes = pc.getRaisableAttributes();
		if (raisableAttributes.size() > 0)
		{
			List<ModifierListItem> items = new ArrayList<ModifierListItem>();
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
			List<ModifierListItem> items = new ArrayList<ModifierListItem>();
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
			List<SpellLevelListItem> items = new ArrayList<SpellLevelListItem>();
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

		bonuses.setItems(availableBonuses);

		// reset state
		this.state = CHOOSE_BONUS;
		updateState();
		this.cardLayout.show(bonusesPane);
		bonuses.setSelected(Leveler.BONUS_HIT_POINTS);
		bonusDetails.show(bonusDetailsMap.get(Leveler.BONUS_HIT_POINTS));
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
		int inset = 5;
		int columnWidth = width/3 - 2*inset;
		int column1 = 5;

		DIYPane spellsPane = new DIYPane();

		DIYLabel spellsTitle = getSubTitle("Select Spells");
		spellsTitle.setBounds(column1, 50, columnWidth, 20);

		spellLearner = new SpellLearningWidget(null,
					new Rectangle(column1, 75, columnWidth*3, height-buttonPaneHeight-75));

		spellsPane.add(spellsTitle);
		spellsPane.add(spellLearner);
		spellsPane.add(buttonPane);

		return spellsPane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getModifiersPane()
	{
		int inset = 5;
		int columnWidth = width/3 - 2*inset;
		int column1 = 5;

		DIYPane modifiersPane = new DIYPane();

		editModifiers = new ModifiersEditWidget(
			column1, 75, columnWidth*3, height-buttonPaneHeight-75, this, false, leveler);
		modifiersPane.add(editModifiers);
		modifiersPane.add(buttonPane);

		return modifiersPane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getBonusesPane()
	{
		int inset = 10;
		int columnWidth = width/3 - 2*inset;
		int column1 = width/6 - inset/2;
		int column2 = column1 + columnWidth + inset;

		DIYPane bonusesPane = new DIYPane();
		Rectangle bonusDetailsBounds = new Rectangle(column2, 75, columnWidth, height-buttonPaneHeight-75);

		DIYLabel bonusesTitle = getSubTitle("Choose a Bonus");
		bonusesTitle.setBounds(column1, 50, columnWidth, 20);

		bonuses = new DIYListBox(new ArrayList());
		bonuses.setBounds(column1, 75, columnWidth, height-buttonPaneHeight-75);
		bonuses.addActionListener(this);
		bonusDetailsMap = new HashMap<String, ContainerWidget>();

		bonusDetailsMap.put(Leveler.BONUS_HIT_POINTS, getLabelPane("Grants this character " +
			"bonus hit points, over and above the regular hit point increase for " +
			"this level.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_ACTION_POINTS, getLabelPane("Grants this character " +
			"bonus action points, over and above the regular action point increase for " +
			"this level.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_MAGIC_POINTS, getLabelPane("Grants this character " +
			"bonus magic points, over and above the regular magic point increase for " +
			"this level.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_ATTRIBUTE, new DIYListBox(new ArrayList(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_MODIFIERS, getLabelPane("Grants this character " +
			"bonus modifiers that can be assigned later in this level up.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.BONUS_SPELL_PICK, getLabelPane("Grants this character " +
			"a bonus spell choice.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.REVITALISE, getLabelPane("Restores this characters " +
			"hit, stealth amd magic points to their maximum and removes most " +
			"baneful conditions.", bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.CHANGE_CLASS, new DIYListBox(new ArrayList(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UNLOCK_MODIFIER, new DIYListBox(new ArrayList(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UNLOCK_SPELL_LEVEL, new DIYListBox(new ArrayList(), bonusDetailsBounds));
		bonusDetailsMap.put(Leveler.UPGRADE_SIGNATURE_WEAPON, new DIYListBox(new ArrayList(), bonusDetailsBounds));

		ArrayList<ContainerWidget> widgets = new ArrayList<ContainerWidget>();
		widgets.addAll(bonusDetailsMap.values());

		bonusDetails = new CardLayoutWidget(bonusDetailsBounds, widgets);

		bonusesPane.add(bonusesTitle);
		bonusesPane.add(bonuses);
		bonusesPane.add(bonusDetails);
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
	private DIYLabel getSubTitle(String titleText)
	{
		DIYLabel title = new DIYLabel(titleText);
		title.setForegroundColour(GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.PLAIN, defaultFont.getSize()+3);
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
			return;
		}
		else if (state == CHOOSE_BONUS)
		{
			this.previous.setText("   Cancel   ");
			previous.setActionMessage(Constants.Messages.BACK_TO_GAME);
		}
		else
		{
			this.previous.setText("< Previous");
			previous.setActionMessage(null);
		}

		switch (state)
		{
			case CHOOSE_BONUS:
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
				break;
			case EDIT_MODIFIERS:
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
				break;
			case CHOOSE_SPELLS:
				if (!appliedModifiers)
				{
					applyModifiers();
				}
				this.spellLearner.refresh(playerCharacter);
				this.cardLayout.show(spellsPane);
				break;
			case FINISHED:
				if (!appliedSpells)
				{
					applySpells();
				}
				break;
			default: throw new MazeException("Illegal state: "+state);
		}

		if (state == EDIT_MODIFIERS && !mustChooseSpells() ||
			state == CHOOSE_SPELLS)
		{
			next.setText("Finish");
			next.setActionMessage(Constants.Messages.BACK_TO_GAME);
		}
		else
		{
			next.setText("Next >");
			next.setActionMessage(null);
		}

		this.next.setEnabled(this.canProceed());
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
			default: throw new MazeException("Illegal state: "+state);
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		if (obj == bonuses)
		{
			bonusDetails.show(bonusDetailsMap.get((String)bonuses.getSelected()));
		}
		else if (obj == next)
		{
			this.state++;
			updateState();
		}
		else if (obj == previous)
		{
			this.state--;
			updateState();
		}
		else if (obj == showClassChangeReq)
		{
			int x = 10;
			int y = 10;
			Rectangle rectangle = new Rectangle(x, y,
				DiyGuiUserInterface.SCREEN_WIDTH-20, DiyGuiUserInterface.SCREEN_HEIGHT-20);

			ContainerWidget c = new ClassChangeRequirementsWidget(playerCharacter);
			Maze.getInstance().getUi().showDialog(new ContainerDialog(
				"Class Change Requirements", c, rectangle));
		}

		this.next.setEnabled(this.canProceed());
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
