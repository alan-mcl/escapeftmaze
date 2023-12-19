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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.event.*;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.npc.NpcFaction;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.v1.V1MazeEvent.*;


/**
 *
 */
public class MazeEventEditor extends JDialog implements ActionListener
{
	public static final int MAX = 400;
	
	private static String[] FACINGS =
		{
			"Unchanged",
			"North",
			"South",
			"East",
			"West",
		};

	private MazeEvent result;

	private int[] dialogLookup = new int[MAX];

	private int dirtyFlag;
	private CardLayout cards;
	private JPanel controls;
	private JButton ok, cancel;
	private JComboBox type;
	private JTextField impl;
	private JComboBox zone;
	private JSpinner zoneX, zoneY;
	private JComboBox zoneFacing;
	private JComboBox spell;
	private JSpinner casterLevel, castingLevel;
	private JComboBox encounterTable;
	private JTextField encounterMazeVariable;
	private JComboBox encounterAttitude;
	private JComboBox encounterAmbushStatus;
	private JComboBox encounterPreScript, encounterPostAppearanceScript;
	private JSpinner flavourTextDelay;
	private JCheckBox shouldClearText;
	private JTextArea flavourText;
	private JSpinner xpAmount;
	private JSpinner goldAmount;
	private JComboBox lootTable;
	private JTextArea clipNames;
	private JTextArea trackNames;
	private JTextField musicState;
	private JSpinner delay;
	private JTextArea signBoardText;
	private JComboBox movePartyFacing;
	private JSpinner movePartyX, movePartyY;
	private Map<String, JTextArea> cckClassesMap;
	private JTextField speechKey;
	private JCheckBox modalSpeech;
	private JTextField storyboardImageResource, storyboardTextKey;
	private JComboBox storyboardTextPlacement;
	private JTextField setUserConfigVar, setUserConfigValue;
	private AnimationPanel animation;
	private JComboBox mazeScript;
	private JTextField removeWallMazeVariable;
	private JSpinner removeWallWallIndex;
	private JCheckBox removeWallIsHoriz;
	private JTextField blockingScreenImage;
	private JSpinner blockingScreenMode;
	private JTextField setMazeVarMazeVar, setMazeVarValue;
	private JSpinner fromX, fromY;
	private JComboBox facing;

