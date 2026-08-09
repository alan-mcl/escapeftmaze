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

package mclachlan.maze.game.event;

import java.util.*;
import java.util.function.Consumer;
import mclachlan.maze.data.StringUtil;
import mclachlan.maze.game.Maze;
import mclachlan.maze.game.MazeEvent;
import mclachlan.maze.stat.CharacterSelection;
import mclachlan.maze.stat.CharacterSelectionMethod;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.ui.diygui.ChooseCharacterCallback;
import mclachlan.maze.util.MazeException;

/**
 * Walks a {@link CharacterSelection} in order, opening Who for player steps
 * and applying automatic methods synchronously. Player steps block the event
 * thread until a valid pick is made (same pattern as {@link DisplayOptionsEvent}).
 */
public class RunCharacterSelectionEvent extends MazeEvent implements ChooseCharacterCallback
{
	private CharacterSelection characterSelection;
	private int methodIndex;
	private List<String> selectedNames = new ArrayList<>();

	private transient Consumer<List<PlayerCharacter>> onComplete;
	private transient List<PlayerCharacter> pendingCandidates = List.of();
	private transient PlayerCharacter pendingPick;

	public RunCharacterSelectionEvent()
	{
	}

	public RunCharacterSelectionEvent(
		CharacterSelection characterSelection,
		int methodIndex,
		List<String> selectedNames,
		Consumer<List<PlayerCharacter>> onComplete)
	{
		this.characterSelection = characterSelection;
		this.methodIndex = methodIndex;
		this.selectedNames = selectedNames == null ? new ArrayList<>() : new ArrayList<>(selectedNames);
		this.onComplete = onComplete;
	}

	public CharacterSelection getCharacterSelection()
	{
		return characterSelection;
	}

	public void setCharacterSelection(CharacterSelection characterSelection)
	{
		this.characterSelection = characterSelection;
	}

	public int getMethodIndex()
	{
		return methodIndex;
	}

	public void setMethodIndex(int methodIndex)
	{
		this.methodIndex = methodIndex;
	}

	public List<String> getSelectedNames()
	{
		return selectedNames;
	}

	public void setSelectedNames(List<String> selectedNames)
	{
		this.selectedNames = selectedNames;
	}

	public void setOnComplete(Consumer<List<PlayerCharacter>> onComplete)
	{
		this.onComplete = onComplete;
	}

	@Override
	public int getDelay()
	{
		return Delay.NONE;
	}

	@Override
	public List<MazeEvent> resolve()
	{
		Maze maze = Maze.getInstance();
		if (characterSelection == null || onComplete == null || maze.getParty() == null)
		{
			return null;
		}

		List<PlayerCharacter> party = maze.getParty().getPlayerCharacters();
		List<CharacterSelectionMethod> methods = characterSelection.getMethods();
		if (methods == null || methods.isEmpty())
		{
			onComplete.accept(List.of());
			return null;
		}

		List<PlayerCharacter> excluded = characterSelection.getExcluded(party);
		Set<PlayerCharacter> selected = new LinkedHashSet<>(
			CharacterSelection.resolveSelectedNames(selectedNames, party));

		for (int i = methodIndex; i < methods.size(); i++)
		{
			CharacterSelectionMethod method = methods.get(i);
			if (method == null)
			{
				continue;
			}

			if (method.isPlayerSelection())
			{
				List<PlayerCharacter> candidates = CharacterSelection.candidatesFor(
					method, party, excluded, selected);
				if (candidates.isEmpty())
				{
					continue;
				}

				pendingPick = null;
				pendingCandidates = candidates;
				maze.getUi().chooseACharacter(this);
				waitForPlayerPick(maze);
				selected.add(pendingPick);
				selectedNames.add(pendingPick.getName());
				pendingPick = null;
				pendingCandidates = List.of();
				continue;
			}

			List<PlayerCharacter> candidates = CharacterSelection.candidatesFor(
				method, party, excluded, selected);
			selected.addAll(method.select(candidates));
		}

		onComplete.accept(new ArrayList<>(selected));
		return null;
	}

	/*-------------------------------------------------------------------------*/
	private void waitForPlayerPick(Maze maze)
	{
		Object mutex = maze.getEventMutex();
		synchronized (mutex)
		{
			while (pendingPick == null)
			{
				try
				{
					mutex.wait();
				}
				catch (InterruptedException e)
				{
					throw new MazeException(e);
				}
			}
		}
	}

	@Override
	public boolean characterChosen(PlayerCharacter pc, int pcIndex)
	{
		if (pc == null || pendingCandidates.isEmpty())
		{
			return false;
		}

		if (!pendingCandidates.contains(pc))
		{
			Maze.getInstance().getUi().addMessage(
				StringUtil.getEventText("msg.character.cannot.be.chosen", pc.getDisplayName()),
				false);
			return false;
		}

		pendingPick = pc;
		synchronized (Maze.getInstance().getEventMutex())
		{
			Maze.getInstance().getEventMutex().notifyAll();
		}
		return true;
	}

	@Override
	public void afterCharacterChosen()
	{
		if (pendingPick == null && !pendingCandidates.isEmpty())
		{
			Maze.getInstance().getUi().chooseACharacter(this);
		}
	}
}
