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
import mclachlan.maze.ui.diygui.render.MazeRendererFactory;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.ui.diygui.Constants.Colour.GOLD;

/**
 *
 */
public class CreateCharacterWidget extends ContainerWidget implements ActionListener
{
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
	private Map<String, ContainerWidget> raceGenderWidgets = new HashMap<String, ContainerWidget>();
	private DIYTextField nameField;
	private DIYButton random, suggestName, showLevelAbilityProgression;
	private LevelAbilityProgressionWidget firstLevel;
	private Map<MagicSys.SpellBook, List<Widget>> spellBookWidgets = new HashMap<MagicSys.SpellBook, List<Widget>>();
	private Map<String, ContainerWidget> classAndRaceKitWidgets = new HashMap<String, ContainerWidget>();
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
	private StatModifierDisplayWidget modifierSummaryWidget1;
	private StatModifierDisplayWidget modifierSummaryWidget2;
	private StatModifierDisplayWidget modifierSummaryWidget3;

	private ActionListener kitListener = new KitActionListener();
	private ActionListener itemWidgetListener = new ItemWidgetActionListener();

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
	private int buttonPaneHeight = 50;
	private List<CharacterClassWrapper> characterClassList = getCharacterClassList();
	private SpellLearningWidget spellLearner;

	/** This is non-null if we're creating a character in a NPC guild. */
	private Npc npc;

	/*-------------------------------------------------------------------------*/
	public CreateCharacterWidget(Rectangle bounds)
	{
		super(bounds);
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

		// set to the first enabled selections
		for (Object obj : characterClasses.getItems())
		{
			if (characterClasses.isEnabled(obj))
			{
				characterClasses.setSelected(obj);
				break;
			}
		}

		this.setCharacterClass(
			((CharacterClassWrapper)characterClasses.getSelected()).characterClass.getName());

		setDefaultPersonality();
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

		titlePane = new DIYPane(new DIYFlowLayout(7,7, DIYToolkit.Align.CENTER));
		DIYLabel title = new DIYLabel(StringUtil.getUiLabel("cc.title"), DIYToolkit.Align.CENTER);
		titlePane.setBounds(x, y, width, 30);
		title.setForegroundColour(Constants.Colour.GOLD);
		Font defaultFont = DiyGuiUserInterface.instance.getDefaultFont();
		Font f = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+5);
		title.setFont(f);
		titlePane.add(title);

		characterTitle = getSubTitle("");
		characterTitle.setBounds(x+100, y+35, width-200, 20);

		this.add(characterTitle);

		buttonPane = new DIYPane(new DIYFlowLayout(15,10,DIYToolkit.Align.RIGHT));
		buttonPane.setInsets(new Insets(0,0,0,20));
		buttonPane.setBounds(0, height-buttonPaneHeight, width, buttonPaneHeight);

		buttonPane.add(previous);
		buttonPane.add(random);
		buttonPane.add(next);

		raceAndGenderPane = getRaceAndGenderPane();
		classesPane = getClassesPane();
		kitsPane = getKitsPane();
		spellsPane = getSpellsPane();
		personalsPane = getPersonalsPane();

		ArrayList<ContainerWidget> cards = new ArrayList<ContainerWidget>();
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
		int inset = 10;
		int columnWidth = width/3 - 2*inset;
		int column1 = 10;

		DIYPane pane = new DIYPane();

