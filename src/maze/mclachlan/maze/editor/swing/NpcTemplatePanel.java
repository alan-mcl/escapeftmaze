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
import java.util.*;
import javax.swing.*;
import mclachlan.maze.arena.StickManVendor;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.v1.DataObject;
import mclachlan.maze.stat.npc.*;
import mclachlan.maze.ui.diygui.Constants;
import mclachlan.maze.util.MazeException;

/**
 *
 */
public class NpcTemplatePanel extends EditorPanel
{
	private JTextField npcScript;
	private JComboBox attitude, foeName, faction, alliesOnCall, zone;
	private JSpinner buysAt, sellsAt, maxPurchasePrice, resistThreats,
		resistBribes, resistSteal, theftCounter, tileX, tileY;
	private ItemTypeComponent willBuyItemTypes;
	private NpcInventoryTemplateComponent npcInventoryTemplate;
	private JCheckBox found, dead, guildMaster;
	private NpcSpeechPanel npcSpeechPanel;
	private JButton colourButton;

	JTextField neutralGreeting, friendlyGreeting, neutralFarewell, friendlyFarewell,
		partyCantAffordItem, characterInventoryFull, notInterestedInBuyingItem,
		cantAffordToBuyItem, npcInventoryFull, doesntWantItem, doesntKnowAbout;

	/*-------------------------------------------------------------------------*/
	public NpcTemplatePanel()
	{
		super(SwingEditor.Tab.NPC_TEMPLATES);
	}

	/*-------------------------------------------------------------------------*/
	public Container getEditControls()
	{
		JTabbedPane tabs = new JTabbedPane();

		tabs.add("Detail", getDetailTab());
		tabs.add("Inventory Template", getInventoryTemplateTab());
		tabs.add("Interactions", getInteractionsTab());
		tabs.add("Dialogue", getDialogTab());

		return tabs;
	}

	private Component getInteractionsTab()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;

		neutralGreeting = new JTextField(50);
		neutralGreeting.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Neutral Greeting:"), neutralGreeting, gbc);

		friendlyGreeting = new JTextField(50);
		friendlyGreeting.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Friendly Greeting:"), friendlyGreeting, gbc);

		neutralFarewell = new JTextField(50);
		neutralFarewell.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Neutral Farewell:"), neutralFarewell, gbc);

		friendlyFarewell = new JTextField(50);
		friendlyFarewell.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Friendly Farewell:"), friendlyFarewell, gbc);

		partyCantAffordItem = new JTextField(50);
		partyCantAffordItem.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Party Can't Afford Item:"), partyCantAffordItem, gbc);

		characterInventoryFull = new JTextField(50);
		characterInventoryFull.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Character Inventory Full:"), characterInventoryFull, gbc);

		notInterestedInBuyingItem = new JTextField(50);
		notInterestedInBuyingItem.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Not Interested In Buying Item:" ), notInterestedInBuyingItem, gbc);

		cantAffordToBuyItem = new JTextField(50);
		cantAffordToBuyItem.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Can't Afford To Buy Item:"), cantAffordToBuyItem, gbc);

