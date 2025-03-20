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

package mclachlan.maze.ui.diygui.animation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.*;
import mclachlan.maze.data.Database;
import mclachlan.maze.game.Maze;
import mclachlan.maze.stat.Dice;
import mclachlan.maze.stat.PlayerCharacter;
import mclachlan.maze.stat.UnifiedActor;
import mclachlan.maze.stat.combat.Combat;
import mclachlan.maze.stat.combat.CombatantData;
import mclachlan.maze.ui.diygui.Animation;
import mclachlan.maze.ui.diygui.DiyGuiUserInterface;

/**
 *
 */
public class ProjectileAnimation extends Animation
{
	// properties for this projectile
	List<String> projectileImages;
	int frameDelay;

	List<Image> images;

	// properties for this animation instance
	int imageWidth, imageHeight;
	long startTime;
	int projX, projY;
	int startX;
	int endX;
	int startY;
	int dX = 20;
	int dY = 10;
	boolean leftHanded;
	int currentImage = 0;
	private boolean finished;

	public ProjectileAnimation()
	{
	}

	/*-------------------------------------------------------------------------*/
	public ProjectileAnimation(List<String> projectileImages, int frameDelay)
	{
		this.frameDelay = frameDelay;
		this.projectileImages = projectileImages;

		this.images = new ArrayList<Image>();
		for (String s : projectileImages)
		{
			this.images.add(Database.getInstance().getImage(s));
		}

		startTime = System.currentTimeMillis();
		imageWidth = images.get(0).getWidth(getUi());
		imageHeight = images.get(0).getHeight(getUi());
	}

	/*-------------------------------------------------------------------------*/
	public void update(Graphics2D g)
	{
		long now = System.currentTimeMillis();
		if (now - startTime > frameDelay)
		{
			if ((dX < 0 && projX <= endX) ||
				(dX > 0 && projX >= endX))
			{
				this.finished = true;
				return;
			}
			
			projX += dX;
			projY += dY;

			startTime = now;
			
			currentImage++;
			if (currentImage > images.size()-1)
			{
				currentImage = 0;
			}
		}

//		g.setColor(Color.RED);
//		g.drawRect(projX, projY, imageHeight*2, imageWidth*2);
		Graphics2D g2d = (Graphics2D)g.create(projX, projY, imageHeight*2, imageWidth*2);

		// assume that the projectile images are all coming pointing left to right
		if (!leftHanded)
		{
			// mirror image
			AffineTransform flipTrans = new AffineTransform();
			flipTrans.setToTranslation((double)imageHeight, 0);
			flipTrans.scale(-1, 1);
			g2d.transform(flipTrans);
		}

		// todo: scale image as it gets "further away"?
		Image image = images.get(currentImage);
		g2d.drawImage(image, 0, 0, getUi());
	}

	/*-------------------------------------------------------------------------*/
	public Animation spawn(AnimationContext context)
	{
		ProjectileAnimation result = new ProjectileAnimation(projectileImages, frameDelay);

		UnifiedActor caster = context.getCaster();
		CombatantData combatantData = caster.getCombatantData();
		Combat combat = (combatantData == null ? null : combatantData.getCombat());
		boolean isPlayerAlly = combat != null && combat.isPlayerAlly(caster);

		boolean isPartyAlly = combatantData != null && isPlayerAlly;
		boolean fromParty = caster instanceof PlayerCharacter || isPartyAlly;

		if (fromParty)
		{
			// spell is cast by the party

			int index;
			if (isPartyAlly)
			{
				// random source
				index = Dice.d6.roll("projectile animation starting index");
			}
			else
			{
				PlayerCharacter pc = (PlayerCharacter)caster;
				index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);
			}

			result.leftHanded = index%2 == 0;
			if (result.leftHanded)
			{
				// left hand side of screen
				result.startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DiyGuiUserInterface.MAZE_WIDTH/2;
				result.endX = DiyGuiUserInterface.SCREEN_WIDTH/2 - imageWidth/2;
				result.startY = DiyGuiUserInterface.SCREEN_HEIGHT/2;
				result.dX = +20;
				result.dY = -10;
			}
			else
			{
				// right hand side of screen
				result.startX = DiyGuiUserInterface.SCREEN_WIDTH/2 + DiyGuiUserInterface.MAZE_WIDTH/2 - imageWidth;
				result.endX = DiyGuiUserInterface.SCREEN_WIDTH/2 - imageWidth/2;
				result.startY = DiyGuiUserInterface.SCREEN_HEIGHT/2;
				result.dX = -20;
				result.dY = -10;
			}
		}
		else
		{
			// spell cast by a foe

			// get the target PC (assume a random direction if there is more than one,
			// or if this is a foe casting a spell at another foe)
			if (context.getTargets().size() == 1 && context.getTargets().get(0) instanceof PlayerCharacter)
			{
				PlayerCharacter pc = (PlayerCharacter)context.getTargets().get(0);
				int index = Maze.getInstance().getParty().getPlayerCharacterIndex(pc);
				result.leftHanded = index%2==0;
			}
			else
			{
				result.leftHanded = Dice.d2.roll("projectile animation random handedness")==1;
			}

			if (result.leftHanded)
			{
				// left hand side of screen
				result.endX = DiyGuiUserInterface.SCREEN_WIDTH/2 - DiyGuiUserInterface.MAZE_WIDTH/2;
				result.startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - imageWidth/2;
				result.startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DiyGuiUserInterface.MAZE_HEIGHT/2; //todo: not to center?
				result.dX = -20;
				result.dY = +10;
			}
			else
			{
				// right hand side of screen
				result.endX = DiyGuiUserInterface.SCREEN_WIDTH/2 + DiyGuiUserInterface.MAZE_WIDTH/2 - imageWidth;
				result.startX = DiyGuiUserInterface.SCREEN_WIDTH/2 - imageWidth/2;
				result.startY = DiyGuiUserInterface.SCREEN_HEIGHT/2 - DiyGuiUserInterface.MAZE_HEIGHT/2; //todo: not to center?
				result.dX = +20;
				result.dY = +10;
			}
		}

		result.projX = result.startX;
		result.projY = result.startY;

		return result;
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public boolean isFinished()
	{
		return this.finished;
	}

	/*-------------------------------------------------------------------------*/
	public List<String> getProjectileImages()
	{
		return projectileImages;
	}

	public int getFrameDelay()
	{
		return frameDelay;
	}

	public void setProjectileImages(List<String> projectileImages)
	{
		this.projectileImages = projectileImages;
	}

	public void setFrameDelay(int frameDelay)
	{
		this.frameDelay = frameDelay;
	}

	/*-------------------------------------------------------------------------*/

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		ProjectileAnimation that = (ProjectileAnimation)o;

		if (getFrameDelay() != that.getFrameDelay())
		{
			return false;
		}
		return getProjectileImages() != null ? getProjectileImages().equals(that.getProjectileImages()) : that.getProjectileImages() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getProjectileImages() != null ? getProjectileImages().hashCode() : 0;
		result = 31 * result + getFrameDelay();
		return result;
	}
}