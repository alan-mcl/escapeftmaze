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
import java.util.List;
import java.util.*;
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v2.V2Loader;
import mclachlan.maze.data.v2.V2Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.game.MazeScript;
import mclachlan.maze.game.event.*;
import mclachlan.maze.game.journal.JournalEntryEvent;
import mclachlan.maze.game.journal.JournalManager;
import mclachlan.maze.map.script.*;
import mclachlan.maze.stat.Stats;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.event.*;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.NullProgressListener;
import mclachlan.maze.util.MazeException;


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
	private MazeEventsComponent encounterPreScript, encounterPostAppearanceScript,
		partyLeavesNeutralScript, partyLeavesFriendlyScript;
	private JSpinner flavourTextDelay;
	private JCheckBox shouldClearText;
	private JComboBox alignment;
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
	private JTextField removeObjectName;
	private JSpinner removeWallWallIndex;
	private JCheckBox removeWallIsHoriz;
	private JTextField blockingScreenImage;
	private JSpinner blockingScreenMode;
	private JTextField setMazeVarMazeVar, setMazeVarValue;
	private JSpinner fromX, fromY;
	private JComboBox facing;
	private JComboBox steKeyModifier;
	private ValueComponent steSkillValue, steSuccessValue;
	private JComboBox steSuccessScript, steFailureScript;

	private JComboBox journalType;
	private JTextField journalKey;
	private JTextArea journalText;

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
		Vector<String> types = new Vector<>();
		for (int i=0; i<MAX; i++)
		{
			String str = describeType(i);
			if (str != null)
			{
				int index = types.size();
				dialogLookup[index] = i;
				types.addElement(str);
			}
		}

		type = new JComboBox(types);
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
				encounterPreScript.refresh(ee.getPreScript() == null ? null : ee.getPreScript().getEvents());
				encounterPostAppearanceScript.refresh(ee.getPostAppearanceScript() == null ? null : ee.getPostAppearanceScript().getEvents());
				partyLeavesNeutralScript.refresh(ee.getPartyLeavesNeutralScript() == null ? null : ee.getPartyLeavesNeutralScript().getEvents());
				partyLeavesFriendlyScript.refresh(ee.getPartyLeavesFriendlyScript() == null ? null : ee.getPartyLeavesFriendlyScript().getEvents());
				break;
			case _FlavourTextEvent:
				FlavourTextEvent fte = (FlavourTextEvent)e;
				flavourText.setText(fte.getFlavourText());
				flavourTextDelay.setValue(fte.getDelay());
				shouldClearText.setSelected(fte.shouldClearText());
				alignment.setSelectedItem(fte.getAlignment());
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
			case _RemoveObjectEvent:
				RemoveObjectEvent roe = (RemoveObjectEvent)e;
				removeObjectName.setText(roe.getObjectName());
				break;
			case _SkillTestEvent:
				SkillTestEvent ste = (SkillTestEvent)e;
				steKeyModifier.setSelectedItem(ste.getKeyModifier() == null ? EditorPanel.NONE : ste.getKeyModifier());
				steSkillValue.setValue(ste.getSkill());
				steSuccessValue.setValue(ste.getSuccessValue());
				steSuccessScript.setSelectedItem(ste.getSuccessScript() == null ? EditorPanel.NONE : ste.getSuccessScript());
				steFailureScript.setSelectedItem(ste.getFailureScript() == null ? EditorPanel.NONE : ste.getFailureScript());
				break;
			case _JournalEntryEvent:
				JournalEntryEvent jee = (JournalEntryEvent)e;
				journalType.setSelectedItem(jee.getType());
				journalKey.setText(jee.getKey() == null ? "" : jee.getKey());
				journalText.setText(jee.getJournalText() == null ? "" : jee.getJournalText());
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
				if (trackNames1 != null)
				{
					for (String s : trackNames1)
					{
						sb1.append(s).append('\n');
					}
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
				MazeScript preScript = null;
				if (encounterPreScript.getEvents() != null && encounterPreScript.getEvents().size()>0)
				{
					preScript = new MazeScript("EncounterActorsEvent.preScript", encounterPreScript.getEvents());
				}
				MazeScript postAppearanceScript = null;
				if (encounterPostAppearanceScript.getEvents() != null && encounterPostAppearanceScript.getEvents().size()>0)
				{
					postAppearanceScript = new MazeScript("EncounterActorsEvent.postAppearanceScript", postAppearanceScript.getEvents());
				}
				MazeScript partyLeavesFriendly = null;
				if (partyLeavesFriendlyScript.getEvents() != null && partyLeavesFriendlyScript.getEvents().size()>0)
				{
					partyLeavesFriendly = new MazeScript("EncounterActorsEvent.partyLeavesFriendly", partyLeavesFriendlyScript.getEvents());
				}
				MazeScript partyLeavesNeutral = null;
				if (partyLeavesNeutralScript.getEvents() != null && partyLeavesNeutralScript.getEvents().size()>0)
				{
					partyLeavesNeutral = new MazeScript("EncounterActorsEvent.partyLeavesNeutral", partyLeavesNeutralScript.getEvents());
				}

				this.result = new EncounterActorsEvent(
					encounterMazeVariable.getText(),
					(String)encounterTable.getSelectedItem(),
					attitude,
					ambushStatus,
					preScript,
					postAppearanceScript,
					partyLeavesNeutral,
					partyLeavesFriendly);
				break;
			case _FlavourTextEvent:
				this.result = new FlavourTextEvent(
					flavourText.getText(),
					(Integer)flavourTextDelay.getValue(),
					shouldClearText.isSelected(),
					(FlavourTextEvent.Alignment)alignment.getSelectedItem());
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
			case _RemoveObjectEvent:
				this.result = new RemoveObjectEvent(removeObjectName.getText());
				break;
			case _SkillTestEvent:
				this.result = new SkillTestEvent(
					steKeyModifier.getSelectedItem() == EditorPanel.NONE ? null : (Stats.Modifier)steKeyModifier.getSelectedItem(),
					steSkillValue.getValue(),
					steSuccessValue.getValue(),
					steSuccessScript.getSelectedItem() == EditorPanel.NONE ? null : (String)steSuccessScript.getSelectedItem(),
					steFailureScript.getSelectedItem() == EditorPanel.NONE ? null : (String)steFailureScript.getSelectedItem());
				break;
			case _JournalEntryEvent:
				this.result = new JournalEntryEvent(
					(JournalManager.JournalType)journalType.getSelectedItem(),
					"".equals(journalKey.getText()) ? null : journalKey.getText(),
					"".equals(journalText.getText()) ? null : journalText.getText());
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
			case _RemoveObjectEvent:
				return getRemoveObjectEventPanel();
			case _SkillTestEvent:
				return getSkillTestEventPanel();
			case _JournalEntryEvent:
				return getJournalEntryEventPanel();
				
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

	private JPanel getJournalEntryEventPanel()
	{
		journalType = new JComboBox(JournalManager.JournalType.values());
		journalKey = new JTextField(20);
		journalText = new JTextArea(10, 35);
		journalText.setLineWrap(true);
		journalText.setWrapStyleWord(true);

		return dirtyGridBagCrap(
			new JLabel("Journal Type:"), journalType,
			new JLabel("Key:"), journalKey,
			new JLabel("Text:"), journalText);
	}

	private JPanel getSkillTestEventPanel()
	{
		Vector mods = new Vector(Stats.allModifiers);
		mods.sort(Comparator.comparing(Object::toString));
		mods.add(0, EditorPanel.NONE);
		steKeyModifier = new JComboBox(mods);

		steSkillValue = new ValueComponent(this.dirtyFlag);
		steSuccessValue = new ValueComponent(this.dirtyFlag);

		Vector scripts = new Vector(Database.getInstance().getMazeScripts().keySet());
		scripts.sort(Comparator.comparing(Object::toString));
		scripts.add(0, EditorPanel.NONE);

		steSuccessScript = new JComboBox(scripts);
		steFailureScript = new JComboBox(new Vector(scripts));

		return dirtyGridBagCrap(
			new JLabel("Key Modifier:"), steKeyModifier,
			new JLabel("Skill:"), steSkillValue,
			new JLabel("Success Value:"), steSuccessValue,
			new JLabel("Success Script:"), steSuccessScript,
			new JLabel("Failure Script:"), steFailureScript);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRemoveObjectEventPanel()
	{
		removeObjectName = new JTextField(30);

		return dirtyGridBagCrap(new JLabel("Object Name:"), removeObjectName);
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
		alignment = new JComboBox(FlavourTextEvent.Alignment.values());
		flavourText = new JTextArea();
		flavourText.setWrapStyleWord(true);
		flavourText.setLineWrap(true);
		flavourText.setColumns(30);
		flavourText.setRows(10);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Alignment: "), alignment,
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

		encounterPreScript = new MazeEventsComponent(dirtyFlag);
		encounterPostAppearanceScript = new MazeEventsComponent(dirtyFlag);
		partyLeavesNeutralScript = new MazeEventsComponent(dirtyFlag);
		partyLeavesFriendlyScript = new MazeEventsComponent(dirtyFlag);

		JPanel result = new JPanel();
		dirtyGridLayoutCrap(
			result,
			new JLabel("Encounter Table: "), encounterTable,
			new JLabel("Maze Variable: "), encounterMazeVariable,
			new JLabel("Attitude: "), encounterAttitude,
			new JLabel("Ambush Status: "), encounterAmbushStatus,
			new JLabel("Pre Script: "), encounterPreScript,
			new JLabel("Post Appearance Script: "), encounterPostAppearanceScript,
			new JLabel("Party Leaves Neutral Script: "), partyLeavesNeutralScript,
			new JLabel("Party Leaves Friendly Script: "), partyLeavesFriendlyScript
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
			case _RemoveObjectEvent:
				return "Remove Object";
			case _SkillTestEvent:
				return "Skill Test";
			case _JournalEntryEvent:
				return "Journal Entry";
					
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
	public static Map<Class, Integer> types;

	// implemented in UI & DB
	public static final int CUSTOM = 0;
	public static final int _ZoneChangeEvent = 1;
	public static final int _CastSpellEvent = 2;
	public static final int _EncounterActorsEvent = 3;
	public static final int _FlavourTextEvent = 4;
	public static final int _GrantExperienceEvent = 5;
	public static final int _GrantGoldEvent = 6;
	public static final int _GrantItemsEvent = 7;
	public static final int _SignBoardEvent = 8;
	public static final int _LootTableEvent = 9;
	public static final int _DelayEvent = 10;
	public static final int _MovePartyEvent = 11;
	public static final int _CharacterClassKnowledgeEvent = 12;
	public static final int _MazeScript = 13;
	public static final int _RemoveWall = 14;
	public static final int _BlockingScreen = 15;
	public static final int _EndGame = 16;
	public static final int _SetMazeVariableEvent = 17;
	public static final int _PersonalitySpeechEvent = 18;
	public static final int _StoryboardEvent = 19;
	public static final int _SetUserConfigEvent = 20;
	public static final int _TogglePortalStateEvent = 21;
	public static final int _RemoveObjectEvent = 22;
	public static final int _SkillTestEvent = 23;
	public static final int _AnimationEvent = 104;
	public static final int _MusicEvent = 149;
	public static final int _SoundEffectEvent = 138;
	public static final int _JournalEntryEvent = 24;

	// not implemented
	public static final int _ActorDiesEvent = 100;
	public static final int _ActorUnaffectedEvent = 101;
	public static final int _AmbushHitEvent = 102;
	public static final int _AmbushMissEvent = 103;
	public static final int _AttackDodgeEvent = 105;
	public static final int _AttackEvent = 106;
	public static final int _AttackHitEvent = 107;
	public static final int _AttackMissEvent = 108;
	public static final int _BreaksFreeEvent = 109;
	public static final int _ConditionEvent = 110;
	public static final int _CowerInFearEvent = 111;
	public static final int _DamageEvent = 112;
	public static final int _DancesWildlyEvent = 113;
	public static final int _DefendEvent = 114;
	public static final int _EquipEvent = 116;
	public static final int _FailureEvent = 117;
	public static final int _FatigueEvent = 118;
	public static final int _FreezeInTerrorEvent = 119;
	public static final int _GagsHelplesslyEvent = 120;
	public static final int _HealingEvent = 121;
	public static final int _HideAttemptEvent = 122;
	public static final int _HideFailsEvent = 123;
	public static final int _HideSucceedsEvent = 124;
	public static final int _ItchesUncontrollablyEvent = 125;
	public static final int _ItemUseEvent = 126;
	public static final int _LaughsMadlyEvent = 127;
	public static final int _NoEffectEvent = 128;
	public static final int _NpcCharmedEvent = 129;
	public static final int _NpcMindreadEvent = 130;
	public static final int _NpcMindreadFailedEvent = 131;
	public static final int _NpcNotCharmedEvent = 132;
	public static final int _RemoveCurseEvent = 133;
	public static final int _RetchesNoisilyEvent = 134;
	public static final int _RunAwayAttemptEvent = 135;
	public static final int _RunAwayFailedEvent = 136;
	public static final int _RunAwaySuccessEvent = 137;
	public static final int _SpecialAbilityUseEvent = 139;
	public static final int _SpellCastEvent = 140;
	public static final int _SpellFizzlesEvent = 141;
	public static final int _StrugglesMightilyEvent = 142;
	public static final int _StumbleBlindlyEvent = 143;
	public static final int _SuccessEvent = 144;
	public static final int _SummoningFailsEvent = 145;
	public static final int _SummoningSucceedsEvent = 146;
	public static final int _TheftSpellFailed = 147;
	public static final int _TheftSpellSucceeded = 148;
	public static final int _BackPartyUpEvent = 149;

	public static final int _ChangeNpcAttitudeEvent = 200;
	public static final int _ChangeNpcLocationEvent = 201;
	public static final int _ChangeNpcTheftCounter = 202;
	public static final int _GiveItemToParty = 203;
	public static final int _NpcAttacksEvent = 204;
	public static final int _NpcLeavesEvent = 205;
	public static final int _NpcSpeechEvent = 206;
	public static final int _NpcTakesItemEvent = 207;
	public static final int _WaitForPlayerSpeech = 208;
	public static final int _ChangeNpcFactionAttitudeEvent = 209;

	static
	{
		types = new HashMap<>();

		// implemented in UI & DB
		types.put(ZoneChangeEvent.class, _ZoneChangeEvent);
		types.put(CastSpellEvent.class, _CastSpellEvent);
		types.put(EncounterActorsEvent.class, _EncounterActorsEvent);
		types.put(FlavourTextEvent.class, _FlavourTextEvent);
		types.put(GrantExperienceEvent.class, _GrantExperienceEvent);
		types.put(GrantGoldEvent.class, _GrantGoldEvent);
		types.put(GrantItemsEvent.class, _GrantItemsEvent);
		types.put(SignBoardEvent.class, _SignBoardEvent);
		types.put(LootTableEvent.class, _LootTableEvent);
		types.put(ActorDiesEvent.class, _ActorDiesEvent);
		types.put(ActorUnaffectedEvent.class, _ActorUnaffectedEvent);
		types.put(AnimationEvent.class, _AnimationEvent);
		types.put(AttackDodgeEvent.class, _AttackDodgeEvent);
		types.put(AttackEvent.class, _AttackEvent);
		types.put(AttackHitEvent.class, _AttackHitEvent);
		types.put(AttackMissEvent.class, _AttackMissEvent);
		types.put(BreaksFreeEvent.class, _BreaksFreeEvent);
		types.put(ConditionEvent.class, _ConditionEvent);
		types.put(CowerInFearEvent.class, _CowerInFearEvent);
		types.put(DamageEvent.class, _DamageEvent);
		types.put(DancesWildlyEvent.class, _DancesWildlyEvent);
		types.put(DefendEvent.class, _DefendEvent);
		types.put(DelayEvent.class, _DelayEvent);
		types.put(MovePartyEvent.class, _MovePartyEvent);
		types.put(CharacterClassKnowledgeEvent.class, _CharacterClassKnowledgeEvent);
		types.put(PersonalitySpeechBubbleEvent.class, _PersonalitySpeechEvent);
		types.put(StoryboardEvent.class, _StoryboardEvent);
		types.put(SetUserConfigEvent.class, _SetUserConfigEvent);
		types.put(TogglePortalStateEvent.class, _TogglePortalStateEvent);
		types.put(RemoveObjectEvent.class, _RemoveObjectEvent);
		types.put(SkillTestEvent.class, _SkillTestEvent);
		types.put(JournalEntryEvent.class, _JournalEntryEvent);

		// not implemented
		types.put(BackPartyUpEvent.class, _BackPartyUpEvent);
		types.put(MazeScriptEvent.class, _MazeScript);
		types.put(RemoveWallEvent.class, _RemoveWall);
		types.put(BlockingScreenEvent.class, _BlockingScreen);
		types.put(EndGameEvent.class, _EndGame);
		types.put(EquipEvent.class, _EquipEvent);
		types.put(FailureEvent.class, _FailureEvent);
		types.put(FatigueEvent.class, _FatigueEvent);
		types.put(FreezeInTerrorEvent.class, _FreezeInTerrorEvent);
		types.put(GagsHelplesslyEvent.class, _GagsHelplesslyEvent);
		types.put(HealingEvent.class, _HealingEvent);
		types.put(HideAttemptEvent.class, _HideAttemptEvent);
		types.put(HideFailsEvent.class, _HideFailsEvent);
		types.put(HideSucceedsEvent.class, _HideSucceedsEvent);
		types.put(ItchesUncontrollablyEvent.class, _ItchesUncontrollablyEvent);
		types.put(ItemUseEvent.class, _ItemUseEvent);
		types.put(LaughsMadlyEvent.class, _LaughsMadlyEvent);
		types.put(NoEffectEvent	.class, _NoEffectEvent	);
		types.put(NpcCharmedEvent.class, _NpcCharmedEvent);
		types.put(NpcMindreadEvent.class, _NpcMindreadEvent);
		types.put(NpcMindreadFailedEvent.class, _NpcMindreadFailedEvent);
		types.put(NpcNotCharmedEvent.class, _NpcNotCharmedEvent);
		types.put(RemoveCurseEvent.class, _RemoveCurseEvent);
		types.put(RetchesNoisilyEvent.class, _RetchesNoisilyEvent);
		types.put(RunAwayAttemptEvent.class, _RunAwayAttemptEvent);
		types.put(RunAwayFailedEvent.class, _RunAwayFailedEvent);
		types.put(RunAwaySuccessEvent.class, _RunAwaySuccessEvent);
		types.put(SoundEffectEvent.class, _SoundEffectEvent);
		types.put(MusicEvent.class, _MusicEvent);
		types.put(SpecialAbilityUseEvent.class, _SpecialAbilityUseEvent);
		types.put(SpellCastEvent.class, _SpellCastEvent);
		types.put(SpellFizzlesEvent.class, _SpellFizzlesEvent);
		types.put(StrugglesMightilyEvent.class, _StrugglesMightilyEvent);
		types.put(StumbleBlindlyEvent.class, _StumbleBlindlyEvent);
		types.put(SuccessEvent.class, _SuccessEvent);
		types.put(SummoningFailsEvent.class, _SummoningFailsEvent);
		types.put(SummoningSucceedsEvent.class, _SummoningSucceedsEvent);
		types.put(TheftSpellFailed.class, _TheftSpellFailed);
		types.put(TheftSpellSucceeded.class, _TheftSpellSucceeded);
		types.put(ChangeNpcAttitudeEvent.class, _ChangeNpcAttitudeEvent);
		types.put(ChangeNpcLocationEvent.class, _ChangeNpcLocationEvent);
		types.put(ChangeNpcTheftCounter.class, _ChangeNpcTheftCounter);
		types.put(GiveItemToParty.class, _GiveItemToParty);
		types.put(NpcAttacksEvent.class, _NpcAttacksEvent);
		types.put(ActorsLeaveEvent.class, _NpcLeavesEvent);
		types.put(NpcSpeechEvent.class, _NpcSpeechEvent);
		types.put(NpcTakesItemEvent.class, _NpcTakesItemEvent);
		types.put(WaitForPlayerSpeech.class, _WaitForPlayerSpeech);
		types.put(SetMazeVariableEvent.class, _SetMazeVariableEvent);
		types.put(ChangeNpcFactionAttitudeEvent.class, _ChangeNpcFactionAttitudeEvent);
	}

	/*-------------------------------------------------------------------------*/
	public static void main(String[] args) throws Exception
	{
		Loader loader = new V2Loader();
		Saver saver = new V2Saver();
		Database db = new Database(loader, saver, Maze.getStubCampaign());
		db.initImpls();
		db.initCaches(new NullProgressListener());


		JFrame owner = new JFrame("test");
		owner.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		while (1==1)
		{
			MazeEventEditor test = new MazeEventEditor(owner, null, -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}

}