		int headerOffset = 50;
		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1, headerOffset, "cc.choose.spells.flava");

		spellLearner = new SpellLearningWidget(null,
			new Rectangle(column1, headerOffset+75,
				columnWidth*3, height-buttonPaneHeight-headerOffset-75));

		pane.add(titlePane);
		pane.add(stepFlavour);
		pane.add(spellLearner);
		pane.add(buttonPane);

		return pane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getKitsPane()
	{
		int inset = 10;
		int columnWidth = width/4 - 2*inset;
		int column1 = inset;
		int column2 = width/4 + inset;
		int column3 = (width/4)*2 + inset;
		int column4 = (width/4)*3 + inset;

		DIYPane pane = new DIYPane();

		int headerOffset = 50;
		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1, headerOffset, "cc.choose.kit.flava");

		DIYLabel kitTitle = getSubTitle(getLabel("cc.kit.title"));
		kitTitle.setForegroundColour(Constants.Colour.GOLD);
		kitTitle.setBounds(inset, headerOffset+50, columnWidth, 20);

		Rectangle bounds = new Rectangle(
			column1, headerOffset+50, columnWidth, (height - buttonPaneHeight - headerOffset));
		ArrayList<ContainerWidget> list = new ArrayList<ContainerWidget>();
		list.addAll(getClassKitWidgets(characterClassList));
		list.addAll(getRaceKitWidgets(getRaceList()));
		for (ContainerWidget cw : list)
		{
			cw.setBounds(column1, headerOffset+50+25, columnWidth, cw.getChildren().size()*15);
		}

		classAndRaceKitCards = new CardLayoutWidget(
			bounds,
			list);
		classAndRaceKitCards.show(list.get(0));

		kitDesc = new DIYTextArea("");
		kitDesc.setBounds(column2, headerOffset + 50 + 25, columnWidth * 2, 170);
		kitDesc.setTransparent(true);

		int kitItemsY = headerOffset+ height - 300;
		DIYLabel kitItemsLabel = getSubTitle(getLabel("cc.kit.items.title"));
		kitItemsLabel.setBounds(column2, kitItemsY, kitItemsLabel.getPreferredSize().width, 20);
		int itemCols = 2;
		int itemRows = 6;
		kitItems = new DIYPane(new DIYGridLayout(itemCols, itemRows,5,5));
		kitItems.setBounds(column2, kitItemsY+kitItemsLabel.getHeight()+5,
			columnWidth*2, 30*itemRows);
		for (int i=0; i<itemCols*itemRows; i++)
		{
			ItemWidget iw = new ItemWidget();
			iw.addActionListener(itemWidgetListener);
			kitItems.add(iw);
		}

		kitResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 100, 100, 100, true, false);
		kitModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		kitModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);
		kitModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, Stats.middleModifiers, false, false);

		kitResourcesWidget.setBounds(column4, headerOffset+50, columnWidth, 3*15);
		kitModifiersWidget1.setBounds(column4, headerOffset+50 +3*15, columnWidth, 6*15);
		kitModifiersWidget2.setBounds(column4, headerOffset+50 +3*15 +6*15, columnWidth, 9*15);
		kitModifiersWidget3.setBounds(column4, headerOffset+50 +3*15 +6*15 +9*15, columnWidth, 10 * 15);

		pane.add(titlePane);
		pane.add(stepFlavour);
		pane.add(kitTitle);
		pane.add(classAndRaceKitCards);
		pane.add(kitDesc);
		pane.add(kitItemsLabel);
		pane.add(kitItems);
		pane.add(kitDesc);
		pane.add(kitResourcesWidget);
		pane.add(kitModifiersWidget1);
		pane.add(kitModifiersWidget2);
		pane.add(kitModifiersWidget3);
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

		List<CharacterClassWrapper> result = new ArrayList<CharacterClassWrapper>();

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
		List<String> list = new ArrayList<String>(Database.getInstance().getPersonalities().keySet());
		Collections.sort(list);
		return list;
	}

	/*-------------------------------------------------------------------------*/
	private ArrayList<ContainerWidget> getClassKitWidgets(List<CharacterClassWrapper> classes)
	{
		ArrayList<ContainerWidget> result = new ArrayList<ContainerWidget>();

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
		ArrayList<ContainerWidget> result = new ArrayList<ContainerWidget>();

		for (String raceName : races)
		{
			Race race = Database.getInstance().getRace(raceName);

			List<StartingKit> startingItems = race.getStartingItems();
			if (startingItems == null || startingItems.isEmpty())
			{
				continue;
			}

			List<StartingKit> items = new ArrayList<StartingKit>();

			for (int i=0; i<startingItems.size(); i++)
			{
				items.add(startingItems.get(i));
			}

			Collections.sort(items, new StartingItemsComparator());

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
		int inset = 10;
		int columnWidth = width/4 - 2*inset;
		int column1 = inset;
		int column2 = width/4 + inset;
		int column3 = (width/4)*2 + inset;
		int column4 = (width/4)*3 + inset;

		DIYPane classesPane = new DIYPane();

		int headerOffset = 50;
		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1, headerOffset, "cc.choose.class.flava");

		DIYLabel classTitle = getSubTitle(getLabel("cc.class.title"));
		classTitle.setBounds(column1, headerOffset+50, columnWidth, 20);
		classTitle.setForegroundColour(Constants.Colour.GOLD);

		characterClasses = new DIYListBox(characterClassList);
		int classesHeight = characterClasses.getPreferredSize().height;
		characterClasses.setBounds(column1, headerOffset+50+25, columnWidth, classesHeight);
		characterClasses.setSelected(characterClassList.get(0));
		characterClasses.addActionListener(this);

		this.classDesc = new DIYTextArea("");
		this.classDesc.setBounds(column2, headerOffset+50+25, columnWidth*2, height/3);
		this.classDesc.setTransparent(true);

		int yyy = this.classDesc.y+ this.classDesc.height;

		DIYLabel spellBooks = getSubTitle(StringUtil.getUiLabel("cc.spell.books"));
		Dimension d = spellBooks.getPreferredSize();
		spellBooks.setBounds(column2, yyy, d.width, d.height);

		yyy = spellBooks.y + spellBooks.height + inset;

		this.spellBooksDesc = new DIYTextArea("");
		this.spellBooksDesc.setTransparent(true);
		this.spellBooksDesc.setBounds(
			column2, yyy,
			columnWidth*2, spellBooks.height*5); // 5 lines of text enough?

		yyy = spellBooksDesc.y + spellBooksDesc.height + inset;

		DIYLabel abilityProgression = getSubTitle(StringUtil.getUiLabel("cc.ability.progression"));
		d = abilityProgression.getPreferredSize();
		abilityProgression.setBounds(
			column2, yyy, d.width, d.height);

		yyy = abilityProgression.y + abilityProgression.height + inset;

		int levelsToPreview = 3;
		Rectangle r = new Rectangle(column2,
			yyy,
			columnWidth * 2, abilityProgression.height * levelsToPreview);
		firstLevel = new LevelAbilityProgressionWidget(null, levelsToPreview, r);
		firstLevel.doLayout();

		yyy = firstLevel.y+firstLevel.height+inset;

		showLevelAbilityProgression = new DIYButton(StringUtil.getUiLabel("cc.show.lap"));
		showLevelAbilityProgression.addActionListener(this);
		d = showLevelAbilityProgression.getPreferredSize();
		showLevelAbilityProgression.setBounds(
			column2, yyy, d.width, d.height);

		classResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, false, false);
		classModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		classModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);
		classModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, Stats.middleModifiers, false, false);

		classResourcesWidget.setBounds(column4, headerOffset+50, columnWidth, 3*15);
		classModifiersWidget1.setBounds(column4, headerOffset + 50 + 3 * 15, columnWidth, 6 * 15);
		classModifiersWidget2.setBounds(column4, headerOffset+50 +3*15+ 6*15, columnWidth, 9*15);
		classModifiersWidget3.setBounds(column4, headerOffset+50 +3*15 +6*15 +9*15, columnWidth, 10*15);

		classesPane.add(titlePane);

		classesPane.add(classTitle);
		classesPane.add(stepFlavour);

		classesPane.add(characterClasses);

		classesPane.add(classDesc);
		classesPane.add(spellBooks);
		classesPane.add(spellBooksDesc);
		classesPane.add(abilityProgression);
		classesPane.add(firstLevel);
		classesPane.add(showLevelAbilityProgression);
		classesPane.add(classResourcesWidget);
		classesPane.add(classModifiersWidget1);
		classesPane.add(classModifiersWidget2);
		classesPane.add(classModifiersWidget3);
		classesPane.add(buttonPane);

		return classesPane;
	}

	/*-------------------------------------------------------------------------*/
	private DIYPane getPersonalsPane()
	{
		int inset = 10;
		int columnWidth = width/4 - 2*inset;
		int column1 = inset;
		int column2 = width/4 + inset;
		int column3 = (width/4)*2 + inset;
		int column4 = (width/4)*3 + inset;

		DIYPane personals = new DIYPane();

		int headerOffset = 50;

		DIYTextArea stepFlavour = getStepFlavourArea(inset, column1, headerOffset, "cc.choose.personals.flava");

		List<String> personalityList = getPersonalityList();
		DIYLabel personalityTitle = getSubTitle(getLabel("cc.personality"));
		personalityTitle.setForegroundColour(Constants.Colour.GOLD);
		personalityTitle.setBounds(column1, headerOffset+50, columnWidth, 20);
		personalities = new DIYListBox(personalityList);
		personalities.setBounds(column1, headerOffset+50+25, columnWidth,
			personalities.getPreferredSize().height);
		setDefaultPersonality();
		personalities.addActionListener(this);

		DIYLabel nameLabel = getSubTitle(getLabel("cc.character.name"));
		nameLabel.setForegroundColour(Constants.Colour.GOLD);
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

		DIYPane namePane = new DIYPane(new DIYFlowLayout(10, 0, DIYToolkit.Align.CENTER));

		suggestName = new DIYButton(getLabel("cc.suggest.name"));
		suggestName.addActionListener(this);

		namePane.add(nameLabel);
		namePane.add(nameField);
		namePane.add(suggestName);
		namePane.setBounds(column2, headerOffset+50+25, columnWidth*2, 20);

		DIYLabel portraitTitle = getSubTitle(getLabel("cc.character.portrait"));
		portraitTitle.setForegroundColour(Constants.Colour.GOLD);
		portraitTitle.setBounds(column2, headerOffset+50+25+20+20, columnWidth*2, 20);
		portraitWidget = new PortraitSelectionWidget(column2, headerOffset+50+25+20+20, columnWidth*2, 150);

		resourcesSummaryWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, false, false);
		modifierSummaryWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		modifierSummaryWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);
		modifierSummaryWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, Stats.middleModifiers, false, false);

		resourcesSummaryWidget.setBounds(column4, headerOffset+50, columnWidth, 3*15);
		modifierSummaryWidget1.setBounds(column4, headerOffset+50 +3*15, columnWidth, 6*15);
		modifierSummaryWidget2.setBounds(column4, headerOffset+50 +3*15 +6*15, columnWidth, 9*15);
		modifierSummaryWidget3.setBounds(column4, headerOffset+50 +3*15 +6*15 +9*15, columnWidth, 10*15);

		personals.add(titlePane);
		personals.add(stepFlavour);

		personals.add(portraitTitle);
		personals.add(portraitWidget);
		personals.add(namePane);

		personals.add(resourcesSummaryWidget);
		personals.add(modifierSummaryWidget1);
		personals.add(modifierSummaryWidget2);
		personals.add(modifierSummaryWidget3);

		personals.add(personalityTitle);
		personals.add(personalities);

		personals.add(buttonPane);

		return personals;
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
		int inset = 10;
		int columnWidth = width/4 - 2*inset;
		int column1 = inset;
		int column2 = width/4 + inset;
		int column3 = (width/4)*2 + inset;
		int column4 = (width/4)*3 + inset;

		DIYPane pane = new DIYPane();

		int headerOffset = 50;

		DIYTextArea stepFlavour = getStepFlavourArea(
			inset, column1, headerOffset, "cc.choose.race.and.gender.flava");

		List<String> raceList = getRaceList();
		DIYLabel raceTitle = getSubTitle(getLabel("cc.race.title"));
		raceTitle.setForegroundColour(Constants.Colour.GOLD);
		raceTitle.setBounds(inset, headerOffset+50, columnWidth, 20);
		races = new DIYListBox(raceList);
		races.setBounds(column1, headerOffset+50+25, columnWidth, races.getPreferredSize().height);
		setDefaultSelectedRace(raceList);
		races.addActionListener(this);

		int nrGenders = ((List<String>)new ArrayList<>(Database.getInstance().getGenders().keySet())).size();
		ArrayList<ContainerWidget> genders = new ArrayList<ContainerWidget>();

		raceDesc = new DIYTextArea("");
		raceDesc.setBounds(column2, headerOffset + 50 + 25, columnWidth * 2, height/3);
		raceDesc.setTransparent(true);

		int genderY = headerOffset+height - 330;
		for (String raceName : raceList)
		{
			Race race = Database.getInstance().getRace(raceName);
			List<Gender> raceGenders = race.getAllowedGenders();
			Collections.sort(raceGenders, new Comparator<Gender>()
			{
				public int compare(Gender o1, Gender o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});

			DIYPane temp = new DIYPane();
			DIYButtonGroup genderBG = new DIYButtonGroup();
			int count = 0;
			for (Gender g : raceGenders)
			{
				DIYRadioButton radioButton = new DIYRadioButton(g.getName(), count==0);
				temp.add(radioButton);
				radioButton.addActionListener(this);
				radioButton.setActionMessage(SET_GENDER);
				radioButton.setBounds(column1, genderY+20*(count+1), columnWidth, 20);
				genderBG.addButton(radioButton);
				count++;
			}

			raceGenderWidgets.put(raceName, temp);
			genders.add(temp);
		}

		raceImage = new DIYLabel();
		raceImage.setBounds(column2, raceDesc.y+raceDesc.height+inset,
			columnWidth*2, raceDesc.height);

		DIYLabel genderLabel = getSubTitle(getLabel("cc.gender.title"));
		genderLabel.setBounds(column1, genderY, genderLabel.getPreferredSize().width, 20);
		raceGenderChoices = new CardLayoutWidget(
			new Rectangle(column1, genderY, columnWidth, 20*nrGenders),
			genders);
		raceGenderChoices.doLayout();

		raceResourcesWidget = new ResourcesDisplayWidget(
			getLabel("cc.resources"), 0, 0, 0, true, false);
		raceModifiersWidget1 = new StatModifierDisplayWidget(
			getLabel("cc.attributes"), null, 6, Stats.attributeModifiers, true, false);
		raceModifiersWidget2 = new StatModifierDisplayWidget(
			getLabel("cc.resistances"), null, 9, Stats.resistances, true, false);

		List<Stats.Modifier> otherModifiers = new ArrayList<Stats.Modifier>(Stats.allModifiers);
		otherModifiers.removeAll(Stats.resourceModifiers);
		otherModifiers.removeAll(Stats.attributeModifiers);
		otherModifiers.removeAll(Stats.resistances);

		raceModifiersWidget3 = new StatModifierDisplayWidget(
			getLabel("cc.other.modifiers"), null, 10, otherModifiers, false, false);

		raceResourcesWidget.setBounds(column4, headerOffset+50, columnWidth, 3*15);
		raceModifiersWidget1.setBounds(column4, headerOffset+50 +3*15, columnWidth, 6*15);
		raceModifiersWidget2.setBounds(column4, headerOffset+50 +3*15 +6*15, columnWidth, 9*15);
		raceModifiersWidget3.setBounds(column4, headerOffset+50 +3*15 +6*15 +9*15, columnWidth, 10*15);

		pane.add(titlePane);
		pane.add(stepFlavour);

		pane.add(genderLabel);
		pane.add(raceGenderChoices);

		pane.add(raceTitle);
		pane.add(races);
		pane.add(raceImage);
		pane.add(raceResourcesWidget);
		pane.add(raceModifiersWidget1);
		pane.add(raceModifiersWidget2);
		pane.add(raceModifiersWidget3);
		pane.add(raceDesc);

		pane.add(buttonPane);

		return pane;
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

		switch(state)
		{
			case CHOOSE_RACE_AND_GENDER:
				this.cardLayout.show(raceAndGenderPane);
				break;
			case CHOOSE_CLASS:
				this.cardLayout.show(classesPane);
				break;
			case CHOOSE_KIT:
				this.cardLayout.show(kitsPane);
				break;
			case CHOOSE_SPELLS:

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
				break;
			case EDIT_PERSONALS:

				int hp = Leveler.calcStartingHitPoints(characterClass, race);
				int ap = Leveler.calcStartingActionPoints(characterClass, race);
				int mp = Leveler.calcStartingMagicPoints(characterClass, race);
				resourcesSummaryWidget.display(hp, ap, mp, false);

				StatModifier summary1 = new StatModifier();
				summary1.addModifiers(raceModifiersWidget1.getStatModifier());
				summary1.addModifiers(classModifiersWidget1.getStatModifier());
				summary1.addModifiers(kitModifiersWidget1.getStatModifier());
				modifierSummaryWidget1.setStatModifier(summary1, false);

				StatModifier summary2 = new StatModifier();
				summary2.addModifiers(raceModifiersWidget2.getStatModifier());
				summary2.addModifiers(classModifiersWidget2.getStatModifier());
				summary2.addModifiers(kitModifiersWidget2.getStatModifier());
				modifierSummaryWidget2.setStatModifier(summary2, false);

				StatModifier summary3 = new StatModifier();
				summary3.addModifiers(raceModifiersWidget3.getStatModifier());
				summary3.addModifiers(classModifiersWidget3.getStatModifier());
				summary3.addModifiers(kitModifiersWidget3.getStatModifier());
				modifierSummaryWidget3.setStatModifier(summary3, false);
				
				this.cardLayout.show(personalsPane);
				break;
			case FINISHED:

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

				break;
			default: throw new MazeException("Illegal state: "+state);
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
	private boolean mustChooseSpells()
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

			if (state == CHOOSE_SPELLS && !mustChooseSpells())
			{
				this.state++;
			}

			updateState();
			return true;
		}
		else if (obj == previous)
		{
			this.state--;

			if (state == CHOOSE_SPELLS && !mustChooseSpells())
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
			portraitWidget.setToRaceAndGender(raceName, gender.getName(), portraitWidget.portraits);
			return true;
		}
		else if (obj == characterClasses)
		{
			String className = ((CharacterClassWrapper)characterClasses.getSelected()).characterClass.getName();
			setCharacterClass(className);
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
				this.portraitWidget.imagePanel.getBounds());
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
				case CHOOSE_RACE_AND_GENDER:
					String raceName = Leveler.getRandomRace();
					races.setSelected(raceName);
					setRace(raceName);

					Gender genderInst = Leveler.getRandomGender(raceName);
					setGender(genderInst.getName());
					break;

				case CHOOSE_CLASS:
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

					break;

				case CHOOSE_KIT:
					DIYListBox kits = (DIYListBox)classAndRaceKitCards.getCurrentWidget();

					StartingKit kit = Leveler.getRandomStartingKit(
						characterClass.getName(),
						race.getName());

					if (kit != null)
					{
						kits.setSelected(kit);
						setStartingKit(kit);
					}
					break;

				case CHOOSE_SPELLS:
					spellLearner.clear();
					List<Spell> randomSpells = Leveler.getRandomSpells(
						spellLearner.getPlayerCharacter());
					spellLearner.setSelectedSpells(randomSpells);
					break;

				case EDIT_PERSONALS:
					portraitWidget.setToRaceAndGender(race.getName(), gender.getName(), portraitWidget.portraits);
					suggestName(race, gender);

					Personality p = Leveler.getRandomPersonality();
					personalities.setSelected(p.getName());
					setPersonality(p.getName());

					String portraitName = Leveler.getRandomPortraitName(race.getName(), gender.getName());
					portraitWidget.setToPortrait(portraitName);

					break;
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
					.append(" (max lvl ").append(ssb.getMaxLevel()).append(")\n")
					.append(ssb.getDescription());
			}
			this.spellBooksDesc.setText(sb.toString());
		}

		// todo: should we pick up the and lvl1 LAP values in the resources display?
		this.classResourcesWidget.display(
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
		this.raceResourcesWidget.display(
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
		if (race.getStartingItems() != null && !race.getStartingItems().isEmpty())
		{
			this.startingKit = si;
		}
		else
		{
			this.startingKit = si;
		}

		kitDesc.setText(startingKit.getDescription()==null?"(null)":startingKit.getDescription());

		StatModifier mod;
		switch (characterClass.getFocus())
		{
			case COMBAT:
				mod = this.startingKit.getCombatModifiers();
				break;
			case STEALTH:
				mod = this.startingKit.getStealthModifiers();
				break;
			case MAGIC:
				mod = this.startingKit.getMagicModifiers();
				break;
			default: throw new MazeException("Invalid focus "+characterClass.getFocus());
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
				event.getEvent() instanceof MouseEvent &&
				((MouseEvent)event.getEvent()).getButton() == MouseEvent.BUTTON3)
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
	private static class CharacterClassWrapper implements Comparable<CharacterClassWrapper>
	{
		CharacterClass characterClass;
		String desc;

		private CharacterClassWrapper(CharacterClass cc)
		{
			this.characterClass = cc;

			StringBuilder sb = new StringBuilder();
			sb.append(cc.getName());

			for (int i=0; i<15-cc.getName().length(); i++)
			{
				sb.append(' ');
			}
			sb.append("(");
			sb.append(cc.getFocus().toString().toLowerCase());
			sb.append(")");

			this.desc = sb.toString();
		}

		public String toString()
		{
			return this.desc;
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
	}
}