		npcInventoryFull = new JTextField(50);
		npcInventoryFull.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("NPC Inventory Full:"), npcInventoryFull, gbc);

		doesntWantItem = new JTextField(50);
		doesntWantItem.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Doesn't Want Item:"), doesntWantItem, gbc);

		doesntKnowAbout = new JTextField(50);
		doesntKnowAbout.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Doesn't Know About %s:"), doesntKnowAbout, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private Component getDialogTab()
	{
		npcSpeechPanel = new NpcSpeechPanel(this.dirtyFlag);
		return npcSpeechPanel;
	}

	/*-------------------------------------------------------------------------*/
	private Component getInventoryTemplateTab()
	{
		JPanel result = new JPanel(new BorderLayout(2,2));

		npcInventoryTemplate = new NpcInventoryTemplateComponent(dirtyFlag);

		result.add(npcInventoryTemplate, BorderLayout.CENTER);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getDetailTab()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		JPanel left = getLeftPanel();
		JPanel right = getRightPanel();

		result.add(left, gbc);

		gbc.gridx++;
		gbc.weightx = 1.0;

		result.add(right, gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getRightPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2,2,2,2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		willBuyItemTypes = new ItemTypeComponent("Will Buy Item Types", dirtyFlag);
		result.add(willBuyItemTypes, gbc);



		return result;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getLeftPanel()
	{
		JPanel result = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = createGridBagConstraints();

		foeName = new JComboBox();
		foeName.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Foe Name:"), foeName, gbc);

		faction = new JComboBox();
		faction.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Faction:"), faction, gbc);

		npcScript = new JTextField(20);
		npcScript.addKeyListener(this);
		dodgyGridBagShite(result, new JLabel("NPC Script:"), npcScript, gbc);

		attitude = new JComboBox(NpcFaction.Attitude.values());
		attitude.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Starting Attitude:"), attitude, gbc);

		alliesOnCall = new JComboBox();
		alliesOnCall.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Allies On Call:"), alliesOnCall, gbc);

		buysAt = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		buysAt.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Buys At:"), buysAt, gbc);

		sellsAt = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		sellsAt.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Sells At:"), sellsAt, gbc);

		maxPurchasePrice = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		maxPurchasePrice.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Max Purchase Price:"), maxPurchasePrice, gbc);

		resistThreats = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		resistThreats.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Resist Threats:"), resistThreats, gbc);

		resistBribes = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		resistBribes.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Resist Bribes:"), resistBribes, gbc);

		resistSteal = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		resistSteal.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Resist Steal:"), resistSteal, gbc);

		theftCounter = new JSpinner(new SpinnerNumberModel(0, -127, 127, 1));
		theftCounter.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Theft Counter:"), theftCounter, gbc);

		zone = new JComboBox();
		zone.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Zone:"), zone, gbc);

		tileX = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		tileX.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Tile X:"), tileX, gbc);

		tileY = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		tileY.addChangeListener(this);
		dodgyGridBagShite(result, new JLabel("Tile Y:"), tileY, gbc);

		colourButton = new JButton(" ... ");
		colourButton.addActionListener(this);
		dodgyGridBagShite(result, new JLabel("Speech Colour:"), colourButton, gbc);

		guildMaster = new JCheckBox("Guild Master?");
		guildMaster.addActionListener(this);
		dodgyGridBagShite(result, guildMaster, new JLabel(), gbc);

		found = new JCheckBox("Is Found?");
		found.addActionListener(this);
		dodgyGridBagShite(result, found, new JLabel(), gbc);

		dead = new JCheckBox("Is Dead?");
		dead.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		result.add(dead, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		result.add(new JLabel(), gbc);

		return result;
	}

	/*-------------------------------------------------------------------------*/
	public Vector<DataObject> loadData()
	{
		return new Vector<>(Database.getInstance().getNpcTemplates().values());
	}

	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector foeTemplates = new Vector(Database.getInstance().getFoeTemplates().keySet());
		Collections.sort(foeTemplates);
		foeName.setModel(new DefaultComboBoxModel(foeTemplates));

		Vector npcFactions = new Vector(Database.getInstance().getNpcFactionTemplates().keySet());
		Collections.sort(npcFactions);
		npcFactions.add(0, EditorPanel.NONE);
		faction.setModel(new DefaultComboBoxModel(npcFactions));

		Vector encounterTables = new Vector(Database.getInstance().getEncounterTables().keySet());
		Collections.sort(encounterTables);
		alliesOnCall.setModel(new DefaultComboBoxModel(encounterTables));

		Vector zones = new Vector(Database.getInstance().getZoneNames());
		Collections.sort(zones);
		zone.setModel(new DefaultComboBoxModel(zones));

		npcInventoryTemplate.initForeignKeys();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh(String name)
	{
		NpcTemplate npc = Database.getInstance().getNpcTemplates().get(name);

		attitude.removeActionListener(this);
		buysAt.removeChangeListener(this);
		sellsAt.removeChangeListener(this);
		maxPurchasePrice.removeChangeListener(this);
		resistThreats.removeChangeListener(this);
		resistBribes.removeChangeListener(this);
		resistSteal.removeChangeListener(this);
		theftCounter.removeChangeListener(this);
		tileX.removeChangeListener(this);
		tileY.removeChangeListener(this);
		foeName.removeActionListener(this);
		faction.removeActionListener(this);
		alliesOnCall.removeActionListener(this);
		zone.removeActionListener(this);

		friendlyGreeting.removeKeyListener(this);
		neutralGreeting.removeKeyListener(this);
		friendlyFarewell.removeKeyListener(this);
		neutralFarewell.removeKeyListener(this);
		partyCantAffordItem.removeKeyListener(this);
		characterInventoryFull.removeKeyListener(this);
		notInterestedInBuyingItem.removeKeyListener(this);
		cantAffordToBuyItem.removeKeyListener(this);
		npcInventoryFull.removeKeyListener(this);
		doesntWantItem.removeKeyListener(this);
		doesntKnowAbout.removeKeyListener(this);

		foeName.setSelectedItem(npc.getFoeName());
		faction.setSelectedItem(npc.getFaction() == null ? EditorPanel.NONE : npc.getFaction());
		npcScript.setText(npc.getScript().getClass().getName());
		attitude.setSelectedItem(npc.getAttitude());
		alliesOnCall.setSelectedItem(npc.getAlliesOnCall());
		buysAt.setValue(npc.getBuysAt());
		sellsAt.setValue(npc.getSellsAt());
		maxPurchasePrice.setValue(npc.getMaxPurchasePrice());
		resistThreats.setValue(npc.getResistThreats());
		resistBribes.setValue(npc.getResistBribes());
		resistSteal.setValue(npc.getResistSteal());
		theftCounter.setValue(npc.getTheftCounter());
		zone.setSelectedItem(npc.getZone());
		tileX.setValue(npc.getTile().x);
		tileY.setValue(npc.getTile().y);
		found.setSelected(npc.isFound());
		dead.setSelected(npc.isDead());
		guildMaster.setSelected(npc.isGuildMaster());

		willBuyItemTypes.refresh(npc.getWillBuyItemTypes());
		NpcInventoryTemplate template = npc.getInventoryTemplate();
		if (template != null)
		{
			npcInventoryTemplate.refresh(template.getRows());
		}
		else
		{
			npcInventoryTemplate.refresh(null);
		}
		npcSpeechPanel.refresh(npc.getDialogue());
		colourButton.setBackground(npc.getSpeechColour());

		friendlyGreeting.setText(npc.getDialogue().getFriendlyGreeting() == null ? "" : npc.getDialogue().getFriendlyGreeting());
		neutralGreeting.setText(npc.getDialogue().getNeutralGreeting() == null ? "" : npc.getDialogue().getNeutralGreeting());
		friendlyFarewell.setText(npc.getDialogue().getFriendlyFarewell() == null ? "" : npc.getDialogue().getFriendlyFarewell());
		neutralFarewell.setText(npc.getDialogue().getNeutralFarewell() == null ? "" : npc.getDialogue().getNeutralFarewell());
		partyCantAffordItem.setText(npc.getDialogue().getPartyCantAffordItem() == null ? "" : npc.getDialogue().getPartyCantAffordItem());
		characterInventoryFull.setText(npc.getDialogue().getCharacterInventoryFull() == null ? "" : npc.getDialogue().getCharacterInventoryFull());
		notInterestedInBuyingItem.setText(npc.getDialogue().getNotInterestedInBuyingItem() == null ? "" : npc.getDialogue().getNotInterestedInBuyingItem());
		cantAffordToBuyItem.setText(npc.getDialogue().getCantAffordToBuyItem() == null ? "" : npc.getDialogue().getCantAffordToBuyItem());
		npcInventoryFull.setText(npc.getDialogue().getNpcInventoryFull() == null ? "" : npc.getDialogue().getNpcInventoryFull());
		doesntWantItem.setText(npc.getDialogue().getDoesntWantItem() == null ? "" : npc.getDialogue().getDoesntWantItem());
		doesntKnowAbout.setText(npc.getDialogue().getDoesntKnowAbout() == null ? "" : npc.getDialogue().getDoesntKnowAbout());

		attitude.addActionListener(this);
		buysAt.addChangeListener(this);
		sellsAt.addChangeListener(this);
		maxPurchasePrice.addChangeListener(this);
		resistThreats.addChangeListener(this);
		resistBribes.addChangeListener(this);
		resistSteal.addChangeListener(this);
		theftCounter.addChangeListener(this);
		tileX.addChangeListener(this);
		tileY.addChangeListener(this);
		foeName.addActionListener(this);
		faction.addActionListener(this);
		alliesOnCall.addActionListener(this);
		zone.addActionListener(this);

		friendlyGreeting.addKeyListener(this);
		neutralGreeting.addKeyListener(this);
		friendlyFarewell.addKeyListener(this);
		neutralFarewell.addKeyListener(this);
		partyCantAffordItem.addKeyListener(this);
		characterInventoryFull.addKeyListener(this);
		notInterestedInBuyingItem.addKeyListener(this);
		cantAffordToBuyItem.addKeyListener(this);
		npcInventoryFull.addKeyListener(this);
		doesntWantItem.addKeyListener(this);
		doesntKnowAbout.addKeyListener(this);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject newItem(String name)
	{
		NpcTemplate npc = new NpcTemplate(
			name,
			(String)foeName.getItemAt(0),
			(String)faction.getItemAt(0),
			NpcFaction.Attitude.NEUTRAL,
			new StickManVendor(),
			(String)alliesOnCall.getItemAt(0),
			0,
			0,
			0,
			new BitSet(),
			new NpcInventoryTemplate(),
			0,
			0,
			0,
			0,
			new NpcSpeech(),
			Constants.Colour.STEALTH_GREEN,
			(String)zone.getItemAt(0),
			new Point(),
			false,
			false, 
			false);
		Database.getInstance().getNpcTemplates().put(name, npc);

		return npc;
	}

	/*-------------------------------------------------------------------------*/
	public void renameItem(String newName)
	{
		NpcTemplate npc = Database.getInstance().getNpcTemplates().remove(currentName);
		npc.setName(newName);
		Database.getInstance().getNpcTemplates().put(newName, npc);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject copyItem(String newName)
	{
		NpcTemplate current = Database.getInstance().getNpcTemplates().get(currentName);

		NpcTemplate npc = new NpcTemplate(
			newName,
			current.getFoeName(),
			current.getFaction(),
			current.getAttitude(),
			current.getScript(),
			current.getAlliesOnCall(),
			current.getBuysAt(),
			current.getSellsAt(),
			current.getMaxPurchasePrice(),
			current.getWillBuyItemTypes(),
			current.getInventoryTemplate(),
			current.getResistThreats(),
			current.getResistBribes(),
			current.getResistSteal(),
			current.getTheftCounter(),
			current.getDialogue(),
			current.getSpeechColour(),
			current.getZone(),
			current.getTile(),
			current.isFound(),
			current.isDead(), 
			current.isGuildMaster());

		Database.getInstance().getNpcTemplates().put(newName, npc);

		return npc;
	}

	/*-------------------------------------------------------------------------*/
	public void deleteItem()
	{
		Database.getInstance().getNpcTemplates().remove(currentName);
	}

	/*-------------------------------------------------------------------------*/
	public DataObject commit(String name)
	{
		NpcTemplate npc = Database.getInstance().getNpcTemplates().get(currentName);

		npc.setFoeName((String)foeName.getSelectedItem());
		try
		{
			Class clazz = Class.forName(npcScript.getText());
			npc.setScript((NpcScript)clazz.newInstance());
		}
		catch (Exception e)
		{
			throw new MazeException(e);
		}
		npc.setAttitude((NpcFaction.Attitude)attitude.getSelectedItem());
		npc.setAlliesOnCall((String)alliesOnCall.getSelectedItem());
		npc.setBuysAt((Integer)buysAt.getValue());
		npc.setSellsAt((Integer)sellsAt.getValue());
		npc.setMaxPurchasePrice((Integer)maxPurchasePrice.getValue());
		npc.setResistThreats((Integer)resistThreats.getValue());
		npc.setResistBribes((Integer)resistBribes.getValue());
		npc.setResistSteal((Integer)resistSteal.getValue());
		npc.setTheftCounter((Integer)theftCounter.getValue());
		npc.setZone((String)zone.getSelectedItem());
		npc.setTile(new Point((Integer)tileX.getValue(), (Integer)tileY.getValue()));
		npc.setFound(found.isSelected());
		npc.setDead(dead.isSelected());
		npc.setWillBuyItemTypes(willBuyItemTypes.getItemTypes());
		npc.setInventoryTemplate(new NpcInventoryTemplate(npcInventoryTemplate.getList()));
		npc.setGuildMaster(guildMaster.isSelected());
		npc.setFaction((String)(faction.getSelectedItem() == EditorPanel.NONE ? null : (String)faction.getSelectedItem()));
		npc.setDialogue(npcSpeechPanel.getDialogue());
		npc.setSpeechColour(colourButton.getBackground());

		npc.getDialogue().setFriendlyGreeting("".equals(friendlyGreeting.getText()) ? null : friendlyGreeting.getText());
		npc.getDialogue().setNeutralGreeting("".equals(neutralGreeting.getText()) ? null : neutralGreeting.getText());
		npc.getDialogue().setFriendlyFarewell("".equals(friendlyFarewell.getText()) ? null : friendlyFarewell.getText());
		npc.getDialogue().setNeutralFarewell("".equals(neutralFarewell.getText()) ? null : neutralFarewell.getText());
		npc.getDialogue().setPartyCantAffordItem("".equals(partyCantAffordItem.getText()) ? null : partyCantAffordItem.getText());
		npc.getDialogue().setCharacterInventoryFull("".equals(characterInventoryFull.getText()) ? null : characterInventoryFull.getText());
		npc.getDialogue().setNotInterestedInBuyingItem("".equals(notInterestedInBuyingItem.getText()) ? null : notInterestedInBuyingItem.getText());
		npc.getDialogue().setCantAffordToBuyItem("".equals(cantAffordToBuyItem.getText()) ? null : cantAffordToBuyItem.getText());
		npc.getDialogue().setNpcInventoryFull("".equals(npcInventoryFull.getText()) ? null : npcInventoryFull.getText());
		npc.getDialogue().setDoesntWantItem("".equals(doesntWantItem.getText()) ? null : doesntWantItem.getText());
		npc.getDialogue().setDoesntKnowAbout("".equals(doesntKnowAbout.getText()) ? null : doesntKnowAbout.getText());

		return npc;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == colourButton)
		{
			NpcTemplate current = Database.getInstance().getNpcTemplates().get(currentName);

			Color c = JColorChooser.showDialog(
				SwingEditor.instance,
				"Choose Colour",
				current.getSpeechColour());

			if (c != null)
			{
				colourButton.setBackground(c);
			}

			SwingEditor.instance.setDirty(dirtyFlag);
		}
		else
		{
			super.actionPerformed(e);
		}
	}
}
