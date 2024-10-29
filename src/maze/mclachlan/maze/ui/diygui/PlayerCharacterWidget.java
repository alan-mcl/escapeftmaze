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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import mclachlan.diygui.DIYButton;
import mclachlan.diygui.DIYComboBox;
import mclachlan.diygui.DIYLabel;
import mclachlan.diygui.DIYPanel;
import mclachlan.diygui.toolkit.ActionEvent;
import mclachlan.diygui.toolkit.ActionListener;
import mclachlan.diygui.toolkit.DIYToolkit;
import mclachlan.diygui.toolkit.RendererProperties;
import mclachlan.diygui.util.HashMapMutableTree;
import mclachlan.diygui.util.MutableTree;
import mclachlan.maze.data.Database;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.*;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.EquipIntention;
import mclachlan.maze.stat.condition.Condition;

/**
 * Widget to display an actor's details.
 */
public class PlayerCharacterWidget extends DIYPanel
	implements ActionListener, ActorActionOption.ActionOptionCallback
{
	private final DIYLabel portrait, portraitFrame, nameLabel, classLabel, nameBoard;
	private final ItemWidget leftHand, rightHand;
	private final FilledBarWidget hpBar, apBar, mpBar;
	private PlayerCharacter playerCharacter;
	private final int index;

	private Rectangle portraitBounds;

	private final DIYButton levelUp;
	private final DIYComboBox<ActorActionOption> action;
	private final DIYComboBox<PlayerCharacter.Stance> stance;

	private final Object pcMutex = new Object();

	private List<DIYLabel> conditionLabels;

	/*-------------------------------------------------------------------------*/
	public PlayerCharacterWidget(int index, Rectangle bounds)
	{
		super(bounds);
		this.index = index;
		setStyle(Style.PANEL_LIGHT);
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		// portrait, name, class
		portrait = new DIYLabel();
		portrait.setIconAlign(DIYToolkit.Align.BOTTOM);
		portraitFrame = new DIYLabel();

		portrait.addActionListener(this);
		portraitFrame.addActionListener(this);

		nameLabel = new DIYLabel("", DIYToolkit.Align.CENTER);
		nameLabel.setForegroundColour(Constants.Colour.GOLD);
		classLabel = new DIYLabel("", DIYToolkit.Align.CENTER);
		nameBoard = new DIYLabel();

		nameLabel.addActionListener(this);
		classLabel.addActionListener(this);
		nameBoard.addActionListener(this);

		// hand contents
		leftHand = new ItemWidget();
		leftHand.setStyle(ItemWidget.Style.ICON_ONLY);
		leftHand.addActionListener(this);

		rightHand = new ItemWidget();
		rightHand.setStyle(ItemWidget.Style.ICON_ONLY);
		rightHand.addActionListener(this);

		// resources
		hpBar = new FilledBarWidget(0,0);
		hpBar.setBarColour(Constants.Colour.COMBAT_RED);
		hpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);

		apBar = new FilledBarWidget(0,0);
		apBar.setBarColour(Constants.Colour.STEALTH_GREEN);
		apBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);

		mpBar = new FilledBarWidget(0,0);
		mpBar.setBarColour(Constants.Colour.MAGIC_BLUE);
		mpBar.setOrientation(FilledBarWidget.Orientation.VERTICAL);

		hpBar.addActionListener(this);
		apBar.addActionListener(this);
		mpBar.addActionListener(this);

		// lvl up
		levelUp = new DIYButton(StringUtil.getUiLabel("pcw.levelup"));
		levelUp.addActionListener(this);
		levelUp.setTooltip(StringUtil.getUiLabel("pcw.levelup.tooltip"));

		// actions
		MutableTree<ActorActionOption> options = new HashMapMutableTree<>();
		action = new DIYComboBox<>(options, new Rectangle(0, 0, 1, 1));
		action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", ""));
		if (index % 2 == 0)
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.RIGHT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.RIGHT);
		}
		else
		{
			action.setPopupDirection(DIYComboBox.PopupDirection.LEFT);
			action.setPopupExpansionDirection(DIYComboBox.PopupExpansionDirection.LEFT);
		}
		action.addActionListener(this);

		// conditions
		// ... are initialised during layout

		// stances
		ArrayList<PlayerCharacter.Stance> stances = new ArrayList<>();
		stance = new DIYComboBox<>(stances, new Rectangle(0, 0, 1, 1));
		stance.addActionListener(this);

		add(portraitFrame);
		add(portrait);
		add(nameLabel);
		add(classLabel);
		add(rightHand);
		add(leftHand);
		add(hpBar);
		add(apBar);
		add(mpBar);

		add(action);
		add(stance);
		add(levelUp);
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public void doLayout()
	{
		RendererProperties rp = DIYToolkit.getInstance().getRendererProperties();

		int border = rp.getProperty(RendererProperties.Property.PANEL_LIGHT_BORDER);
		int inset = rp.getProperty(RendererProperties.Property.INSET);
		int handSize = rp.getProperty(RendererProperties.Property.ITEM_WIDGET_SIZE);
		int conditionSize = rp.getProperty(RendererProperties.Property.CONDITION_ICON_SIZE);

		// portrait frame
		BufferedImage pf = rp.getImageResource("pcw/portrait_frame");
		portraitFrame.setIcon(pf);
		portraitFrame.setBounds(new Rectangle(
			x +border +inset,
			y +border +inset,
			pf.getWidth(),
			pf.getHeight()));

		// portrait bounds
		int portraitHeight = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_HEIGHT);
		int portraitWidth = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_WIDTH);
		int portraitFrameBorder = rp.getProperty(RendererProperties.Property.PCW_PORTRAIT_FRAME_BORDER);
		Rectangle portraitBounds = new Rectangle(
			portraitFrame.x +portraitFrame.width/2 -portraitWidth/2,
			portraitFrame.y +portraitFrame.height -portraitFrameBorder -portraitHeight,
			portraitWidth,
			portraitHeight);

		this.portrait.setBounds(portraitBounds);
		this.portraitBounds = portraitBounds;

		// hand icon bounds
		Rectangle rightHandBounds = new Rectangle(
			portraitFrame.x + portraitFrame.width + inset / 2,
			portraitFrame.y + portraitFrame.height - inset / 2,
			handSize,
			handSize);
		Rectangle leftHandBounds = new Rectangle(
			rightHandBounds.x + rightHandBounds.width + inset / 2,
			rightHandBounds.y,
			handSize,
			handSize);

		leftHand.setBounds(leftHandBounds);
		rightHand.setBounds(rightHandBounds);

		// resource bars
		int startX = rightHandBounds.x;
		int barTop = portraitFrame.y;
		int barWidth = (width -portraitFrame.width -inset/2 -inset*4 -border*2) /3;
		int barHeight = portraitFrame.height -inset;

		Rectangle hpBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);
		startX += (barWidth+ inset);
		Rectangle apBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);
		startX += (barWidth+ inset);
		Rectangle mpBarBounds = new Rectangle(startX, barTop, barWidth, barHeight);

		hpBar.setBounds(hpBarBounds);
		apBar.setBounds(apBarBounds);
		mpBar.setBounds(mpBarBounds);

		// name and class labels
		int nameLabelHeight = 15;
		BufferedImage nb = rp.getImageResource("pcw/name_board");
		nameBoard.setIcon(nb);
		nameBoard.setBounds(
			x +border +inset,
			y +border +inset +portraitFrame.height +inset,
			portraitFrame.width,
			nameLabelHeight*2);

		Rectangle nameLabelBounds = new Rectangle(
			nameBoard.x,
			nameBoard.y,
			nameBoard.width,
			nameLabelHeight);
		Rectangle classLabelBounds = new Rectangle(
			nameBoard.x,
			nameBoard.y + nameLabelHeight,
			nameBoard.width,
			nameLabelHeight);
		nameLabel.setBounds(nameLabelBounds);
		classLabel.setBounds(classLabelBounds);

		// condition icons
		Rectangle conditionsArea = new Rectangle(
			portraitFrame.x + portraitFrame.width - conditionSize,
			portraitFrame.y,
			handSize,
			portraitFrame.height);

		int bestMaxConditions = conditionsArea.height / (conditionSize+inset/2);
		conditionLabels = new ArrayList<>(bestMaxConditions);
		for (int i=0; i<bestMaxConditions; i++)
		{
			DIYLabel cw = new DIYLabel();
			conditionLabels.add(cw);
			this.add(cw);

			cw.setBounds(
				conditionsArea.x,
				conditionsArea.y +i*(conditionSize+inset/2),
				conditionSize,
				conditionSize);
		}

		// action button bounds
		Rectangle actionBounds = new Rectangle(
			x + border +inset,
			classLabelBounds.y + classLabelBounds.height +inset,
			width -border*2 -inset*2,
			height -border*2 -portraitFrame.height -nameLabelHeight*2 -inset*4);
		getAction().setBounds(actionBounds);
		getAction().setVisible(false);

		// stance combo bounds
		Rectangle stanceBounds = new Rectangle(
			x +border +inset,
			y +border +inset,
			portraitFrame.width /2,
			actionBounds.height);
		getStance().setBounds(stanceBounds);
		getStance().setVisible(false);

		// lvl up button
		Dimension ps = levelUp.getPreferredSize();
		levelUp.setBounds(
			x +border +inset,
			y +border +inset,
			ps.width,
			ps.height);
		levelUp.setVisible(false);
	}

	/*-------------------------------------------------------------------------*/
	public void setPlayerCharacter(PlayerCharacter playerCharacter)
	{
		synchronized (pcMutex)
		{
			this.playerCharacter = playerCharacter;
		}
		refresh();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		refresh();
	}

	/*-------------------------------------------------------------------------*/
	public void refresh()
	{
		boolean thisEnabled = this.isEnabled();

		if (playerCharacter == null)
		{
			portrait.setIcon(null);
			nameLabel.setVisible(false);
			classLabel.setVisible(false);

			leftHand.setVisible(false);
			rightHand.setVisible(false);

			hpBar.setVisible(false);
			apBar.setVisible(false);
			mpBar.setVisible(false);

			levelUp.setVisible(false);
			action.setVisible(false);
			stance.setVisible(false);

			if (conditionLabels != null)
			{
				for (DIYLabel cw : conditionLabels)
				{
					cw.setIcon(null);
					cw.getListeners().clear();
					cw.addActionListener(this);
				}
			}
			return;
		}
		else
		{
			portrait.setIcon(Database.getInstance().getImage(playerCharacter.getPortrait()));

			nameLabel.setVisible(true);
			classLabel.setVisible(true);
			nameLabel.setText(playerCharacter.getDisplayName());
			String raceClassText = playerCharacter.getRace().getName() + " " + playerCharacter.getCharacterClass().getName();
			FontMetrics fm = DIYToolkit.getInstance().getComponent().getFontMetrics(DiyGuiUserInterface.instance.getDefaultFont());
			if (fm.stringWidth(raceClassText) > classLabel.width)
			{
				// too long, just use the class name
				raceClassText = playerCharacter.getCharacterClass().getName();
			}
			classLabel.setText(raceClassText);

			// resource bars
			hpBar.setVisible(true);
			apBar.setVisible(true);
			mpBar.setVisible(true);

			CurMaxSub hp = playerCharacter.getHitPoints();
			CurMax ap = playerCharacter.getActionPoints();
			CurMax mp = playerCharacter.getMagicPoints();

			hpBar.set(hp.getCurrent(), hp.getMaximum(), hp.getSub());
			apBar.set(ap.getCurrent(), ap.getMaximum());
			mpBar.set(mp.getCurrent(), mp.getMaximum());

			if (hp.getSub() > 0)
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.fatigue.tooltip", hp.getCurrent(), hp.getMaximum(), hp.getSub()));
			}
			else
			{
				hpBar.setTooltip(StringUtil.getUiLabel("pcw.hp.tooltip", hp.getCurrent(), hp.getMaximum()));
			}

			apBar.setTooltip(StringUtil.getUiLabel("pcw.ap.tooltip", ap.getCurrent(), ap.getMaximum()));
			mpBar.setTooltip(StringUtil.getUiLabel("pcw.mp.tooltip", mp.getCurrent(), mp.getMaximum()));

			// left hand item
			if (playerCharacter.getSecondaryWeapon() != null)
			{
				leftHand.setItem(playerCharacter.getSecondaryWeapon());
				leftHand.setTooltip(playerCharacter.getSecondaryWeapon().getDisplayName());
			}
			else
			{
				leftHand.setItem(playerCharacter.getRace().getLeftHandItem());
				leftHand.setTooltip(null);
			}

			// right hand item
			if (playerCharacter.getPrimaryWeapon() != null)
			{
				rightHand.setItem(playerCharacter.getPrimaryWeapon());
				rightHand.setTooltip(playerCharacter.getPrimaryWeapon().getDisplayName());
			}
			else
			{
				rightHand.setItem(playerCharacter.getRace().getRightHandItem());
				rightHand.setTooltip(null);
			}

			leftHand.setVisible(true);
			rightHand.setVisible(true);

			// conditions
			ArrayList<Condition> pcConditions = new ArrayList<>(playerCharacter.getConditions());
			for (int i=0; i<conditionLabels.size(); i++)
			{
				DIYLabel cw = conditionLabels.get(i);
				if (i < pcConditions.size())
				{
					Condition condition = pcConditions.get(i);
					cw.setIcon(Database.getInstance().getImage(condition.getDisplayIcon()));
					cw.setTooltip(condition.getDisplayName());
					cw.getListeners().clear();
					cw.addActionListener(event -> {
						DiyGuiUserInterface.instance.popupConditionDetailsDialog(condition);
						return true;
					});
				}
				else
				{
					cw.setIcon(null);
					cw.setTooltip(null);
					cw.getListeners().clear();
					cw.addActionListener(this);
				}
			}

			// lvl up and stance is dependant on combat/movement
			Combat combat = Maze.getInstance().getCurrentCombat();
			if (combat != null)
			{
				stance.setModel(playerCharacter.getCharacterStanceOptions(Maze.getInstance(), combat));
				stance.setVisible(true);
				stance.setEnabled(thisEnabled && !stance.getModel().isEmpty() && !(stance.getModel().size() == 1));

				levelUp.setVisible(false);
				levelUp.setEnabled(false);
			}
			else
			{
				stance.setEnabled(thisEnabled);
				stance.setVisible(false);

				if (playerCharacter.isLevelUpPending())
				{
					levelUp.setVisible(true);
					levelUp.setEnabled(thisEnabled);
				}
				else
				{
					levelUp.setVisible(false);
					levelUp.setEnabled(false);
				}
			}
			this.getPlayerCharacter().setStance(stance.getSelected());

			// action
			action.setModel(playerCharacter.getCharacterActionOptions(Maze.getInstance(), combat));
			action.setVisible(true);
			action.setEnabled(thisEnabled && !action.getModel().isEmpty() && !(action.getModel().size() == 1));

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT ||
				Maze.getInstance().getState() == Maze.State.COMBAT)
			{
				if (combat == null)
				{
					action.setEditorText(StringUtil.getUiLabel("pcw.take.an.action", playerCharacter.getDisplayName()));
					action.setEnabled(!action.getModel().isEmpty());
				}
				else
				{
					action.setEditorText(null);
					action.getSelected().select(playerCharacter, combat, this);
				}
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void handleHandWidgetClick(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			Item item;
			// right click to bring up item details
			if (e.getSource() == rightHand)
			{
				item = playerCharacter.getPrimaryWeapon();
			}
			else
			{
				item = playerCharacter.getSecondaryWeapon();
			}

			if (item != null)
			{
				DiyGuiUserInterface.instance.popupItemDetailsWidget(item);
			}
		}
		else
		{
			// interpret any other click on the weapons as a swap
			playerCharacter.swapWeapons();
			refresh();
		}
	}

	/*-------------------------------------------------------------------------*/
	public boolean actionPerformed(ActionEvent event)
	{
		Object source = event.getSource();
		if (source == levelUp && Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			Maze.getInstance().levelUp(this.playerCharacter);
			return true;
		}
		else if (source == action)
		{
			ActorActionOption selected = action.getSelected();

			if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
			{
				selected.select(this.getPlayerCharacter(), null, this);
			}
			else
			{
				selected.select(this.getPlayerCharacter(), Maze.getInstance().getCurrentCombat(), this);
			}

			return true;
		}
		else if (source == stance)
		{
			this.getPlayerCharacter().setStance(stance.getSelected());
			return true;
		}
		else if (source == leftHand || source == rightHand)
		{
			handleHandWidgetClick((MouseEvent)event.getEvent());
		}
		else if (Maze.getInstance().getState() == Maze.State.MOVEMENT)
		{
			// anything else = dive into the inventory
			inventory();
		}

		return false;
	}

	/*-------------------------------------------------------------------------*/
	public PlayerCharacter getPlayerCharacter()
	{
		return playerCharacter;
	}

	/*-------------------------------------------------------------------------*/
	public DIYButton getLevelUp()
	{
		return levelUp;
	}

	/*-------------------------------------------------------------------------*/
	public int getIndex()
	{
		return index;
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox<ActorActionOption> getAction()
	{
		return action;
	}

	/*-------------------------------------------------------------------------*/
	public DIYComboBox<PlayerCharacter.Stance> getStance()
	{
		return stance;
	}

	/*-------------------------------------------------------------------------*/
	public Rectangle getPortraitBounds()
	{
		return portraitBounds;
	}

	/*-------------------------------------------------------------------------*/
	public void selected(ActorActionIntention intention)
	{
		if (Maze.getInstance().getState() != Maze.State.COMBAT)
		{
			if (intention instanceof EquipIntention)
			{
				inventory();
			}
			else
			{
				GameSys.getInstance().processPlayerCharacterIntentionOutsideCombat(
					intention, playerCharacter);
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private void inventory()
	{
		Maze.getInstance().getUi().characterSelected(this.playerCharacter);
		Maze.getInstance().setState(Maze.State.INVENTORY);
	}
}