	/*-------------------------------------------------------------------------*/
	public MazeEventEditor(Frame owner, MazeEvent event, int dirtyFlag) throws HeadlessException
	{
		super(owner, "Edit Maze Event", true);
		this.dirtyFlag = dirtyFlag;

		for (int i = 0; i < dialogLookup.length; i++)
		{
			dialogLookup[i] = -1;
		}

		JPanel top = new JPanel();
		Vector<String> vec = new Vector<String>();
		for (int i=0; i<MAX; i++)
		{
			String str = describeType(i);
			if (str != null)
			{
				int index = vec.size();
				dialogLookup[index] = i;
				vec.addElement(str);
			}
		}
		type = new JComboBox(vec);
		type.addActionListener(this);
		top.add(new JLabel("Type"));
		top.add(type);

		cards = new CardLayout(3, 3);
		controls = new JPanel(cards);
		for (int i=0; i<MAX; i++)
		{
			JPanel c = getControls(i);
			if (c != null)
			{
				controls.add(c, String.valueOf(i));
			}
		}

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (event != null)
		{
			setState(event);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void setState(MazeEvent e)
	{
		int meType;
		if (types.containsKey(e.getClass()))
		{
			meType = types.get(e.getClass());
		}
		else
		{
			meType = CUSTOM;
		}

		for (int i = 0; i < dialogLookup.length; i++)
		{
			if (dialogLookup[i] == meType)
			{
				type.setSelectedIndex(i);
				break;
			}
		}

		switch (meType)
		{
			case CUSTOM:
				impl.setText(e.getClass().getName());
				break;
			case _ZoneChangeEvent:
				ZoneChangeEvent zce = (ZoneChangeEvent)e;
				zone.setSelectedItem(zce.getZone());
				zoneX.setValue(zce.getPos().x);
				zoneY.setValue(zce.getPos().y);
				zoneFacing.setSelectedIndex(zce.getFacing());
				break;
			case _CastSpellEvent:
				CastSpellEvent cse = (CastSpellEvent)e;
				spell.setSelectedItem(cse.getSpellName());
				casterLevel.setValue(cse.getCasterLevel());
				castingLevel.setValue(cse.getCastingLevel());
				break;
			case _EncounterActorsEvent:
				EncounterActorsEvent ee = (EncounterActorsEvent)e;
				encounterMazeVariable.setText(ee.getMazeVariable());
				encounterTable.setSelectedItem(ee.getEncounterTable());
				encounterAttitude.setSelectedItem(ee.getAttitude() == null ? EditorPanel.NONE : ee.getAttitude());
				encounterAmbushStatus.setSelectedItem(ee.getAmbushStatus() == null ? Combat.AmbushStatus.NONE : ee.getAmbushStatus());
				encounterPreScript.setSelectedItem(ee.getPreScript() == null ? EditorPanel.NONE : ee.getPreScript());
				encounterPostAppearanceScript.setSelectedItem(ee.getPostAppearanceScript() == null ? EditorPanel.NONE : ee.getPostAppearanceScript());
				break;
			case _FlavourTextEvent:
				FlavourTextEvent fte = (FlavourTextEvent)e;
				flavourText.setText(fte.getFlavourText());
				flavourTextDelay.setValue(fte.getDelay());
				shouldClearText.setSelected(fte.shouldClearText());
				break;
			case _GrantExperienceEvent:
				GrantExperienceEvent gee = (GrantExperienceEvent)e;
				xpAmount.setValue(gee.getAmount());
				break;
			case _GrantGoldEvent:
				GrantGoldEvent gge = (GrantGoldEvent)e;
				goldAmount.setValue(gge.getAmount());
				break;
			case _GrantItemsEvent:
				break;
			case _SignBoardEvent:
				SignBoardEvent sbe = (SignBoardEvent)e;
				signBoardText.setText(sbe.getSignBoardText());
				break;
			case _LootTableEvent:
				LootTableEvent lte = (LootTableEvent)e;
				lootTable.setSelectedItem(lte.getLootTable().getName());
				break;
			case _DelayEvent:
				DelayEvent de = (DelayEvent)e;
				delay.setValue(de.getDelay());
				break;
			case _MovePartyEvent:
				MovePartyEvent mpe = (MovePartyEvent)e;
				movePartyFacing.setSelectedIndex(mpe.getFacing());
				movePartyX.setValue(mpe.getPos().x);
				movePartyY.setValue(mpe.getPos().y);
				break;
			case _CharacterClassKnowledgeEvent:
				CharacterClassKnowledgeEvent ccke = (CharacterClassKnowledgeEvent)e;
				Map<String, String> map = ccke.getKnowledgeText();
				for (String characterClass : map.keySet())
				{
					JTextArea textArea = cckClassesMap.get(characterClass);
					textArea.setText(map.get(characterClass));
				}
				break;
			case _PersonalitySpeechEvent:
				PersonalitySpeechBubbleEvent spbe = (PersonalitySpeechBubbleEvent)e;
				speechKey.setText(spbe.getSpeechKey());
				modalSpeech.setSelected(spbe.isModal());
				break;
			case _StoryboardEvent:
				StoryboardEvent se = (StoryboardEvent)e;
				storyboardImageResource.setText(se.getImageResource());
				storyboardTextKey.setText(se.getTextResource());
				storyboardTextPlacement.setSelectedItem(se.getTextPlacement());
				break;
			case _SetUserConfigEvent:
				SetUserConfigEvent suce = (SetUserConfigEvent)e;
				setUserConfigVar.setText(suce.getVar());
				setUserConfigValue.setText(suce.getValue());
				break;

			case _MazeScript:
				MazeScriptEvent mse = (MazeScriptEvent)e;
				mazeScript.setSelectedItem(mse.getScript());
				break;
			case _RemoveWall:
				RemoveWallEvent rwe = (RemoveWallEvent)e;
				removeWallIsHoriz.setSelected(rwe.isHorizontalWall());
				removeWallMazeVariable.setText(rwe.getMazeVariable());
				removeWallWallIndex.setValue(rwe.getWallIndex());
				break;
			case _BlockingScreen:
				BlockingScreenEvent bse = (BlockingScreenEvent)e;
				blockingScreenImage.setText(bse.getImageResource());
				blockingScreenMode.setValue(bse.getMode());
				break;
			case _EndGame:
				break;
			case _SetMazeVariableEvent:
				SetMazeVariableEvent sme = (SetMazeVariableEvent)e;
				setMazeVarMazeVar.setText(sme.getMazeVariable());
				setMazeVarValue.setText(sme.getValue());
				break;
			case _TogglePortalStateEvent:
				TogglePortalStateEvent tpse = (TogglePortalStateEvent)e;
				fromX.setValue(tpse.getTile().x);
				fromY.setValue(tpse.getTile().y);
				facing.setSelectedItem(FACINGS[tpse.getFacing()]);
				break;

			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent	:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
				break;
			case _SoundEffectEvent:
				SoundEffectEvent see = (SoundEffectEvent)e;
				List<String> clipNames1 = see.getClipNames();
				StringBuilder sb = new StringBuilder();
				for (String s : clipNames1)
				{
					sb.append(s).append('\n');
				}
				clipNames.setText(sb.toString());
				break;
			case _MusicEvent:
				MusicEvent me = (MusicEvent)e;
				List<String> trackNames1 = me.getTrackNames();
				StringBuilder sb1 = new StringBuilder();
				for (String s : trackNames1)
				{
					sb1.append(s).append('\n');
				}
				trackNames.setText(sb1.toString());
				musicState.setText(me.getMusicState()==null?"":me.getMusicState());
				break;
			case _AnimationEvent:
				AnimationEvent ae = (AnimationEvent)e;
				animation.setState(ae.getAnimation());
				break;

			case _ChangeNpcFactionAttitudeEvent:
			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:

			default:
		}
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		int meType = dialogLookup[type.getSelectedIndex()];

		switch (meType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					this.result = (MazeEvent)clazz.newInstance();
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case _ZoneChangeEvent:
				this.result = new ZoneChangeEvent(
					(String)zone.getSelectedItem(),
					new Point((Integer)zoneX.getValue(), (Integer)zoneY.getValue()),
					zoneFacing.getSelectedIndex());
				break;
			case _CastSpellEvent:
				this.result = new CastSpellEvent(
					(String)spell.getSelectedItem(),
					(Integer)casterLevel.getValue(),
					(Integer)castingLevel.getValue());
				break;
			case _EncounterActorsEvent:
				NpcFaction.Attitude attitude = encounterAttitude.getSelectedItem() == EditorPanel.NONE ? null : (NpcFaction.Attitude)encounterAttitude.getSelectedItem();
				Combat.AmbushStatus ambushStatus = encounterAmbushStatus.getSelectedItem() == EditorPanel.NONE ? null : (Combat.AmbushStatus)encounterAmbushStatus.getSelectedItem();
				String preScript = encounterPreScript.getSelectedItem() == EditorPanel.NONE ? null : (String)encounterPreScript.getSelectedItem();
				String postAppearanceScript = encounterPostAppearanceScript.getSelectedItem() == EditorPanel.NONE ? null : (String)encounterPostAppearanceScript.getSelectedItem();

				this.result = new EncounterActorsEvent(
					encounterMazeVariable.getText(),
					(String)encounterTable.getSelectedItem(),
					attitude,
					ambushStatus,
					preScript,
					postAppearanceScript);
				break;
			case _FlavourTextEvent:
				this.result = new FlavourTextEvent(
					flavourText.getText(),
					(Integer)flavourTextDelay.getValue(),
					shouldClearText.isSelected());
				break;
			case _GrantExperienceEvent:
				this.result = new GrantExperienceEvent(
					(Integer)xpAmount.getValue(),
					null);
				break;
			case _GrantGoldEvent:
				this.result = new GrantGoldEvent(
					(Integer)goldAmount.getValue());
				break;
			case _GrantItemsEvent:
				break;
			case _SignBoardEvent:
				String s = signBoardText.getText();
				this.result = new SignBoardEvent(s);
				break;
			case _LootTableEvent:
				this.result = new LootTableEvent(
					Database.getInstance().getLootTable((String)lootTable.getSelectedItem()));
				break;
			case _DelayEvent:
				this.result = new DelayEvent((Integer)delay.getValue());
				break;
			case _MovePartyEvent:
				this.result = new MovePartyEvent(
					new Point((Integer)movePartyX.getValue(), (Integer)movePartyY.getValue()), 
					movePartyFacing.getSelectedIndex());
				break;
			case _CharacterClassKnowledgeEvent:
				Map<String, String> kMap = new HashMap<>();
				for (String c : cckClassesMap.keySet())
				{
					String value = cckClassesMap.get(c).getText();
					if (value != null && value.length() > 0)
					{
						kMap.put(c, value);
					}
				}
				this.result = new CharacterClassKnowledgeEvent(kMap);
				break;
			case _PersonalitySpeechEvent:
				this.result = new PersonalitySpeechBubbleEvent(speechKey.getText(), modalSpeech.isSelected());
				break;
			case _StoryboardEvent:
				this.result = new StoryboardEvent(
					storyboardImageResource.getText(),
					storyboardTextKey.getText(),
					(StoryboardEvent.TextPlacement)storyboardTextPlacement.getSelectedItem());
				break;
			case _SetUserConfigEvent:
				this.result = new SetUserConfigEvent(
					setUserConfigVar.getText(),
					setUserConfigValue.getText());
				break;

			case _MazeScript:
				this.result = new MazeScriptEvent((String)mazeScript.getSelectedItem());
				break;
			case _RemoveWall:
				this.result = new RemoveWallEvent(
					removeWallMazeVariable.getText(), 
					removeWallIsHoriz.isSelected(), 
					(Integer)removeWallWallIndex.getValue());
				break;
			case _BlockingScreen:
				this.result = new BlockingScreenEvent(
					blockingScreenImage.getText(),
					(Integer)(blockingScreenMode.getValue()));
				break;
			case _EndGame:
				this.result = new EndGameEvent();
				break;
			case _SetMazeVariableEvent:
				this.result = new SetMazeVariableEvent(setMazeVarMazeVar.getText(), setMazeVarValue.getText());
				break;
			case _TogglePortalStateEvent:
				this.result = new TogglePortalStateEvent(new Point((Integer)fromX.getValue(), (Integer)fromY.getValue()), facing.getSelectedIndex());
				break;

			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent	:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
				break;
			case _SoundEffectEvent:
				List<String> list = Arrays.asList(clipNames.getText().split("\\n"));
				this.result = new SoundEffectEvent(list);
				break;
			case _MusicEvent:
				List<String> list1 = Arrays.asList(trackNames.getText().split("\\n"));
				this.result = new MusicEvent(list1, "".equals(musicState.getText())?null:musicState.getText());
				break;
			case _AnimationEvent:
				this.result = new AnimationEvent(animation.getAnimation());
				break;

			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:

			default:
		}
	}

	public MazeEvent getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getControls(int i)
	{
		switch (i)
		{
			case CUSTOM:
				return getCustomPanel();
			case _ZoneChangeEvent:
				return getZoneChangePanel();
			case _CastSpellEvent:
				return getCastSpellPanel();
			case _EncounterActorsEvent:
				return getEncounterPanel();
			case _FlavourTextEvent:
				return getFlavourTextPanel();
			case _GrantExperienceEvent:
				return getGrantExperiencePanel();
			case _GrantGoldEvent:
				return getGrantGoldPanel();
			case _GrantItemsEvent:
				return null;
			case _SignBoardEvent:
				return getSignBoardPanel();
			case _LootTableEvent:
				return getLootTablePanel();
			case _DelayEvent:
				return getDelayEventPanel();
			case _MovePartyEvent:
				return getMovePartyPanel();
			case _CharacterClassKnowledgeEvent:
				return getCharacterClassKnowledgePanel();
			case _PersonalitySpeechEvent:
				return getSpeechBubbleEventPanel();
			case _StoryboardEvent:
				return getStoryboardEventPanel();
			case _SetUserConfigEvent:
				return getSetUserConfigEventPanel();
			case _MazeScript:
				return getMazeScriptEventPanel();
			case _RemoveWall:
				return getRemoveWallPanel();
			case _BlockingScreen:
				return getBlockingScreenPanel();
			case _EndGame:
				return getEndGamePanel();
			case _SetMazeVariableEvent:
				return getSetMazeVarPanel();
			case _TogglePortalStateEvent:
				return getTogglePortalStatePanel();
				
			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent	:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
				return null;
			case _SoundEffectEvent:
				return getSoundEffectPanel();
			case _MusicEvent:
				return getMusicPanel();
			case _AnimationEvent:
				return getAnimationPanel();

			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:

			default: return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getTogglePortalStatePanel()
	{
		fromX = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		fromY = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		facing = new JComboBox(FACINGS);

		return dirtyGridBagCrap(
			new JLabel("From X:"), fromX,
			new JLabel("From Y:"), fromY,
			new JLabel("Facing:"), facing);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSetUserConfigEventPanel()
	{
		setUserConfigVar = new JTextField(25);
		setUserConfigValue = new JTextField(25);

		return dirtyGridBagCrap(
			new JLabel("User Config Key:"), setUserConfigVar,
			new JLabel("Value:"), setUserConfigValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSetMazeVarPanel()
	{
		setMazeVarMazeVar = new JTextField(25);
		setMazeVarValue = new JTextField(25);

		return dirtyGridBagCrap(
			new JLabel("Maze Variable:"), setMazeVarMazeVar,
			new JLabel("Value:"), setMazeVarValue);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getEndGamePanel()
	{
		return new JPanel();
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getBlockingScreenPanel()
	{
		blockingScreenImage = new JTextField(20);
		blockingScreenMode = new JSpinner(new SpinnerListModel(new Integer[]{-1, 1}));
		
		return dirtyGridBagCrap(
			new JLabel("Image Resource:"), blockingScreenImage,
			new JLabel("Mode:"), blockingScreenMode);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRemoveWallPanel()
	{
		removeWallMazeVariable = new JTextField(20);
		removeWallWallIndex = new JSpinner(new SpinnerNumberModel(0, 0, 999999, 1));
		removeWallIsHoriz = new JCheckBox("Horizontal?");
		
		return dirtyGridBagCrap(
			new JLabel("Maze Variable:"), removeWallMazeVariable,
			new JLabel("Wall Index:"), removeWallWallIndex,
			removeWallIsHoriz, new JLabel());
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getMazeScriptEventPanel()
	{
		Vector<String> scripts = 
			new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(scripts);
		mazeScript = new JComboBox(scripts);

		JButton edit = new JButton("Edit Maze Scripts...");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new EditorPanelDialog("Edit Maze Scripts", new MazeScriptPanel());

				Vector<String> scripts =
					new Vector<String>(Database.getInstance().getMazeScripts().keySet());
				Collections.sort(scripts);
				mazeScript.setModel(new DefaultComboBoxModel(scripts));
			}
		});
		
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Maze Script: "), mazeScript,
			new JLabel(), edit);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCharacterClassKnowledgePanel()
	{
		JTabbedPane cckTabs = new JTabbedPane(JTabbedPane.LEFT);
		cckClassesMap = new HashMap<String, JTextArea>();

		List<String> classes = new ArrayList<>(Database.getInstance().getCharacterClasses().keySet());
		Collections.sort(classes);

		for (String c : classes)
		{
			JTextArea knowledgeText = new JTextArea();
			knowledgeText.setWrapStyleWord(true);
			knowledgeText.setLineWrap(true);
			knowledgeText.setColumns(30);
			knowledgeText.setRows(10);

			cckTabs.addTab(c, new JScrollPane(knowledgeText));
			cckClassesMap.put(c, knowledgeText);
		}

		JPanel result = new JPanel(new BorderLayout());
		result.add(new JScrollPane(
			cckTabs,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
			BorderLayout.CENTER);		

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSpeechBubbleEventPanel()
	{
		JPanel result = new JPanel();

		speechKey = new JTextField(40);
		modalSpeech = new JCheckBox("Modal?");

		dirtyGridLayoutCrap(
			result,
			new JLabel("Speech Key:"), speechKey,
			modalSpeech, new JLabel());

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getStoryboardEventPanel()
	{
		JPanel result = new JPanel();

		storyboardImageResource = new JTextField(40);
		storyboardTextKey = new JTextField(40);
		storyboardTextPlacement = new JComboBox(StoryboardEvent.TextPlacement.values());

		dirtyGridLayoutCrap(
			result,
			new JLabel("Image Resource:"), storyboardImageResource,
			new JLabel("Text Resource:"), storyboardTextKey,
			new JLabel("Text Placement:"), storyboardTextPlacement);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getMovePartyPanel()
	{
		movePartyFacing = new JComboBox(FACINGS);
		movePartyX = new JSpinner(new SpinnerNumberModel(0, -1, 256, 1));
		movePartyY = new JSpinner(new SpinnerNumberModel(0, -1, 256, 1));
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("X: "), movePartyX,
			new JLabel("Y: "), movePartyY,
			new JLabel("Facing: "), movePartyFacing);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSignBoardPanel()
	{
		signBoardText = new JTextArea();
		signBoardText.setWrapStyleWord(true);
		signBoardText.setLineWrap(true);
		signBoardText.setColumns(30);
		signBoardText.setRows(10);
		
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Text: "), new JScrollPane(signBoardText));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getDelayEventPanel()
	{
		delay = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(result, new JLabel("Delay (ms):"), delay);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getAnimationPanel()
	{
		animation = new AnimationPanel();
		return animation;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSoundEffectPanel()
	{
		clipNames = new JTextArea(20, 30);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel(), new JLabel("One clip per line, a random one will be played"),
			new JLabel("Clips: "), clipNames);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getMusicPanel()
	{
		trackNames = new JTextArea(20, 30);
		musicState = new JTextField(20);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Music State:"), musicState,
			new JLabel(), new JLabel("One track per line, they will be looped in random order"),
			new JLabel("Tracks: "), trackNames);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLootTablePanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getLootTables().keySet());
		Collections.sort(vec);
		lootTable = new JComboBox(vec);

		JButton edit = new JButton("Edit Loot Tables...");
		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new EditorPanelDialog("Edit Loot Tables", new LootTablePanel());

				Vector<String> vec =
					new Vector<String>(Database.getInstance().getLootTables().keySet());
				Collections.sort(vec);
				lootTable.setModel(new DefaultComboBoxModel(vec));
			}
		});

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Loot Table: "), lootTable,
			new JLabel(), edit);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getGrantGoldPanel()
	{
		goldAmount = new JSpinner(new SpinnerNumberModel(0, 0, 999999999, 1));

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Gold: "), goldAmount);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getGrantExperiencePanel()
	{
		xpAmount = new JSpinner(new SpinnerNumberModel(0, 0, 999999999, 1));

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Experience: "), xpAmount);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getFlavourTextPanel()
	{
		flavourTextDelay = new JSpinner(new SpinnerNumberModel(-1, -1, 99999, 1));
		shouldClearText = new JCheckBox("Clear Text?");
		flavourText = new JTextArea();
		flavourText.setWrapStyleWord(true);
		flavourText.setLineWrap(true);
		flavourText.setColumns(30);
		flavourText.setRows(10);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Delay: "), flavourTextDelay,
			shouldClearText, null,
			new JLabel("Flavour Text: "), new JScrollPane(flavourText));
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getEncounterPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getEncounterTables().keySet());
		Collections.sort(vec);
		encounterTable = new JComboBox(vec);
		encounterMazeVariable = new JTextField(20);

		NpcFaction.Attitude[] values = NpcFaction.Attitude.values();
		Vector attitudes = new Vector();
		Collections.addAll(attitudes, values);
		attitudes.add(0, EditorPanel.NONE);
		encounterAttitude = new JComboBox(attitudes);

		Combat.AmbushStatus[] statuses = Combat.AmbushStatus.values();
		Vector ambushStatuses = new Vector();
		Collections.addAll(ambushStatuses, statuses);
		ambushStatuses.add(0, EditorPanel.NONE);
		encounterAmbushStatus = new JComboBox(ambushStatuses);

		Vector vec2 = new Vector(Database.getInstance().getMazeScripts().keySet());
		Collections.sort(vec2);
		vec2.add(0, EditorPanel.NONE);
		encounterPreScript = new JComboBox(vec2);

		encounterPostAppearanceScript = new JComboBox(vec2);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Encounter Table: "), encounterTable,
			new JLabel("Maze Variable: "), encounterMazeVariable,
			new JLabel("Attitude: "), encounterAttitude,
			new JLabel("Ambush Status: "), encounterAmbushStatus,
			new JLabel("Pre Script: "), encounterPreScript,
			new JLabel("Post Appearance Script: "), encounterPostAppearanceScript
			);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCastSpellPanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(vec);
		spell = new JComboBox(vec);
		casterLevel = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		castingLevel = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Spell: "), spell,
			new JLabel("Caster Level: "), casterLevel,
			new JLabel("Casting Level: "), castingLevel);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getZoneChangePanel()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getZoneNames());
		Collections.sort(vec);
		zone = new JComboBox(vec);
		zoneX = new JSpinner(new SpinnerNumberModel(0, -1, 256, 1));
		zoneY = new JSpinner(new SpinnerNumberModel(0, -1, 256, 1));
		zoneFacing = new JComboBox(FACINGS);
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Zone: "), zone,
			new JLabel("X: "), zoneX,
			new JLabel("Y: "), zoneY,
			new JLabel("Facing: "), zoneFacing);
		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		JPanel result = new JPanel();
		dirtyGridLayoutCrap(result, new JLabel("Custom Impl: "), impl);
		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	private JPanel dirtyGridBagCrap(Component... comps)
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		for (int i=0; i<comps.length; i+=2)
		{
			dodgyGridBagShite(result, comps[i], comps[i+1], gbc);
		}

		gbc.weighty = 1.0;
		dodgyGridBagShite(result, new JLabel(), new JLabel(), gbc);

		return result;
	}
	
	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		gbc.weightx = 0.0;
		gbc.gridx=0;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
		gbc.gridy++;
	}

	/*-------------------------------------------------------------------------*/
	protected GridBagConstraints createGridBagConstraints()
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.WEST;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	private void dirtyGridLayoutCrap(JPanel panel, Component... comps)
	{
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		for (int i=0; i<comps.length; i+=2)
		{
			if (i == comps.length-2)
			{
				gbc.weighty = 1;
			}
			gbc.gridx = 0;
			gbc.weightx = 0;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 2;
				gbc.weightx = 1;
			}
			panel.add(comps[i], gbc);
			gbc.gridx = 1;
			gbc.weightx = 1;
			if (comps[i+1] == null)
			{
				gbc.gridwidth = 1;
			}
			else
			{
				panel.add(comps[i+1], gbc);
			}
			gbc.gridy++;
		}
	}

	/*-------------------------------------------------------------------------*/
	private String describeType(int i)
	{
		switch (i)
		{
			case CUSTOM:
				return "Custom";
			case _ZoneChangeEvent:
				return "Zone Change";
			case _CastSpellEvent:
				return "Cast Spell At Party";
			case _EncounterActorsEvent:
				return "Encounter Actors";
			case _FlavourTextEvent:
				return "Flavour Text";
			case _GrantExperienceEvent:
				return "Grant Experience";
			case _GrantGoldEvent:
				return "Grant Gold";
			case _GrantItemsEvent:
				return null;
			case _SignBoardEvent:
				return "Sign Board";
			case _LootTableEvent:
				return "Grant Items From A Loot Table";
			case _DelayEvent:
				return "Delay";
			case _MovePartyEvent:
				return "Move/Rotate Party";
			case _CharacterClassKnowledgeEvent:
				return "Character Class Knowledge";
			case _PersonalitySpeechEvent:
				return "Personality Speech";
			case _StoryboardEvent:
				return "Story Board Screen";
			case _SetUserConfigEvent:
				return "Set User Config Value";
			case _MazeScript:
				return "Execute Maze Script";
			case _RemoveWall:
				return "Remove Wall";
			case _BlockingScreen:
				return "Display Blocking Screen";
			case _EndGame:
				return "End Game";
			case _SetMazeVariableEvent:
				return "Set Maze Variable";
			case _TogglePortalStateEvent:
				return "Toggle Portal State";
					
			case _ActorDiesEvent:
			case _ActorUnaffectedEvent:
			case _AmbushHitEvent:
			case _AmbushMissEvent:
			case _AttackDodgeEvent:
			case _AttackEvent:
			case _AttackHitEvent:
			case _AttackMissEvent:
			case _BreaksFreeEvent:
			case _ConditionEvent:
			case _CowerInFearEvent:
			case _DamageEvent:
			case _DancesWildlyEvent:
			case _DefendEvent:
			case _EquipEvent:
			case _FailureEvent:
			case _FatigueEvent:
			case _FreezeInTerrorEvent:
			case _GagsHelplesslyEvent:
			case _HealingEvent:
			case _HideAttemptEvent:
			case _HideFailsEvent:
			case _HideSucceedsEvent:
			case _ItchesUncontrollablyEvent:
			case _ItemUseEvent:
			case _LaughsMadlyEvent:
			case _NoEffectEvent:
			case _NpcCharmedEvent:
			case _NpcMindreadEvent:
			case _NpcMindreadFailedEvent:
			case _NpcNotCharmedEvent:
			case _RemoveCurseEvent:
			case _RetchesNoisilyEvent:
			case _RunAwayAttemptEvent:
			case _RunAwayFailedEvent:
			case _RunAwaySuccessEvent:
			case _SpecialAbilityUseEvent:
			case _SpellCastEvent:
			case _SpellFizzlesEvent:
			case _StrugglesMightilyEvent:
			case _StumbleBlindlyEvent:
			case _SuccessEvent:
			case _SummoningFailsEvent:
			case _SummoningSucceedsEvent:
			case _TheftSpellFailed:
			case _TheftSpellSucceeded:
				return null;
			case _SoundEffectEvent:
				return "Sound Effect";
			case _MusicEvent:
				return "Music";
			case _AnimationEvent:
				return "Animation";

			case _ChangeNpcAttitudeEvent:
			case _ChangeNpcLocationEvent:
			case _ChangeNpcTheftCounter:
			case _GiveItemToParty:
			case _NpcAttacksEvent:
			case _NpcLeavesEvent:
			case _NpcSpeechEvent:
			case _NpcTakesItemEvent:
			case _WaitForPlayerSpeech:
			case _ChangeNpcFactionAttitudeEvent:

			default: return null;
		}
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == type)
		{
			int index = type.getSelectedIndex();
			if (index > -1)
			{
				cards.show(controls, String.valueOf(dialogLookup[index]));
			}
		}
		else if (e.getSource() == ok)
		{
			// save changes
			saveResult();
			if (SwingEditor.instance != null)
			{
				SwingEditor.instance.setDirty(dirtyFlag);
			}
			setVisible(false);
		}
		else if (e.getSource() == cancel)
		{
			// discard changes
			setVisible(false);
		}
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V1Loader();
		Saver saver = new V1Saver();
		new Database(loader, saver, Maze.getStubCampaign());

		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		while (1==1)
		{
			MazeEventEditor test = new MazeEventEditor(owner, null, -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}

}
