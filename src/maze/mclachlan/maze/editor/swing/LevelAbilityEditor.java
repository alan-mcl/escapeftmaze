/*
 * Copyright (c) 2014 Alan McLachlan
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
import javax.swing.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.Loader;
import mclachlan.maze.data.Saver;
import mclachlan.maze.data.v1.V1Loader;
import mclachlan.maze.data.v1.V1Saver;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.magic.Spell;
import mclachlan.maze.util.MazeException;

import static mclachlan.maze.data.v1.V1LevelAbility.*;


/**
 *
 */
public class LevelAbilityEditor extends JDialog implements ActionListener
{
	public static final int MAX = 5;

	private LevelAbility result;

	private int[] dialogLookup = new int[MAX];

	private int dirtyFlag;
	private CardLayout cards;
	private JPanel controls;
	private JButton ok, cancel, delete;
	private JComboBox type;
	private JTextField impl;
	private boolean deleted = false;

	// common attributes
	private JTextField displayName, description, key;

	// Stat Modifier ability
	private StatModifierComponent statModifier;
	// Banner Modifier ability
	private StatModifierComponent bannerModifier;
	// Special Ability ability
	private JComboBox spell;
	private ValueComponent castingLevelValue;
	// Add Spell Picks
	private JSpinner spellPicks;

