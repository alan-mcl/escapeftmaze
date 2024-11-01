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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import mclachlan.diygui.*;
import mclachlan.diygui.toolkit.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.ActorEncounter;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.MagicSys;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.stat.magic.SpellBook;
import mclachlan.maze.stat.npc.Npc;
import mclachlan.maze.ui.diygui.animation.SpeechBubble;
import mclachlan.maze.ui.diygui.render.maze.MazeRendererFactory;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class CreateCharacterWidget extends ContainerWidget implements ActionListener
{
	private final RendererProperties rp;
	private DIYButton next, previous;
	private CardLayoutWidget cardLayout;
	private int state = CHOOSE_RACE_AND_GENDER;

	private static final int CHOOSE_RACE_AND_GENDER = 1;
	private static final int CHOOSE_CLASS = 2;
	private static final int CHOOSE_KIT = 3;
	private static final int CHOOSE_SPELLS = 4;
	private static final int EDIT_PERSONALS = 5;
	private static final int FINISHED = 6;

	private static final String SET_GENDER = "Set Gender";

	private DIYPane raceAndGenderPane;
	private DIYPane personalsPane;
	private DIYPane classesPane;
	private DIYPane kitsPane;
	private DIYPane spellsPane;

	private DIYListBox races;
	private DIYListBox personalities;
	private DIYTextArea raceDesc;
	private DIYLabel raceImage;
	private DIYListBox characterClasses;
	private DIYTextArea classDesc, spellBooksDesc;
	private DIYTextArea kitDesc;
	private DIYPane kitItems;
	private CardLayoutWidget classAndRaceKitCards;
	private CardLayoutWidget raceGenderChoices;
	private final Map<String, ContainerWidget> raceGenderWidgets = new HashMap<>();
	private DIYTextField nameField;
	private DIYButton random, suggestName, showLevelAbilityProgression;
	private LevelAbilityProgressionWidget firstLevel;
	private final Map<MagicSys.SpellBook, List<Widget>> spellBookWidgets = new HashMap<>();
	private final Map<String, ContainerWidget> classAndRaceKitWidgets = new HashMap<>();
	private PortraitSelectionWidget portraitWidget;
	private ResourcesDisplayWidget raceResourcesWidget;
	private StatModifierDisplayWidget raceModifiersWidget1;
	private StatModifierDisplayWidget raceModifiersWidget2;
	private StatModifierDisplayWidget raceModifiersWidget3;
	private ResourcesDisplayWidget classResourcesWidget;
	private StatModifierDisplayWidget classModifiersWidget1;
	private StatModifierDisplayWidget classModifiersWidget2;
	private StatModifierDisplayWidget classModifiersWidget3;
	private ResourcesDisplayWidget kitResourcesWidget;
	private StatModifierDisplayWidget kitModifiersWidget1;
	private StatModifierDisplayWidget kitModifiersWidget2;
	private StatModifierDisplayWidget kitModifiersWidget3;
	private ResourcesDisplayWidget resourcesSummaryWidget;
	private StatModifierDisplayWidget modifiersSummaryWidget1;
	private StatModifierDisplayWidget modifiersSummaryWidget2;
	private StatModifierDisplayWidget modifiersSummaryWidget3;

	private final ActionListener kitListener = new KitActionListener();
	private final ActionListener itemWidgetListener = new ItemWidgetActionListener();

	private DIYLabel characterTitle;

	// selected stuff
	private PlayerCharacter playerCharacter;
	private String name;
	private CharacterClass characterClass;
	private Personality personality;
	private Race race;
	private Gender gender;
	private StartingKit startingKit;

	private DIYPane buttonPane, titlePane;
	private final int buttonPaneHeight;
	private final List<CharacterClassWrapper> characterClassList = getCharacterClassList();
	private SpellLearningWidget spellLearner;

	/** This is non-null if we're creating a character in a NPC guild. */
	private Npc npc;

	/*-------------------------------------------------------------------------*/
	public CreateCharacterWidget(Rectangle bounds)
	{
		super(bounds);

		rp = DIYToolkit.getInstance().getRendererProperties();
		buttonPaneHeight = rp.getProperty(RendererProperties.Property.BUTTON_PANE_HEIGHT) +
			rp.getProperty(RendererProperties.Property.INSET);

		this.buildGUI(bounds);
	}

	/*-------------------------------------------------------------------------*/
	public String getWidgetName()
	{
		return DIYToolkit.NONE;
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		this.playerCharacter = null;
		this.personality = null;
		this.name = null;
		this.characterClass = null;
		this.race = null;
		this.gender = null;
		this.startingKit = null;
		ActorEncounter currentActorEncounter = Maze.getInstance().getCurrentActorEncounter();
		if (currentActorEncounter != null)
		{
			this.npc = (Npc)currentActorEncounter.getLeader();
		}

		this.state = CHOOSE_RACE_AND_GENDER;
		updateState();

		this.nameField.setFocus(true);
		this.nameField.setText("");
		setDefaultSelectedRace(races.getItems());
		this.setRace((String)races.getSelected());
		portraitWidget.setToRaceAndGender(race.getName(), gender.getName(), portraitWidget.portraits);

		selectDefaultCharacterClass();

		setDefaultPersonality();
	}

	/*-------------------------------------------------------------------------*/
	private void selectDefaultCharacterClass()
	{
		// set to the first enabled selections
		for (Object obj : characterClasses.getItems())
		{
			if (characterClasses.isEnabled(obj))
			{
				characterClasses.setSelected(obj);
				break;
			}
		}

		this.setCharacterClass(((CharacterClassWrapper)characterClasses.getSelected()).characterClass.getName());
	}

	/*-------------------------------------------------------------------------*/
	private void buildGUI(Rectangle bounds)
	{
		next = new DIYButton(StringUtil.getUiLabel("cc.next"));
		previous = new DIYButton(StringUtil.getUiLabel("cc.previous"));
		next.addActionListener(this);
		previous.addActionListener(this);
		random = new DIYButton(StringUtil.getUiLabel("cc.random"));
		random.addActionListener(this);

		int inset = rp.getProperty(RendererProperties.Property.INSET);
		titlePane = new DIYPane(new DIYFlowLayout(7,7, DIYToolkit.Align.CENTER));
		DIYLabel title = new DIYLabel(StringUtil.getUiLabel("cc.title"), DIYToolkit.Align.CENTER);
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

		characterTitle = getSubTitle("");
		characterTitle.setBounds(
			x+100, titlePane.y +titlePane.height +inset, width-200, 20);

		this.add(characterTitle);

		buttonPane = new DIYPane(new DIYFlowLayout(15,0,DIYToolkit.Align.RIGHT));
		buttonPane.setInsets(new Insets(5,0,5,20));
		buttonPane.setBounds(0, height-buttonPaneHeight, width, buttonPaneHeight);

		buttonPane.add(previous);
		buttonPane.add(random);
		buttonPane.add(next);

		raceAndGenderPane = getRaceAndGenderPane();
		classesPane = getClassesPane();
		kitsPane = getKitsPane();
		spellsPane = getSpellsPane();
		personalsPane = getPersonalsPane();

		ArrayList<ContainerWidget> cards = new ArrayList<>();
		cards.add(raceAndGenderPane);
		cards.add(classesPane);
		cards.add(kitsPane);
		cards.add(spellsPane);
		cards.add(personalsPane);

		cardLayout = new CardLayoutWidget(bounds, cards);
		this.add(cardLayout);
		
		this.updateState();
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getSpellsPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/4;

		int column2x = column1x + columnWidth + inset;

		int headerOffset = 50;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int contentTop = headerOffset + 45 +panelBorderInset;
		int contentHeight = height -contentTop -buttonPaneHeight;

		DIYPane pane = new DIYPane();

		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1x, headerOffset, "cc.choose.spells.flava");

		DIYPanel spellPanel = getFixedPanel(column2x, contentTop, columnWidth*2 +inset, contentHeight);
		spellPanel.setLayoutManager(null);

		spellLearner = new SpellLearningWidget(null,
			new Rectangle(
				column2x +panelBorderInset,
				contentTop +panelBorderInset,
				columnWidth*2 +inset -panelBorderInset*2,
				contentHeight -panelBorderInset*2));

		spellPanel.add(spellLearner);

		pane.add(titlePane);
		pane.add(stepFlavour);
		pane.add(spellPanel);
		pane.add(buttonPane);

		return pane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getKitsPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/4;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;
		int column4x = column3x + columnWidth + inset;

		int headerOffset = 50;
		int contentTop = headerOffset + 50;
		int contentHeight = height -contentTop -buttonPaneHeight;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int titleHeight = 20;

		DIYPane pane = new DIYPane();

		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1x, headerOffset, "cc.choose.kit.flava");

		// column 1: kit list

		DIYLabel kitTitle = getSubTitle(getLabel("cc.kit.title"));
		kitTitle.setForegroundColour(Constants.Colour.GOLD);
		kitTitle.setBounds(
			column1x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		Rectangle listBounds = new Rectangle(
			column1x +panelBorderInset,
			contentTop +panelBorderInset +kitTitle.height +inset/2,
			columnWidth -panelBorderInset*2,
			contentHeight -panelBorderInset*2);

		ArrayList<ContainerWidget> list = new ArrayList<>();
		list.addAll(getClassKitWidgets(characterClassList));
		list.addAll(getRaceKitWidgets(getRaceList()));
		for (ContainerWidget cw : list)
		{
			cw.setBounds(listBounds.x, listBounds.y, listBounds.width, cw.getChildren().size()*20);
		}

		classAndRaceKitCards = new CardLayoutWidget(
			listBounds,
			list);
		classAndRaceKitCards.show(list.get(0));

		DIYPanel classListPanel = getFixedPanel(
			column1x,
			contentTop,
			columnWidth,
			contentHeight /3 *2);
		classListPanel.setLayoutManager(null);
		classListPanel.add(kitTitle);
		classListPanel.add(classAndRaceKitCards);

		// column 2: kit desc, kit inventory

		int yy = headerOffset + 45 + panelBorderInset;
		DIYPanel kitDescPanel = getFixedPanel(
			column2x,
			yy,
			columnWidth*2 +inset,
			classListPanel.y +classListPanel.height -yy);

		kitDesc = new DIYTextArea("                                            " +
			"                                                                   ");
		kitDesc.setTransparent(true);
		kitDescPanel.add(kitDesc);

		DIYPanel kitItemsPanel = getFixedPanel(
			column1x,
			kitDescPanel.y+ kitDescPanel.height +inset,
			columnWidth*3 +inset*2,
			height -kitDescPanel.y -kitDescPanel.height -inset -buttonPaneHeight);
		kitItemsPanel.setLayoutManager(null);

		DIYLabel kitItemsTitle = getSubTitle(getLabel("cc.kit.items.title"));
		kitItemsTitle.setBounds(
			kitItemsPanel.x +panelBorderInset,
			kitDescPanel.y + kitDescPanel.height + inset +panelBorderInset,
			columnWidth*3 +inset*2 -panelBorderInset*2,
			titleHeight);

		int itemCols = 4;
		int itemRows = 3;
		kitItems = new DIYPane(new DIYGridLayout(itemCols, itemRows,5,5));
		kitItems.setBounds(
			kitItemsPanel.x +panelBorderInset +inset/2,
			kitItemsTitle.y +titleHeight +inset/2,
			columnWidth*3 +inset*2 -panelBorderInset*2,
			kitItemsPanel.height -titleHeight -panelBorderInset*2 -inset);
		for (int i=0; i<itemCols*itemRows; i++)
		{
			ItemWidget iw = new ItemWidget();
			iw.addActionListener(itemWidgetListener);
			kitItems.add(iw);
		}

		kitItemsPanel.add(kitItemsTitle);
		kitItemsPanel.add(kitItems);

		// column 3: resources and stats

		DIYLabel modifiersTitle = getSubTitle(getLabel("cc.kit.modifiers"));
		modifiersTitle.setBounds(
			column4x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		kitResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, false, false);
		kitModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		kitModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);

		List<Stats.Modifier> otherModifiers = new ArrayList<>(Stats.allModifiers);
		otherModifiers.removeAll(Stats.resourceModifiers);
		otherModifiers.removeAll(Stats.attributeModifiers);
		otherModifiers.removeAll(Stats.resistances);

		kitModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, otherModifiers, false, false);

		int rowHeight = 18;
		int topY = contentTop +titleHeight +inset;
		kitResourcesWidget.setBounds(column4x, topY, columnWidth, 27+ 3* rowHeight);
		kitModifiersWidget1.setBounds(column4x, topY +29 +3* rowHeight, columnWidth, 6* rowHeight);
		kitModifiersWidget2.setBounds(column4x, topY +31 +3* rowHeight +6* rowHeight, columnWidth, 9* rowHeight);
		kitModifiersWidget3.setBounds(column4x, topY +33 +
			3* rowHeight +6* rowHeight +9* rowHeight, columnWidth, 10* rowHeight);

		kitResourcesWidget.setInsets(new Insets(panelBorderInset, panelBorderInset, 0, panelBorderInset));
		kitModifiersWidget1.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		kitModifiersWidget2.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		kitModifiersWidget3.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));

		DIYPanel kitModifiersPanel = getFixedPanel(
			column4x, contentTop, columnWidth, contentHeight);
		kitModifiersPanel.setLayoutManager(null);

		kitModifiersPanel.add(modifiersTitle);
		kitModifiersPanel.add(kitResourcesWidget);
		kitModifiersPanel.add(kitModifiersWidget1);
		kitModifiersPanel.add(kitModifiersWidget2);
		kitModifiersPanel.add(kitModifiersWidget3);
		
		pane.add(titlePane);
		pane.add(stepFlavour);

		pane.add(classListPanel);

		pane.add(kitDescPanel);
		pane.add(kitItemsPanel);

		pane.add(kitModifiersPanel);
		pane.add(buttonPane);

		return pane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYTextArea getStepFlavourArea(int inset, int column1,
		int headerOffset, String s)
	{
		DIYTextArea stepFlavour = new DIYTextArea(getLabel(s));

		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.ITALIC, defaultFont.getSize());

		stepFlavour.setFont(f);
		stepFlavour.setTransparent(true);
		stepFlavour.setAlignment(DIYToolkit.Align.CENTER);
		stepFlavour.setForegroundColour(MazeRendererFactory.LABEL_FOREGROUND);
		stepFlavour.setBounds(column1, 30+headerOffset/2, width-inset*2, headerOffset/2);
		return stepFlavour;
	}

	/*-------------------------------------------------------------------------*/
	private List<CharacterClassWrapper> getCharacterClassList()
	{
		Map<String, CharacterClass> map = Database.getInstance().getCharacterClasses();

		List<CharacterClassWrapper> result = new ArrayList<>();

		for (CharacterClass c : map.values())
		{
			CharacterClassWrapper ccw = new CharacterClassWrapper(c);
			result.add(ccw);
		}

		Collections.sort(result);
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private List<String> getRaceList()
	{
		List<String> list = new ArrayList<>(Database.getInstance().getRaces().keySet());
		Collections.sort(list);
		return list;
	}

	/*-------------------------------------------------------------------------*/
	private List<String> getPersonalityList()
	{
		List<String> list = new ArrayList<>(Database.getInstance().getPersonalities().keySet());
		Collections.sort(list);
		return list;
	}

	/*-------------------------------------------------------------------------*/
	private ArrayList<ContainerWidget> getClassKitWidgets(List<CharacterClassWrapper> classes)
	{
		ArrayList<ContainerWidget> result = new ArrayList<>();

		for (CharacterClassWrapper wrapper : classes)
		{
			CharacterClass cc = wrapper.characterClass;
			String className = cc.getName();

			List<StartingKit> startingItems = Leveler.getKitsForClass(className);

			List<StartingKit> items = new ArrayList<>(startingItems);

			if (items.size() > 0)
			{
				items.sort(new StartingItemsComparator());

				DIYListBox listBox = new DIYListBox(items);
				listBox.addActionListener(kitListener);
				listBox.setSelected(items.get(0));

				this.classAndRaceKitWidgets.put(className, listBox);
				result.add(listBox);
			}
		}

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private ArrayList<ContainerWidget> getRaceKitWidgets(List<String> races)
	{
		ArrayList<ContainerWidget> result = new ArrayList<>();

		for (String raceName : races)
		{
			Race race = Database.getInstance().getRace(raceName);

			List<StartingKit> startingItems = race.getStartingItems();
			if (startingItems == null || startingItems.isEmpty())
			{
				continue;
			}

			List<StartingKit> items = new ArrayList<>(startingItems);

			items.sort(new StartingItemsComparator());

			DIYListBox listBox = new DIYListBox(items);
			listBox.addActionListener(kitListener);
			listBox.setSelected(items.get(0));

			this.classAndRaceKitWidgets.put(raceName, listBox);
			result.add(listBox);
		}

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getClassesPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/4;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;
		int column4x = column3x + columnWidth + inset;

		int headerOffset = 50;
		int contentTop = headerOffset + 50;
		int contentHeight = height -contentTop -buttonPaneHeight;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int titleHeight = 20;

		DIYPane pane = new DIYPane();

		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1x, headerOffset, "cc.choose.class.flava");

		// column 1: class selection

		DIYLabel classTitle = getSubTitle(getLabel("cc.class.title"));
		classTitle.setBounds(column1x, contentTop+panelBorderInset, columnWidth, titleHeight);
		classTitle.setForegroundColour(Constants.Colour.GOLD);

		characterClasses = new DIYListBox(characterClassList);

		// todo: how to accommodate more classes? scrolling?

		DIYPanel classListPanel = getFixedPanel(column1x, contentTop, columnWidth, contentHeight);
		classListPanel.setLayoutManager(null);
		classListPanel.add(classTitle);
		classListPanel.add(characterClasses);

		characterClasses.setBounds(
			column1x +panelBorderInset,
			contentTop +panelBorderInset +classTitle.height +inset/2,
			columnWidth -panelBorderInset*2,
			contentHeight -panelBorderInset*2 -classTitle.height);
		characterClasses.setSelected(characterClassList.get(0));
		characterClasses.addActionListener(this);

		// column 2: desc, spell books, progression

		// class desc

		DIYPanel classDescPanel = getFixedPanel(
			column2x,
			headerOffset + 45 + panelBorderInset,
			columnWidth*2 +inset,
			(contentHeight-panelBorderInset-inset*2) / 2);

		this.classDesc = new DIYTextArea(characterClassList.get(0).characterClass.getDescription());
		this.classDesc.setTransparent(true);
		classDescPanel.add(classDesc);

		// spell books

		DIYPanel classSpellBooksPanel = getFixedPanel(
			column2x,
			classDescPanel.y + classDescPanel.height +inset,
			columnWidth*2 +inset,
			(contentHeight-panelBorderInset-inset*2) / 4);
		classSpellBooksPanel.setLayoutManager(null);

		DIYLabel spellBooksTitle = getSubTitle(StringUtil.getUiLabel("cc.spell.books"));
		spellBooksTitle.setBounds(
			column2x, classSpellBooksPanel.y +panelBorderInset,
			columnWidth*2+inset, titleHeight);

		this.spellBooksDesc = new DIYTextArea("");
		this.spellBooksDesc.setTransparent(true);
		this.spellBooksDesc.setBounds(
			column2x +panelBorderInset, spellBooksTitle.y+titleHeight,
			columnWidth*2+inset-panelBorderInset*2, classSpellBooksPanel.height-spellBooksTitle.height-panelBorderInset*2);

		classSpellBooksPanel.add(spellBooksTitle);
		classSpellBooksPanel.add(spellBooksDesc);

		// ability progression

		DIYPanel classLapPanel = getFixedPanel(
			column2x,
			classSpellBooksPanel.y + classSpellBooksPanel.height +inset,
			columnWidth*2 +inset,
			height -classSpellBooksPanel.y -classSpellBooksPanel.height -inset -buttonPaneHeight);
		classLapPanel.setLayoutManager(null);

		DIYLabel abilityProgressionTitle = getSubTitle(StringUtil.getUiLabel("cc.ability.progression"));
		abilityProgressionTitle.setBounds(
			column2x, classLapPanel.y+panelBorderInset, columnWidth*2+inset, titleHeight);

		showLevelAbilityProgression = new DIYButton(StringUtil.getUiLabel("cc.show.lap"));
		showLevelAbilityProgression.addActionListener(this);
		Dimension d = showLevelAbilityProgression.getPreferredSize();
		showLevelAbilityProgression.setBounds(
			column2x +columnWidth*2 +inset -panelBorderInset -d.width,
			classLapPanel.y+classLapPanel.height-panelBorderInset-d.height,
			d.width, d.height);

		int levelsToPreview = 3;
		Rectangle r = new Rectangle(
			column2x +panelBorderInset,
			classLapPanel.y +panelBorderInset +titleHeight +inset/2,
			columnWidth*2 +inset -panelBorderInset*2 -showLevelAbilityProgression.width,
			classLapPanel.height -panelBorderInset*2 -titleHeight -inset);
		firstLevel = new LevelAbilityProgressionWidget(null, levelsToPreview, r, true);
		firstLevel.doLayout();

		classLapPanel.add(abilityProgressionTitle);
		classLapPanel.add(firstLevel);
		classLapPanel.add(showLevelAbilityProgression);

		// column 3: resources and stats

		DIYLabel modifiersTitle = getSubTitle(getLabel("cc.class.modifiers"));
		modifiersTitle.setBounds(
			column4x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		classResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, false, false);
		classModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		classModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);

		List<Stats.Modifier> otherModifiers = new ArrayList<>(Stats.allModifiers);
		otherModifiers.removeAll(Stats.resourceModifiers);
		otherModifiers.removeAll(Stats.attributeModifiers);
		otherModifiers.removeAll(Stats.resistances);

		classModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, otherModifiers, false, false);

		int rowHeight = 18;
		int topY = contentTop +titleHeight +inset;
		classResourcesWidget.setBounds(column4x, topY, columnWidth, 27+ 3* rowHeight);
		classModifiersWidget1.setBounds(column4x, topY +29 +3* rowHeight, columnWidth, 6* rowHeight);
		classModifiersWidget2.setBounds(column4x, topY +31 +3* rowHeight +6* rowHeight, columnWidth, 9* rowHeight);
		classModifiersWidget3.setBounds(column4x, topY +33 +
			3* rowHeight +6* rowHeight +9* rowHeight, columnWidth, 10* rowHeight);

		classResourcesWidget.setInsets(new Insets(panelBorderInset, panelBorderInset, 0, panelBorderInset));
		classModifiersWidget1.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		classModifiersWidget2.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		classModifiersWidget3.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));

		DIYPanel classModifiersPanel = getFixedPanel(
			column4x, contentTop, columnWidth, contentHeight);
		classModifiersPanel.setLayoutManager(null);

		classModifiersPanel.add(modifiersTitle);
		classModifiersPanel.add(classResourcesWidget);
		classModifiersPanel.add(classModifiersWidget1);
		classModifiersPanel.add(classModifiersWidget2);
		classModifiersPanel.add(classModifiersWidget3);

		pane.add(titlePane);
		pane.add(stepFlavour);

		pane.add(classListPanel);

		pane.add(classDescPanel);
		pane.add(classSpellBooksPanel);
		pane.add(classLapPanel);

		pane.add(classModifiersPanel);

		pane.add(buttonPane);

		return pane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getPersonalsPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/4;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;
		int column4x = column3x + columnWidth + inset;

		int headerOffset = 50;
		int contentTop = headerOffset + 50;
		int contentHeight = height -contentTop -buttonPaneHeight;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int titleHeight = 20;

		DIYPane pane = new DIYPane();

		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1x, headerOffset, "cc.choose.personals.flava");

		// column 1: personalities

		DIYPanel personalitiesPanel = getFixedPanel(column1x, contentTop, columnWidth, contentHeight);
		personalitiesPanel.setLayoutManager(null);

		List<String> personalityList = getPersonalityList();
		DIYLabel personalityTitle = getSubTitle(getLabel("cc.personality"));
		personalityTitle.setForegroundColour(Constants.Colour.GOLD);
		personalityTitle.setBounds(
			column1x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		personalities = new DIYListBox(personalityList);
		personalities.setBounds(
			column1x +panelBorderInset,
			contentTop +panelBorderInset +titleHeight + inset/2,
			columnWidth -panelBorderInset*2,
			personalities.getPreferredSize().height);
		setDefaultPersonality();
		personalities.addActionListener(this);

		personalitiesPanel.add(personalityTitle);
		personalitiesPanel.add(personalities);

		// column 2: name and portrait

		DIYPanel namePanel = getFixedPanel(
			column2x,
			headerOffset + 45 + panelBorderInset,
			columnWidth*2 +inset,
			contentHeight/5);
		namePanel.setLayoutManager(null);

		DIYPanel portraitPanel = getFixedPanel(
			column2x,
			namePanel.y +namePanel.height +titleHeight +inset,
			columnWidth*2 +inset,
			contentHeight/3);
		portraitPanel.setLayoutManager(null);

		DIYLabel nameTitle = getSubTitle(getLabel("cc.character.name"));
		nameTitle.setForegroundColour(Constants.Colour.GOLD);
		nameField = new DIYTextField("",20)
		{
			@Override
			public void processKeyPressed(KeyEvent e)
			{
				super.processKeyPressed(e);
				next.setEnabled(canProceed());
				setName(this.getText().trim());
			}

			@Override
			public void setText(String text)
			{
				super.setText(text);
				next.setEnabled(canProceed());
				setName(this.getText().trim());
			}
		};

		nameTitle.setBounds(
			namePanel.x +panelBorderInset,
			namePanel.y +panelBorderInset,
			namePanel.width -panelBorderInset*2,
			titleHeight);

		int nameFieldWidth = 200;

		nameField.setBounds(
			namePanel.x + namePanel.width/2 -nameFieldWidth/2,
			nameTitle.y + nameTitle.height +inset,
			nameFieldWidth,
			titleHeight*2);

		suggestName = new DIYButton(getLabel("cc.suggest.name"));
		suggestName.addActionListener(this);

		suggestName.setBounds(
			namePanel.x +namePanel.width -panelBorderInset -inset/2 -suggestName.getPreferredSize().width,
//			namePanel.y +namePanel.height -panelBorderInset -inset/2 -suggestName.getPreferredSize().height,
			nameField.y,
			suggestName.getPreferredSize().width,
//			suggestName.getPreferredSize().height
			nameField.height
		);

		DIYLabel portraitTitle = getSubTitle(getLabel("cc.character.portrait"));
		portraitTitle.setForegroundColour(Constants.Colour.GOLD);
		portraitTitle.setBounds(
			column2x +panelBorderInset,
			portraitPanel.y +panelBorderInset,
			columnWidth*2 +inset -panelBorderInset*2,
			titleHeight);
		portraitWidget = new PortraitSelectionWidget(
			column2x +panelBorderInset,
			portraitTitle.y +portraitTitle.height +inset +inset/2,
			columnWidth*2 +inset -panelBorderInset*2,
			portraitPanel.height -panelBorderInset*2 -portraitTitle.height-inset*3);

		namePanel.add(nameTitle);
		namePanel.add(nameField);
		namePanel.add(suggestName);
		portraitPanel.add(portraitTitle);
		portraitPanel.add(portraitWidget);

		// column 3: resources and stats

		DIYLabel modifiersTitle = getSubTitle(getLabel("cc.final.modifiers"));
		modifiersTitle.setBounds(
			column4x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		resourcesSummaryWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, false, false);
		modifiersSummaryWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		modifiersSummaryWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);

		List<Stats.Modifier> otherModifiers = new ArrayList<>(Stats.allModifiers);
		otherModifiers.removeAll(Stats.resourceModifiers);
		otherModifiers.removeAll(Stats.attributeModifiers);
		otherModifiers.removeAll(Stats.resistances);

		modifiersSummaryWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, otherModifiers, false, false);

		int rowHeight = 18;
		int topY = contentTop +titleHeight +inset;
		resourcesSummaryWidget.setBounds(column4x, topY, columnWidth, 27+ 3* rowHeight);
		modifiersSummaryWidget1.setBounds(column4x, topY +29 +3* rowHeight, columnWidth, 6* rowHeight);
		modifiersSummaryWidget2.setBounds(column4x, topY +31 +3* rowHeight +6* rowHeight, columnWidth, 9* rowHeight);
		modifiersSummaryWidget3.setBounds(column4x, topY +33 +
			3* rowHeight +6* rowHeight +9* rowHeight, columnWidth, 10* rowHeight);

		resourcesSummaryWidget.setInsets(new Insets(panelBorderInset, panelBorderInset, 0, panelBorderInset));
		modifiersSummaryWidget1.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		modifiersSummaryWidget2.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		modifiersSummaryWidget3.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));

		DIYPanel summaryModifiersPanel = getFixedPanel(
			column4x, contentTop, columnWidth, contentHeight);
		summaryModifiersPanel.setLayoutManager(null);

		summaryModifiersPanel.add(modifiersTitle);
		summaryModifiersPanel.add(resourcesSummaryWidget);
		summaryModifiersPanel.add(modifiersSummaryWidget1);
		summaryModifiersPanel.add(modifiersSummaryWidget2);
		summaryModifiersPanel.add(modifiersSummaryWidget3);
		

		pane.add(titlePane);
		pane.add(stepFlavour);

		pane.add(personalitiesPanel);
		pane.add(namePanel);
		pane.add(portraitPanel);
		pane.add(summaryModifiersPanel);

		pane.add(buttonPane);

		return pane;
	}

	/*-------------------------------------------------------------------------*/
	private void setDefaultPersonality()
	{
		String defaultPersonality = (String)personalities.getItems().get(0);
		personalities.setSelected(defaultPersonality);
		setPersonality(defaultPersonality);
	}

	/*-------------------------------------------------------------------------*/
	private String getLabel(String key)
	{
		return StringUtil.getUiLabel(key);
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getRaceAndGenderPane()
	{
		int inset, column1x;

		inset = column1x = 10;
		int columnWidth = (width-5*inset)/4;

		int column2x = column1x + columnWidth + inset;
		int column3x = column2x + columnWidth + inset;
		int column4x = column3x + columnWidth + inset;

		int headerOffset = 50;
		int contentTop = headerOffset + 50;
		int contentHeight = height -contentTop -buttonPaneHeight;
		int panelBorderInset = rp.getProperty(RendererProperties.Property.PANEL_MED_BORDER);
		int titleHeight = 20;

		DIYPane pane = new DIYPane();

		DIYTextArea stepFlavour = getStepFlavourArea(
			inset, column1x, headerOffset, "cc.choose.race.and.gender.flava");

		// column 1: race and gender

		List<String> raceList = getRaceList();
		DIYLabel raceTitle = getSubTitle(getLabel("cc.race.title"));
		raceTitle.setForegroundColour(Constants.Colour.GOLD);
		raceTitle.setBounds(column1x, contentTop +panelBorderInset, columnWidth, titleHeight);
		races = new DIYListBox(raceList);
		races.setBounds(column1x+panelBorderInset, contentTop+panelBorderInset+raceTitle.height+inset/2, columnWidth-panelBorderInset*2, races.getPreferredSize().height);
		setDefaultSelectedRace(raceList);
		races.addActionListener(this);

		int nrGenders = ((List<String>)new ArrayList<>(Database.getInstance().getGenders().keySet())).size();
		ArrayList<ContainerWidget> genders = new ArrayList<>();

		int genderY = headerOffset+height - 330;

		DIYLabel genderTitle = getSubTitle(getLabel("cc.gender.title"));
		genderTitle.setBounds(column1x, genderY, columnWidth, titleHeight);

		int genderRowHeight = 25;
		for (String raceName : raceList)
		{
			Race race = Database.getInstance().getRace(raceName);
			List<Gender> raceGenders = race.getAllowedGenders();
			raceGenders.sort(Comparator.comparing(Gender::getName));

			DIYPane temp = new DIYPane();
			DIYButtonGroup genderBG = new DIYButtonGroup();
			int count = 0;
			for (Gender g : raceGenders)
			{
				DIYRadioButton radioButton = new DIYRadioButton(g.getName(), count==0);
				temp.add(radioButton);
				radioButton.addActionListener(this);
				radioButton.setActionMessage(SET_GENDER);
				radioButton.setBounds(column1x+panelBorderInset, genderY+genderRowHeight*(count+1), columnWidth-panelBorderInset*2, genderRowHeight);
				genderBG.addButton(radioButton);
				count++;
			}

			raceGenderWidgets.put(raceName, temp);
			genders.add(temp);
		}

		raceGenderChoices = new CardLayoutWidget(
			new Rectangle(column1x, genderY+20, columnWidth, genderRowHeight*nrGenders),
			genders);
		raceGenderChoices.doLayout();

		DIYPanel raceAndGenderPanel = getFixedPanel(column1x, contentTop, columnWidth, contentHeight);
		raceAndGenderPanel.setLayoutManager(null);

		raceAndGenderPanel.add(raceTitle);
		raceAndGenderPanel.add(races);
		raceAndGenderPanel.add(genderTitle);
		raceAndGenderPanel.add(raceGenderChoices);

		// column 2: desc and image

		DIYPanel raceDescPanel = getFixedPanel(
			column2x,
			headerOffset + 45 + panelBorderInset,
			columnWidth*2 +inset,
			(contentHeight-panelBorderInset-inset) / 2);

		raceDesc = new DIYTextArea("");
		raceDesc.setTransparent(true);
		raceDescPanel.add(raceDesc);

		DIYPanel raceImagePanel = getFixedPanel(
			column2x,
			raceDescPanel.y+ raceDescPanel.height +inset,
			columnWidth*2 +inset,
			height -raceDescPanel.y -raceDescPanel.height -inset -buttonPaneHeight);

		raceImage = new DIYLabel();
		raceImage.setBounds(
			raceImagePanel.x +panelBorderInset,
			raceImagePanel.y +panelBorderInset,
			columnWidth*2 +inset -panelBorderInset*2,
			raceImagePanel.height -panelBorderInset*2);

		// column 3: resources and stats

		DIYLabel modifiersTitle = getSubTitle(getLabel("cc.race.modifiers"));
		modifiersTitle.setBounds(
			column4x +panelBorderInset,
			contentTop +panelBorderInset,
			columnWidth -panelBorderInset*2,
			titleHeight);

		raceResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, true, false);
		raceModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		raceModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);

		List<Stats.Modifier> otherModifiers = new ArrayList<>(Stats.allModifiers);
		otherModifiers.removeAll(Stats.resourceModifiers);
		otherModifiers.removeAll(Stats.attributeModifiers);
		otherModifiers.removeAll(Stats.resistances);

		raceModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, otherModifiers, false, false);

		int rowHeight = 18;
		int topY = contentTop + titleHeight + inset;
		raceResourcesWidget.setBounds(column4x, topY, columnWidth, 27+ 3* rowHeight);
		raceModifiersWidget1.setBounds(column4x, topY +29 +3* rowHeight, columnWidth, 6* rowHeight);
		raceModifiersWidget2.setBounds(column4x, topY +31 +3* rowHeight +6* rowHeight, columnWidth, 9* rowHeight);
		raceModifiersWidget3.setBounds(column4x, topY +33 +
			3* rowHeight +6* rowHeight +9* rowHeight, columnWidth, 10* rowHeight);

		raceResourcesWidget.setInsets(new Insets(panelBorderInset, panelBorderInset, 0, panelBorderInset));
		raceModifiersWidget1.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		raceModifiersWidget2.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));
		raceModifiersWidget3.setInsets(new Insets(0, panelBorderInset, 0, panelBorderInset));

		DIYPanel raceModifiersPanel = getFixedPanel(
			column4x, contentTop, columnWidth, contentHeight);
		raceModifiersPanel.setLayoutManager(null);

		raceModifiersPanel.add(modifiersTitle);
		raceModifiersPanel.add(raceResourcesWidget);
		raceModifiersPanel.add(raceModifiersWidget1);
		raceModifiersPanel.add(raceModifiersWidget2);
		raceModifiersPanel.add(raceModifiersWidget3);

		pane.add(titlePane);
		pane.add(stepFlavour);

		pane.add(raceAndGenderPanel);

		pane.add(raceDescPanel);
		pane.add(raceImagePanel);
		pane.add(raceImage);

		pane.add(raceModifiersPanel);

		pane.add(buttonPane);

		return pane;
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
	private void setDefaultSelectedRace(List<String> raceList)
	{
		// we need to get the exact reference because DIYListBox does reference
		// checks on its items
		String race = null;
		String defaultRace = Maze.getInstance().getCampaign().getDefaultRace();
		
		for (String s : raceList)
		{
			if (s.equals(defaultRace))
			{
				race = s;
			}
		}
		
		if (raceList.contains(race))
		{
			races.setSelected(race);
		}
		else
		{
			races.setSelected(raceList.get(1));
		}
	}

	/*-------------------------------------------------------------------------*/
	private void updateState()
	{
		this.playerCharacter = null;

		if (state <= 0)
		{
			finished();
			return;
		}
		else if (state == CHOOSE_RACE_AND_GENDER)
		{
			this.previous.setText("   Cancel   ");
		}
		else
		{
			this.previous.setText("< Previous");
			previous.setActionMessage(null);
		}

		if (state == EDIT_PERSONALS)
		{
			next.setText("Finish");
		}
		else
		{
			next.setText("Next >");
			next.setActionMessage(null);
		}

		if (state == CHOOSE_RACE_AND_GENDER)
		{
			characterTitle.setText(getLabel("cc.choose.race.and.gender"));
			String raceName = (String)races.getSelected();
			setRace(raceName);
			portraitWidget.setToRaceAndGender(raceName, gender.getName(), portraitWidget.portraits);
		}
		else if (state == CHOOSE_CLASS)
		{
			characterTitle.setText(getLabel("cc.choose.class") + " " +
				gender.getName()+" "+race.getName());
			String className = ((CharacterClassWrapper)characterClasses.getSelected()).characterClass.getName();
			setCharacterClass(className);
		}
		else if (state == CHOOSE_KIT)
		{
			characterTitle.setText(getLabel("cc.choose.kit") + " " +
				gender.getName()+" "+race.getName()+" "+characterClass.getName());
		}
		else if (state == CHOOSE_SPELLS)
		{
			characterTitle.setText(getLabel("cc.choose.spells") + " " +
				gender.getName()+" "+race.getName()+" "+characterClass.getName());
		}
		else if (state == EDIT_PERSONALS)
		{
			characterTitle.setText(getLabel("cc.choose.personals") + " " +
				gender.getName()+" "+race.getName()+" "+characterClass.getName());
		}

		switch (state)
		{
			case CHOOSE_RACE_AND_GENDER -> this.cardLayout.show(raceAndGenderPane);
			case CHOOSE_CLASS -> this.cardLayout.show(classesPane);
			case CHOOSE_KIT -> this.cardLayout.show(kitsPane);
			case CHOOSE_SPELLS ->
			{

				// need a temp pc
				PlayerCharacter pc = getTempPlayerCharacterObject();
				SpellBook sb = new SpellBook();
/*
				for (StartingSpellBook ssb : startingSpellBooks)
				{
					sb.addLimit(ssb.getSpellBook(), ssb.getMaxLevel(), ssb.getLevelOffset());
				}
*/

				pc.setSpellBook(sb);
				this.spellLearner.refresh(pc);
				this.cardLayout.show(spellsPane);
			}
			case EDIT_PERSONALS ->
			{
				int hp = Leveler.calcStartingHitPoints(characterClass, race);
				int ap = Leveler.calcStartingActionPoints(characterClass, race);
				int mp = Leveler.calcStartingMagicPoints(characterClass, race);
				resourcesSummaryWidget.setResources(hp, ap, mp, false);
				StatModifier summary1 = new StatModifier();
				summary1.addModifiers(raceModifiersWidget1.getStatModifier());
				summary1.addModifiers(classModifiersWidget1.getStatModifier());
				summary1.addModifiers(kitModifiersWidget1.getStatModifier());
				modifiersSummaryWidget1.setStatModifier(summary1, false);
				StatModifier summary2 = new StatModifier();
				summary2.addModifiers(raceModifiersWidget2.getStatModifier());
				summary2.addModifiers(classModifiersWidget2.getStatModifier());
				summary2.addModifiers(kitModifiersWidget2.getStatModifier());
				modifiersSummaryWidget2.setStatModifier(summary2, false);
				StatModifier summary3 = new StatModifier();
				summary3.addModifiers(raceModifiersWidget3.getStatModifier());
				summary3.addModifiers(classModifiersWidget3.getStatModifier());
				summary3.addModifiers(kitModifiersWidget3.getStatModifier());
				modifiersSummaryWidget3.setStatModifier(summary3, false);
				this.cardLayout.show(personalsPane);
			}
			case FINISHED ->
			{

				// finally, create the character!

				// get the pic
				String portrait = portraitWidget.portraits.get(portraitWidget.currentImage);

				// create the base character
				playerCharacter = new Leveler().createNewPlayerCharacter(
					name,
					characterClass,
					race,
					gender,
					portrait,
					personality,
					spellLearner.getSelectedSpells());

				// apply the player selected kit
				playerCharacter.applyStartingKit(this.startingKit);
				playerCharacter.setPersonality(this.personality);

				// add to the guild
				if (npc == null)
				{
					Maze.getInstance().addPlayerCharacterToGuild(playerCharacter);
				}
				else
				{
					Maze.getInstance().addPlayerCharacterToGuild(playerCharacter, npc);
				}
				finished();
			}
			default -> throw new MazeException("Illegal state: " + state);
		}

		this.next.setEnabled(this.canProceed());
		this.random.setEnabled(isRandomSupported());
	}

	/*-------------------------------------------------------------------------*/
	private void finished()
	{
		if (Maze.getInstance().getCurrentActorEncounter() != null)
		{
			Maze.getInstance().setState(Maze.State.ENCOUNTER_ACTORS);
		}
		else
		{
			Maze.getInstance().setState(Maze.State.MAINMENU);
		}
	}

	/*-------------------------------------------------------------------------*/
	private PlayerCharacter getTempPlayerCharacterObject()
	{
		PlayerCharacter temp = new Leveler().createNewPlayerCharacter(
			name,
			characterClass,
			race,
			gender,
			"",
			personality,
			null);
		temp.applyStartingKit(startingKit);
		return temp;
	}

	/*-------------------------------------------------------------------------*/
	private boolean isRandomSupported()
	{
		return state == EDIT_PERSONALS ||
			state == CHOOSE_CLASS ||
			state == CHOOSE_KIT ||
			state == CHOOSE_SPELLS ||
			state == CHOOSE_RACE_AND_GENDER;
	}

	/*-------------------------------------------------------------------------*/
	private boolean characterMustChooseSpells()
	{
		if (characterClass == null)
		{
			return false;
		}

		if (!characterClass.getProgression().hasMagicAbility(1))
		{
			return false;
		}

		if (!characterClass.getProgression().hasSpellPicks(1))
		{
			return false;
		}

		return true;
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object obj = event.getSource();
		String message = event.getMessage();
		Maze.getInstance().getUi().stopAllAnimations();

		//--- keyed on message
		if (SET_GENDER.equals(message))
		{
			setGender(((DIYRadioButton)obj).getCaption());
			selectDefaultCharacterClass();
			return true;
		}

		//--- keyed on object
		if (obj == next)
		{
			if (next.getText().equals("Finish")) //hack
			{
				this.state = FINISHED;
			}
			else
			{
				this.state++;
			}

			if (state == CHOOSE_SPELLS && !characterMustChooseSpells())
			{
				this.state++;
			}

			updateState();
			return true;
		}
		else if (obj == previous)
		{
			this.state--;

			if (state == CHOOSE_SPELLS && !characterMustChooseSpells())
			{
				this.state--;
			}

			updateState();
			return true;
		}
		else if (obj == races)
		{
			String raceName = (String)races.getSelected();
			setRace(raceName);
			selectDefaultCharacterClass();
			portraitWidget.setToRaceAndGender(raceName, gender.getName(), portraitWidget.portraits);
			return true;
		}
		else if (obj == characterClasses)
		{
			String className = ((CharacterClassWrapper)characterClasses.getSelected()).characterClass.getName();
			setCharacterClass(className);
			this.next.setEnabled(this.canProceed());
			return true;
		}
		else if (obj == personalities)
		{
			String pName = (String)personalities.getSelected();
			setPersonality(pName);

			SpeechUtil.getInstance().genericSpeech(
				Personality.BasicSpeech.PERSONALITY_SELECTED.getKey(),
				playerCharacter,
				personality,
				this.portraitWidget.imagePanel.getBounds(),
				SpeechBubble.Orientation.BELOW_RIGHT);
			return true;
		}
		else if (obj == suggestName)
		{
			suggestName(race, gender);
			return true;
		}
		else if (obj == random)
		{
			switch (this.state)
			{
				case CHOOSE_RACE_AND_GENDER ->
				{
					String raceName = Leveler.getRandomRace();
					races.setSelected(raceName);
					setRace(raceName);
					selectDefaultCharacterClass();
					Gender genderInst = Leveler.getRandomGender(raceName);
					setGender(genderInst.getName());
					selectDefaultCharacterClass();
				}
				case CHOOSE_CLASS ->
				{
					// todo: move to the Leveler getRandomCharacterClass implementation
					Dice classD = new Dice(1, this.characterClassList.size(), -1);
					CharacterClassWrapper ccw = null;
					while (ccw == null)
					{
						ccw = characterClassList.get(classD.roll("CCW: class"));
						if (!characterClasses.isEnabled(ccw))
						{
							ccw = null;
						}
					}
					characterClasses.setSelected(ccw);
					setCharacterClass(ccw.characterClass.getName());
				}
				case CHOOSE_KIT ->
				{
					DIYListBox kits = (DIYListBox)classAndRaceKitCards.getCurrentWidget();
					StartingKit kit = Leveler.getRandomStartingKit(
						characterClass.getName(),
						race.getName());
					if (kit != null)
					{
						kits.setSelected(kit);
						setStartingKit(kit);
					}
				}
				case CHOOSE_SPELLS ->
				{
					spellLearner.clear();
					List<Spell> randomSpells = Leveler.getRandomSpells(
						spellLearner.getPlayerCharacter());
					spellLearner.setSelectedSpells(randomSpells);
				}
				case EDIT_PERSONALS ->
				{
					portraitWidget.setToRaceAndGender(race.getName(), gender.getName(), portraitWidget.portraits);
					suggestName(race, gender);
					Personality p = Leveler.getRandomPersonality();
					personalities.setSelected(p.getName());
					setPersonality(p.getName());
					String portraitName = Leveler.getRandomPortraitName(race.getName(), gender.getName());
					portraitWidget.setToPortrait(portraitName);
				}
			}
			return true;
		}
		else if (obj == showLevelAbilityProgression)
		{
			int x = 100;
			int y = 100;
			Rectangle rectangle = new Rectangle(x, y,
				DiyGuiUserInterface.SCREEN_WIDTH-200, DiyGuiUserInterface.SCREEN_HEIGHT-200);

			ContainerWidget c = new LevelAbilityProgressionWidget(characterClass);
			Maze.getInstance().getUi().showDialog(new ContainerDialog(
				StringUtil.getUiLabel("lapw.title",
					characterClass.getName()), c, rectangle));

			return true;
		}

		this.next.setEnabled(this.canProceed());
		this.random.setEnabled(isRandomSupported());

		return false;
	}

	/*-------------------------------------------------------------------------*/
	private void suggestName(Race race, Gender gender)
	{
		String result = Leveler.getRandomName(race, gender);

		if (result != null)
		{
			nameField.setText(result);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setName(String nameStr)
	{
		if (nameStr == null || nameStr.length() == 0)
		{
			name = null;
		}
		else
		{
			name = nameStr;
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setPersonality(String pName)
	{
		this.personality = Database.getInstance().getPersonalities().get(pName);
	}

	/*-------------------------------------------------------------------------*/
	private void setCharacterClass(String className)
	{
		this.characterClass = Database.getInstance().getCharacterClass(className);
		LevelAbilityProgression progression = characterClass.getProgression();

		String desc = characterClass.getDescription();
		this.classDesc.setText(desc);
		this.firstLevel.refresh(characterClass);
		StatModifier mod = new StatModifier(characterClass.getStartingModifiers());

		List<StartingSpellBook> magicAbility = progression.getMagicAbility(20);

		if (magicAbility.isEmpty())
		{
			this.spellBooksDesc.setText("None");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			for (StartingSpellBook ssb : magicAbility)
			{
				sb.append(ssb.getSpellBook().getName())
					.append(" (max lvl ").append(ssb.getMaxLevel()).append("): ")
					.append(ssb.getDescription());
			}
			this.spellBooksDesc.setText(sb.toString());
		}

		// todo: should we pick up the and lvl1 LAP values in the resources display?
		this.classResourcesWidget.setResources(
			characterClass.getStartingHitPoints(),
			characterClass.getStartingActionPoints(),
			characterClass.getStartingMagicPoints(),
			false);
		this.classModifiersWidget1.setStatModifier(mod, false);
		this.classModifiersWidget2.setStatModifier(mod, false);
		this.classModifiersWidget3.setStatModifier(mod, false);

		if (race.getStartingItems() != null && !race.getStartingItems().isEmpty())
		{
			this.classAndRaceKitCards.show(this.classAndRaceKitWidgets.get(race.getName()));
		}
		else
		{
			this.classAndRaceKitCards.show(this.classAndRaceKitWidgets.get(className));
		}

		DIYListBox list = (DIYListBox)classAndRaceKitCards.getCurrentWidget();
		this.setStartingKit((StartingKit)list.getSelected());
	}

	/*-------------------------------------------------------------------------*/
	private void setRace(String raceName)
	{
		this.race = Database.getInstance().getRace(raceName);

		String desc = race.getDescription();
		if (race.isLocked())
		{
			this.raceDesc.setText(getLabel("cc.locked")+"\n\n"+race.getUnlockDescription());
			this.raceImage.setIcon(null);
		}
		else
		{
			this.raceDesc.setText(desc);
			this.raceImage.setIcon(Database.getInstance().getImage(race.getCharacterCreationImage()));
		}
		StatModifier mods = new StatModifier(race.getStartingModifiers());
		mods.addModifiers(race.getConstantModifiers());
		mods.addModifiers(race.getBannerModifiers());
		this.raceResourcesWidget.setResources(
			race.getStartingHitPointPercent(),
			race.getStartingActionPointPercent(),
			race.getStartingMagicPointPercent(),
			race.isLocked());
		this.raceModifiersWidget1.setStatModifier(mods, race.isLocked());
		this.raceModifiersWidget2.setStatModifier(mods, race.isLocked());
		if (!race.isLocked())
		{
			this.raceModifiersWidget3.setStatModifier(mods, false);
		}
		else
		{
			this.raceModifiersWidget3.setStatModifier(new StatModifier(), true);
		}

		this.raceGenderChoices.show(this.raceGenderWidgets.get(raceName));
		if (race.getAllowedGenders().contains(this.gender))
		{
			setGender(this.gender.getName());
		}
		else
		{
			setGender(race.getAllowedGenders().get(0).getName());
		}

		for (MagicSys.SpellBook sb : this.spellBookWidgets.keySet())
		{
			for (Widget w : spellBookWidgets.get(sb))
			{
				w.setEnabled(sb.isAllowedRace(race) && sb.isAllowedGender(gender));
			}
		}

		for (Object obj : this.characterClasses.getItems())
		{
			CharacterClassWrapper ccw = (CharacterClassWrapper)obj;
			CharacterClass cc = ccw.characterClass;
			boolean enabled = cc.isAllowedRace(race) && cc.isAllowedGender(gender);
			this.characterClasses.setEnabled(ccw, enabled);
		}

		if (race.getStartingItems() != null && !race.getStartingItems().isEmpty())
		{
			DIYListBox list = (DIYListBox)classAndRaceKitCards.getCurrentWidget();
			this.setStartingKit((StartingKit)list.getSelected());
		}
		else if (this.characterClass != null)
		{
			DIYListBox list = (DIYListBox)classAndRaceKitCards.getCurrentWidget();
			this.setStartingKit((StartingKit)list.getSelected());
		}
	}

	/*-------------------------------------------------------------------------*/
	private void setGender(String genderName)
	{
		this.gender = Database.getInstance().getGenders().get(genderName);

		for (MagicSys.SpellBook sb : this.spellBookWidgets.keySet())
		{
			for (Widget w : spellBookWidgets.get(sb))
			{
				w.setEnabled(sb.isAllowedGender(gender) && sb.isAllowedRace(race));
			}
		}
		for (Object obj : this.characterClasses.getItems())
		{
			CharacterClassWrapper ccw = (CharacterClassWrapper)obj;
			CharacterClass cc = ccw.characterClass;
			boolean enabled = cc.isAllowedGender(gender) && cc.isAllowedRace(race);
			this.characterClasses.setEnabled(ccw, enabled);
		}

		for (Widget w : this.raceGenderWidgets.get(race.getName()).getChildren())
		{
			DIYRadioButton radio = (DIYRadioButton)w;
			String caption = radio.getCaption();
			radio.setSelected(caption.equals(genderName));
		}

		portraitWidget.setToRaceAndGender(race.getName(), gender.getName(), portraitWidget.portraits);
	}

	/*-------------------------------------------------------------------------*/
	private void setStartingKit(StartingKit si)
	{
		this.startingKit = si;

		kitDesc.setText(startingKit.getDescription()==null?"(null)":startingKit.getDescription());

		StatModifier mod;
		if (this.characterClass != null)
		{
			mod = switch (characterClass.getFocus())
				{
					case COMBAT -> this.startingKit.getCombatModifiers();
					case STEALTH -> this.startingKit.getStealthModifiers();
					case MAGIC -> this.startingKit.getMagicModifiers();
					default ->
						throw new MazeException("Invalid focus " + characterClass.getFocus());
				};
		}
		else
		{
			// default to something
			mod = startingKit.getCombatModifiers();
		}

		this.kitModifiersWidget1.setStatModifier(mod, false);
		this.kitModifiersWidget2.setStatModifier(mod, false);
		this.kitModifiersWidget3.setStatModifier(mod, false);

		// clear starting item widgets
		for (Widget w : kitItems.getChildren())
		{
			ItemWidget iw = (ItemWidget)w;
			iw.setItem(null);
		}

		Iterator<Widget> iterator = kitItems.getChildren().iterator();
		// populate with starting items from this kit
		for (String s : startingKit.getStartingItemNames())
		{
			Item item = GameSys.getInstance().createItemForStartingKit(s,
				getTempPlayerCharacterObject());

			if (item != null)
			{
				item.setIdentificationState(Item.IdentificationState.IDENTIFIED);

				if (iterator.hasNext())
				{
					ItemWidget iw = (ItemWidget)iterator.next();
					iw.setItem(item);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private boolean canProceed()
	{
		switch (state)
		{
			case 0:
				// ignore
				return true;
			case CHOOSE_RACE_AND_GENDER:
			{
				return races.isEnabled(races.getSelected()) &&
					!race.isLocked();
			}
			case CHOOSE_CLASS:
			{
				// ensure that a deselected item can't proceed
				return characterClasses.isEnabled(characterClasses.getSelected());
			}
			case CHOOSE_KIT:
			case CHOOSE_SPELLS:
			{
				// can save spell picks
				return true;
			}
			case EDIT_PERSONALS:
			{
				return nameField.getText().trim().length() > 0;
			}
			case FINISHED:
				return true;
			default: throw new MazeException("Illegal state: "+state);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void popupItemDetailsDialog(Item item)
	{
		DiyGuiUserInterface.instance.popupItemDetailsWidget(item);
	}

	/*-------------------------------------------------------------------------*/
	private static class StartingItemsComparator implements Comparator<StartingKit>
	{
		public int compare(StartingKit s1, StartingKit s2)
		{
			if (s1.getDisplayName().equalsIgnoreCase("default"))
			{
				return -1;
			}
			else if (s2.getDisplayName().equalsIgnoreCase("default"))
			{
				return 1;
			}
			else
			{
				return s1.getDisplayName().compareTo(s2.getDisplayName());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private class KitActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
		{
			DIYListBox listBox = (DIYListBox)event.getSource();

			StartingKit si = (StartingKit)listBox.getSelected();
			if (si != null)
			{
				setStartingKit(si);
				return true;
			}

			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private class ItemWidgetActionListener implements ActionListener
	{
		public boolean actionPerformed(ActionEvent event)
		{
			if (event.getSource() instanceof ItemWidget &&
				event.getEvent() instanceof MouseEvent)
			{
				Item item = ((ItemWidget)event.getSource()).getItem();
				if (item != null)
				{
					popupItemDetailsDialog(item);
					return true;
				}
			}

			return false;
		}
	}

	/*-------------------------------------------------------------------------*/
	private static class CharacterClassWrapper implements Comparable<CharacterClassWrapper>,
		DIYListBox.ListItemWithIcon
	{
		private final CharacterClass characterClass;

		private CharacterClassWrapper(CharacterClass cc)
		{
			this.characterClass = cc;
		}

		public String toString()
		{
			return this.characterClass.getName();
		}

		public int compareTo(CharacterClassWrapper other)
		{
			if (this.characterClass.getFocus() == other.characterClass.getFocus())
			{
				return this.characterClass.getName().compareTo(other.characterClass.getName());
			}
			else
			{
				return this.characterClass.getFocus().compareTo(other.characterClass.getFocus());
			}
		}

		@Override
		public Object getItem()
		{
			return this;
		}

		@Override
		public BufferedImage getIcon()
		{
			return switch (this.characterClass.getFocus())
				{
					case COMBAT -> Database.getInstance().getImage("item/combat");
					case STEALTH -> Database.getInstance().getImage("item/stealth");
					case MAGIC -> Database.getInstance().getImage("item/magic");
					default -> throw new MazeException("invalid: "+this.characterClass.getFocus());
				};
		}
	}
}
