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

package mclachlan.maze.editor.swing.map;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mclachlan.maze.data.Database;
import mclachlan.maze.editor.swing.*;
import mclachlan.maze.map.Portal;
import mclachlan.maze.map.Zone;

/**
 *
 */
public class PortalDetailsPanel extends JPanel
	implements ActionListener, ChangeListener, KeyListener, ThiefToolsCallback, TileScriptComponentCallback
{
	private Portal portal;
	private Zone zone;

	// portal properties
	private JTextField mazeVariable;
	private JCheckBox twoWay;
	private JComboBox initialState;
	private JLabel from, to;
	private JComboBox fromFacing, toFacing;
	private JCheckBox canForce, canPick, canSpellPick;
	private JSpinner hitPointCostToForce, resistForce;
	private ThiefToolsPanel difficulty;
	private JComboBox keyItem;
	private JCheckBox consumeKey;
	private JComboBox mazeScript;
	private JButton quickAssignMazeVar;
	private SingleTileScriptComponent stateChangeScript;
	
	private static final String[] directions =
		{
			"North",
			"South",
			"East",
			"West"
		};

	/*-------------------------------------------------------------------------*/
	public PortalDetailsPanel(Zone zone)
	{
		this.zone = zone;

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		GridBagConstraints gbc = createGridBagConstraints();
		
		from = new JLabel();
		dodgyGridBagShite(content, new JLabel("From:"), from, gbc);
		
		fromFacing = new JComboBox(directions);
		fromFacing.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("From Facing:"), fromFacing, gbc);
		
		to = new JLabel();
		dodgyGridBagShite(content, new JLabel("To:"), to, gbc);
		
		toFacing = new JComboBox(directions);
		toFacing.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("To Facing:"), toFacing, gbc);
		
		mazeVariable = new JTextField(20);
		mazeVariable.addActionListener(this);

		quickAssignMazeVar = new JButton("Maze Variable:");
		quickAssignMazeVar.setToolTipText("Quick assign maze var");
		quickAssignMazeVar.addActionListener(this);

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		content.add(quickAssignMazeVar, gbc);
		gbc.weightx = 0.0;
		gbc.gridx++;
		content.add(mazeVariable, gbc);

		twoWay = new JCheckBox("Two Way?");
		twoWay.addActionListener(this);
		dodgyGridBagShite(content, twoWay, null, gbc);
		
		String[] items = new String[]
			{
				Portal.State.LOCKED,
				Portal.State.UNLOCKED,
				Portal.State.WALL_LIKE,
			};
		initialState = new JComboBox(items);
		initialState.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Initial State:"), initialState, gbc);

		stateChangeScript = new SingleTileScriptComponent(null, SwingEditor.Tab.ZONES, this, zone);
		stateChangeScript.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("State Change Script:"), stateChangeScript, gbc);
		
		canForce = new JCheckBox("Can Force?");
		canForce.addActionListener(this);
		dodgyGridBagShite(content, canForce, null, gbc);
		
		canPick = new JCheckBox("Can Pick?");
		canPick.addActionListener(this);
		dodgyGridBagShite(content, canPick, null, gbc);
		
		canSpellPick = new JCheckBox("Can Spell Pick?");
		canSpellPick.addActionListener(this);
		dodgyGridBagShite(content, canSpellPick, null, gbc);
		
		hitPointCostToForce = new JSpinner(new SpinnerNumberModel(0, 0, 256, 1));
		hitPointCostToForce.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("HP Cost To Force:"), hitPointCostToForce, gbc);
		
		resistForce = new JSpinner(new SpinnerNumberModel(0, -128, 128, 1));
		resistForce.addChangeListener(this);
		dodgyGridBagShite(content, new JLabel("Resist Force:"), resistForce, gbc);
		
		difficulty = new ThiefToolsPanel("Thief Tools To Pick", -1, this);

		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		content.add(difficulty, gbc);
		gbc.gridwidth = 1;
		
		mazeScript = new JComboBox();
		mazeScript.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Maze Script:"), mazeScript, gbc);

		keyItem = new JComboBox();
		keyItem.addActionListener(this);
		dodgyGridBagShite(content, new JLabel("Key Item:"), keyItem, gbc);
		
		consumeKey = new JCheckBox("Consume Key?");
		consumeKey.addActionListener(this);

		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.gridx=0;
		gbc.gridy++;
		content.add(consumeKey, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		content.add(new JLabel(), gbc);

		initForeignKeys();

		this.setLayout(new BorderLayout());
		this.add(
			new JScrollPane(
				content,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			BorderLayout.CENTER);
	}
	
	/*-------------------------------------------------------------------------*/
	public void initForeignKeys()
	{
		Vector<String> vec = new Vector<String>(Database.getInstance().getItemList());
		vec.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec);
		keyItem.setModel(new DefaultComboBoxModel(vec));
		
		Vector<String> vec2 = new Vector<String>(Database.getInstance().getMazeScripts().keySet());
		vec2.insertElementAt(EditorPanel.NONE, 0);
		Collections.sort(vec2);
		mazeScript.setModel(new DefaultComboBoxModel(vec2));
	}
	
	/*-------------------------------------------------------------------------*/
	public void refresh(Portal portal)
	{
		this.portal = portal;
		
		mazeVariable.removeKeyListener(this);
		twoWay.removeActionListener(this);
		initialState.removeActionListener(this);
		canForce.removeActionListener(this);
		canPick.removeActionListener(this);
		canSpellPick.removeActionListener(this);
		hitPointCostToForce.removeChangeListener(this);
		resistForce.removeChangeListener(this);
		keyItem.removeActionListener(this);
		consumeKey.removeActionListener(this);
		fromFacing.removeActionListener(this);
		toFacing.removeActionListener(this);
		mazeScript.removeActionListener(this);

		Point t = portal.getTo();
		Point f = portal.getFrom();
		to.setText(calcIndex(t)+" = ("+t.x+","+t.y+")");
		toFacing.setSelectedIndex(portal.getToFacing()-1);
		from.setText(calcIndex(f)+" = ("+f.x+","+f.y+")");
		fromFacing.setSelectedIndex(portal.getFromFacing()-1);
		mazeVariable.setText(portal.getMazeVariable());
		twoWay.setSelected(portal.isTwoWay());
		initialState.setSelectedItem(portal.getInitialState());
		stateChangeScript.refresh(portal.getStateChangeScript(), zone);
		canForce.setSelected(portal.canForce());
		canPick.setSelected(portal.canPick());
		canSpellPick.setSelected(portal.canSpellPick());
		hitPointCostToForce.setValue(portal.getHitPointCostToForceLock());
		resistForce.setValue(portal.getResistForceOpen());
		difficulty.refresh(portal.getDifficulty(), portal.getRequired());
		mazeScript.setSelectedItem(portal.getMazeScript()==null?EditorPanel.NONE:portal.getMazeScript());
		keyItem.setSelectedItem(portal.getKeyItem()==null?EditorPanel.NONE:portal.getKeyItem());
		consumeKey.setSelected(portal.isConsumeKeyItem());
		
		mazeVariable.addKeyListener(this);
		twoWay.addActionListener(this);
		initialState.addActionListener(this);
		canForce.addActionListener(this);
		canPick.addActionListener(this);
		canSpellPick.addActionListener(this);
		hitPointCostToForce.addChangeListener(this);
		resistForce.addChangeListener(this);
		keyItem.addActionListener(this);
		consumeKey.addActionListener(this);
		fromFacing.addActionListener(this);
		toFacing.addActionListener(this);
		mazeScript.addActionListener(this);
	}
	
	/*-------------------------------------------------------------------------*/
	private int calcIndex(Point p)
	{
		int width = zone.getWidth();
		return p.y*width + p.x%width;
	}

	/*-------------------------------------------------------------------------*/
	protected void dodgyGridBagShite(JPanel panel, Component a, Component b, GridBagConstraints gbc)
	{
		if (a == null) a = new JLabel();
		if (b == null) b = new JLabel();
		
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridx=0;
		gbc.gridy++;
		panel.add(a, gbc);
		gbc.weightx = 1.0;
		gbc.gridx++;
		panel.add(b, gbc);
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
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		return gbc;
	}

	/*-------------------------------------------------------------------------*/
	public void actionPerformed(ActionEvent e)
	{
		Object obj = e.getSource();
		if (obj == twoWay)
		{
			portal.setTwoWay(twoWay.isSelected());
		}
		else if (obj == initialState)
		{
			portal.setInitialState((String)initialState.getSelectedItem());
		}
		else if (obj == stateChangeScript)
		{
			portal.setStateChangeScript(stateChangeScript.getScript());
		}
		else if (obj == fromFacing)
		{
			portal.setFromFacing(fromFacing.getSelectedIndex()+1);
		}
		else if (obj == toFacing)
		{
			portal.setToFacing(toFacing.getSelectedIndex()+1);
		}
		else if (obj == canPick)
		{
			portal.setCanPick(canPick.isSelected());
		}
		else if (obj == canForce)
		{
			portal.setCanForce(canForce.isSelected());
		}
		else if (obj == canSpellPick)
		{
			portal.setCanSpellPick(canSpellPick.isSelected());
		}
		else if (obj == keyItem)
		{
			if (keyItem.getSelectedItem().equals(EditorPanel.NONE))
			{
				portal.setKeyItem(null);
			}
			else
			{
				portal.setKeyItem((String)keyItem.getSelectedItem());
			}
		}
		else if (obj == consumeKey)
		{
			portal.setConsumeKeyItem(consumeKey.isSelected());
		}
		else if (obj == mazeScript)
		{
			if (mazeScript.getSelectedItem().equals(EditorPanel.NONE))
			{
				portal.setMazeScript(null);
			}
			else
			{
				portal.setMazeScript((String)mazeScript.getSelectedItem());
			}
		}
		else if (obj == quickAssignMazeVar)
		{
			if (zone == null)
			{
				return;
			}
			
			Set<String> existingPortalMazeVars = new HashSet<String>();
			
			// collect all existing encounter maze vars
			for (Portal p : zone.getPortals())
			{
				if (p.getMazeVariable() != null)
				{
					existingPortalMazeVars.add(p.getMazeVariable());
				}
			}
			
			// iterate over our template string and take the first available one
			String zoneName = zone.getName().toLowerCase();
			zoneName = zoneName.replaceAll("\\s", ".");
			int count = 0;
			while (true)
			{
				String s = zoneName + ".portal." + count++;
				if (!existingPortalMazeVars.contains(s))
				{
					mazeVariable.setText(s);
					portal.setMazeVariable(s);
					break;
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == hitPointCostToForce)
		{
			portal.setHitPointCostToForce((Integer)hitPointCostToForce.getValue());
		}
		else if (e.getSource() == resistForce)
		{
			portal.setResistForce((Integer)resistForce.getValue());
		}
	}

	/*-------------------------------------------------------------------------*/
	public void keyTyped(KeyEvent e)
	{
		if (e.getSource() == mazeVariable)
		{
			if (portal != null)
			{
				portal.setMazeVariable(mazeVariable.getText());
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	public void keyPressed(KeyEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void keyReleased(KeyEvent e)
	{
	}

	/*-------------------------------------------------------------------------*/
	public void thiefToolsChanged(ThiefToolsPanel component)
	{
		if (component == difficulty)
		{
			if (portal != null)
			{
				portal.setDifficulty(difficulty.getDifficulties());
				portal.setRequired(difficulty.getRequired());
			}
		}
	}

	@Override
	public void tileScriptChanged(Component component)
	{
		portal.setStateChangeScript(((SingleTileScriptComponent)component).getScript());
	}
}