	/*-------------------------------------------------------------------------*/
	public LevelAbilityEditor(Frame owner, LevelAbility ability, int dirtyFlag) throws HeadlessException
	{
		super(owner, "Edit Level Ability", true);
		this.dirtyFlag = dirtyFlag;

		for (int i = 0; i < dialogLookup.length; i++)
		{
			dialogLookup[i] = -1;
		}

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

		key = new JTextField(20);
		key.addActionListener(this);

		displayName = new JTextField(20);
		displayName.addActionListener(this);

		description = new JTextField(20);
		description.addActionListener(this);

		JPanel top = dirtyGridBagCrap(
			new JLabel("Key"), key,
			new JLabel("Type"), type,
			new JLabel("Display Name"), displayName,
			new JLabel("Description"), description);

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

		delete = new JButton("Delete");
		delete.addActionListener(this);
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(ok);
		buttons.add(cancel);
		buttons.add(delete);

		this.setLayout(new BorderLayout(3,3));
		this.add(top, BorderLayout.NORTH);
		this.add(controls, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);

		if (ability != null)
		{
			setState(ability);
		}

		this.pack();
		setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/*-------------------------------------------------------------------------*/
	private void setState(LevelAbility la)
	{
		int laType;
		if (types.containsKey(la.getClass()))
		{
			laType = types.get(la.getClass());
		}
		else
		{
			laType = CUSTOM;
		}

		for (int i = 0; i < dialogLookup.length; i++)
		{
			if (dialogLookup[i] == laType)
			{
				type.setSelectedIndex(i);
				break;
			}
		}

		key.setText(la.getKey());
		displayName.setText(la.getDisplayName());
		description.setText(la.getDescription());

		switch (laType)
		{
			case CUSTOM:
				impl.setText(la.getClass().getName());
				break;
			case STAT_MODIFIER:
				StatModifierLevelAbility smla = (StatModifierLevelAbility)la;
				statModifier.setModifier(smla.getModifier());
				break;
			case BANNER_MODIFIER:
				BannerModifierLevelAbility bmla = (BannerModifierLevelAbility)la;
				bannerModifier.setModifier(bmla.getBannerModifier());
				break;
			case SPECIAL_ABILITY:
				SpecialAbilityLevelAbility sala = (SpecialAbilityLevelAbility)la;
				spell.setSelectedItem(sala.getAbility().getSpell().getName());
				castingLevelValue.setValue(sala.getAbility().getCastingLevel());
				break;
			case SPELL_PICKS:
				AddSpellPicks asp = (AddSpellPicks)la;
				spellPicks.setValue(asp.getSpellPicks());
				break;
			default:
				throw new MazeException("invalid: "+laType);
		}
	}

	/*-------------------------------------------------------------------------*/
	private void saveResult()
	{
		int laType = dialogLookup[type.getSelectedIndex()];

		switch (laType)
		{
			case CUSTOM:
				try
				{
					Class clazz = Class.forName(impl.getText());
					result = (LevelAbility)clazz.newInstance();
					result.setKey(key.getText());
					result.setDisplayName(displayName.getText());
					result.setDescription(description.getText());
				}
				catch (Exception x)
				{
					throw new MazeException(x);
				}
				break;
			case STAT_MODIFIER:
				this.result = new StatModifierLevelAbility(
					key.getText(),
					displayName.getText(),
					description.getText(),
					statModifier.getModifier());
				break;
			case BANNER_MODIFIER:
				this.result = new BannerModifierLevelAbility(
					key.getText(),
					displayName.getText(),
					description.getText(),
					bannerModifier.getModifier());
				break;
			case SPECIAL_ABILITY:
				Spell spell1 = Database.getInstance().getSpell((String)spell.getSelectedItem());
				this.result = new SpecialAbilityLevelAbility(
					key.getText(),
					displayName.getText(),
					description.getText(),
					new SpellLikeAbility(spell1, castingLevelValue.getValue()));
				break;
			case SPELL_PICKS:
				this.result = new AddSpellPicks(
					key.getText(),
					displayName.getText(),
					description.getText(),
					(Integer)spellPicks.getValue());
				break;
			default:
				throw new MazeException("invalid: "+laType);
		}
	}

	/*-------------------------------------------------------------------------*/
	public LevelAbility getResult()
	{
		return result;
	}

	/*-------------------------------------------------------------------------*/
	public boolean isDeleted()
	{
		return deleted;
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getControls(int i)
	{
		switch (i)
		{
			case CUSTOM:
				return getCustomPanel();
			case STAT_MODIFIER:
				return getStatModifierPanel();
			case BANNER_MODIFIER:
				return getBannerModifierPanel();
			case SPECIAL_ABILITY:
				return getSpecialAbilityPanel();
			case SPELL_PICKS:
				return getSpellPicksPanel();
			default:
				throw new MazeException("invalid: "+i);
		}
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getSpellPicksPanel()
	{
		spellPicks = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
		return dirtyGridBagCrap(new JLabel("Spell Picks: "), spellPicks);
	}

	/*-------------------------------------------------------------------------*/
	private JPanel getCustomPanel()
	{
		impl = new JTextField(20);
		return dirtyGridBagCrap(new JLabel("Custom Impl: "), impl);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getStatModifierPanel()
	{
		statModifier = new StatModifierComponent(this.dirtyFlag);
		return dirtyGridBagCrap(new JLabel("Modifiers:"), statModifier);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getBannerModifierPanel()
	{
		bannerModifier = new StatModifierComponent(this.dirtyFlag);
		return dirtyGridBagCrap(new JLabel("Banner Modifiers:"), bannerModifier);
	}

	/*-------------------------------------------------------------------------*/
	public JPanel getSpecialAbilityPanel()
	{
		Vector<String> spells = new Vector<String>(Database.getInstance().getSpells().keySet());
		Collections.sort(spells);
		spell = new JComboBox(spells);

		castingLevelValue = new ValueComponent(this.dirtyFlag);

		return dirtyGridBagCrap(
			new JLabel("Spell:"), spell,
			new JLabel("Casting Level:"), castingLevelValue);
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
	private String describeType(int i)
	{
		switch (i)
		{
			case CUSTOM:
				return "Custom";
			case STAT_MODIFIER:
				return "Stat Modifier";
			case BANNER_MODIFIER:
				return "Banner Modifier";
			case SPECIAL_ABILITY:
				return "Spell-like Ability";
			case SPELL_PICKS:
				return "Add Spell Picks";
			default:
				throw new MazeException("invalid "+i);
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
				if (index == SPELL_PICKS)
				{
					// short cuts
					displayName.setText("lap_name_spellpicks+1");
					description.setText("lap_desc_spellpicks+1");
				}
				else
				{
					displayName.setText("");
					description.setText("");
				}

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
		else if (e.getSource() == delete)
		{
			deleted = true;
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
		owner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		while (1==1)
		{
			LevelAbilityEditor test = new LevelAbilityEditor(owner, null, -1);
			System.out.println("test.result = [" + test.result + "]");
		}
	}
}
